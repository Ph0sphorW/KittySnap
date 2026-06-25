package org.icarus.kittysnap.napcat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.database.DatabaseManager;
import org.icarus.kittysnap.napcat.handler.handlers.BuildResult;
import org.icarus.kittysnap.napcat.handler.SegmentHandler;
import org.icarus.kittysnap.napcat.onebotapi.OB11Message;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public class MessageDispatcher {

    private final JavaPlugin plugin;
    private final ConfigurationManager cfg;
    private final CopyOnWriteArraySet<GroupEntry> groupListeners;
    @Setter
    private DatabaseManager databaseManager;
    @Setter
    private SegmentHandler segmentHandler;
    private BiConsumer<String, Object[]> debugConsumer;

    MessageDispatcher(JavaPlugin plugin, ConfigurationManager cfg, CopyOnWriteArraySet<GroupEntry> groupListeners) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.groupListeners = groupListeners;
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
            debug("debug.msg-received", json);

            if (!"message".equals(root.getString("post_type"))
                    || !"group".equals(root.getString("message_type"))) {
                debug("debug.msg-ignored", root.getString("post_type"));
                return;
            }

            long groupId = root.getLongValue("group_id");
            if (groupId == 0) return;

            OB11Message napMsg = JSON.parseObject(json, OB11Message.class);
            napMsg.parseMessageSegments(root);

            cfg.logFine("dispatch.received", groupId, napMsg.getSenderId(), napMsg.getSegments().toString());

            BuildResult result = segmentHandler.buildDisplay(napMsg.getSegments(), groupId, databaseManager);
            String displayContent = result.displayContent();

            // raw message
            if (databaseManager != null) {
                String nick = napMsg.getSender() != null ? napMsg.getSender().getDisplayName() : "";
                String raw = napMsg.getRawMessage() != null ? napMsg.getRawMessage() : "";
                databaseManager.insertGroupMessage(groupId, napMsg.getSenderId(), nick, raw, napMsg.getMessageId(), napMsg.getMessageSeq(), napMsg.getTime());
            }

            // 下发
            boolean handled = false;
            for (GroupEntry entry : groupListeners) {
                if (entry.groupId() != groupId) continue;
                handled = true;
                long userId = napMsg.getSenderId();
                String nickname = napMsg.getSender() != null ? napMsg.getSender().getDisplayName() : String.valueOf(userId);

                cfg.logFine("dispatch.dispatching", groupId, userId, displayContent);
                debug("debug.msg-dispatched", groupId, userId, displayContent);

                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        entry.listener().onGroupMessage(napMsg, groupId, userId, displayContent);
                        cfg.logFine("dispatch.broadcasted", groupId, nickname);
                    } catch (Exception e) {
                        cfg.logWarning("dispatch.listener-error");
                        plugin.getLogger().log(Level.WARNING, "", e);
                    }
                });
            }

            if (!handled) {
                cfg.logFine("dispatch.not-monitored", groupId);
                cfg.logFine("group.unhandled-msg", groupId);
                debug("debug.group-not-monitored", groupId);
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, cfg.raw("dispatch.parse-error"), e);
        }
    }
}
