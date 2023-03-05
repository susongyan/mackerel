package com.zuomagai.mackerel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 * @author susongyan
 **/
public class MackerelStatement implements Statement {

    private volatile boolean isClosed = false;
    private final Statement delegate;
    private final MackerelConnection mackerelConnection;

    public MackerelStatement(Statement delegate, MackerelConnection mackerelConnection) {
        this.delegate = delegate;
        this.mackerelConnection = mackerelConnection;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        } else {
            return this.delegate.unwrap(iface);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(this.getClass());
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        try {
            return this.delegate.executeQuery(sql);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        try {
            return this.delegate.executeUpdate(sql);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void close() throws SQLException {
        if (this.isClosed) {
            return;
        }

        try {
            this.delegate.close();
            this.isClosed = true;
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        try {
            return this.delegate.getMaxFieldSize();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        try {
            this.delegate.setMaxFieldSize(max);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int getMaxRows() throws SQLException {
        try {
            return this.delegate.getMaxRows();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        try {
            this.delegate.setMaxRows(max);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        try {
            this.delegate.setEscapeProcessing(enable);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        try {
            return this.delegate.getQueryTimeout();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        try {
            this.delegate.setQueryTimeout(seconds);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void cancel() throws SQLException {
        try {
            this.delegate.cancel();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        try {
            return this.delegate.getWarnings();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void clearWarnings() throws SQLException {
        try {
            this.delegate.clearWarnings();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        try {
            this.delegate.setCursorName(name);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        try {
            return this.delegate.execute(sql);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        try {
            return this.delegate.getResultSet();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int getUpdateCount() throws SQLException {
        try {
            return this.delegate.getUpdateCount();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        try {
            return this.delegate.getMoreResults();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        try {
            this.delegate.setFetchDirection(direction);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int getFetchDirection() throws SQLException {
        try {
            return this.delegate.getFetchDirection();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        try {
            this.delegate.setFetchSize(rows);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int getFetchSize() throws SQLException {
        try {
            return this.delegate.getFetchSize();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        try {
            return this.delegate.getResultSetConcurrency();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int getResultSetType() throws SQLException {
        try {
            return this.delegate.getResultSetType();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        try {
            this.delegate.addBatch(sql);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void clearBatch() throws SQLException {
        try {
            this.delegate.clearBatch();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int[] executeBatch() throws SQLException {
        try {
            return this.delegate.executeBatch();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return this.delegate.getConnection();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        try {
            return this.delegate.getMoreResults(current);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        try {
            return this.delegate.getGeneratedKeys();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        try {
            return this.delegate.executeUpdate(sql, autoGeneratedKeys);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        try {
            return this.delegate.executeUpdate(sql, columnIndexes);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        try {
            return this.delegate.executeUpdate(sql, columnNames);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        try {
            return this.delegate.execute(sql, autoGeneratedKeys);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        try {
            return this.delegate.execute(sql, columnIndexes);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        try {
            return this.delegate.execute(sql, columnNames);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        try {
            return this.delegate.getResultSetHoldability();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.isClosed;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        try {
            this.delegate.setPoolable(poolable);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public boolean isPoolable() throws SQLException {
        try {
            return this.delegate.isPoolable();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        try {
            this.delegate.closeOnCompletion();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        try {
            return this.delegate.isCloseOnCompletion();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    protected void checkException(SQLException sqlEx) {
        this.mackerelConnection.checkException(sqlEx);
    }
}
