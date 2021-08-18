package com.zuomagai.mackerel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 卑微铲屎官，负责补足罐头里的鱼和检查
 */
public class Feeder implements AutoCloseable {

    private MackerelCan mackerelCan;
    private ExecutorService feedExecutor; //投喂线程
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
            Connection connection = DriverManager.getConnection(mackerelCan.getJdbcUrl(), mackerelCan.getUserName(),
                    mackerelCan.getPassword());
            mackerelCan.add(new Mackerel(connection));
            System.out.println(">>> create success (" + (System.currentTimeMillis() - start) + "ms): current="
                    + mackerelCan.getCurrentSize() + ", creating=" + creatingQueue.size() + ", max="
                    + mackerelCan.getMaxSize());
        } catch (SQLException e) {
            // TODO log
            e.printStackTrace();
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
