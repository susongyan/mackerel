package com.zuomagai.mackerel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 马鲛鱼罐头 (connection pool)
 * 
 * @author : holysu
 **/
public class MackerelCan {

    private CopyOnWriteArrayList<Mackerel> mackerels = new CopyOnWriteArrayList<>();

    // 后进先出，刚用完归还的连接相对于空闲较久的连接更鲜活
    private BlockingQueue<Mackerel> idleMackerels = new LinkedBlockingDeque<>();

    private Feeder feeder;

    // region properties, allowd changed on running
    private volatile int minIdle;
    private volatile int maxSize;
    private volatile long maxWait;
    private volatile long validateWindow; //TODO 取出来的连接，空闲超过多久就要校验有效性，叫什么好呢？ 
    private volatile long maxIdleTime;
    private volatile long minIdleTime;
    // endregion

    private long connectionTimeout;
    private long validateTimeout;

    public MackerelCan(MackerelConfig config) {

    }

    public void init(MackerelConfig config) {
        validateAndInitConfig(config);
        // feeder
        // evictor/cat
    }

    private void validateAndInitConfig(MackerelConfig config) {
        setMinIdle(config.getMinIdle());
        setMaxSize(config.getMaxSize());
        setMaxWait(config.getMaxWait());
        setValidateWindow(config.getValidateWindow());
        setMaxIdleTime(config.getMaxIdleTime());
        setMinIdleTime(config.getMinIdleTime());
        setConnectionTimeout(config.getConnectionTimeout());
        setValidateTimeout(config.getValidateTimeout());
    }

    public void setMinIdle(int minIdle) {
        if (minIdle < 1) 
            throw new IllegalArgumentException("minIdle cannot less than 1");
        this.minIdle = minIdle;
    }

    public void setMaxSize(int maxSize) {
        if (maxSize < 1) 
            throw new IllegalArgumentException("maxSize cannot less than 1");
        this.maxSize = maxSize;
    }

    public void setMaxWait(long maxWait) {
        if (maxWait < 0) 
            throw new IllegalArgumentException("maxWait cannot less than 0");
        this.maxWait = maxWait;
    }

    public void setValidateWindow(long validateWindow) {
        if (validateWindow < 1000) 
            throw new IllegalArgumentException("validateWindow cannot less than 5000");
        this.validateWindow = validateWindow;
    }

    public void setMaxIdleTime(long maxIdleTime) {
        if (maxIdleTime < (60 * 1000))
            throw new IllegalArgumentException("maxIdleTime cannot less than 1 minute");
        this.maxIdleTime = maxIdleTime;
    }

    public void setMinIdleTime(long minIdleTime) {
        if (minIdleTime < 60 * 1000) 
            throw new IllegalArgumentException("minIdleTime cannot less than 1 minute");
        this.minIdleTime = minIdleTime;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setValidateTimeout(long validateTimeout) {
        this.validateTimeout = validateTimeout;
    }
}
