package com.zuomagai.mackerel;

/**
 * @author susongyan
 **/
public enum EnumDriver {
    MYSQL("jdbc:mysql://", "com.mysql.jdbc.Driver"), POSTGRESQL("jdbc:postgresql://", "org.postgresql.Driver");

    private final String prefix;
    private final String driverName;

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
        throw new IllegalArgumentException("unsupported driver by mackerel, jdbcUrl=" + jdbcUrl);
    }
}
