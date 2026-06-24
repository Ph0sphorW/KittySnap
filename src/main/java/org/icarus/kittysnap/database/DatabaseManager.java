package org.icarus.kittysnap.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import org.icarus.kittysnap.config.ConfigurationManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.BiConsumer;
import java.util.logging.Level;

/**
 * 数据库管理器
 * <p>
 * 使用 HikariCP 连接池管理数据库连接，默认使用 H2 嵌入式数据库，
 * 可通过 config.yml 切换至 MySQL 等数据库。
 * <p>
 * 连接池参数与表名均在 config.yml 中可配。
 */
public class DatabaseManager {

    private final JavaPlugin plugin;
    private final ConfigurationManager cfg;
    private final String tableName;

    private HikariDataSource dataSource;

    @Setter
    private BiConsumer<String, Object[]> debugConsumer;

    // ==================== 构造 ====================

    public DatabaseManager(JavaPlugin plugin, ConfigurationManager cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.tableName = cfg.getDbTableName();
    }

    private void debug(String key, Object... args) {
        if (debugConsumer != null) {
            debugConsumer.accept(key, args);
        }
    }

    // ==================== 初始化与关闭 ====================

    /**
     * 初始化数据库连接池并建表
     */
    public void init() {
        cfg.logInfo("db-initializing");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(cfg.getDbJdbcUrl());
        hikariConfig.setDriverClassName(cfg.getDbDriverClass());
        hikariConfig.setUsername(cfg.getDbUsername());
        hikariConfig.setPassword(cfg.getDbPassword());

        hikariConfig.setMaximumPoolSize(cfg.getDbPoolMaxSize());
        hikariConfig.setMinimumIdle(cfg.getDbPoolMinIdle());
        hikariConfig.setConnectionTimeout(cfg.getDbPoolConnectionTimeout());
        hikariConfig.setIdleTimeout(cfg.getDbPoolIdleTimeout());
        hikariConfig.setMaxLifetime(cfg.getDbPoolMaxLifetime());

        // 连接池命名（便于日志识别）
        hikariConfig.setPoolName(cfg.getDbPoolName());

        // JDBC 驱动连接属性（语句缓存优化）
        if (cfg.isDbCachePrepStmts()) {
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize",
                    String.valueOf(cfg.getDbPrepStmtCacheSize()));
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit",
                    String.valueOf(cfg.getDbPrepStmtCacheSqlLimit()));
        }

        try {
            dataSource = new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            cfg.logSevere("db-connect-error", e.getMessage());
            cfg.logSevere("db-pool-init-failed");
            plugin.getLogger().log(Level.SEVERE, "", e);
            return;
        }

        try (Connection conn = getConnection()) {
            if (conn == null) {
                cfg.logSevere("db-connect-error", "无法获取连接");
                return;
            }
        } catch (SQLException e) {
            cfg.logSevere("db-connect-error", e.getMessage());
            return;
        }

        createTable();

        cfg.logInfo("db-initialized");
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            cfg.logInfo("db-shutdown");
        }
    }

    // ==================== 连接管理 ====================

    /**
     * 从连接池获取一个连接
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            return null;
        }
        return dataSource.getConnection();
    }

    /** 连接池是否已就绪 */
    public boolean isReady() {
        return dataSource != null && !dataSource.isClosed();
    }

    // ==================== 建表 ====================

    private void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS %s (
                    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                    group_id    BIGINT       NOT NULL,
                    user_id     BIGINT       NOT NULL,
                    nickname    VARCHAR(128),
                    raw_message TEXT         NOT NULL,
                    message_id  BIGINT,
                    message_seq VARCHAR(64),
                    msg_time    BIGINT,
                    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
                )
                """.formatted(tableName);

        String indexSql = """
                CREATE INDEX IF NOT EXISTS idx_%s_group_id
                    ON %s (group_id)
                """.formatted(tableName, tableName);

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            stmt.execute(indexSql);
            cfg.logInfo("db-table-created", tableName);
        } catch (SQLException e) {
            cfg.logSevere("db-table-create-failed");
            plugin.getLogger().log(Level.SEVERE, "", e);
        }
    }

    // ==================== 插入消息 ====================

    /**
     * 插入一条收到的群消息到数据库
     *
     * @param groupId    群号
     * @param userId     发送者 QQ
     * @param nickname   发送者昵称（可为空）
     * @param rawMessage 消息原文
     * @param messageId  消息 ID
     * @param messageSeq 消息序列号
     * @param msgTime    消息时间戳
     * @return 自增 ID，失败返回 -1
     */
    public long insertGroupMessage(long groupId, long userId, String nickname,
                                   String rawMessage, long messageId,
                                   String messageSeq, long msgTime) {
        if (!isReady()) return -1;

        String sql = "INSERT INTO %s (group_id, user_id, nickname, raw_message, message_id, message_seq, msg_time) "
                .formatted(tableName) +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        long start = System.currentTimeMillis();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, groupId);
            ps.setLong(2, userId);
            int truncateLen = cfg.getNicknameTruncateLength();
            ps.setString(3, nickname != null && nickname.length() > truncateLen
                    ? nickname.substring(0, truncateLen) : nickname);
            ps.setString(4, rawMessage);
            ps.setLong(5, messageId);
            ps.setString(6, messageSeq);
            ps.setLong(7, msgTime);

            ps.executeUpdate();

            long elapsed = System.currentTimeMillis() - start;

            long generatedId = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedId = rs.getLong(1);
                }
            }

            cfg.logInfo("db-insert-message", groupId, userId, generatedId);
            debug("debug-db-insert", groupId, userId, rawMessage, generatedId, elapsed);

            if (debugConsumer != null && dataSource != null) {
                debug("debug-db-pool",
                        dataSource.getHikariPoolMXBean().getActiveConnections(),
                        dataSource.getHikariPoolMXBean().getIdleConnections(),
                        dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
            }

            return generatedId;

        } catch (SQLException e) {
            cfg.logWarning("db-insert-error", e.getMessage());
            cfg.logWarning("db-insert-failed");
            plugin.getLogger().log(Level.WARNING, "", e);
            return -1;
        }
    }

    /**
     * 便捷方法：仅用必要参数插入群消息
     */
    public long insertGroupMessage(long groupId, long userId, String nickname, String rawMessage) {
        return insertGroupMessage(groupId, userId, nickname, rawMessage, 0, null, System.currentTimeMillis() / 1000);
    }
}
