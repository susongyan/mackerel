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
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zuomagai.mackerel.util.StringUtils;

/**
 * proxy connection (1) close => return to pool (2) do some statics monitor
 * 
 *  TODO catch conn ex 
 * @author S.S.Y
 **/
public class MackerelConnection implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(MackerelConnection.class);

    private static final boolean DEFAULT_AUTO_COMMIT = true;
    private static final boolean DEFAULT_READ_ONLY = false;
    private static final int ISOLATION_NOT_SET = -1;

    private Mackerel mackerel;
    private Connection delegate;

    private boolean isClosed = false;
    private boolean autoCommit = DEFAULT_AUTO_COMMIT;
    private boolean readOnly = DEFAULT_READ_ONLY;
    private int networkTimeout; // 连接超时，暂时不支持；建议在 jdbcUrl设置socketTimeout
    private int transactionIsolation = ISOLATION_NOT_SET;
    private String catalog;
    private String schema;

    private static Boolean supportNetworkTimeout = null;

    private String initialCatalog;
    private String initialSchema;
    private int initialNetworkTimeout;
    private int initialTransactionIsolation = ISOLATION_NOT_SET;

    public MackerelConnection(Mackerel mackerel, Connection delegate) {
        this.mackerel = mackerel;
        this.delegate = delegate;
        this.initialCatalog = mackerel.getMackerelCan().getCatalog();
        this.initialSchema = mackerel.getMackerelCan().getSchema();

        setup();
    }

    @Override
    public void close() throws SQLException {
        // reset connection first
        reset();
        // cas status first & return pool
        mackerel.backToCan();
    }

    /**
     * 关闭物理连接
     *
     * @throws SQLException
     */
    public void closePhysical() throws SQLException {
        this.isClosed = true;
        this.delegate.close();
    }

    /**
     * 获取连接的初始属性值，以便连接归还的时候重置会话级别的属性，避免影响下次连接取出后的行为 注意: 不同数据库的 jdbc
     * api支持程度和实现逻辑不一定一致，有的是空方法有的是直接抛出异常；比如 pg不支持 networkTimeout 设置，会抛出异常
     * 所以还是需要根据自己要支持的数据库驱动，根据他们的实现的差异来处理异常（忽略还是阻断执行）
     * <p>
     * //TODO 异常处理， 底层连接每次set属性的时候都会检查连接是否已关闭，虽然说刚创建的连接一般是可用的，但是不能保证问题导致连接断开
     * //TODO setUp 的处理挪到 can 池子里，有些判断是全局的，如supportNetworkTimeout 判断一次就好了 ?
     */
    private void setup() {

        // pg不支持 networkTimeut
        // setNetworkTimeout(executor, timeout); 重置的时候 我也不知道怎么设置这个executor => 先不支持

        // if (supportNetworkTimeout == null || supportNetworkTimeout) {
        // try {
        // this.initialNetworkTimeout = this.real.getNetworkTimeout();
        // } catch (SQLException e) {
        // // ignore
        // // "get/set networkTimeout not supported by driver"
        // supportNetworkTimeout = false;
        // }
        // this.networkTimeout = this.initialNetworkTimeout;
        // }

        try {
            if (StringUtils.isNotEmpty(this.initialCatalog)) {
                this.delegate.setCatalog(this.initialCatalog);
            }
            if (StringUtils.isNotEmpty(this.initialSchema)) {
                this.delegate.setSchema(this.initialSchema);
            }

            this.initialCatalog = this.delegate.getCatalog();
            this.initialSchema = this.delegate.getSchema();
            this.initialTransactionIsolation = this.delegate.getTransactionIsolation();
            this.catalog = this.initialCatalog;
            this.schema = this.initialSchema;
            this.transactionIsolation = initialTransactionIsolation;
        } catch (SQLException e) {
            LOGGER.error("setup connection properties fail", e);
            throw new MackerelException("set up connection properties fail", e);
        }
    }

    private void reset() throws SQLException {
        if (this.autoCommit != DEFAULT_AUTO_COMMIT) {
            this.delegate.setAutoCommit(DEFAULT_AUTO_COMMIT);
        }

        if (this.readOnly != DEFAULT_READ_ONLY) {
            this.delegate.setReadOnly(DEFAULT_READ_ONLY);
        }

        if (!Objects.equals(this.catalog, this.initialCatalog)) {
            this.delegate.setCatalog(this.initialCatalog);
        }

        if (!Objects.equals(this.schema, this.initialSchema)) {
            this.delegate.setSchema(this.initialSchema);
        }

        if (this.transactionIsolation != this.initialTransactionIsolation) {
            this.delegate.setTransactionIsolation(this.initialTransactionIsolation);
        }

        this.delegate.clearWarnings();
    }
    // region delegate

    @Override
    public void abort(Executor executor) throws SQLException {
        this.isClosed = true;
        delegate.abort(executor);
    }

    @Override
    public void clearWarnings() throws SQLException {
        checkState();
        delegate.clearWarnings();
    }

    @Override
    public void commit() throws SQLException {
        checkState();
        delegate.commit();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        checkState();
        return delegate.createArrayOf(typeName, elements);
    }

    @Override
    public Blob createBlob() throws SQLException {
        checkState();
        return delegate.createBlob();
    }

    @Override
    public Clob createClob() throws SQLException {
        checkState();
        return delegate.createClob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        checkState();
        return delegate.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        checkState();
        return delegate.createSQLXML();
    }

    @Override
    public Statement createStatement() throws SQLException {
        checkState();
        return delegate.createStatement();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        checkState();
        return delegate.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        checkState();
        return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        checkState();
        return delegate.createStruct(typeName, attributes);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        checkState();
        return delegate.getAutoCommit();
    }

    @Override
    public String getCatalog() throws SQLException {
        checkState();
        return delegate.getCatalog();
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        checkState();
        return delegate.getClientInfo();
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        checkState();
        return delegate.getClientInfo(name);
    }

    @Override
    public int getHoldability() throws SQLException {
        checkState();
        return delegate.getHoldability();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        checkState();
        return delegate.getMetaData();
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException("getNetTimeoutExecutor()");
    }

    @Override
    public String getSchema() throws SQLException {
        checkState();
        return delegate.getSchema();
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        checkState();
        return delegate.getTransactionIsolation();
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        checkState();
        return delegate.getTypeMap();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        checkState();
        return delegate.getWarnings();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.isClosed;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        checkState();
        return delegate.isReadOnly();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        checkState();
        return delegate.isValid(timeout);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        checkState();
        return delegate.nativeSQL(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        checkState();
        return delegate.prepareCall(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        checkState();
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        checkState();
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        checkState();
        return delegate.prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        checkState();
        return delegate.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        checkState();
        return delegate.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        checkState();
        return delegate.prepareStatement(sql, columnNames);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        checkState();
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        checkState();
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        checkState();
        delegate.releaseSavepoint(savepoint);
    }

    @Override
    public void rollback() throws SQLException {
        checkState();
        delegate.rollback();
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        checkState();
        delegate.rollback(savepoint);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        checkState();
        delegate.setAutoCommit(autoCommit);
        this.autoCommit = autoCommit;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        checkState();
        delegate.setCatalog(catalog);
        this.catalog = catalog;
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        //TODO 特殊处理
        delegate.setClientInfo(properties);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        //TODO 特殊处理
        delegate.setClientInfo(name, value);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        delegate.setHoldability(holdability);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNetworkTimeout(Executor executor, int milliseconds)");
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        checkState();
        delegate.setReadOnly(readOnly);
        this.readOnly = readOnly;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        checkState();
        return delegate.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        checkState();
        return delegate.setSavepoint(name);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        checkState();
        delegate.setSchema(schema);
        this.schema = schema;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        checkState();
        delegate.setTransactionIsolation(level);
        this.transactionIsolation = level;
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        checkState();
        delegate.setTypeMap(map);
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
            return this.delegate.unwrap(iface);
        }
    }

    /**
     * 抛出原生的 SQLException 是为了方便 业务在使用的时候，根据原始sqlException 做判断
     * 
     * @throws SQLException
     */
    private void checkState() throws SQLException {
        if (this.isClosed) {
            throw new SQLException("Connection is closed");
        }     
    }

    /**
     * //TODO  判断sql 执行遇到的连接异常的错误
     * 
     * @param sqlEx
     */
     void checkException(SQLException sqlEx) {

     }

}
