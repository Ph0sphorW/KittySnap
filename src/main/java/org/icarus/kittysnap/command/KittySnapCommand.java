package org.icarus.kittysnap.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.icarus.kittysnap.KittySnap;
import org.icarus.kittysnap.config.ConfigurationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * KittySnap 命令执行器
 * <p>
 * 主命令: /kittysnap (别名: /ks, /bot)
 * <p>
 * 子命令:
 * <ul>
 *   <li>addgroup &lt;群号&gt;     — 添加监听群聊</li>
 *   <li>delgroup &lt;群号&gt;     — 删除监听群聊</li>
 *   <li>reconnect                — 重连 Napcat</li>
 *   <li>send &lt;群号&gt; &lt;消息&gt; — 向监听群发送消息</li>
 *   <li>debug                    — 切换调试模式</li>
 * </ul>
 */
public class KittySnapCommand implements TabExecutor {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "addgroup", "delgroup", "reconnect", "send", "debug", "reload"
    );

    private static final List<String> SUBCOMMAND_DESC_KEYS = Arrays.asList(
            "addgroup-desc", "delgroup-desc", "reconnect-desc",
            "send-desc", "debug-desc", "reload-desc"
    );

    private final KittySnap plugin;
    private final ConfigurationManager cfg;

    public KittySnapCommand(KittySnap plugin, ConfigurationManager cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!sender.hasPermission("kittysnap.admin")) {
            sender.sendMessage(cfg.prefixed("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase();
        return switch (sub) {
            case "addgroup" -> handleAddGroup(sender, args);
            case "delgroup" -> handleDelGroup(sender, args);
            case "reconnect" -> handleReconnect(sender);
            case "send" -> handleSend(sender, args);
            case "debug" -> handleDebug(sender);
            case "reload" -> handleReload(sender);
            default -> {
                sender.sendMessage(cfg.prefixed("unknown-subcommand", sub));
                sendHelp(sender, label);
                yield true;
            }
        };
    }

    // ==================== 子命令处理 ====================

    private boolean handleAddGroup(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(cfg.prefixed("addgroup-usage"));
            return true;
        }
        long groupId = parseGroupId(args[1], sender);
        if (groupId <= 0) return true;

        // 保存到配置文件（如果已存在会返回 false）
        boolean added = cfg.addListenGroup(groupId);
        if (!added) {
            sender.sendMessage(cfg.prefixed("addgroup-already-exists", groupId));
            return true;
        }

        // 注册到运行时监听
        plugin.getNapcatClient().addGroup(groupId, (napMsg, gid, uid, content) -> {
            plugin.getLogger().info(cfg.logGroupMsgFormat(gid, uid, content));
        });

        sender.sendMessage(cfg.prefixed("addgroup-done", groupId));
        return true;
    }

    private boolean handleDelGroup(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(cfg.prefixed("delgroup-usage"));
            return true;
        }
        long groupId = parseGroupId(args[1], sender);
        if (groupId <= 0) return true;

        // 从配置文件移除
        boolean removed = cfg.removeListenGroup(groupId);
        if (!removed) {
            sender.sendMessage(cfg.prefixed("delgroup-not-found", groupId));
            return true;
        }

        // 从运行时移除
        plugin.getNapcatClient().removeGroup(groupId);

        sender.sendMessage(cfg.prefixed("delgroup-done", groupId));
        return true;
    }

    private boolean handleReconnect(CommandSender sender) {
        sender.sendMessage(cfg.prefixed("reconnect-done"));
        plugin.getNapcatClient().reconnect();
        return true;
    }

    private boolean handleSend(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(cfg.prefixed("send-usage"));
            return true;
        }

        long groupId = parseGroupId(args[1], sender);
        if (groupId <= 0) return true;

        // 检查群是否在监听列表中
        Set<Long> monitored = plugin.getNapcatClient().getMonitoredGroups();
        if (!monitored.contains(groupId)) {
            sender.sendMessage(cfg.prefixed("send-not-monitored", groupId));
            return true;
        }

        // 将剩余参数拼接为消息内容
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        plugin.getNapcatClient().sendGroupMessage(groupId, message);
        sender.sendMessage(cfg.prefixed("send-done", groupId));
        return true;
    }

    private boolean handleDebug(CommandSender sender) {
        boolean newState = !plugin.isDebugMode();
        plugin.setDebugMode(newState, sender);

        if (newState) {
            sender.sendMessage(cfg.prefixed("debug-toggled-on"));
        } else {
            sender.sendMessage(cfg.prefixed("debug-toggled-off"));
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(cfg.prefixed("reload-done"));
        return true;
    }

    // ==================== Tab 补全 ====================

    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      @NonNull Command command,
                                      @NonNull String alias,
                                      String @NonNull [] args) {
        if (!sender.hasPermission("kittysnap.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, new ArrayList<>());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            return switch (sub) {
                case "addgroup" -> List.of("<group_id>");
                case "delgroup" -> tabCompleteGroups(args[1]);
                case "send" -> tabCompleteGroups(args[1]);
                default -> Collections.emptyList();
            };
        }

        if (args.length >= 3 && "send".equalsIgnoreCase(args[0])) {
            return List.of("<text>");
        }

        return Collections.emptyList();
    }

    // ==================== 工具方法 ====================

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(cfg.prefixed("usage-header"));
        for (int i = 0; i < SUBCOMMANDS.size(); i++) {
            sender.sendMessage(cfg.prefixed("usage-line",
                    label, SUBCOMMANDS.get(i), cfg.raw(SUBCOMMAND_DESC_KEYS.get(i))));
        }
        sender.sendMessage(cfg.prefixed("usage-footer"));
    }

    private long parseGroupId(String input, CommandSender sender) {
        try {
            long id = Long.parseLong(input);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException e) {
            sender.sendMessage(cfg.prefixed("invalid-group-id", input));
            return 0;
        }
    }

    private List<String> tabCompleteGroups(String partial) {
        Set<Long> groups = plugin.getNapcatClient().getMonitoredGroups();
        return groups.stream()
                .map(String::valueOf)
                .filter(id -> id.startsWith(partial))
                .collect(Collectors.toList());
    }
}
