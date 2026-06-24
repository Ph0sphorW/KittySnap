package org.icarus.kittysnap.chat;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.icarus.kittysnap.napcat.IGroupMessageListener;
import org.icarus.kittysnap.napcat.NapcatMessage;
import org.icarus.kittysnap.config.ConfigurationManager;

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

        // 从 MessagesConfig 直接获取模板（保留 MiniMessage 标签，不被 raw() 剥离）
        String template = cfg.getMessages() != null ? cfg.getMessages().getQqMessageFormat() : null;
        if (template == null) {
            template = "<gray>[QQ]</gray> <yellow>%s</yellow><gray>: %s</gray>";
        }
        // 仅转义 displayName（来自消息 sender 对象，未在 Dispatcher 中转义）
        // content 已在 MessageDispatcher 中完成 CQ:reply 解析、MiniMessage 标签转义和格式化
        String escapedDisplayName = displayName.replace("<", "\\<");
        String formatted = String.format(template, escapedDisplayName, content);

        // 解析 MiniMessage 并广播给所有在线玩家
        Component component = cfg.safeDeserialize(formatted);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(component);
        }

        // 同时输出到控制台（不含颜色标签，使用 raw() 剥离标签）
        cfg.logInfo("qq-message-log", groupId, displayName, content);
    }

}
