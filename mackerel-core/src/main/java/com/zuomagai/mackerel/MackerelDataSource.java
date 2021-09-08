package com.zuomagai.mackerel;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 *
 * @author S.S.Y
 **/
public class MackerelDataSource implements DataSource, AutoCloseable {

    private static final AtomicInteger id = new AtomicInteger(0);
    private static final int STATUS_OPEN = 1;
    private static final int STATUS_SHUTDOWN = 2;

    private String name;
    private MackerelCan mackerelCan;
    private volatile int status;

    public MackerelDataSource(MackerelConfig config) {
        validateConfig(config);
        this.name = config.getName();
        this.mackerelCan = new MackerelCan(config);
        if (config.isCheckFailFast()) {
            this.mackerelCan.checkFailFast();
        }
    }

    public void init() {
        insureOpen();
    }

    @Override
    public Connection getConnection() throws SQLException {
        insureOpen();
        return mackerelCan.getMackerel().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        insureOpen();
        return mackerelCan.getMackerel().getConnection();
    }

    private void validateConfig(MackerelConfig config) {
        if (config.getName() == null) {
            config.setName("MackerelDataSource#" + id.getAndIncrement());
        }

        //TODO 初始化的时候要不要检查 jdbcUrl、username、password的有效性？
    }

    /**
     * mysql-connector 5.0.0 (2005-12-22)之后就支持 ServiceLoader SPI，不需要显示 Class.forName 显示注册驱动
     * pg connector Version 42.2.13 (2020-06-04) 也支持 ServiceLoader SPI了
     * 仅当 classpath 存在多个db厂商的driver 或者没提供 service-provider spi的driver，才需要显示注册驱动
     * 
     * @param config
     */
    private void validateDriver(MackerelConfig config) {
        String driverName = EnumDriver.findDriver(config.getJdbcUrl());
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("driver(" + driverName + ") not exists");
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            return iface.cast(this);
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return this.getClass().isAssignableFrom(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void close() throws Exception {
        //TODO: close pool resources 
        mackerelCan.close();
    }

    private synchronized void insureOpen() {
        if (status == STATUS_SHUTDOWN) {
            throw new MackerelStatusException("Mackerel DataSource has bean closed");
        }

        if (status != STATUS_OPEN) {
            this.mackerelCan.init();
            status = STATUS_OPEN;
        }
    }
}
