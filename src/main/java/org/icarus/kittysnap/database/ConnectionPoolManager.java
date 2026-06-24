package org.icarus.kittysnap.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.icarus.kittysnap.config.ConfigurationManager;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * HikariCP 连接池管理器：负责连接池的创建、获取连接、关闭。
 */
public class ConnectionPoolManager {

    private final ConfigurationManager cfg;
    private HikariDataSource dataSource;

    ConnectionPoolManager(ConfigurationManager cfg) {
        this.cfg = cfg;
    }

    /**
     * 初始化连接池，返回 true 表示成功
     */
    boolean init() {
        HikariConfig hk = new HikariConfig();
        hk.setJdbcUrl(cfg.getDbJdbcUrl());
        hk.setDriverClassName(cfg.getDbDriverClass());
        hk.setUsername(cfg.getDbUsername());
        hk.setPassword(cfg.getDbPassword());
        hk.setMaximumPoolSize(cfg.getDbPoolMaxSize());
        hk.setMinimumIdle(cfg.getDbPoolMinIdle());
        hk.setConnectionTimeout(cfg.getDbPoolConnectionTimeout());
        hk.setIdleTimeout(cfg.getDbPoolIdleTimeout());
        hk.setMaxLifetime(cfg.getDbPoolMaxLifetime());
        hk.setPoolName(cfg.getDbPoolName());

        if (cfg.isDbCachePrepStmts()) {
            hk.addDataSourceProperty("cachePrepStmts", "true");
            hk.addDataSourceProperty("prepStmtCacheSize", String.valueOf(cfg.getDbPrepStmtCacheSize()));
            hk.addDataSourceProperty("prepStmtCacheSqlLimit", String.valueOf(cfg.getDbPrepStmtCacheSqlLimit()));
        }

        try {
            dataSource = new HikariDataSource(hk);
        } catch (Exception e) {
            cfg.logSevere("db-connect-error", e.getMessage());
            return false;
        }

        // 测试连接
        try (Connection conn = getConnection()) {
            if (conn == null) {
                cfg.logSevere("db-connect-error", "无法获取连接");
                return false;
            }
        } catch (SQLException e) {
            cfg.logSevere("db-connect-error", e.getMessage());
            return false;
        }

        return true;
    }

    Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) return null;
        return dataSource.getConnection();
    }

    boolean isReady() {
        return dataSource != null && !dataSource.isClosed();
    }

    /**
     * 获取连接池统计信息（用于调试）
     */
    PoolStats getPoolStats() {
        if (dataSource == null || dataSource.isClosed()) return new PoolStats(0, 0, 0);
        var mx = dataSource.getHikariPoolMXBean();
        return new PoolStats(mx.getActiveConnections(), mx.getIdleConnections(),
                mx.getThreadsAwaitingConnection());
    }

    record PoolStats(int active, int idle, int waiting) {
    }

    void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
