package com.zuomagai.mackerel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executor;
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

    public boolean isEvicted() {
        return status.get() == STATUS_EVICTED;
    }

    public boolean reserve() {
        return status.compareAndSet(STATUS_IDLE, STATUS_RESERVED);
    }

    public boolean markActive() {
        this.lastTakenOutTime = System.currentTimeMillis();
        return status.compareAndSet(STATUS_IDLE, STATUS_ACTIVE);
    }

    public boolean markIdle() {
        return status.compareAndSet(STATUS_RESERVED, STATUS_IDLE);
    }

    public boolean markEvicted() {
        return status.compareAndSet(STATUS_RESERVED, STATUS_EVICTED);
    }

    public void forceMarkEvicted() {
        status.set(STATUS_EVICTED);
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

    public boolean backToCan() {
        boolean ret = status.compareAndSet(STATUS_ACTIVE, STATUS_IDLE);
        if (ret) {
            this.idleTime = System.currentTimeMillis();
            this.mackerelCan.returnIdle(this);
            LOGGER.debug("--> after return: " + mackerelCan.getStatistics());
        }
        return ret;
    }

    public boolean validate() {
        try {
            long start = System.currentTimeMillis();
            LOGGER.debug("validate... {}", toString());
            //暂不考虑的古董版本不支持 jdbc4.0 的驱动
            lastValidateTime = System.currentTimeMillis();
            boolean valid = this.connection.isValid(mackerelCan.getValidateTimeout() / 1000);
            LOGGER.debug("validate {} {} {}", toString(), (System.currentTimeMillis() - start) + "ms",
                    valid ? "success" : "fail");
            return valid;
        } catch (SQLException e) {
            LOGGER.error("validate connection fail", e);
            return false;
        }
    }

    public void quit() {
        this.status.set(STATUS_EVICTED);
    }

    public void closeQuietly() {
        try {
            if (!this.connection.isClosed()) {
                LOGGER.debug("closing connection quietly... {}", this);
                this.connection.closePhysical();
            }
            this.connection = null;
        } catch (SQLException e) {
            LOGGER.warn("close connection quietly fail", e);
        }
    }

    public void abortQuietly(Executor executor) {
        try {
            if (!this.connection.isClosed()) {
                LOGGER.debug("abort connection quietly... {}", this);
                this.connection.abort(executor);
            }
            this.connection = null;
        } catch (SQLException e) {
            LOGGER.warn("abort connection quietly fail", e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
    }
}
