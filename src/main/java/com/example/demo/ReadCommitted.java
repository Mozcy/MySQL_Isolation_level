package com.example.demo;

import com.example.utils.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

/**
 * 测试 READ COMMITTED (读提交) 产生 不可重复读(指一个事务范围内, 多次查询某个数据, 却得到不同的结果。)
 * 开启事务A和事务B两个事务, 在事务A中使用 update 语句将 id=1 的记录行 age 字段改为 10。
 * 此时, 在事务B中使用 select 语句进行查询, 我们发现在事务A提交之前, 事务B中查询到的记录 age 一直是1,
 * 直到事务A提交, 此时在事务B中 select 查询, 发现 age 的值已经是 10 了。
 */
public class ReadCommitted {
    private static Logger log = LoggerFactory.getLogger(ReadCommitted.class);
    private static volatile boolean flag = true;

    public static void main(String[] args) {
        new A().start();
        new B().start();
    }


    static class A extends Thread {
        public A() {
            super("A");
        }

        @Override
        public void run() {
            Connection conn = JdbcUtils.getConnection();
            try {
                conn.setAutoCommit(false);
                //设置隔离级别 读提交
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                String sql = "update user set age = 10 where id = 1";
                PreparedStatement ps = conn.prepareStatement(sql);
                int i = ps.executeUpdate();
                TimeUnit.MILLISECONDS.sleep(500);
                conn.commit();
                log.info("线程: " + getName() + ((i == 1) ? " 更新成功" : " 更新失败"));
            } catch (Exception e) {
                e.printStackTrace();
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
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                String sql = "select * from user";
                PreparedStatement statement = conn.prepareStatement(sql);
                while (flag) {
                    TimeUnit.MILLISECONDS.sleep(100);
                    ResultSet resultSet = statement.executeQuery();
                    resultSet.next();
                    int age = resultSet.getInt("age");
                    if (age == 10) {
                        log.info("线程: " + Thread.currentThread().getName() + " 产生不可重复读: age=" + age);
                        flag = false;
                        //更新回原值
                        conn.prepareStatement("update user set age = 1 where id = 1").executeUpdate();
                    } else {
                        log.info("线程: " + Thread.currentThread().getName() + " 获取值: age=" + age);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
