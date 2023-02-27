package com.zuomagai.mackerel;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * @author susongyan
 **/
public class MackerelPreparedStatement extends MackerelStatement implements PreparedStatement {

    private final PreparedStatement delegate;

    public MackerelPreparedStatement(PreparedStatement delegate, MackerelConnection mackerelConnection) {
        super(delegate, mackerelConnection);
        this.delegate = delegate;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        try {
            return this.delegate.executeQuery();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        try {
            return this.delegate.executeUpdate();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        try {
            this.delegate.setNull(parameterIndex, sqlType);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        try {
            this.delegate.setBoolean(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        try {
            this.delegate.setByte(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        try {
            this.delegate.setShort(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        try {
            this.delegate.setInt(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        try {
            this.delegate.setLong(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        try {
            this.delegate.setFloat(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        try {
            this.delegate.setDouble(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        try {
            this.delegate.setBigDecimal(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        try {
            this.delegate.setString(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        try {
            this.delegate.setBytes(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        try {
            this.delegate.setDate(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        try {
            this.delegate.setTime(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        try {
            this.delegate.setTimestamp(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        try {
            this.delegate.setAsciiStream(parameterIndex, x, length);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        try {
            this.delegate.setUnicodeStream(parameterIndex, x, length);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        try {
            this.delegate.setBinaryStream(parameterIndex, x, length);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void clearParameters() throws SQLException {
        try {
            this.delegate.clearParameters();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        try {
            this.delegate.setObject(parameterIndex, x, targetSqlType);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        try {
            this.delegate.setObject(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public boolean execute() throws SQLException {
        try {
            return this.delegate.execute();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void addBatch() throws SQLException {
        try {
            this.delegate.addBatch();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        try {
            this.delegate.setCharacterStream(parameterIndex, reader, length);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        try {
            this.delegate.setRef(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        try {
            this.delegate.setBlob(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        try {
            this.delegate.setClob(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        try {
            this.delegate.setArray(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        try {
            return this.delegate.getMetaData();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        try {
            this.delegate.setDate(parameterIndex, x, cal);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        try {
            this.delegate.setTime(parameterIndex, x, cal);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        try {
            this.delegate.setTimestamp(parameterIndex, x, cal);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        try {
            this.delegate.setNull(parameterIndex, sqlType, typeName);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        try {
            this.delegate.setURL(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        try {
            return this.delegate.getParameterMetaData();
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        try {
            this.delegate.setRowId(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        try {
            this.delegate.setNString(parameterIndex, value);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        try {
            this.delegate.setNCharacterStream(parameterIndex, value, length);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        try {
            this.delegate.setNClob(parameterIndex, value);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        try {
            this.delegate.setClob(parameterIndex, reader, length);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        try {
            this.delegate.setBlob(parameterIndex, inputStream, length);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        try {
            this.delegate.setNClob(parameterIndex, reader, length);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        try {
            this.delegate.setSQLXML(parameterIndex, xmlObject);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        try {
            this.delegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        try {
            this.delegate.setAsciiStream(parameterIndex, x, length);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        try {
            this.delegate.setBinaryStream(parameterIndex, x, length);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        try {
            this.delegate.setCharacterStream(parameterIndex, reader, length);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        try {
            this.delegate.setAsciiStream(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        try {
            this.delegate.setBinaryStream(parameterIndex, x);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        try {
            this.delegate.setCharacterStream(parameterIndex, reader);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        try {
            this.delegate.setNCharacterStream(parameterIndex, value);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        try {
            this.delegate.setClob(parameterIndex, reader);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        try {
            this.delegate.setBlob(parameterIndex, inputStream);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        try {
            this.delegate.setNClob(parameterIndex, reader);
        } catch (SQLException e) {
            this.checkException(e);
            throw e;
        }
    }
}
