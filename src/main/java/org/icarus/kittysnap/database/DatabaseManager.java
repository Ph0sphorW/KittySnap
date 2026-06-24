package org.icarus.kittysnap.database;

import lombok.Getter;
import org.icarus.kittysnap.config.ConfigurationManager;

import java.util.function.BiConsumer;

/**
 * 数据库管理器外观：组合 {@link ConnectionPoolManager} + {@link MessageRepository}。
 */
public class DatabaseManager {

    private final ConnectionPoolManager pool;
    @Getter
    private final MessageRepository messageRepo;
    private final ConfigurationManager cfg;

    public DatabaseManager(ConfigurationManager cfg) {
        this.cfg = cfg;
        this.pool = new ConnectionPoolManager(cfg);
        this.messageRepo = new MessageRepository(cfg, pool);
    }

    /**
     * 初始化连接池并建表
     */
    public boolean init() {
        cfg.logInfo("db-initializing");
        if (!pool.init()) return false;
        messageRepo.createTable();
        cfg.logInfo("db-initialized");
        return true;
    }

    /**
     * 关闭连接池
     */
    public void shutdown() {
        pool.shutdown();
        cfg.logInfo("db-shutdown");
    }

    public boolean isReady() {
        return pool.isReady();
    }

    public void setDebugConsumer(BiConsumer<String, Object[]> consumer) {
        messageRepo.setDebugConsumer(consumer);
    }

    /**
     * 便捷委托
     */
    public boolean insertGroupMessage(long groupId, long userId, String nickname, String rawMessage,
                                   long messageId, String messageSeq, long msgTime) {
        return messageRepo.insert(groupId, userId, nickname, rawMessage, messageId, messageSeq, msgTime);
    }

    public boolean insertGroupMessage(long groupId, long userId, String nickname, String rawMessage) {
        return messageRepo.insert(groupId, userId, nickname, rawMessage);
    }

    /**
     * 按 OneBot message_id 查询已存储的原始消息
     */
    public MessageRepository.OriginalMessage queryOriginalMessage(long groupId, long messageId) {
        return messageRepo.queryByMessageId(groupId, messageId);
    }
}
