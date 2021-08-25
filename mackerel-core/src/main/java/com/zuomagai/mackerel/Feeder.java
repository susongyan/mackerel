package com.zuomagai.mackerel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 卑微铲屎官，负责补足罐头里的鱼和检查
 * 
 * @author S.S.Y
 */
public class Feeder implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Feeder.class);

    private MackerelCan mackerelCan;
    private ExecutorService feedExecutor; // 投喂者线程
    private ScheduledExecutorService shovelScheduler; // 铲屎线程
    private ExecutorService sweepExecutor; //清理线程
    private LinkedBlockingQueue<Runnable> creatingQueue;

    public Feeder(MackerelCan mackerelCan) {
        this.mackerelCan = mackerelCan;
    }

    public void init() {
        creatingQueue = new LinkedBlockingQueue<>();
        feedExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, creatingQueue,
                new NamedThreadFactory("Feed-Thread"));
        sweepExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                new NamedThreadFactory("Sweep-Thread"));
        shovelScheduler = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("Shovel-Thread"));
        shovelScheduler.scheduleWithFixedDelay(new Shovel(this.mackerelCan, this.sweepExecutor),
                mackerelCan.getValidateWindow(), mackerelCan.getValidateWindow(), TimeUnit.MILLISECONDS);
        feed(mackerelCan.getMinIdle());
    }

    public void feed(int number) {
        for (int i = 0; i < number; i++) {
            feedExecutor.execute(() -> {
                incubateMackerel();
            });
        }
    }

    private void incubateMackerel() {
        if (!shouldFeed()) {
            return;
        }
        try {
            long start = System.currentTimeMillis();
            LOGGER.debug("-------creating new connection ------");
            Connection connection = DriverManager.getConnection(mackerelCan.getJdbcUrl(), mackerelCan.getUserName(),
                    mackerelCan.getPassword());
            Mackerel mackerel = new Mackerel(mackerelCan, connection);
            mackerelCan.add(mackerel);
            LOGGER.debug("--> create success (" + (System.currentTimeMillis() - start) + "ms): current="
                    + mackerelCan.getCurrentSize() + ",creating=" + creatingQueue.size() + ",max="
                    + mackerelCan.getMaxSize());
        } catch (Exception e) {
            LOGGER.error("create connection fail", e);
        }
    }

    private boolean shouldFeed() {
        int onCreating = creatingQueue.size();
        int currentSize = mackerelCan.getCurrentSize();
        return mackerelCan.getMaxSize() >= onCreating + currentSize;
    }

    static class Shovel implements Runnable {
        private MackerelCan can;
        private Executor sweeper;

        public Shovel(MackerelCan can, ExecutorService sweeper) {
            this.can = can;
        }

        @Override
        public void run() {
            CopyOnWriteArrayList<Mackerel> snapshot = can.getAllMackerels();
            int currentTotal = snapshot.size();

            List<Mackerel> toEvicts = new ArrayList<>();
            for (int i = 0; i < snapshot.size(); i++) {
                Mackerel underTest = snapshot.get(i);
                // 只检测空闲连接
                if (!underTest.isIdle()) {
                    continue;
                }

                // 1. minIdleTime, maxIdleTime
                boolean needEvict = false;
                if (currentTotal > can.getMinIdle()) {
                    if (underTest.getIdleDuration() > can.getMinIdleTime() && underTest.reserve()) {
                        needEvict = true;
                    }
                } else {
                    if (underTest.getIdleDuration() > can.getMaxIdleTime() && underTest.reserve()) {
                        needEvict = true;
                    }
                }

                // 2. testWhileIdle
                if (can.isTestWhileIdle() && underTest.getIdleDuration() > can.getValidateWindow()
                        && (System.currentTimeMillis() - underTest.getLastValidateTime()) > can.getValidateTimeout()
                        && underTest.reserve()) { // 这个时候可能被取出了，需要先cas下预占
                    if (!underTest.validate()) {
                        needEvict = true;
                    } else {
                        underTest.renewValiateTime();
                        underTest.markIdle();
                    }
                }

                if (needEvict && underTest.markEvicted()) {
                    toEvicts.add(underTest);
                    currentTotal--;
                }

                if (toEvicts.size() > 0) {
                    // 先移除这些待关闭的连接， 以免 shouldFeed() 判断不准
                    can.getAllMackerels().removeAll(toEvicts);
                    // close quitely
                    for (Mackerel toEvict : toEvicts) {
                        sweeper.execute(() -> toEvict.closeQuietly());
                    }
                }
            }
            //TODO 如果少于min了，则要补足连接

        }
    }

    static class NamedThreadFactory implements ThreadFactory {
        private static final AtomicInteger id = new AtomicInteger(1);
        private String threadNamePrefix;

        public NamedThreadFactory(String prefix) {
            this.threadNamePrefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(threadNamePrefix + "#" + id.getAndIncrement());
            return thread;
        }
    }

    @Override
    public void close() throws Exception {
        // 直接关闭运行中的任务
        feedExecutor.shutdownNow();
        sweepExecutor.shutdownNow();
        shovelScheduler.shutdownNow();
    }
}
