package org.icarus.kittysnap.config;

import de.exlll.configlib.Configuration;
import lombok.Getter;

/**
 * KittySnap 语言消息配置 — 由 ConfigLib 自动映射 {@code messages.yml}。
 * <p>
 * 嵌套 {@code @Configuration} 类按板块组织，写入 YAML 后自动产生分层缩进。
 */
@Configuration
@Getter
public final class MessagesConfig {

    public MessagesConfig() {
    }

    // ==================== 通用 ====================

    public String prefix = "<gradient:#D7FFFF:#33CCFF>[KittySnap]</gradient> ";

    // ==================== 插件生命周期 ====================

    public PluginMessages plugin = new PluginMessages();

    @Configuration
    @Getter
    public static final class PluginMessages {
        public String enabled = "<#99FF99>KittySnap 已启动。"
                + "\n" + "<#99FF99>Made by </#99FF99><#D7FFFF>Ph0sphorW<#D7FFFF><#99FF99> with ❤</#99FF99>";
        public String disabled = "<#99FF99>See you next time!";
        public String configReloaded = "<#99FF99>配置文件与语言文件已重载。";
    }

    // ==================== WebSocket 连接 ====================

    public WebsocketMessages websocket = new WebsocketMessages();

    @Configuration
    @Getter
    public static final class WebsocketMessages {
        public String connecting = "<#999999>正在连接到 Napcat WebSocket: %s";
        public String connected = "<#99FF99>Napcat WebSocket 已连接: %s";
        public String connectFailed = "<#FF9999>Napcat WebSocket 连接失败: %s";
        public String connectError = "<#FF9999>创建 WebSocket 连接时出错。";
        public String disconnected = "<#FFFF99>Napcat WebSocket 已断开。";
        public String reconnecting = "<#999999>将在 %d 秒后尝试重连...";
        public String notConnected = "<#FF9999>WebSocket 未连接，无法发送消息。";
        public String opened = "<#99FF99>WebSocket 连接已打开.";
        public String closed = "<#FFFF99>WebSocket 连接已关闭: %d %s";
        public String error = "<#FF9999>WebSocket 错误: %s";
        public String authEnabled = "<#999999>WebSocket 连接需要使用 Token 认证。";
        public String heartbeatTimeout = "<#FFFF99>心跳超时: 已过 %d 秒（阈值 %d 秒），触发重连。";
        public String sendFailed = "<#FF9999>发送消息到群 %d 失败: %s";
    }

    // ==================== 群监听 ====================

    public GroupMessages group = new GroupMessages();

    @Configuration
    @Getter
    public static final class GroupMessages {
        public String listenerAdded = "<#99FF99>已添加群 %d 的消息监听。";
        public String listenerRemoved = "<#99FF99>已移除群 %d 的消息监听。";
        public String listenerError = "<#FF9999>群消息监听器执行出错: [群=%d | 用户=%s]";
        public String unhandledMsg = "<#999999>收到未监听的群消息: %d";
    }

    // ==================== 游戏→QQ 转发 ====================

    public ChatForwardMessages chatForward = new ChatForwardMessages();

    @Configuration
    @Getter
    public static final class ChatForwardMessages {
        public String enabled = "<#99FF99>游戏聊天 → QQ群 转发已启用。";
        public String disabled = "<#FFFF99>游戏聊天 → QQ群 转发已禁用。";
        public String forwarded = "<#999999>已转发聊天到群: %s | %s";
    }

    // ==================== 调试日志 ====================

    public DebugMessages debug = new DebugMessages();

    @Configuration
    @Getter
    public static final class DebugMessages {
        public String enabled = "<#99FF99>调试模式已启用。";
        public String disabled = "<#FFFF99>调试模式已关闭。";
        public String heartbeat = "<gray>[调试] 心跳包: time=%d";
        public String wsOpen = "<gray>[调试] WebSocket 连接已打开";
        public String wsClose = "<gray>[调试] WebSocket 已关闭: %d %s";
        public String wsError = "<gray>[调试] WebSocket 错误: %s";
        public String msgReceived = "<gray>[调试] 收到原始消息: %s";
        public String msgIgnored = "<gray>[调试] 忽略非群消息: type=%s";
        public String msgDispatched = "<gray>[调试] 分发给群 %d: uid=%d content=%s";
        public String groupNotMonitored = "<gray>[调试] 群 %d 未在监听列表中";
        public String heartbeatTimeout = "<gray>[调试] 心跳超时: elapsed=%ds threshold=%ds";
        public String dbInsert = "<gray>[调试-数据库] 写入消息: group=%d user=%d content=%s row=%d time=%dms";
        public String dbPool = "<gray>[调试-数据库] 连接池: active=%d idle=%d waiting=%d";
    }

    // ==================== 命令 ====================

    public CommandMessages command = new CommandMessages();

    @Configuration
    @Getter
    public static final class CommandMessages {
        public String noPermission = "<#FF9999>你没有权限使用此命令";
        public String unknownSubcommand = "<#FF9999>未知子命令: %s";
        public String usageHeader = "<gold>===== KittySnap 命令帮助 =====";
        public String usageFooter = "<gold>------------------------------";
        public String usageLine = "<#FFFF99>/<white>%s %s <gray>- %s";
        public String addgroupUsage = "<#FF9999>用法: /kittysnap addgroup 群号";
        public String addgroupDone = "<#99FF99>已添加群 %d 到监听列表并已保存到配置文件";
        public String addgroupDesc = "添加监听群聊";
        public String delgroupUsage = "<#FF9999>用法: /kittysnap delgroup 群号";
        public String delgroupDone = "<#99FF99>已从监听列表移除群 %d 并已保存到配置文件";
        public String delgroupNotFound = "<#FF9999>群 %d 不在当前监听列表中";
        public String delgroupDesc = "删除监听群聊";
        public String reconnectDone = "<#99FF99>正在重新连接 Napcat WebSocket...";
        public String reconnectDesc = "重连 Napcat";
        public String sendUsage = "<#FF9999>用法: /kittysnap send 群号 消息";
        public String sendNotMonitored = "<#FF9999>群 %d 不在监听列表中，无法发送";
        public String sendConnecting = "<#FF9999>WebSocket 未连接，消息待连接后发送";
        public String sendDone = "<#99FF99>已向群 %d 发送消息";
        public String sendDesc = "向指定监听群发送消息";
        public String debugToggledOn = "<#99FF99>调试模式已开启，技术信息将输出到后台和你的聊天框";
        public String debugToggledOff = "<#FFFF99>调试模式已关闭";
        public String debugDesc = "切换调试模式";
        public String reloadDone = "<#99FF99>配置文件与语言文件已重载";
        public String reloadDesc = "重载配置文件与语言文件";
    }

    // ==================== 数据库 ====================

    public DatabaseMessages database = new DatabaseMessages();

    @Configuration
    @Getter
    public static final class DatabaseMessages {
        public String initializing = "<#99FF99>正在初始化数据库连接池...";
        public String initialized = "<#99FF99>数据库连接池初始化完成。";
        public String shutdown = "<#FFFF99>数据库连接池已关闭。";
        public String insertMessage = "<#999999>已保存群消息到数据库 [群=%d | 用户=%d] row=%d";
        public String insertError = "<#FFFF99>保存群消息到数据库时出错: %s";
        public String connectError = "<#FFFF99>数据库连接失败: %s";
        public String tableCreated = "<#99FF99>数据表已创建/已存在: %s";
        public String poolInitFailed = "<#FFFF99>数据库连接池初始化失败。";
        public String tableCreateFailed = "<#FFFF99>创建数据表失败。";
        public String insertFailed = "<#FFFF99>插入消息到数据库失败。";
    }

    // ==================== 内部 ====================

    public InternalMessages internal = new InternalMessages();

    @Configuration
    @Getter
    public static final class InternalMessages {
        public String commandRegisterFailed = "<#FFFF99>注册命令失败！检查你的插件列表，是否有同名插件冲突？";
        public String noListenGroupsConfigured = "<#FFFF99>config.yml 中未配置任何监听群。";
        public String addgroupAlreadyExists = "<#FF9999>群 %d 已在监听列表中，无需重复添加。";
        public String invalidGroupId = "<#FF9999>无效的群号: %s";
        public String missingMessageKey = "<#FF9999>messages.yml 中缺少信息: %s";
        public String readMessageFailed = "<#FF9999>读取消息 %s 失败。";
        public String missingKeyFallback = "<missing: %s>";
        public String logGroupMsgFormat = "[群: %d] [用户: %d] %s";
        public String unknownSender = "未知用户";
    }

    // ==================== 消息分发 ====================

    public DispatchMessages dispatch = new DispatchMessages();

    @Configuration
    @Getter
    public static final class DispatchMessages {
        public String received = "<#CCFFFF>[MSG-DISPATCH] 收到群消息: group=%d user=%d segments=%s";
        public String dispatching = "<#CCFFFF>[MSG-DISPATCH] 正在分发给监听器: group=%d user=%d content=%s";
        public String broadcasted = "<#CCFFFF>[QQ→GAME] QQ消息已成功广播到游戏内: group=%d user=%s";
        public String listenerError = "<#FF9999>[QQ→GAME] 广播器执行异常。";
        public String notMonitored = "<#CCFFFF>[MSG-DISPATCH] 群 %d 不在监听列表中，已忽略。";
        public String parseError = "<#FF9999>解析 Napcat 消息时出错。";
    }

    // ==================== QQ→游戏 转发 ====================

    public QQMessages qq = new QQMessages();

    @Configuration
    @Getter
    public static final class QQMessages {
        public String messageFormat = "<gray>[QQ]</gray> <#FFFF99>%s</#FFFF99><gray>: %s</gray>";
        public String messageHoverFormat = """
                <gray>QQ: </gray><white>%d</white>
                <gray>时间: </gray><white>%s</white>
                <gray>身份: </gray><white>%s</white>""";
        public String messageLog = "[QQ消息] [群=%d] <%s> %s";
    }

    // ==================== OB11 段展示 ====================

    public SegmentMessages segment = new SegmentMessages();

    @Configuration
    @Getter
    public static final class SegmentMessages {
        public String imageText = "[图片]";
        public String imageGifText = "[动画表情]";
        public String imagePreviewHover = "<gray>点击预览图片</gray>";
        public String cardText = "[卡片消息]";
        public String markdownText = "[Markdown消息]";
        public String unknownText = "[其它消息]";
        public String atAll = "@全体成员";
        public String atPrefix = "@";
        public String replyFormat = "<gray>[回复 </gray><#99FF99>%s</green><gray>: %s</gray><gray>]</gray>";
        public String replyUnknown = "<gray>[回复 未知消息]</gray>";
    }
}
