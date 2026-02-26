package dev.sample;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@WebListener
public class ApplicationContextListener implements ServletContextListener {

    private HikariDataSource masterDs;
    private HikariDataSource replicaDs;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // 1. 쓰기용 (Master) DataSource 설정
        HikariConfig masterConfig = new HikariConfig();
        masterConfig.setJdbcUrl(
                "jdbc:mysql://localhost:13306/card_db?serverTimezone=Asia/Seoul&useServerPrepStmts=true&cachePrepStmts=true&prepStmtCacheSize=250");
        masterConfig.setUsername("root");
        masterConfig.setPassword("1234");
        masterConfig.setMaximumPoolSize(10);
        masterDs = new HikariDataSource(masterConfig);
        ctx.setAttribute("MASTER_DATA_SOURCE", masterDs);

        // 2. 읽기용 (Replica) DataSource 설정
        HikariConfig replicaConfig = new HikariConfig();
        replicaConfig.setJdbcUrl(
                "jdbc:mysql://localhost:13307/card_db?serverTimezone=Asia/Seoul&useServerPrepStmts=true&cachePrepStmts=true&prepStmtCacheSize=250");
        replicaConfig.setUsername("root");
        replicaConfig.setPassword("1234");
        replicaConfig.setMaximumPoolSize(10);
        replicaDs = new HikariDataSource(replicaConfig);
        ctx.setAttribute("REPLICA_DATA_SOURCE", replicaDs);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (masterDs != null)
            masterDs.close();
        if (replicaDs != null)
            replicaDs.close();
    }

    public static DataSource getMasterDataSource(ServletContext ctx) {
        return (DataSource) ctx.getAttribute("MASTER_DATA_SOURCE");
    }

    public static DataSource getReplicaDataSource(ServletContext ctx) {
        return (DataSource) ctx.getAttribute("REPLICA_DATA_SOURCE");
    }
}
