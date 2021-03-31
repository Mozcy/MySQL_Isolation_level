package com.example.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 工具类 封装 MySQL 连接
 */
public class JdbcUtils {
    private static Logger log = LoggerFactory.getLogger(JdbcUtils.class);

    /**
     * 返回MySQL 连接
     *
     * @return Connection
     * @throws Exception
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            InputStream resource = JdbcUtils.class.getClassLoader().getResourceAsStream("db.properties");
            Properties properties = new Properties();
            properties.load(resource);
            Class.forName(properties.getProperty("driver"));
            conn = DriverManager.getConnection(properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password"));
        } catch (Exception e) {
            log.error("数据库连接失败, 请检查db.properties文件");
            e.printStackTrace();
        }
        return conn;
    }


    /**
     * 关闭MySQL 连接
     *
     * @param cb
     */
    public static void close(Cloneable... cb) {
        for (Cloneable cloneable : cb) {
            if (cb != null) {
                cb.clone();
            }
        }
    }

}
