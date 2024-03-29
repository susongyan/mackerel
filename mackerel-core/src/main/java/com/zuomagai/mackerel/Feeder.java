package com.zuomagai.mackerel;

import com.zuomagai.mackerel.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 卑微铲屎官，负责补足罐头里的鱼和剔除多余的
 *
 * @author S.S.Y
 */
public class Feeder implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Feeder.class);

    private volatile boolean closed = false;
    private final MackerelCan mackerelCan;
    private ThreadPoolExecutor feedExecutor; // 投喂者线程
    private ScheduledExecutorService shovelScheduler; // 铲屎线程
    private ThreadPoolExecutor sweepExecutor; //清理线程
    private LinkedBlockingQueue<Runnable> creatingQueue;

    public Feeder(MackerelCan mackerelCan) {
        this.mackerelCan = mackerelCan;
    }

    public void init() {
        creatingQueue = new LinkedBlockingQueue<>();
        feedExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, creatingQueue,
                new NamedThreadFactory("mackerel-feeder-thread"));
        sweepExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                new NamedThreadFactory("mackerel-sweeper-thread"));
        shovelScheduler = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("mackerel-shovel-thread"));
        shovelScheduler.scheduleAtFixedRate(new Shovel(this, this.mackerelCan),
                mackerelCan.getValidateWindow(), mackerelCan.getValidateWindow(), TimeUnit.MILLISECONDS);
        feed(mackerelCan.getMinIdle());
    }

    public void feed(int number) {
        if (number <= 0)
            return;
        for (int i = 0; i < number; i++) {
            feedExecutor.execute(this::feedOneMackerel);
        }
    }

    public void feed() {
        int toFeed = mackerelCan.getMinIdle() - mackerelCan.getAliveSize();
        if (toFeed > 0)
            feed(toFeed);
    }

    public void feedOneMackerel() {
        if (!shouldFeed()) {
            return;
        }
        try {
            incubateMackerel();
        } catch (Exception e) {
            LOGGER.error("create connection fail", e);
        }
    }

    public void incubateMackerel() throws SQLException {
        Connection connection;
        long start = System.currentTimeMillis();
        LOGGER.debug("-------creating new connection ------");
        connection = DriverManager.getConnection(mackerelCan.getJdbcUrl(), mackerelCan.getUserName(),
                mackerelCan.getPassword());
        Mackerel mackerel = new Mackerel(mackerelCan, connection);
        mackerelCan.add(mackerel);
        LOGGER.debug("--> create success (" + (System.currentTimeMillis() - start) + "ms): current="
                + mackerelCan.getAliveSize() + ",creating=" + creatingQueue.size() + ",max="
                + mackerelCan.getMaxSize());
    }

    private boolean shouldFeed() {
        if (this.closed) {
            LOGGER.debug("feeder is closed, not feed anymore");
            return false;
        }
        int currentSize = mackerelCan.getAliveSize() + creatingQueue.size();
        return (mackerelCan.getMaxSize() > currentSize)
                && (mackerelCan.getWaitingThreadCount() > 0 || currentSize < mackerelCan.getMinIdle());
    }

    static class Shovel implements Runnable {
        private final Feeder feeder;
        private final MackerelCan can;

        public Shovel(Feeder feeder, MackerelCan can) {
            this.feeder = feeder;
            this.can = can;
        }

        @Override
        public void run() {
            LOGGER.debug("start shoveling...");

            CopyOnWriteArrayList<Mackerel> snapshot = can.getAllMackerels();
            int currentTotal = snapshot.size();

            List<Mackerel> toEvicts = new ArrayList<>();
            for (Mackerel underTest : snapshot) {
                // 清理 testWhileIdle 阶段标记的不可用连接
                if (underTest.isEvicted()) {
                    LOGGER.debug("shoveling... found {} evicted!!! going to sweep it", underTest);
                    toEvicts.add(underTest);
                    currentTotal--;
                    continue;
                }

                // 只检测空闲连接
                if (!underTest.isIdle()) {
                    continue;
                }

                // minIdleTime, maxIdleTime
                boolean needEvict = false;
                if (currentTotal > can.getMinIdle()) {
                    if (underTest.getIdleDuration() > can.getMinIdleTime() && underTest.reserve()) {
                        LOGGER.debug("shoveling... found {} touch fish over minIdleTime={}ms!!! going to sweep it",
                                underTest, can.getMinIdleTime());
                        needEvict = true;
                    }
                } else {
                    if (underTest.getIdleDuration() > can.getMaxIdleTime() && underTest.reserve()) {
                        LOGGER.debug("shoveling... found {} touch fish over maxIdleTime={}ms!!! going to sweep it",
                                underTest, can.getMaxIdleTime());
                        needEvict = true;
                    }
                }

                if (needEvict && underTest.markEvicted()) {
                    toEvicts.add(underTest);
                    currentTotal--;
                }
            }

            LOGGER.debug("shoveling... found {} mackerels need be evicted", toEvicts.size());
            if (toEvicts.size() > 0) {
                can.remove(toEvicts);
                // close quietly
                for (Mackerel toEvict : toEvicts) {
                    this.feeder.sweep(toEvict);
                }
            }
            // 补足连接
            feeder.feed();
        }
    }

    public void sweep(Mackerel toSweep) {
        this.sweepExecutor.execute(() -> {
            LOGGER.debug("sweeping " + toSweep);
            toSweep.closeQuietly();
        });

    }

    @Override
    public void close() {
        LOGGER.debug("closing feeder...");
        this.closed = true;
        //注意顺序
        feedExecutor.shutdown();
        shovelScheduler.shutdown();
        sweepExecutor.shutdown();
    }
}
