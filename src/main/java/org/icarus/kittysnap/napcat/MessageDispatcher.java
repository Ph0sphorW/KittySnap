package org.icarus.kittysnap.napcat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.bukkit.plugin.java.JavaPlugin;
import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.database.DatabaseManager;
import org.icarus.kittysnap.napcat.ob11.OB11Segment;
import org.icarus.kittysnap.napcat.ob11.handler.BuildResult;
import org.icarus.kittysnap.napcat.ob11.handler.OB11SegmentHandler;

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
    private OB11SegmentHandler segmentHandler;
    private BiConsumer<String, Object[]> debugConsumer;

    MessageDispatcher(JavaPlugin plugin, ConfigurationManager cfg, ExecutorService executor,
                      CopyOnWriteArraySet<GroupEntry> groupListeners) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.executor = executor;
        this.groupListeners = groupListeners;
    }

    void setDatabaseManager(DatabaseManager db) {
        this.databaseManager = db;
    }

    void setSegmentHandler(OB11SegmentHandler handler) {
        this.segmentHandler = handler;
    }

    void updateExecutor(ExecutorService exec) {
        this.executor = exec;
    }

    void setDebugConsumer(BiConsumer<String, Object[]> c) {
        this.debugConsumer = c;
    }

    private void debug(String key, Object... args) {
        if (debugConsumer != null) debugConsumer.accept(key, args);
    }

    /**
     * 由 executor 线程调用
     */
    void dispatch(String json) {
        try {
            JSONObject root = JSON.parseObject(json);
            int t = cfg.getNapcatDebugTruncateLength();
            debug("debug-msg-received", json.length() > t ? json.substring(0, t) + "..." : json);

            if (!"message".equals(root.getString("post_type"))
                    || !"group".equals(root.getString("message_type"))) {
                String pt = root.getString("post_type");
                String mt = root.getString("message_type");
                plugin.getLogger().info("[MSG-DISPATCH] 忽略非群消息: post_type=" + pt + ", message_type=" + mt);
                debug("debug-msg-ignored", pt);
                return;
            }

            long groupId = root.getLongValue("group_id");
            if (groupId == 0) {
                plugin.getLogger().warning("[MSG-DISPATCH] 消息中 group_id 为 0，无法处理");
                return;
            }

            NapcatMessage napMsg = JSON.parseObject(json, NapcatMessage.class);

            // — 解析 OB11 消息段 —
            napMsg.parseMessageSegments(root);
            plugin.getLogger().info("[MSG-DISPATCH] 收到群消息: group=" + groupId
                    + " user=" + napMsg.getSenderId()
                    + " segments=" + napMsg.getSegments());

            // — 通过 OB11SegmentHandler 构建展示文本 —
            final String displayContent;
            if (segmentHandler != null) {
                BuildResult result = segmentHandler.buildDisplay(
                        napMsg.getSegments(), groupId, databaseManager);
                displayContent = result.displayContent();
            } else {
                // 无 handler 时降级：拼接所有段的纯文本
                StringBuilder fallback = new StringBuilder();
                for (OB11Segment seg : napMsg.getSegments()) {
                    if (seg instanceof org.icarus.kittysnap.napcat.ob11.OB11MessageText textSeg) {
                        String txt = textSeg.getText();
                        if (txt != null) fallback.append(txt);
                    }
                }
                String raw = fallback.toString().trim();
                displayContent = raw.isEmpty() ? "(消息)" : raw.replace("<", "\\<");
            }

            // — DB —
            if (databaseManager != null) {
                String nick = napMsg.getSender() != null ? napMsg.getSender().getDisplayName() : "";
                String raw = napMsg.getRawMessage() != null ? napMsg.getRawMessage() : "";
                long u = napMsg.getSenderId();
                long mId = napMsg.getMessageId();
                String mSeq = napMsg.getMessageSeq();
                long t2 = napMsg.getTime();
                executor.execute(() -> databaseManager.insertGroupMessage(groupId, u, nick, raw, mId, mSeq, t2));
            }

            // — 分发 —
            boolean handled = false;
            for (GroupEntry entry : groupListeners) {
                if (entry.groupId() != groupId) continue;
                handled = true;
                long userId = napMsg.getSenderId();
                String nickname = napMsg.getSender() != null
                        ? napMsg.getSender().getDisplayName() : String.valueOf(userId);

                plugin.getLogger().info("[MSG-DISPATCH] → 正在分发给监听器: group=" + groupId
                        + " user=" + userId + " content=\"" + displayContent + "\"");
                debug("debug-msg-dispatched", groupId, userId, displayContent);

                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        entry.listener().onGroupMessage(napMsg, groupId, userId, displayContent);
                        plugin.getLogger().info("[QQ→GAME] QQ消息已成功广播到游戏内: group=" + groupId + " user=" + nickname);
                    } catch (Exception e) {
                        cfg.logWarning("listener-error", groupId, nickname);
                        plugin.getLogger().log(Level.WARNING, "[QQ→GAME] 广播器执行异常", e);
                    }
                });
            }

            if (!handled) {
                plugin.getLogger().warning("[MSG-DISPATCH] 群 " + groupId + " 不在监听列表中，已忽略");
                cfg.logFine("unhandled-group-msg", groupId);
                debug("debug-group-not-monitored", groupId);
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "解析 Napcat 消息时出错", e);
        }
    }
}
