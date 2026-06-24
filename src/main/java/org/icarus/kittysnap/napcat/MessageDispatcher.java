package org.icarus.kittysnap.napcat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.bukkit.plugin.java.JavaPlugin;
import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.database.DatabaseManager;

import java.net.http.WebSocket;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.logging.Level;

/**
 * 消息分发器：解析 Napcat JSON → 过滤群消息 → 写入 DB → 回调监听器。
 */
public class MessageDispatcher {

    private final JavaPlugin plugin;
    private final ConfigurationManager cfg;
    private volatile ExecutorService executor;
    private final CopyOnWriteArraySet<GroupEntry> groupListeners;
    private DatabaseManager databaseManager;
    private BiConsumer<String, Object[]> debugConsumer;

    MessageDispatcher(JavaPlugin plugin, ConfigurationManager cfg, ExecutorService executor,
                      CopyOnWriteArraySet<GroupEntry> groupListeners) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.executor = executor;
        this.groupListeners = groupListeners;
    }

    void setDatabaseManager(DatabaseManager db) { this.databaseManager = db; }
    void updateExecutor(ExecutorService exec) { this.executor = exec; }
    void setDebugConsumer(BiConsumer<String, Object[]> c) { this.debugConsumer = c; }

    private void debug(String key, Object... args) {
        if (debugConsumer != null) debugConsumer.accept(key, args);
    }

    /** 由 executor 线程调用 */
    void dispatch(String json) {
        try {
            JSONObject root = JSON.parseObject(json);
            int t = cfg.getNapcatDebugTruncateLength();
            debug("debug-msg-received", json.length() > t ? json.substring(0, t) + "..." : json);

            if (!"message".equals(root.getString("post_type"))
                    || !"group".equals(root.getString("message_type"))) {
                debug("debug-msg-ignored", root.getString("post_type"));
                return;
            }

            long groupId = root.getLongValue("group_id");
            if (groupId == 0) return;

            NapcatMessage napMsg = JSON.parseObject(json, NapcatMessage.class);

            // — DB —
            if (databaseManager != null) {
                String nick = napMsg.getSender() != null ? napMsg.getSender().getDisplayName() : "";
                String raw = napMsg.getRawMessage() != null ? napMsg.getRawMessage() : "";
                long g = groupId, u = napMsg.getSenderId();
                long mId = napMsg.getMessageId();
                String mSeq = napMsg.getMessageSeq();
                long t2 = napMsg.getTime();
                executor.execute(() -> databaseManager.insertGroupMessage(g, u, nick, raw, mId, mSeq, t2));
            }

            // — 分发 —
            boolean handled = false;
            for (GroupEntry entry : groupListeners) {
                if (entry.groupId() != groupId) continue;
                handled = true;
                String content = napMsg.getRawMessage() != null ? napMsg.getRawMessage() : "";
                long userId = napMsg.getSenderId();
                String nickname = napMsg.getSender() != null
                        ? napMsg.getSender().getDisplayName() : String.valueOf(userId);

                debug("debug-msg-dispatched", groupId, userId, content);

                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        entry.listener().onGroupMessage(napMsg, groupId, userId, content);
                    } catch (Exception e) {
                        cfg.logWarning("listener-error", groupId, nickname);
                        plugin.getLogger().log(Level.WARNING, "", e);
                    }
                });
            }

            if (!handled) {
                cfg.logFine("unhandled-group-msg", groupId);
                debug("debug-group-not-monitored", groupId);
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "解析 Napcat 消息时出错", e);
        }
    }
}
