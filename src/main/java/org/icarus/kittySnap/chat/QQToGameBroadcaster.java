package org.icarus.kittySnap.chat;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.icarus.kittySnap.NapcatApi.IGroupMessageListener;
import org.icarus.kittySnap.NapcatApi.NapcatMessage;
import org.icarus.kittySnap.config.ConfigurationManager;

/**
 * QQ 群消息 → 游戏内 广播器
 * <p>
 * 实现 {@link IGroupMessageListener}，收到 QQ 群消息后转发到游戏内所有在线玩家的聊天栏。
 */
public class QQToGameBroadcaster implements IGroupMessageListener {

    private static final String QQ_MSG_FORMAT_KEY = "qq-message-format";

    private final ConfigurationManager cfg;
    @Getter
    private final MiniMessage miniMessage;

    public QQToGameBroadcaster(ConfigurationManager cfg) {
        this.cfg = cfg;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public void onGroupMessage(NapcatMessage message, long groupId, long userId, String content) {
        // 构造显示名称（优先群名片）
        String displayName = message.getSender() != null
                ? message.getSender().getDisplayName()
                : String.valueOf(userId);

        // 获取消息模板，填充占位符
        String template = cfg.raw(QQ_MSG_FORMAT_KEY);
        if (template == null) {
            template = "<gray>[QQ]</gray> <yellow>%s</yellow><gray>: %s</gray>";
        }
        String formatted = String.format(template, displayName, content);

        // 解析 MiniMessage 并广播给所有在线玩家
        Component component = cfg.safeDeserialize(formatted);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(component);
        }

        // 同时输出到控制台（不含颜色标签）
        cfg.logInfo("qq-message-log", groupId, displayName, content);
    }

}
