package com.example.demo.core.utils;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Druid连接池工具类
 * @author felix
 */
public class JDBCUtilsDruid {
    private static DataSource ds=null;

    static{
        InputStream is = JDBCUtilsDruid.class.getClassLoader().getResourceAsStream("druid.properties");
        Properties properties=new Properties();

        try {
            properties.load(is);
            ds= DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static DataSource getDateSource(){
        return ds;
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void close(ResultSet resultSet, Statement statement, Connection connection){
        closeSource(resultSet);
        closeSource(statement);
        closeSource(connection);
    }

    public static void close(Statement statement,Connection connection){
        close(null,statement,connection);
    }

    private static void closeSource(ResultSet resultSet){
        try {
            if (resultSet!=null)
                resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            resultSet=null;
        }
    }
    private static void closeSource(Statement statement){
        try {
            if (statement!=null)
                statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            statement=null;
        }
    }
    private static void closeSource(Connection connection){
        try {
            if(connection!=null)
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            connection=null;
        }
    }
}