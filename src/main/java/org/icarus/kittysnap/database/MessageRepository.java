package org.icarus.kittysnap.database;

import org.icarus.kittysnap.config.ConfigurationManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.BiConsumer;
import java.util.logging.Level;

/**
 * 群消息数据访问层：建表、插入消息。
 */
public class MessageRepository {

    private final ConfigurationManager cfg;
    private final ConnectionPoolManager pool;
    private final String tableName;

    private BiConsumer<String, Object[]> debugConsumer;

    MessageRepository(ConfigurationManager cfg, ConnectionPoolManager pool) {
        this.cfg = cfg;
        this.pool = pool;
        this.tableName = cfg.getDbTableName();
    }

    void setDebugConsumer(BiConsumer<String, Object[]> c) {
        this.debugConsumer = c;
    }

    private void debug(String key, Object... args) {
        if (debugConsumer != null) debugConsumer.accept(key, args);
    }

    /**
     * 建表
     */
    void createTable() {
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
                )""".formatted(tableName);

        String idx = "CREATE INDEX IF NOT EXISTS idx_%s_group_id ON %s (group_id)".formatted(tableName, tableName);

        try (Connection conn = pool.getConnection(); Statement st = conn.createStatement()) {
            st.execute(sql);
            st.execute(idx);
            cfg.logInfo("db-table-created", tableName);
        } catch (SQLException e) {
            cfg.logSevere("db-table-create-failed");
            java.util.logging.Logger.getGlobal().log(Level.SEVERE, "", e);
        }
    }

    /**
     * 插入消息，返回自增 ID 或 -1
     */
    boolean insert(long groupId, long userId, String nickname, String rawMessage,
                long messageId, String messageSeq, long msgTime) {
        if (!pool.isReady()) return false;

        String sql = "INSERT INTO %s (group_id, user_id, nickname, raw_message, message_id, message_seq, msg_time) "
                .formatted(tableName) + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        long start = System.currentTimeMillis();

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int trunc = cfg.getNicknameTruncateLength();
            ps.setLong(1, groupId);
            ps.setLong(2, userId);
            ps.setString(3, nickname != null && nickname.length() > trunc ? nickname.substring(0, trunc) : nickname);
            ps.setString(4, rawMessage);
            ps.setLong(5, messageId);
            ps.setString(6, messageSeq);
            ps.setLong(7, msgTime);
            ps.executeUpdate();

            long elapsed = System.currentTimeMillis() - start;
            long id = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) id = rs.getLong(1);
            }

            cfg.logInfo("db-insert-message", groupId, userId, id);
            debug("debug-db-insert", groupId, userId, rawMessage, id, elapsed);

            var stats = pool.getPoolStats();
            debug("debug-db-pool", stats.active(), stats.idle(), stats.waiting());

            return true;

        } catch (SQLException e) {
            cfg.logWarning("db-insert-error", e.getMessage());
            java.util.logging.Logger.getGlobal().log(Level.WARNING, "", e);
            return false;
        }
    }

    boolean insert(long groupId, long userId, String nickname, String rawMessage) {
        return insert(groupId, userId, nickname, rawMessage, 0, null, System.currentTimeMillis() / 1000);
    }
}
