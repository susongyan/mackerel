package com.zuomagai.mackerel;

import com.zuomagai.mackerel.util.StringUtils;

import java.sql.SQLException;

/**
 * 猫主子， 鼻子能嗅出罐头里的鱼是否新鲜
 *
 * https://www.postgresql.org/docs/9.4/errcodes-appendix.html
 * https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html
 */
public class Cat {

    private static final String CONNECTION_STATE_ERROR_PREFIX = "08";

    /**
     * check connection is broken in runtime
     *
     * @param sqlException
     * @return
     */
    public static boolean checkSmelly(SQLException sqlException) {
        String sqlState = sqlException.getSQLState();
        // connection broken
        if (StringUtils.isNotEmpty(sqlState) && sqlState.startsWith(CONNECTION_STATE_ERROR_PREFIX)) {
            return true;
        }
        return false;
    }

}
