package com.zuomagai.mackerel;

import java.sql.Connection;

/**
 * connection
 * @author : holysu
 **/
public class Mackerel {
    // properties
    // 1. connection
    // 2. state
    // 3. lifetime controller

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_EVICTED = 2;

    private Connection connection;
    private volatile int status = STATUS_IDLE;
    private long returnTime;

    public Mackerel(Connection connection) {
        this.connection = connection;
    }

    public long getIdleDuration () {
        if (returnTime == 0)
            return 0;
        return System.currentTimeMillis() - returnTime;
    }
}
