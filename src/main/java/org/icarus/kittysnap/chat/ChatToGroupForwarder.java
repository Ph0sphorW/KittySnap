package org.icarus.kittysnap.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.icarus.kittysnap.napcat.NapcatWebSocketClient;
import org.icarus.kittysnap.config.ConfigurationManager;

import java.util.List;
import java.util.Set;

/**
 * 游戏内聊天 → QQ群 转发器
 * <p>
 * 监听 {@link AsyncChatEvent}，将玩家聊天消息转发到
 * {@code chat-forward.target-groups} 中指定的 QQ 群。
 * <p>
 * 如果 target-groups 为空，则转发到主配置 {@code groups} 中的所有群。
 */
public class ChatToGroupForwarder implements Listener {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final NapcatWebSocketClient napcatClient;
    private final ConfigurationManager cfg;

    public ChatToGroupForwarder(NapcatWebSocketClient napcatClient,
                                ConfigurationManager cfg) {
        this.napcatClient = napcatClient;
        this.cfg = cfg;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        if (!cfg.isChatForwardEnabled()) return;

        Player player = event.getPlayer();
        String format = cfg.getChatForwardFormat();

        // 将 Component 转纯文本再格式化
        String displayName = PLAIN.serialize(player.displayName());
        String message = PLAIN.serialize(event.message());
        String forwardText = String.format(format, displayName, message);

        // 获取目标群列表
        List<Long> targetGroups = cfg.getChatForwardTargetGroups();
        if (targetGroups.isEmpty()) {
            // 未单独指定则转发到所有监听群
            Set<Long> monitored = napcatClient.getMonitoredGroups();
            for (long groupId : monitored) {
                napcatClient.sendGroupMessage(groupId, forwardText);
            }
        } else {
            for (long groupId : targetGroups) {
                napcatClient.sendGroupMessage(groupId, forwardText);
            }
        }

        cfg.logFine("chat-forwarded", player.getName(), forwardText);
    }
}
