package com.zuomagai.mackerel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * @author S.S.Y
 **/
public class MackerelDataSource implements DataSource, AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MackerelDataSource.class);
    private static final int STATUS_OPEN = 1;
    private static final int STATUS_SHUTDOWN = 2;

    private final MackerelCan mackerelCan;
    private volatile int status;

    public MackerelDataSource(MackerelConfig config) {
        this.mackerelCan = new MackerelCan(config);
        if (config.isCheckFailFast()) {
            this.mackerelCan.checkFailFast();
        }
    }

    public void init() {
        insureOpen();
    }

    public MackerelCan getMackerelCan() {
        return mackerelCan;
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
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(this.getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw new SQLException("cannot unwrap MackerelDataSource to " + iface.getName());
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
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void close() throws Exception {
        if (status == STATUS_SHUTDOWN) {
            return;
        }
        status = STATUS_SHUTDOWN;
        LOGGER.debug("closing MackerelDataSource {}...", this.mackerelCan);
        mackerelCan.close();
    }

    private void insureOpen() {
        if (status == STATUS_SHUTDOWN) {
            throw new MackerelStatusException("MackerelDataSource " + this.mackerelCan + " has bean closed");
        }

        if (status != STATUS_OPEN) {
            LOGGER.debug("init MackerelDataSource {}...", this.mackerelCan);
            status = STATUS_OPEN;
            this.mackerelCan.init();
        }
    }
}
