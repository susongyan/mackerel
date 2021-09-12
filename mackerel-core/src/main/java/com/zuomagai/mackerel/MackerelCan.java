package com.zuomagai.mackerel;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 马鲛鱼罐头 (connection pool)
 * 
 * @author S.S.Y
 **/
public class MackerelCan implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MackerelCan.class);
    private static final AtomicInteger id = new AtomicInteger(0);

    private CopyOnWriteArrayList<Mackerel> mackerels = new CopyOnWriteArrayList<>();

    // FILO后进先出，刚用完归还的连接相对于空闲较久的连接更鲜活
    private BlockingDeque<Mackerel> idleMackerels = new LinkedBlockingDeque<>();

    private volatile boolean closed = false;
    private Feeder feeder;
    private AtomicInteger waitingThreadCount = new AtomicInteger(0);

    private final String poolName;
    private final String jdbcUrl;
    private final String userName;
    private final String password;
    private final String catalog;
    private final String schema;

    // region pool properties, allow changed on running
    private volatile int minIdle;
    private volatile int maxSize;
    private volatile long maxWait;
    private volatile boolean testWhileIdle;
    private volatile long validateWindow; // 检测窗口，间隔多久进行一次检测
    private volatile int validateTimeout; // 检测活性超时时间
    private volatile long validateIdleTime; // testWhileIdle=true 时，如果连接空闲超过validateIdle则需要检测活性

    private volatile long maxIdleTime;
    private volatile long minIdleTime;
    // endregion

    public MackerelCan(MackerelConfig config) {
        if (config.getPoolName() == null) {
            config.setPoolName("MackerelDataSource#" + id.getAndIncrement());
        }
        this.poolName = config.getPoolName();
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
    }

    public void checkFailFast() {
        //重试三次，避免由于网络原因导致的连接失败
        int retries = 3;
        SQLException fail = null;
        while (retries > 0) {
            try {
                this.feeder.incubateMackerel();
                return;
            } catch (SQLException e) {
                retries--;
                fail = e;
            }
        }
        if (fail != null) {
            throw new MackerelInitException("check fail fast", fail);
        }
    }

    private void validateAndInitConfig(MackerelConfig config) {
        setMinIdle(config.getMinIdle());
        setMaxSize(config.getMaxSize());
        setMaxWait(config.getMaxWait());
        setTestWhileIdle(config.isTestWhileIdle());
        setValidateWindow(config.getValidateWindow());
        setValidateTimeout(config.getValidateTimeout());
        setValidateIdleTime(config.getValidateIdleTime());
        setMaxIdleTime(config.getMaxIdleTime());
        setMinIdleTime(config.getMinIdleTime());
    }

    public Mackerel getMackerel() {
        long start = System.currentTimeMillis();
        long end = start + this.maxWait;
        long waitTime = this.maxWait > 0 ? this.maxWait : 0;

        int waiting = this.waitingThreadCount.getAndIncrement();
        try {
            while (waitTime >= 0 && !this.closed) {
                Mackerel mackerel = null;
                if (this.maxWait <= 0) {
                    mackerel = idleMackerels.takeFirst();
                } else if (waitTime > 0) {
                    mackerel = idleMackerels.pollFirst(waitTime, TimeUnit.MILLISECONDS);
                    waitTime = end - System.currentTimeMillis();
                }

                if (mackerel != null) {
                    //1. testWhileIdle 
                    if (testWhileIdle && mackerel.getIdleDuration() > validateIdleTime && !mackerel.validate()) {
                        mackerel.markEvicted();
                    }
                    //2. markActive
                    else if (mackerel.markActive()) {
                        return mackerel;
                    }
                }
            }
            throw new MackerelException(
                    "cannot get connection after wait " + (System.currentTimeMillis() - start) + "ms");
        } catch (InterruptedException e) {
            throw new MackerelException("fetching connection interrupted", e);
        } finally {
            if (!this.closed) {
                feeder.feed(waiting);
            }
            this.waitingThreadCount.decrementAndGet();
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

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public void setValidateWindow(long validateWindow) {
        if (validateWindow < (10 * 1000))
            throw new IllegalArgumentException("validateWindow cannot less than 10 seconds");
        this.validateWindow = validateWindow;
    }

    public void setValidateIdleTime(long validateIdleTime) {
        if (validateWindow < (1 * 1000))
            throw new IllegalArgumentException("validateWindow cannot less than 1 seconds");
        this.validateIdleTime = validateIdleTime;
    }

    public void setValidateTimeout(int validateTimeout) {
        this.validateTimeout = validateTimeout;
    }

    public void setMaxIdleTime(long maxIdleTime) {
        if (maxIdleTime < (60 * 1000))
            throw new IllegalArgumentException("maxIdleTime cannot less than 1 minute");
        this.maxIdleTime = maxIdleTime;
    }

    public void setMinIdleTime(long minIdleTime) {
        if (minIdleTime < 30 * 1000)
            throw new IllegalArgumentException("minIdleTime cannot less than 30 seconds");
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

    public long getMaxWait() {
        return maxWait;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public long getValidateWindow() {
        return validateWindow;
    }

    public long getValidateIdleTime() {
        return validateIdleTime;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchema() {
        return schema;
    }

    public int getValidateTimeout() {
        return validateTimeout;
    }

    public long getMinIdleTime() {
        return minIdleTime;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public int getCurrentSize() {
        return mackerels.size();
    }

    public int getWaitingThreadCount() {
        return waitingThreadCount.get();
    }

    public CopyOnWriteArrayList<Mackerel> getAllMackerels() {
        return this.mackerels;
    }

    public void add(Mackerel mackerel) {
        this.mackerels.add(mackerel);
        // 塞到头部，刚创建或归还的连接可用性比较靠谱
        this.idleMackerels.addFirst(mackerel);
    }

    public void remove(List<Mackerel> removed) {
        this.mackerels.removeAll(removed);
        this.idleMackerels.removeAll(removed);
    }

    public void returnIdle(Mackerel mackerel) {
        this.idleMackerels.addFirst(mackerel);
    }

    public String getStatistics() {
        return "total=" + this.mackerels.size() + ", idle=" + idleMackerels.size() + ", waiting=" + waitingThreadCount.get();
    }

    @Override
    public void close() throws Exception {
        LOGGER.debug("closing MackerelCan...");

        this.closed = true;

        feeder.close();

        ExecutorService abortExecutor = Executors.newFixedThreadPool(1);
        for (Mackerel mackerel : mackerels) {
            if (mackerel.isIdle()) {
                mackerel.closeQuietly();
            } else {
                //根据 abort() 的语义，正在执行的连接，基于底层驱动的实现逻辑，允许sql执行完毕再关闭
                mackerel.abortQuietly(abortExecutor);
            }
        }
        abortExecutor.shutdown();
        mackerels.clear();
        idleMackerels.clear();
    }

    @Override
    public String toString() {
        return this.poolName;
    }
}
