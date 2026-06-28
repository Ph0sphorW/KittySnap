package org.icarus.kittysnap.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.icarus.kittysnap.utils.listeners.IGroupMessageListener;
import org.icarus.kittysnap.napcat.onebot.OB11Message;
import org.icarus.kittysnap.config.ConfigurationManager;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    public void onGroupMessage(OB11Message message, long groupId, long userId, String content,
                               @Nullable List<Component> clickComponents) {
        OB11Message.Sender sender = message.getSender();
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

        var messages = cfg.getMessages();
        String hoverFormat = messages != null ? messages.getQq().getMessageHoverFormat() : null;
        if (hoverFormat == null) {
            hoverFormat = "<gray>QQ: </gray><white>%d</white>\\n<gray>时间: </gray><white>%s</white>\\n<gray>身份: </gray><white>%s</white>";
        }

        String escapedName = displayName.replace("<", "\\<");
        String hoverDisplay = "<hover:show_text:'" + hoverFormat.formatted(qqId, timeStr, roleDisplay) + "'>"
                + escapedName + "</hover>";

        // 外层模板
        String template = messages != null ? messages.getQq().getMessageFormat() : null;
        if (template == null) {
            template = "<gray>[QQ]</gray> <yellow>%s</yellow><gray>: %s</gray>";
        }

        // content 已在 Dispatcher 中转义
        String formatted = String.format(template, hoverDisplay, content);

        // 广播基本消息
        Component baseComponent = cfg.safeDeserialize(formatted);

        // 如果有可点击组件（如图片预览），追加到消息末尾
        if (clickComponents != null && !clickComponents.isEmpty()) {
            Component full = baseComponent;
            for (Component cc : clickComponents) {
                full = full.append(Component.space()).append(cc);
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(full);
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(baseComponent);
            }
        }

        cfg.logInfo("qq.message-log", groupId, displayName, content);
    }
}
