package org.icarus.kittysnap.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.icarus.kittysnap.napcat.IGroupMessageListener;
import org.icarus.kittysnap.napcat.NapcatMessage;
import org.icarus.kittysnap.config.ConfigurationManager;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 收到 QQ 群消息后，经过转译转发到游戏内所有在线玩家的聊天栏
 */
public class QQToGameBroadcaster implements IGroupMessageListener {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final ConfigurationManager cfg;

    public QQToGameBroadcaster(ConfigurationManager cfg) {
        this.cfg = cfg;
    }

    @Override
    public void onGroupMessage(NapcatMessage message, long groupId, long userId, String content) {
        NapcatMessage.Sender sender = message.getSender();
        String displayName = sender != null ? sender.getDisplayName() : String.valueOf(userId);
        long qqId = sender != null ? sender.getUserId() : userId;
        String role = sender != null ? sender.getRole() : "member";
        String timeStr = TIME_FMT.format(Instant.ofEpochSecond(message.getTime()));

        // hover QQ号、时间、群身份
        String roleDisplay = switch (role) {
            case "owner" -> "群主";
            case "admin" -> "管理员";
            default -> "群成员";
        };

        var m = cfg.getMessages();
        String hoverFormat = m != null ? m.getQq().getMessageHoverFormat() : null;
        if (hoverFormat == null) {
            hoverFormat = "<gray>QQ: </gray><white>%d</white>\\n<gray>时间: </gray><white>%s</white>\\n<gray>身份: </gray><white>%s</white>";
        }

        String escapedName = displayName.replace("<", "\\<");
        String hoverDisplay = "<hover:show_text:'" + hoverFormat.formatted(qqId, timeStr, roleDisplay) + "'>"
                + escapedName + "</hover>";

        // 外层模板
        String template = m != null ? m.getQq().getMessageFormat() : null;
        if (template == null) {
            template = "<gray>[QQ]</gray> <yellow>%s</yellow><gray>: %s</gray>";
        }

        // content 已在 Dispatcher 中转义
        String formatted = String.format(template, hoverDisplay, content);

        // 广播
        Component component = cfg.safeDeserialize(formatted);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(component);
        }

        cfg.logInfo("qq.message-log", groupId, displayName, content);
    }
}
