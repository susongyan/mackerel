package com.zuomagai.mackerel;

/**
 * 
 * @author : holysu
 **/
public enum EnumDriver {
    MYSQL("jdbc:mysql://", "com.mysql.jdbc.Driver"), POSTGRELSQL("jdbc:postgresql://", "org.postgresql.Driver");

    private String prefix;
    private String driverName;

    EnumDriver(String prefix, String driverName) {
        this.prefix = prefix;
        this.driverName = driverName;
    }

    public static String findDriver(String jdbcUrl) {
        for (EnumDriver supportedDriver : EnumDriver.values()) {
             if (jdbcUrl.startsWith(supportedDriver.prefix)) {
                 return supportedDriver.driverName;
             }
        }
        throw new IllegalArgumentException("unsupported driver by macherel, jdbcUrl=" + jdbcUrl);
    }
}
