package com.zuomagai.mackerel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/**
 * 卑微铲屎官，负责补足罐头里的鱼
 */
public class Feeder implements AutoCloseable{

    private MackerelCan mackerelCan;
    private ExecutorService feedExecutor;
    private LinkedBlockingQueue<Runnable> creatingQueue;

    public Feeder(MackerelCan mackerelCan) {
        this.mackerelCan = mackerelCan;
    }

    public void init() {
        creatingQueue = new LinkedBlockingQueue<>();
        feedExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, creatingQueue);
    }

    public void feed(int number) {
        feedExecutor.execute(() -> {
        // TODO 判断是否还能创建 maxSize、minIdle、waiting
            incubateMackerel();
        });
    }

    private void incubateMackerel() {
        try {
            Connection connection = DriverManager.getConnection(mackerelCan.getJdbcUrl(), mackerelCan.getUserName(), mackerelCan.getPassword());
            mackerelCan.add(new Mackerel(connection));
        } catch (SQLException e) {
            // TODO log
            e.printStackTrace();
        }
    }
    
    @Override
    public void close() throws Exception {
        feedExecutor.shutdownNow();
    }
}
