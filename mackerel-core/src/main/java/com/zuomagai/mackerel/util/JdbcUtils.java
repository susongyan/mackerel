package com.zuomagai.mackerel.util;

import java.sql.SQLException;

/**
 * @author S.S.Y
 **/
public class JdbcUtils {

    public static boolean isConnectionException(SQLException sqlException) {
        return sqlException.getSQLState() != null && sqlException.getSQLState().startsWith("08");
    }
}
