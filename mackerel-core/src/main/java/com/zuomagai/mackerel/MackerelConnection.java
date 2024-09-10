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
 * <p>
 *  TODO catch conn ex
 *
 * @author S.S.Y
 **/
public class MackerelConnection implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(MackerelConnection.class);

    private static final boolean DEFAULT_AUTO_COMMIT = true;
    private static final boolean DEFAULT_READ_ONLY = false;
    private static final int ISOLATION_NOT_SET = -1;

    private Mackerel mackerel;
    private Connection delegate;

    /**
     * 物理连接是否关闭
     */
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
        if (this.isClosed) {
            return;
        }
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
        try {
            checkState();
            delegate.commit();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        try {
            checkState();
            return delegate.createArrayOf(typeName, elements);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public Blob createBlob() throws SQLException {
        try {
            checkState();
            return delegate.createBlob();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public Clob createClob() throws SQLException {
        try {
            checkState();
            return delegate.createClob();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public NClob createNClob() throws SQLException {
        try {
            checkState();
            return delegate.createNClob();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        try {
            checkState();
            return delegate.createSQLXML();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public Statement createStatement() throws SQLException {
        try {
            checkState();
            return new MackerelStatement(delegate.createStatement(), this);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        try {
            checkState();
            return new MackerelStatement(delegate.createStatement(resultSetType, resultSetConcurrency), this);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        try {
            checkState();
            return new MackerelStatement(delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability), this);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        try {
            checkState();
            return delegate.createStruct(typeName, attributes);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        try {
            checkState();
            return delegate.getAutoCommit();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public String getCatalog() throws SQLException {
        try {
            checkState();
            return delegate.getCatalog();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        try {
            checkState();
            return delegate.getClientInfo();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        try {
            checkState();
            return delegate.getClientInfo(name);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public int getHoldability() throws SQLException {
        try {
            checkState();
            return delegate.getHoldability();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        try {
            checkState();
            return delegate.getMetaData();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException("getNetTimeoutExecutor()");
    }

    @Override
    public String getSchema() throws SQLException {
        try {
            checkState();
            return delegate.getSchema();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        try {
            checkState();
            return delegate.getTransactionIsolation();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        try {
            checkState();
            return delegate.getTypeMap();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        try {
            checkState();
            return delegate.getWarnings();
        } catch (
                SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.isClosed;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return this.readOnly;
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        try {
            checkState();
            return delegate.isValid(timeout);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        try {
            checkState();
            return delegate.nativeSQL(sql);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        try {
            checkState();
            return delegate.prepareCall(sql);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        try {
            checkState();
            return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);

        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        try {
            checkState();
            return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        try {
            checkState();
            return new MackerelPreparedStatement(delegate.prepareStatement(sql), this);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        try {
            checkState();
            return new MackerelPreparedStatement(delegate.prepareStatement(sql, autoGeneratedKeys), this);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        try {
            checkState();
            return new MackerelPreparedStatement(delegate.prepareStatement(sql, columnIndexes), this);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        try {
            checkState();
            return new MackerelPreparedStatement(delegate.prepareStatement(sql, columnNames), this);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        try {
            checkState();
            return new MackerelPreparedStatement(delegate.prepareStatement(sql, resultSetType, resultSetConcurrency), this);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
        try {
            checkState();
            return new MackerelPreparedStatement(delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability), this);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        try {
            checkState();
            delegate.releaseSavepoint(savepoint);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public void rollback() throws SQLException {
        try {
            checkState();
            delegate.rollback();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        try {
            checkState();
            delegate.rollback(savepoint);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        try {
            checkState();
            delegate.setAutoCommit(autoCommit);
            this.autoCommit = autoCommit;
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        try {
            checkState();
            delegate.setCatalog(catalog);
            this.catalog = catalog;
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        try {
            delegate.setClientInfo(properties);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        try {
            delegate.setClientInfo(name, value);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        try {
            delegate.setHoldability(holdability);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNetworkTimeout(Executor executor, int milliseconds)");
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        try {
            checkState();
            delegate.setReadOnly(readOnly);
            this.readOnly = readOnly;
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        try {
            checkState();
            return delegate.setSavepoint();
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        try {
            checkState();
            return delegate.setSavepoint(name);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        try {
            checkState();
            delegate.setSchema(schema);
            this.schema = schema;
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        try {
            checkState();
            delegate.setTransactionIsolation(level);
            this.transactionIsolation = level;
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        try {
            checkState();
            delegate.setTypeMap(map);
        } catch (SQLException e) {
            checkException(e);
            throw e;
        }
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

    public void checkException(SQLException sqlEx) {
        // f connection broken, recycle it
        if (Cat.checkSmelly(sqlEx)) {
            this.mackerel.quit();
        }
    }
}
