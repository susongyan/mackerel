package com.zuomagai.mackerel;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * connection
 * 
 * @author S.S.Y
 **/
public class Mackerel {

    // properties
    // 1. connection
    // 2. state
    // 3. lifetime controller
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_EVICTED = 2;

    private MackerelCan mackerelCan;
    private Connection connection;
    private volatile AtomicInteger status = new AtomicInteger(STATUS_IDLE);
    private long returnTime;

    public Mackerel(MackerelCan mackerelCan, Connection connection) {
        this.mackerelCan = mackerelCan;
        this.connection = new MackerelConnection(this, connection);
        this.markActive();
    }

    public Connection getConnection() {
        return this.connection;
    }

    public MackerelCan getMackerelCan() {
        return this.mackerelCan;
    }

    public long getIdleDuration() {
        if (returnTime == 0)
            return 0;
        return System.currentTimeMillis() - returnTime;
    }

    public boolean markActive() {
        return status.compareAndSet(STATUS_IDLE, STATUS_ACTIVE);
    }

    public boolean returnIdle() {
        boolean ret = status.compareAndSet(STATUS_ACTIVE, STATUS_IDLE);
        if (ret) {
            this.returnTime = System.currentTimeMillis();
            this.mackerelCan.returnIdle(this);
        }
        return ret;
    }
}
