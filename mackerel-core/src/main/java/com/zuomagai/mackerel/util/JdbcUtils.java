package com.zuomagai.mackerel.util;

import java.sql.SQLException;

/**
 * @author susongyan
 **/
public class JdbcUtils {

    public static boolean isConnectionException(SQLException sqlException) {
        return sqlException.getSQLState() != null && sqlException.getSQLState().startsWith("08");
    }
}
