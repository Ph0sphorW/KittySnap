package org.icarus.kittysnap.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.icarus.kittysnap.config.ConfigurationManager;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPoolManager {

    private final ConfigurationManager cfg;
    private HikariDataSource dataSource;

    ConnectionPoolManager(ConfigurationManager cfg) {
        this.cfg = cfg;
    }

    @SuppressWarnings("ExtractMethodRecommender")
    boolean init() {
        HikariConfig hk = new HikariConfig();
        hk.setJdbcUrl(cfg.getConfig().getDatabase().getJdbcUrl());
        hk.setDriverClassName(cfg.getConfig().getDatabase().getDriverClass());
        hk.setUsername(cfg.getConfig().getDatabase().getUsername());
        hk.setPassword(cfg.getConfig().getDatabase().getPassword());
        hk.setMaximumPoolSize(cfg.getDbPoolMaxSize());
        hk.setMinimumIdle(cfg.getDbPoolMinIdle());
        hk.setConnectionTimeout(cfg.getDbPoolConnectionTimeout());
        hk.setIdleTimeout(cfg.getDbPoolIdleTimeout());
        hk.setMaxLifetime(cfg.getDbPoolMaxLifetime());
        hk.setPoolName(cfg.getConfig().getDatabase().getPool().getPoolName());

        if (cfg.isDbCachePrepStmts()) {
            hk.addDataSourceProperty("cachePrepStmts", "true");
            hk.addDataSourceProperty("prepStmtCacheSize", String.valueOf(cfg.getDbPrepStmtCacheSize()));
            hk.addDataSourceProperty("prepStmtCacheSqlLimit", String.valueOf(cfg.getDbPrepStmtCacheSqlLimit()));
        }

        try {
            dataSource = new HikariDataSource(hk);
        } catch (Exception e) {
            cfg.logSevere("database.connect-error", e.getMessage());
            return false;
        }

        // 测试连接
        try (Connection conn = getConnection()) {
            if (conn == null) {
                cfg.logSevere("database.connect-error", "Database connection error");
                return false;
            }
        } catch (SQLException e) {
            cfg.logSevere("database.connect-error", e.getMessage());
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
     * 获取连接池统计信息
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
