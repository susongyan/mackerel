package com.zuomagai.mackerel;

/**
 * @author : S.S.Y
 **/
public class MackerelConfig {

    private String name; 

    // url config
    private String jdbcUrl;
    private String userName;
    private String password;

    // pool config, can change onlive
    private int minIdle = 10;
    private int maxSize = 10;
    private long maxWait = 800;
    private boolean testWhileIdle = true; //连接取出时，如果空闲超过一定时间则要检验有效性
    private long validateWindow = 1 * 60 * 1000;
    private long maxIdleTime = 30 * 60 * 1000; // default 30 minutes
    private long minIdleTime = 10 * 60 * 1000; // default 10 minutes

    // optional
    private String catalog;
    private String schema;

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

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public long getValidateWindow() {
        return validateWindow;
    }

    public void setValidateWindow(long validateWindow) {
        this.validateWindow = validateWindow;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public long getMinIdleTime() {
        return minIdleTime;
    }

    public void setMinIdleTime(long minIdleTime) {
        this.minIdleTime = minIdleTime;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getSchema() {
        return schema;
    }
}
