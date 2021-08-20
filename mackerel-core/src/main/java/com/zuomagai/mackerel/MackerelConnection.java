package com.zuomagai.mackerel;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * proxy connection (1) close => return to pool (2) do some statics monitor
 * 
 * @author S.S.Y
 **/
public class MackerelConnection implements Connection {

    private Mackerel mackerel;
    private Connection real;

    private boolean readOnly = false;
    private boolean autoCommit = true;
    private int networkTimeout;
    private int transactionIsolation;
    private String catalog;
    private String schema;

    private Map<String, Class<?>> typeMap;

    public MackerelConnection(Mackerel mackerel, Connection real) {
        this.mackerel = mackerel;
        this.real = real;
        setUp();
    }

    @Override
    public void close() throws SQLException {
        // reset connection first
        reset();
        // cas status first & return pool
        mackerel.returnIdle();
    }

    /**
     * 设置初始属性，以便连接归还的时候重置会话级别的属性，避免影响下次连接取出后的行为
     */
    public void setUp() {
         
    }

    public void reset() {
        // TODO reset before return pool
        // autoCommit
        // readOnly
        // catalog
        // schema
        // isolation level
        // networkTimeout
        // ...
    }

    // region delegate
    @Override
    public void abort(Executor executor) throws SQLException {
        real.abort(executor);
    }

    @Override
    public void clearWarnings() throws SQLException {
        real.clearWarnings();
    }

    @Override
    public void commit() throws SQLException {
        real.commit();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return real.createArrayOf(typeName, elements);
    }

    @Override
    public Blob createBlob() throws SQLException {
        return real.createBlob();
    }

    @Override
    public Clob createClob() throws SQLException {
        return real.createClob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return real.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return real.createSQLXML();
    }

    @Override
    public Statement createStatement() throws SQLException {
        return real.createStatement();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return real.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return real.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return real.createStruct(typeName, attributes);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return real.getAutoCommit();
    }

    @Override
    public String getCatalog() throws SQLException {
        return real.getCatalog();
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return real.getClientInfo();
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return real.getClientInfo(name);
    }

    @Override
    public int getHoldability() throws SQLException {
        return real.getHoldability();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return real.getMetaData();
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return real.getNetworkTimeout();
    }

    @Override
    public String getSchema() throws SQLException {
        return real.getSchema();
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return real.getTransactionIsolation();
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return real.getTypeMap();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return real.getWarnings();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return real.isClosed();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return real.isReadOnly();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return real.isValid(timeout);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return real.nativeSQL(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return real.prepareCall(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return real.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return real.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return real.prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return real.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return real.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return real.prepareStatement(sql, columnNames);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return real.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return real.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        real.releaseSavepoint(savepoint);
    }

    @Override
    public void rollback() throws SQLException {
        real.rollback();
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        real.rollback(savepoint);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        real.setAutoCommit(autoCommit);
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        real.setCatalog(catalog);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        real.setClientInfo(properties);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        real.setClientInfo(name, value);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        real.setHoldability(holdability);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        real.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        real.setReadOnly(readOnly);
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return real.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return real.setSavepoint(name);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        real.setSchema(schema);
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        real.setTransactionIsolation(level);
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        real.setTypeMap(map);
    }
    // endregion

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(this.getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        } else {
            return this.real.unwrap(iface);
        }
    }  
}
