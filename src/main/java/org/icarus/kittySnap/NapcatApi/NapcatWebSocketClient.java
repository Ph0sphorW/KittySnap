package org.icarus.kittySnap.NapcatApi;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.icarus.kittySnap.config.ConfigurationManager;
import org.icarus.kittySnap.database.DatabaseManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Level;

/**
 * Napcat WebSocket 客户端
 * <p>
 * 通过正向 WebSocket 连接 Napcat（Napcat 作为服务器，本客户端主动连接）。
 * 如果收到的消息类型为 group，则分发给已注册的监听器。
 * <p>
 * 特性：自动重连、消息队列缓冲（连接未就绪时自动排队）、超时控制。
 */
public class NapcatWebSocketClient {

    private final String THREAD_NAME;
    private final int CLOSE_CODE;
    private final String CLOSE_REASON;
    private final int DEBUG_TRUNCATE_LENGTH;
    private final String MANUAL_RECONNECT_REASON;
    private final String AUTH_TOKEN;
    private final long CONNECT_TIMEOUT_SECONDS;
    private final long HEARTBEAT_TIMEOUT_SECONDS;

    private final JavaPlugin plugin;
    private final ConfigurationManager cfg;
    private final String wsUrl;
    private final int reconnectDelay;

    @Setter
    private DatabaseManager databaseManager;

    private final CopyOnWriteArraySet<GroupEntry> groupListeners = new CopyOnWriteArraySet<>();

    /** 待发送消息队列：连接未就绪时先缓存 */
    private final Queue<PendingMessage> pendingQueue = new ConcurrentLinkedQueue<>();

    private WebSocket webSocket;
    private HttpClient httpClient;
    @Getter
    private volatile boolean connected = false;
    private boolean autoReconnect = true;
    /** 防止重复调度重连的守卫标志 */
    private volatile boolean reconnectScheduled = false;

    private ExecutorService executor;
    private volatile long lastHeartbeatTime = 0;

    /** Bukkit 定时任务 ID（用于心跳检测） */
    private int heartbeatTaskId = -1;

    @Setter
    private BiConsumer<String, Object[]> debugConsumer;

    public NapcatWebSocketClient(JavaPlugin plugin, ConfigurationManager cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.wsUrl = cfg.getWsUrl();
        this.reconnectDelay = cfg.getReconnectDelay();
        this.THREAD_NAME = cfg.getNapcatThreadName();
        this.CLOSE_CODE = cfg.getNapcatCloseCode();
        this.CLOSE_REASON = cfg.getNapcatCloseReason();
        this.DEBUG_TRUNCATE_LENGTH = cfg.getNapcatDebugTruncateLength();
        this.MANUAL_RECONNECT_REASON = cfg.getNapcatManualReconnectReason();
        this.AUTH_TOKEN = cfg.getNapcatToken();
        this.CONNECT_TIMEOUT_SECONDS = cfg.getNapcatConnectTimeout();
        this.HEARTBEAT_TIMEOUT_SECONDS = cfg.getNapcatHeartbeatTimeout();
    }

    // ==================== 调试支持 ====================

    public boolean isDebugMode() { return debugConsumer != null; }

    private void debug(String key, Object... args) {
        if (debugConsumer != null) debugConsumer.accept(key, args);
    }

    // ==================== 群监听注册 ====================

    public void addGroup(long groupId, IGroupMessageListener listener) {
        groupListeners.add(new GroupEntry(groupId, listener));
        cfg.logInfo("group-listener-added", groupId);
    }

    public void addGroups(Collection<Long> groupIds, IGroupMessageListener listener) {
        for (long gid : groupIds) addGroup(gid, listener);
    }

    public boolean removeGroup(long groupId) {
        boolean removed = groupListeners.removeIf(e -> e.groupId == groupId);
        if (removed) cfg.logInfo("group-listener-removed", groupId);
        return removed;
    }

    public void removeGroup(long groupId, IGroupMessageListener listener) {
        groupListeners.remove(new GroupEntry(groupId, listener));
    }

    public Set<Long> getMonitoredGroups() {
        Set<Long> groups = new HashSet<>();
        for (GroupEntry e : groupListeners) groups.add(e.groupId);
        return groups;
    }

    // ==================== 连接管理 ====================

    public void connect() {
        if (connected) return;

        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, THREAD_NAME);
            t.setDaemon(true);
            return t;
        });

        httpClient = HttpClient.newHttpClient();
        doConnect();

        // 启动心跳超时检测：每 15 秒检查一次
        heartbeatTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (connected && lastHeartbeatTime > 0) {
                long elapsed = (System.currentTimeMillis() - lastHeartbeatTime) / 1000;
                if (elapsed > HEARTBEAT_TIMEOUT_SECONDS) {
                    cfg.logWarning("ws-heartbeat-timeout", elapsed, HEARTBEAT_TIMEOUT_SECONDS);
                    debug("debug-heartbeat-timeout", elapsed, HEARTBEAT_TIMEOUT_SECONDS);
                    // 标记断开连接并触发重连（不调用 reconnect() 以避免 executor 反复重建）
                    if (connected) {
                        connected = false;
                        try {
                            if (webSocket != null) webSocket.sendClose(CLOSE_CODE, "Heartbeat timeout");
                        } catch (Exception ignored) {}
                        scheduleReconnect();
                    }
                }
            }
        }, 15 * 20L, 15 * 20L).getTaskId();
    }

    public void reconnect() {
        autoReconnect = true;
        connected = false;
        try { if (webSocket != null) webSocket.sendClose(CLOSE_CODE, MANUAL_RECONNECT_REASON); } catch (Exception ignored) {}
        if (executor != null && !executor.isShutdown()) { executor.shutdownNow(); }
        connect();
    }

    private void doConnect() {
        try {
            cfg.logInfo("ws-connecting", wsUrl);
            var wsBuilder = httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS));

            if (AUTH_TOKEN != null && !AUTH_TOKEN.isEmpty()) {
                wsBuilder.header("Authorization", "Bearer " + AUTH_TOKEN);
                cfg.logInfo("ws-auth-enabled");
            }

            CompletableFuture<WebSocket> future = wsBuilder.buildAsync(
                    URI.create(wsUrl), new NapcatWebSocketListener());

            // 超时控制 + 连接成功/失败处理
            future.orTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .thenAccept(ws -> {
                        this.webSocket = ws;
                        this.connected = true;
                        this.lastHeartbeatTime = System.currentTimeMillis();
                        cfg.logInfo("ws-connected", wsUrl);
                        // 连接成功后立即发送队列中的积压消息
                        flushPendingQueue();
                    })
                    .exceptionally(ex -> {
                        cfg.logWarning("ws-connect-failed", ex.getMessage());
                        this.connected = false;
                        scheduleReconnect();
                        return null;
                    });
        } catch (Exception e) {
            cfg.logWarning("ws-connect-error");
            plugin.getLogger().log(Level.WARNING, "", e);
            scheduleReconnect();
        }
    }

    public void disconnect() {
        autoReconnect = false;
        connected = false;
        // 取消心跳检测任务
        if (heartbeatTaskId != -1) {
            Bukkit.getScheduler().cancelTask(heartbeatTaskId);
            heartbeatTaskId = -1;
        }
        try { if (webSocket != null) webSocket.sendClose(CLOSE_CODE, CLOSE_REASON); } catch (Exception ignored) {}
        try { if (executor != null) executor.shutdownNow(); } catch (Exception ignored) {}
        cfg.logInfo("ws-disconnected");
    }

    private void scheduleReconnect() {
        if (!autoReconnect || !plugin.isEnabled() || reconnectScheduled) return;
        reconnectScheduled = true;
        cfg.logInfo("ws-reconnecting", reconnectDelay);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            reconnectScheduled = false;
            if (autoReconnect && !connected) doConnect();
        }, reconnectDelay * 20L);
    }

    // ==================== 消息发送（含队列） ====================

    /**
     * 向指定群发送消息。<p>
     * 如果 WebSocket 未连接，消息会自动排队，待连接后发送。
     */
    public void sendGroupMessage(long groupId, String message) {
        if (connected && webSocket != null) {
            // 统一通过 executor 串行发送，避免 WebSocket.sendText() 并发问题
            executor.execute(() -> doSend(groupId, message));
        } else {
            cfg.logInfo("ws-reconnecting", 0);
            pendingQueue.add(new PendingMessage(groupId, message));
        }
    }

    private void doSend(long groupId, String message) {
        if (!connected || webSocket == null) {
            pendingQueue.add(new PendingMessage(groupId, message));
            return;
        }
        JSONObject payload = new JSONObject();
        payload.put("action", "send_group_msg");
        JSONObject params = new JSONObject();
        params.put("group_id", groupId);
        params.put("message", message);
        payload.put("params", params);
        String json = JSON.toJSONString(payload);
        // 串行化保证：每次 sendText 完成后再发下一次
        webSocket.sendText(json, true)
                .thenRun(() -> {
                    // 发送成功后尝试发送队列中的下一条
                    PendingMessage next = pendingQueue.poll();
                    if (next != null) doSend(next.groupId, next.message);
                })
                .exceptionally(ex -> {
                    cfg.logWarning("ws-send-failed", groupId, ex.getMessage());
                    return null;
                });
    }

    /** 连接建立后发送积压队列中的所有消息 */
    private void flushPendingQueue() {
        PendingMessage pm = pendingQueue.poll();
        if (pm != null) doSend(pm.groupId, pm.message);
    }

    // ==================== WebSocket 监听器 ====================

    private class NapcatWebSocketListener implements Listener {

        private final StringBuilder messageBuffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket ws) {
            cfg.logInfo("ws-opened");
            debug("debug-ws-open");
        }

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            messageBuffer.append(data);
            if (last) {
                String fullMsg = messageBuffer.toString();
                messageBuffer.setLength(0);
                if (executor != null && !executor.isShutdown()) {
                    executor.execute(() -> handleMessage(fullMsg));
                }
            }
            return Listener.super.onText(ws, data, last);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int code, String reason) {
            connected = false;
            cfg.logInfo("ws-closed", code, reason != null ? reason : "");
            debug("debug-ws-close", code, reason != null ? reason : "");
            scheduleReconnect();
            return Listener.super.onClose(ws, code, reason);
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            cfg.logWarning("ws-error", error.getMessage());
            debug("debug-ws-error", error.getMessage());
        }
    }

    // ==================== 消息分发 ====================

    private void handleMessage(String json) {
        try {
            JSONObject root = JSON.parseObject(json);

            debug("debug-msg-received", json.length() > DEBUG_TRUNCATE_LENGTH
                    ? json.substring(0, DEBUG_TRUNCATE_LENGTH) + "..." : json);

            // 心跳包
            if ("meta_event".equals(root.getString("post_type"))
                    && "heartbeat".equals(root.getString("meta_event_type"))) {
                lastHeartbeatTime = System.currentTimeMillis();
                debug("debug-heartbeat", lastHeartbeatTime);
                return;
            }

            // 只处理群消息
            if (!"message".equals(root.getString("post_type"))) {
                debug("debug-msg-ignored", root.getString("post_type"));
                return;
            }
            if (!"group".equals(root.getString("message_type"))) {
                debug("debug-msg-ignored", root.getString("message_type"));
                return;
            }

            long groupId = root.getLongValue("group_id");
            if (groupId == 0) return;

            NapcatMessage napMsg = JSON.parseObject(json, NapcatMessage.class);

            // 异步写入数据库
            if (databaseManager != null && executor != null && !executor.isShutdown()) {
                String nickname = napMsg.getSender() != null ? napMsg.getSender().getDisplayName() : "";
                String content = napMsg.getRawMessage() != null ? napMsg.getRawMessage() : "";
                final long fGid = groupId, fUid = napMsg.getSenderId();
                final String fNick = nickname, fCont = content;
                final long fMsgId = napMsg.getMessageId();
                final String fMsgSeq = napMsg.getMessageSeq();
                final long fTime = napMsg.getTime();
                executor.execute(() ->
                        databaseManager.insertGroupMessage(fGid, fUid, fNick, fCont, fMsgId, fMsgSeq, fTime));
            }

            boolean handled = false;
            for (GroupEntry entry : groupListeners) {
                if (entry.groupId == groupId) {
                    handled = true;
                    String content = napMsg.getRawMessage() != null ? napMsg.getRawMessage() : "";
                    long userId = napMsg.getSenderId();
                    String nickname = napMsg.getSender() != null
                            ? napMsg.getSender().getDisplayName() : String.valueOf(userId);

                    debug("debug-msg-dispatched", groupId, userId, content);

                    final String fc = content;
                    final long fu = userId;
                    final String fn = nickname;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        try {
                            entry.listener.onGroupMessage(napMsg, groupId, fu, fc);
                        } catch (Exception e) {
                            cfg.logWarning("listener-error", groupId, fn);
                            plugin.getLogger().log(Level.WARNING, "", e);
                        }
                    });
                }
            }

            if (!handled) {
                cfg.logFine("unhandled-group-msg", groupId);
                debug("debug-group-not-monitored", groupId);
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "解析 Napcat 消息时出错", e);
        }
    }

    // ==================== 内部实体 ====================

    private record GroupEntry(long groupId, IGroupMessageListener listener) {}

    /** 待发送的群消息 */
    private record PendingMessage(long groupId, String message) {}
}
