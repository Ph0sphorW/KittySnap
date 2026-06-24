package org.icarus.kittysnap.napcat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.bukkit.plugin.java.JavaPlugin;
import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.database.DatabaseManager;
import org.icarus.kittysnap.handler.handlers.BuildResult;
import org.icarus.kittysnap.handler.OB11SegmentHandler;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public class MessageDispatcher {

    private final JavaPlugin plugin;
    private final ConfigurationManager cfg;
    private final CopyOnWriteArraySet<GroupEntry> groupListeners;
    private DatabaseManager databaseManager;
    private OB11SegmentHandler segmentHandler;
    private BiConsumer<String, Object[]> debugConsumer;

    MessageDispatcher(JavaPlugin plugin, ConfigurationManager cfg,
                      CopyOnWriteArraySet<GroupEntry> groupListeners) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.groupListeners = groupListeners;
    }

    void setDatabaseManager(DatabaseManager db) {
        this.databaseManager = db;
    }

    void setSegmentHandler(OB11SegmentHandler handler) {
        this.segmentHandler = handler;
    }

    void setDebugConsumer(BiConsumer<String, Object[]> c) {
        this.debugConsumer = c;
    }

    private void debug(String key, Object... args) {
        if (debugConsumer != null) debugConsumer.accept(key, args);
    }

    void dispatch(String json) {
        try {
            JSONObject root = JSON.parseObject(json);
            debug("debug-msg-received", json);

            if (!"message".equals(root.getString("post_type"))
                    || !"group".equals(root.getString("message_type"))) {
                debug("debug-msg-ignored", root.getString("post_type"));
                return;
            }

            long groupId = root.getLongValue("group_id");
            if (groupId == 0) return;

            NapcatMessage napMsg = JSON.parseObject(json, NapcatMessage.class);
            napMsg.parseMessageSegments(root);

            plugin.getLogger().info("[MSG-DISPATCH] 收到群消息: group=" + groupId
                    + " user=" + napMsg.getSenderId()
                    + " segments=" + napMsg.getSegments());

            BuildResult result = segmentHandler.buildDisplay(
                    napMsg.getSegments(), groupId, databaseManager);
            String displayContent = result.displayContent();

            // — 写入数据库 —
            if (databaseManager != null) {
                String nick = napMsg.getSender() != null ? napMsg.getSender().getDisplayName() : "";
                databaseManager.insertGroupMessage(groupId, napMsg.getSenderId(), nick, displayContent,
                        napMsg.getMessageId(), napMsg.getMessageSeq(), napMsg.getTime());
            }

            // 下发
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
