package com.zuomagai.macherel;

/**
 * @author : holysu
 * @since : 2021/3/31
 **/
public class MacherelConfig {

    private int initialSize;
    private int minIdle;
    private int maxSize;
    private long maxWait;
    private boolean testWhileIdle;
    private long validateWindow = 5000;
    private long maxIdleTime;

    private long maxLifetime = 7 * 3600 * 1000;
}
