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
        cfg.logInfo("db-initializing");
        if (!pool.init()) return false;
        messageRepo.createTable();
        cfg.logInfo("db-initialized");
        return true;
    }

    public void shutdown() {
        pool.shutdown();
        cfg.logInfo("db-shutdown");
    }

    public void setDebugConsumer(BiConsumer<String, Object[]> consumer) {
        messageRepo.setDebugConsumer(consumer);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean insertGroupMessage(long groupId, long userId, String nickname, String processedMessage,
                                   long messageId, String messageSeq, long msgTime) {
        return messageRepo.insert(groupId, userId, nickname, processedMessage, messageId, messageSeq, msgTime);
    }

    /**
     * 按 message_id 查询已存储的原始消息
     */
    public MessageRepository.OriginalMessage queryOriginalMessage(long groupId, long messageId) {
        return messageRepo.queryByMessageId(groupId, messageId);
    }
}
