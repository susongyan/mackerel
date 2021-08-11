package com.zuomagai.mackerel;

/**
 * @author : holysu
 **/
public class MackerelConfig {

    private String name; 

    // jdbc config
    private String jdbcUrl;
    private String userName;
    private String password;

    // pool config
    private int initialSize;
    private int minIdle;
    private int maxSize;
    private long maxWait;
    private boolean testWhileIdle;
    private long validateWindow = 5000;
    private long maxIdleTime;

    private long maxLifetime = 7 * 3600 * 1000;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
