package com.zuomagai.mackerel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * connection holder
 * 
 * @author S.S.Y
 **/
public class Mackerel {
    private static final Logger LOGGER = LoggerFactory.getLogger(Mackerel.class);

    // properties
    // 1. connection
    // 2. state
    // 3. lifetime controller

    // valid status cycle:
    // 1. idle -> reserved/active
    // 2. reserved -> evicted/idle
    // 3. active -> idle
    private static final int STATUS_EVICTED = -1;
    private static final int STATUS_RESERVED = 0;
    private static final int STATUS_IDLE = 1;
    private static final int STATUS_ACTIVE = 2;

    private MackerelCan mackerelCan;
    private MackerelConnection connection;
    private volatile AtomicInteger status = new AtomicInteger(STATUS_IDLE);
    private long lastTakenOutTime;
    private long idleTime = System.currentTimeMillis();
    private long lastValidateTime;

    public Mackerel(MackerelCan mackerelCan, Connection connection) {
        this.mackerelCan = mackerelCan;
        this.connection = new MackerelConnection(this, connection);
    }

    public Connection getConnection() {
        return this.connection;
    }

    public boolean isIdle() {
        return status.get() == STATUS_IDLE;
    }

    public boolean reserve() {
        return status.compareAndSet(STATUS_IDLE, STATUS_RESERVED);
    }

    public boolean markActive() {
        return status.compareAndSet(STATUS_IDLE, STATUS_ACTIVE);
    }

    public boolean markIdle() {
        return status.compareAndSet(STATUS_RESERVED, STATUS_IDLE);
    }

    public boolean markEvicted() {
        return status.compareAndSet(STATUS_RESERVED, STATUS_EVICTED);
    }

    public MackerelCan getMackerelCan() {
        return this.mackerelCan;
    }

    public long getIdleDuration() {
        return System.currentTimeMillis() - idleTime;
    }

    public long getLastValidateDuration() {
        return System.currentTimeMillis() - lastValidateTime;
    }

    public boolean takenOut() {
        boolean ret = status.compareAndSet(STATUS_IDLE, STATUS_ACTIVE);
        if (ret)
            lastTakenOutTime = System.currentTimeMillis();
        return ret;
    }

    public boolean backToCan() {
        boolean ret = status.compareAndSet(STATUS_ACTIVE, STATUS_IDLE);
        if (ret) {
            this.idleTime = System.currentTimeMillis();
            this.mackerelCan.returnIdle(this);
            LOGGER.debug("--> after return: " + mackerelCan.toString());
        }
        return ret;
    }

    public boolean validate() {
        try {
            //暂不考虑的古董版本不支持 jdbc4.0 的驱动
            lastValidateTime = System.currentTimeMillis();
            return this.connection.isValid(mackerelCan.getValidateTimeout() / 1000);
        } catch (SQLException e) {
            LOGGER.error("valid connection fail", e);
            return false;
        }
    }

    public void closeQuietly() {
        try {
            if (!this.connection.isClosed()) {
                LOGGER.debug("closing connection quietly... " + this.toString());
                this.connection.closeInternal();
            }
            this.connection = null;
        } catch (SQLException e) {
            LOGGER.warn("close connection quietly fail", e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
    }
}
