package com.auto.mail.datasource.presto;

import com.auto.mail.config.HikariPoolConfig;
import com.auto.mail.model.Datasource;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.*;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PrestoConnPool {

    private static Logger logger = getLogger(PrestoConnPool.class);
    private static HikariDataSource hikariDataSource;

    @Resource
    private HikariPoolConfig hikariConfig;

     private Datasource datasource;

    public void PrestoConnPool(Datasource datasource){
        this.datasource= datasource;
    }
    /**
     * 初始化连接池
     */
    @PostConstruct
    public void initialize(){
        if(hikariDataSource==null) {
            logger.info("presto init.........");
            if(datasource!=null) {
                String URL = datasource.getUrl();
                try {
                    try {
                        Class.forName("com.facebook.presto.jdbc.PrestoDriver").newInstance();
                    } catch (InstantiationException e) {
                        logger.error("PrestoConnectionPool InstantiationException:" + e.getMessage());
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        logger.error("PrestoConnectionPool IllegalAccessException:" + e.getMessage());
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        logger.error("PrestoConnectionPool ClassNotFoundException:" + e.getMessage());
                        e.printStackTrace();
                    }
                    hikariDataSource = new HikariDataSource();
                    hikariDataSource.setDriverClassName("com.facebook.presto.jdbc.PrestoDriver");
                    hikariDataSource.setJdbcUrl(URL);
                    hikariDataSource.setUsername(datasource.getUsername());
                    hikariDataSource.setPassword(datasource.getPassword());
                    hikariDataSource.setPoolName(hikariConfig.getPoolName());
                    hikariDataSource.setConnectionTestQuery(hikariConfig.getConnectionTestQuery());
                    hikariDataSource.setMaximumPoolSize(hikariConfig.getMaximumPoolSize());
                    hikariDataSource.setConnectionTimeout(hikariConfig.getConnectionTimeout());
                    hikariDataSource.setMinimumIdle(hikariConfig.getMinimumIdle());
                    hikariDataSource.setAutoCommit(hikariConfig.getAutoCommit());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                logger.error("presto datasource is null--------------------------");
            }
        }
    }

    /**
     * 获取Connection连接
     */

    public Connection getConnection(){
        Connection connection = null;
        try{
            //TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            connection =hikariDataSource.getConnection();
        }catch (SQLException e){
            logger.error("PrestoConnectionPool SQLException:" + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * 关闭Connection连接和statement
     */
    void closeConnectionAndStatement(Connection connection, Statement statement){
        try {
            if (statement != null) {
                statement.close();
            }
            if(connection != null){
                connection.close();
            }
        } catch (Throwable e) {
            logger.error("PrestoConnectionPool closeConnection SQLException:" + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 关闭ResultSet,PreparedStatement,Connection
     */
    public void releaseResource(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                    logger.error("出错了e={},关闭rs失败",e);
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                    logger.error("出错了e={},关闭statement失败",e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                    logger.error("出错了e={},关闭conn失败",e);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭ResultSet,Statement,Connection
     */
    void closeResource(Connection conn, Statement ps, ResultSet rs) {
        try {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                    logger.error("closeResource 出错了e={},关闭rs失败",e);
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                    logger.error("closeResource 出错了e={},关闭statement失败",e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                    logger.error("closeResource 出错了e={},关闭conn失败",e);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
