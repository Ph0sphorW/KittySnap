package org.icarus.kittysnap.chat;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.icarus.kittysnap.napcat.IGroupMessageListener;
import org.icarus.kittysnap.napcat.NapcatMessage;
import org.icarus.kittysnap.config.ConfigurationManager;
import org.jspecify.annotations.NonNull;

/**
 * 收到 QQ 群消息后，经过转译转发到游戏内所有在线玩家的聊天栏。
 */
public class QQToGameBroadcaster implements IGroupMessageListener {

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

        // 获取消息模板
        String formatted = getString(content, displayName);

        // 广播
        Component component = cfg.safeDeserialize(formatted);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(component);
        }

        // 输出到控制台
        cfg.logInfo("qq-message-log", groupId, displayName, content);
    }

    private @NonNull String getString(String content, String displayName) {
        String template = (cfg.getMessages() != null) ? cfg.getMessages().getQqMessageFormat() : null;
        if (template == null) {
            template = "<gray>[QQ]</gray> <yellow>%s</yellow><gray>: %s</gray>";
        }
        // 仅转义 displayName
        String escapedDisplayName = displayName.replace("<", "\\<");
        return String.format(template, escapedDisplayName, content);
    }

}
