package com.zuomagai.mackerel.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.zuomagai.mackerel.MackerelConfig;
import com.zuomagai.mackerel.MackerelDataSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MackerelDataSourceTest {
    private static final Logger LOGGER =  LoggerFactory.getLogger(MackerelDataSourceTest.class);

    private static final String jdbcUrl =
            "jdbc:h2:mem:mackerel_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false";
    private static final String userName = "sa";
    private static final String password = "";

    @BeforeClass
    public static void initSchema() throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, userName, password);
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_user");
            statement.execute("CREATE TABLE t_user (id BIGINT PRIMARY KEY, name VARCHAR(64), age INT)");
            statement.execute("INSERT INTO t_user (id, name, age) VALUES (1, 'alice', 20)");
            statement.execute("INSERT INTO t_user (id, name, age) VALUES (2, 'bob', 30)");
        }
    }

    @Test
    public void basicUsage() {

    }

    @Test
    public void lowPressure() {
        MackerelConfig config = new MackerelConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUserName(userName);
        config.setPassword(password);
        config.setMaxWait(0);

        config.setMinIdle(5);
        config.setMaxSize(10);
        config.setTestWhileIdle(true);
        config.setValidateWindow(10000);
        config.setValidateIdleTime(1000);

        config.setMinIdleTime(30000);
        config.setMaxIdleTime(60000);

        Connection connection = null;
        final MackerelDataSource dataSource = new MackerelDataSource(config);
        try {
            connection = dataSource.getConnection();
            ResultSet result = connection.createStatement().executeQuery("select * from t_user");

            TimeUnit.MILLISECONDS.sleep(1000); // wait connection create
            System.out.println();
            while (result.next()) {
                System.out.println(
                        "id=" + result.getLong(1) + ", name=" + result.getString(2) + ", age=" + result.getInt(3));
            }

            // 模拟3个连接活跃，1个连接每隔maxIdleTime被回收重建
            for (int i = 0; i < 3; i++) {
                new Thread(() -> {
                    while (true) {
                        try {
                            Random random = new Random();
                            TimeUnit.MILLISECONDS.sleep(500 + random.nextInt(1000));
                            Connection c = dataSource.getConnection();
                            long bizStart = System.currentTimeMillis();
                            TimeUnit.MILLISECONDS.sleep(500 + random.nextInt(1000)); //fake use time
                            LOGGER.info("do some logic...{}ms", (System.currentTimeMillis() - bizStart));
                            c.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            TimeUnit.MINUTES.sleep(Integer.MAX_VALUE);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 连接偶尔有些压力，minIdle个连接数不够用 需要创建到maxSize
     */
    @Test
    public void mediumPressure() {
        MackerelConfig config = new MackerelConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUserName(userName);
        config.setPassword(password);

        config.setMaxWait(1600);
        config.setMinIdle(5);
        config.setMaxSize(10);
        config.setTestWhileIdle(true);
        config.setValidateWindow(10000);
        config.setValidateIdleTime(1000);

        config.setMinIdleTime(30000);
        config.setMaxIdleTime(60000);

        final MackerelDataSource dataSource = new MackerelDataSource(config);

        try {
            // 模拟15个连接活跃
            for (int i = 0; i < 12; i++) {
                new Thread(() -> {
                    while (true) {
                        try {
                            Random random = new Random();
                            TimeUnit.SECONDS.sleep(random.nextInt(5));
                            Connection c = dataSource.getConnection();
                            long bizStart = System.currentTimeMillis();
                            TimeUnit.MILLISECONDS.sleep(600 + random.nextInt(2000)); //fake use time
                            LOGGER.info("do some logic...{}ms", (System.currentTimeMillis() - bizStart));
                            c.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            TimeUnit.MINUTES.sleep(Integer.MAX_VALUE);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                dataSource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testClose() {
        MackerelConfig config = new MackerelConfig();
        config.setPoolName("testDatabase#1");
        config.setJdbcUrl(jdbcUrl);
        config.setUserName(userName);
        config.setPassword(password);

        config.setMaxWait(1600);
        config.setMinIdle(5);
        config.setMaxSize(10);
        config.setTestWhileIdle(true);
        config.setValidateWindow(10000);
        config.setValidateIdleTime(1000);

        config.setMinIdleTime(30000);
        config.setMaxIdleTime(60000);

        final MackerelDataSource dataSource = new MackerelDataSource(config);

        try {
              // 模拟3个连接活跃，1个连接每隔maxIdleTime被回收重建
              for (int i = 0; i < 3; i++) {
                new Thread(() -> {
                    while (true) {
                        try {
                            Random random = new Random();
                            TimeUnit.MILLISECONDS.sleep(500 + random.nextInt(1000));
                            Connection c = dataSource.getConnection();
                            long bizStart = System.currentTimeMillis();
                            TimeUnit.MILLISECONDS.sleep(500 + random.nextInt(1000)); //fake use time
                            LOGGER.info("do some logic...{}ms", (System.currentTimeMillis() - bizStart));
                            c.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            TimeUnit.SECONDS.sleep(10);
            Connection activeConnection = dataSource.getConnection();
            activeConnection.prepareStatement("select 1;").executeQuery();
            dataSource.close();
            System.out.println("connection is closed? after datasouece closed: " + activeConnection.isClosed());

            dataSource.getConnection();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } 
    }
}
