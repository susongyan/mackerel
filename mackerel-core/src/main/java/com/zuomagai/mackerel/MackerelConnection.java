package com.zuomagai.mackerel;

import com.zuomagai.mackerel.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * proxy connection (1) close => return to pool (2) do some statics monitor
 *
 * @author S.S.Y
 **/
public class MackerelConnection implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(MackerelConnection.class);

    private static final boolean DEFAULT_AUTO_COMMIT = true;
    private static final boolean DEFAULT_READ_ONLY = false;
    private static final int ISOLATION_NOT_SET = -1;

    private final Mackerel mackerel;
    private final Connection real;

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

    public MackerelConnection(Mackerel mackerel, Connection real) {
        this.mackerel = mackerel;
        this.real = real;
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
        this.real.close();
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
                this.real.setCatalog(this.initialCatalog);
            }
            if (StringUtils.isNotEmpty(this.initialSchema)) {
                this.real.setSchema(this.initialSchema);
            }

            this.initialCatalog = this.real.getCatalog();
            this.initialSchema = this.real.getSchema();
            this.initialTransactionIsolation = this.real.getTransactionIsolation();
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
            this.real.setAutoCommit(DEFAULT_AUTO_COMMIT);
        }

        if (this.readOnly != DEFAULT_READ_ONLY) {
            this.real.setReadOnly(DEFAULT_READ_ONLY);
        }

        if (!Objects.equals(this.catalog, this.initialCatalog)) {
            this.real.setCatalog(this.initialCatalog);
        }

        if (!Objects.equals(this.schema, this.initialSchema)) {
            this.real.setSchema(this.initialSchema);
        }

        if (this.transactionIsolation != this.initialTransactionIsolation) {
            this.real.setTransactionIsolation(this.initialTransactionIsolation);
        }

        this.real.clearWarnings();
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
        throw new SQLFeatureNotSupportedException("getNetTimeoutExecutor()");
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
        this.autoCommit = autoCommit;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        real.setCatalog(catalog);
        this.catalog = catalog;
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
        throw new SQLFeatureNotSupportedException("setNetworkTimeout(Executor executor, int milliseconds)");
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        real.setReadOnly(readOnly);
        this.readOnly = readOnly;
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
        this.schema = schema;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        real.setTransactionIsolation(level);
        this.transactionIsolation = level;
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
