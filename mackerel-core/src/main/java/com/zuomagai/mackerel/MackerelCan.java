package com.zuomagai.mackerel;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * 马鲛鱼罐头 (connection pool)
 * 
 * @author S.S.Y
 **/
public class MackerelCan implements AutoCloseable {

    private CopyOnWriteArrayList<Mackerel> mackerels = new CopyOnWriteArrayList<>();

    // FILO后进先出，刚用完归还的连接相对于空闲较久的连接更鲜活
    private BlockingDeque<Mackerel> idleMackerels = new LinkedBlockingDeque<>();

    private Feeder feeder;

    private final String jdbcUrl;
    private final String userName;
    private final String password;
    private final String catalog;
    private final String schema;

    // region pool properties, allow changed on running
    private volatile int minIdle;
    private volatile int maxSize;
    private volatile long maxWait;
    private volatile long validateWindow; // TODO 取出来的连接，空闲超过多久就要校验有效性，叫这个名字好像不大好？
    private volatile long maxIdleTime;
    private volatile long minIdleTime;
    // endregion

    public MackerelCan(MackerelConfig config) {
        this.jdbcUrl = config.getJdbcUrl();
        this.userName = config.getUserName();
        this.password = config.getPassword();
        this.catalog = config.getCatalog();
        this.schema = config.getSchema();
        validateAndInitConfig(config);
    }

    public void init() {
        // feeder
        feeder = new Feeder(this);
        feeder.init();
        // evictor/cat
    }

    private void validateAndInitConfig(MackerelConfig config) {
        setMinIdle(config.getMinIdle());
        setMaxSize(config.getMaxSize());
        setMaxWait(config.getMaxWait());
        setValidateWindow(config.getValidateWindow());
        setMaxIdleTime(config.getMaxIdleTime());
        setMinIdleTime(config.getMinIdleTime());
    }

    public Mackerel getMackerel() {
        long start = System.currentTimeMillis();
        try {
            if (this.maxWait <= 0) {
                return idleMackerels.takeFirst();
            }
            Mackerel mackerel = idleMackerels.pollFirst(this.maxWait, TimeUnit.MILLISECONDS);
            if (mackerel == null)
                throw new MackerelException(
                        "cannot get connection after wait " + (System.currentTimeMillis() - start) + "ms");
            else
                return mackerel;
        } catch (InterruptedException e) {
            throw new MackerelException("fetching connection interrupted", e);
        }
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

    public String getJdbcUrl() {
        return this.jdbcUrl;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchema() {
        return schema;
    }

    public int getCurrentSize() {
        return mackerels.size();
    }

    public void add(Mackerel mackerel) {
        this.mackerels.add(mackerel);
        // 塞到头部，刚创建或归还的连接可用性比较靠谱
        this.idleMackerels.addFirst(mackerel); 
    }

    public void returnIdle(Mackerel mackerel) {
        this.idleMackerels.addFirst(mackerel);
        System.out.println(">>> after return: " + toString());
    }

    @Override
    public void close() throws Exception {
        // TODO 关闭任务
        feeder.close();

        //TODO 关闭所有db连接
    }

    @Override
    public String toString() {
        return "total=" + this.mackerels.size() 
            + ", idle=" + this.idleMackerels.size();
    }
}
