package com.example.demo;

import com.example.utils.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * 测试 MySQL 第一个级别 读未提交
 * 产生脏读现象
 */
public class ReadUncommitted {
    private static Logger log = LoggerFactory.getLogger(ReadUncommitted.class);
    private static volatile boolean flag = true;

    public static void main(String[] args) {

        A a = new A();
        B b = new B();
        a.start();
        b.start();

    }

    /**
     * A线程 模拟事务提交过程中 出现意外情况导致事务回滚
     */
    static class A extends Thread {
        public A() {
            super("A");
        }

        @Override
        public void run() {
            Connection conn = JdbcUtils.getConnection();
            while (flag) {
                try {
                    //设置手动提交事务, JDBC 默认是开启自动提交事务
                    conn.setAutoCommit(false);
                    //设置事务隔离级别: 读未提交
                    conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                    String sql = "update user set age = 10 where id = 1";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.executeUpdate();
                    //模拟出错
                    int i = 1 / 0;
                    conn.commit();
                } catch (Exception e) {
                    try {
                        conn.rollback();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    log.info("线程: " + getName() + " 模拟出错 让事务回滚: " + e.getMessage());
                }
            }
        }


    }

    static class B extends Thread {
        public B() {
            super("B");
        }

        @Override
        public void run() {
            Connection conn = JdbcUtils.getConnection();
            try {
                while (flag) {
                    conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                    String sql = "select * from user";
                    PreparedStatement statement = conn.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery();
                    resultSet.next();
                    int age = resultSet.getInt("age");
                    if (age == 10) {
                        log.info("线程:" + Thread.currentThread().getName() + " 脏读: age=" + age);
                        flag = false;
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}
