package com.zuomagai.mackerel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 卑微铲屎官，负责补足罐头里的鱼和检查
 * @author S.S.Y
 */
public class Feeder implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Feeder.class);

    private MackerelCan mackerelCan;
    private ExecutorService feedExecutor; //投喂线程 //TODO 改成多线程？， 注意连接数判断
    private ExecutorService shovelExecutor; //铲屎线程
    private LinkedBlockingQueue<Runnable> creatingQueue;

    public Feeder(MackerelCan mackerelCan) {
        this.mackerelCan = mackerelCan;
    }

    public void init() {
        creatingQueue = new LinkedBlockingQueue<>();
        feedExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, creatingQueue);
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
            System.out.println("-------creating new connection ------");
            Connection connection = DriverManager.getConnection(mackerelCan.getJdbcUrl(), 
                    mackerelCan.getUserName(),
                    mackerelCan.getPassword());
            Mackerel mackerel = new Mackerel(mackerelCan, connection);
            mackerelCan.add(mackerel);
            System.out.println("--> create success (" + (System.currentTimeMillis() - start) + "ms):"
                    + " current=" + mackerelCan.getCurrentSize()
                    + ",creating=" + creatingQueue.size()
                    + ",max="+ mackerelCan.getMaxSize());
        } catch (Exception e) {
            LOGGER.error("create connection fail", e);
        }
    }

    private boolean shouldFeed() {
        int onCreating = creatingQueue.size();
        int currentSize = mackerelCan.getCurrentSize();
        return mackerelCan.getMaxSize() >= onCreating + currentSize;
    }

    @Override
    public void close() throws Exception {
        feedExecutor.shutdownNow();
    }
}
