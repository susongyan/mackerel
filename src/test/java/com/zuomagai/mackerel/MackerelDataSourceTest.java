package com.zuomagai.mackerel;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

public class MackerelDataSourceTest {
   
    @Test
    public void basic() {
        MackerelConfig config = new MackerelConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3307/test?socketTimeout=5000&connectTimeout=5000");
        config.setUserName("root");
        config.setPassword("root");
        
        try {
            MackerelDataSource dataSource = new MackerelDataSource(config); 
            ResultSet result = dataSource.getConnection().createStatement().executeQuery("select * from t_user");
            while(result.next()) { 
                System.out.println("id=" + result.getLong(1) + ", name=" + result.getString(2) + ", age=" + result.getInt(3));
            }
         
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}