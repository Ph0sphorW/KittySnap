package org.icarus.kittysnap.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.icarus.kittysnap.KittySnap;
import org.icarus.kittysnap.napcat.NapcatWebSocketClient;
import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.config.KittySnapConfig;
import org.icarus.kittysnap.utils.MessageBatcher;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 将玩家聊天消息转发到 {@code chat-forward.target-groups} 中指定的 QQ 群 <p>
 * 如果 {@code target-groups} 为空，则转发到配置 {@code groups} 中的所有群
 */
public class ChatToGroupForwarder implements Listener {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final NapcatWebSocketClient napcatClient;
    private final ConfigurationManager cfg;
    private final MessageBatcher batcher;

    public ChatToGroupForwarder(NapcatWebSocketClient napcatClient,
                                ConfigurationManager cfg,
                                KittySnap plugin) {
        this.napcatClient = napcatClient;
        this.cfg = cfg;

        KittySnapConfig.ChatForward.Batching bc = cfg.getConfig().getChatForward().getBatching();
        if (bc.isEnabled()) {
            this.batcher = new MessageBatcher(plugin, bc.getMaxBatchSize(), bc.getMaxBatchInterval(), this::sendBatch);
        } else {
            this.batcher = null;
        }
    }

    /**
     * 在插件关闭时调用，清空积压
     */
    public void shutdown() {
        if (batcher != null) batcher.close();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        if (!cfg.getConfig().getChatForward().isEnabled()) return;

        Player player = event.getPlayer();
        String format = cfg.getConfig().getChatForward().getFormat();
        String prefix = cfg.getConfig().getChatForward().getChatPrefix();

        // Component 转纯文本格式化
        String displayName = PLAIN.serialize(player.displayName());
        String message = PLAIN.serialize(event.message());

        // 仅匹配前缀的消息才转发
        if (!prefix.isEmpty()) {
            if (!message.startsWith(prefix)) return;
            message = message.substring(prefix.length()).trim();
            if (message.isEmpty()) return;
        }

        String forwardText = String.format(format, displayName, message);

        if (batcher != null) {
            batcher.add(forwardText);
        } else {
            sendImmediately(forwardText);
        }

        cfg.logFine("chat-forward.forwarded", player.getName(), forwardText);
    }

    // -------------------- 立即发送（非批处理模式） --------------------

    private void sendImmediately(String text) {
        List<Long> targetGroups = cfg.getConfig().getChatForward().getTargetGroups();
        if (targetGroups.isEmpty()) {
            for (long groupId : napcatClient.getMonitoredGroups()) {
                napcatClient.sendGroupMessage(groupId, text);
            }
        } else {
            for (long groupId : targetGroups) {
                napcatClient.sendGroupMessage(groupId, text);
            }
        }
    }

    // -------------------- 批处理发送 --------------------

    /**
     * 将一批消息合并为一条发送
     */
    private void sendBatch(List<String> batch) {
        String combined = batch.stream().collect(Collectors.joining("\n"));
        List<Long> targetGroups = cfg.getConfig().getChatForward().getTargetGroups();
        if (targetGroups.isEmpty()) {
            Set<Long> monitored = napcatClient.getMonitoredGroups();
            for (long groupId : monitored) {
                napcatClient.sendGroupMessage(groupId, combined);
            }
        } else {
            for (long groupId : targetGroups) {
                napcatClient.sendGroupMessage(groupId, combined);
            }
        }
    }
}
