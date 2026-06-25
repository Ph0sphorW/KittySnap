package org.icarus.kittysnap.database;

import lombok.Getter;
import org.icarus.kittysnap.config.ConfigurationManager;

import java.util.function.BiConsumer;

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
     * 存在建表，不存在跳
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean init() {
        cfg.logInfo("database.initializing");
        if (!pool.init()) return false;
        messageRepo.createTable();
        cfg.logInfo("database.initialized");
        return true;
    }

    public void shutdown() {
        pool.shutdown();
        cfg.logInfo("database.shutdown");
    }

    public void setDebugConsumer(BiConsumer<String, Object[]> consumer) {
        messageRepo.setDebugConsumer(consumer);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean insertGroupMessage(long groupId, long userId, String nickname, String rawMessage,
                                   long messageId, String messageSeq, long msgTime) {
        return messageRepo.insert(groupId, userId, nickname, rawMessage, messageId, messageSeq, msgTime);
    }

    /**
     * 按 message_id 查询已存储的原始消息
     */
    public MessageRepository.OriginalMessage queryOriginalMessage(long groupId, long messageId) {
        return messageRepo.queryByMessageId(groupId, messageId);
    }
}
