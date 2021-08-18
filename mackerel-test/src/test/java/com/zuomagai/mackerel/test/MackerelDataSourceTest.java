package com.zuomagai.mackerel.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.zuomagai.mackerel.MackerelConfig;
import com.zuomagai.mackerel.MackerelDataSource;

import org.junit.Test;

public class MackerelDataSourceTest {
   
    @Test
    public void basic() {
        MackerelConfig config = new MackerelConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3307/test?socketTimeout=5000&connectTimeout=5000");
        config.setUserName("root");
        config.setPassword("root");
        
        Connection connection = null;
        MackerelDataSource dataSource = null;
        try {
            dataSource = new MackerelDataSource(config); 

            TimeUnit.MILLISECONDS.sleep(1000);

            connection = dataSource.getConnection();
            ResultSet result = connection.createStatement().executeQuery("select * from t_user");
            while(result.next()) { 
                System.out.println("id=" + result.getLong(1) + ", name=" + result.getString(2) + ", age=" + result.getInt(3));
            }
         
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
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
}