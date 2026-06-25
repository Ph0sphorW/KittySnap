package org.icarus.kittysnap.config;

import de.exlll.configlib.Configuration;
import lombok.Getter;

/**
 * KittySnap 语言消息配置 — 由 ConfigLib 自动映射 {@code messages.yml}。
 * <p>
 * 每个 String 字段对应一条消息，支持 {@link String#format} 风格占位符。
 * 字段名使用 camelCase，写入 YAML 时自动转为 kebab-case。
 */
@Configuration
@Getter
public final class MessagesConfig {

    public MessagesConfig() {}

    // -------------------- 通用 --------------------

    public String prefix = "[KittySnap]";

    // -------------------- 插件 --------------------

    public String pluginEnabled = "<green>KittySnap 插件已启动";
    public String pluginDisabled = "<red>KittySnap 插件已关闭";
    public String configReloaded = "<green>配置文件与语言文件已重载";

    // -------------------- WebSocket 连接 --------------------

    public String wsConnecting = "正在连接到 Napcat WebSocket: %s";
    public String wsConnected = "<green>✔ Napcat WebSocket 已连接: %s";
    public String wsConnectFailed = "<red>✘ Napcat WebSocket 连接失败: %s";
    public String wsConnectError = "创建 WebSocket 连接时出错";
    public String wsDisconnected = "<yellow>Napcat WebSocket 已断开";
    public String wsReconnecting = "将在 %d 秒后尝试重连...";
    public String wsNotConnected = "<red>WebSocket 未连接，无法发送消息";
    public String wsOpened = "<green>WebSocket 连接已打开";
    public String wsClosed = "<yellow>WebSocket 连接已关闭: %d %s";
    public String wsError = "<red>WebSocket 错误: %s";
    public String wsAuthEnabled = "WebSocket 连接使用 Token 认证";
    public String wsHeartbeatTimeout = "<yellow>心跳超时: 已过 %d 秒（阈值 %d 秒），触发重连";
    public String wsSendFailed = "<red>发送消息到群 %d 失败: %s";

    // -------------------- 群监听 --------------------

    public String groupListenerAdded = "已添加群 %d 的消息监听";
    public String groupListenerRemoved = "已移除群 %d 的消息监听";
    public String listenerError = "群消息监听器执行出错 [群=%d | 用户=%s]";
    public String unhandledGroupMsg = "收到未监听的群消息: %d";

    // -------------------- 聊天转发 --------------------

    public String chatForwardEnabled = "<green>游戏聊天 → QQ群 转发已启用";
    public String chatForwardDisabled = "<yellow>游戏聊天 → QQ群 转发已禁用";
    public String chatForwarded = "已转发聊天到群: %s | %s";

    // -------------------- 调试模式 --------------------

    public String debugEnabled = "<green>调试模式已启用";
    public String debugDisabled = "<yellow>调试模式已关闭";
    public String debugHeartbeat = "<gray>[调试] 心跳包: time=%d";
    public String debugWsOpen = "<gray>[调试] WebSocket 连接已打开";
    public String debugWsClose = "<gray>[调试] WebSocket 已关闭: %d %s";
    public String debugWsError = "<gray>[调试] WebSocket 错误: %s";
    public String debugMsgReceived = "<gray>[调试] 收到原始消息: %s";
    public String debugMsgIgnored = "<gray>[调试] 忽略非群消息: type=%s";
    public String debugMsgDispatched = "<gray>[调试] 分发给群 %d: uid=%d content=%s";
    public String debugGroupNotMonitored = "<gray>[调试] 群 %d 未在监听列表中";
    public String debugHeartbeatTimeout = "<gray>[调试] 心跳超时: elapsed=%ds threshold=%ds";

    // -------------------- 数据库调试 --------------------

    public String debugDbInsert = "<gray>[调试-DB] 写入消息: group=%d user=%d content=%s row=%d time=%dms";
    public String debugDbPool = "<gray>[调试-DB] 连接池: active=%d idle=%d waiting=%d";

    // -------------------- 命令相关 --------------------

    public String noPermission = "<red>你没有权限使用此命令";
    public String unknownSubcommand = "<red>未知子命令: %s";
    public String cmdUsageHeader = "<gold>===== KittySnap 命令帮助 =====";
    public String cmdUsageFooter = "<gold>--------------------==========";
    public String cmdUsageLine = "<yellow>/<white>%s %s <gray>- %s";

    public String addgroupUsage = "<red>用法: /kittysnap addgroup 群号";
    public String addgroupDone = "<green>已添加群 %d 到监听列表并已保存到配置文件";
    public String addgroupDesc = "添加监听群聊";

    public String delgroupUsage = "<red>用法: /kittysnap delgroup 群号";
    public String delgroupDone = "<green>已从监听列表移除群 %d 并已保存到配置文件";
    public String delgroupNotFound = "<red>群 %d 不在当前监听列表中";
    public String delgroupDesc = "删除监听群聊";

    public String reconnectDone = "<green>正在重新连接 Napcat WebSocket...";
    public String reconnectDesc = "重连 Napcat";

    public String sendUsage = "<red>用法: /kittysnap send 群号 消息";
    public String sendNotMonitored = "<red>群 %d 不在监听列表中，无法发送";
    public String sendConnecting = "<red>WebSocket 未连接，消息待连接后发送";
    public String sendDone = "<green>已向群 %d 发送消息";
    public String sendDesc = "向指定监听群发送消息";

    public String debugToggledOn = "<green>调试模式已开启，技术信息将输出到后台和你的聊天框";
    public String debugToggledOff = "<yellow>调试模式已关闭";
    public String debugDesc = "切换调试模式";

    public String reloadDone = "<green>配置文件与语言文件已重载";
    public String reloadDesc = "重载配置文件与语言文件";

    // -------------------- 数据库 --------------------

    public String dbInitializing = "正在初始化数据库连接池...";
    public String dbInitialized = "<green>数据库连接池初始化完成";
    public String dbShutdown = "<yellow>数据库连接池已关闭";
    public String dbInsertMessage = "已保存群消息到数据库 [群=%d | 用户=%d] row=%d";
    public String dbInsertError = "保存群消息到数据库时出错: %s";
    public String dbConnectError = "数据库连接失败: %s";
    public String dbTableCreated = "<green>数据表已创建/已存在: %s";
    public String dbPoolInitFailed = "数据库连接池初始化失败";
    public String dbTableCreateFailed = "创建数据表失败";
    public String dbInsertFailed = "插入消息到数据库失败";

    // -------------------- 内部日志 --------------------

    public String commandRegisterFailed = "无法注册命令 kittysnap，请检查 plugin.yml";
    public String noListenGroupsConfigured = "config.yml 中未配置任何监听群组 (groups)";
    public String addgroupAlreadyExists = "<red>群 %d 已在监听列表中，无需重复添加";
    public String invalidGroupId = "<red>无效的群号: %s";
    public String missingMessageKey = "messages.yml 中缺少消息键: %s";
    public String readMessageFailed = "读取消息 %s 失败";
    public String missingKeyFallback = "<missing: %s>";

    // -------------------- 日志格式 --------------------

    public String logGroupMsgFormat = "[群: %d] [用户: %d] %s";

    // -------------------- QQ游戏转发 --------------------

    public String qqMessageFormat = "<gray>[QQ]</gray> <yellow>%s</yellow><gray>: %s</gray>";
    public String qqMessageLog = "[QQ消息] [群=%d] <%s> %s";

    // -------------------- OB11 段展示文本 --------------------

    public String segmentImageText = "[图片]";
    public String segmentImageGifText = "[动画表情]";
    public String segmentImagePreviewHover = "<gray>点击预览图片</gray>";
    public String segmentCardText = "[卡片消息]";
    public String segmentMarkdownText = "[Markdown消息]";
    public String segmentUnknownText = "[其它消息]";
    public String segmentAtAll = "@全体成员";
    public String segmentAtPrefix = "@";
    public String segmentReplyFormat = "<gray>[回复 </gray><green>%s</green><gray>: %s</gray><gray>]</gray>";
    public String segmentReplyUnknown = "<gray>[回复 未知消息]</gray>";

    public String dbUnknownSender = "未知用户";
}
