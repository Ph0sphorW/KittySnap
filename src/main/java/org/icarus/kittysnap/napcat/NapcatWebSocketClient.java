package org.icarus.kittysnap.napcat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.database.DatabaseManager;

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
 * Napcat WebSocket 客户端：连接 Napcat，收发群消息。
 * <p>
 * 连接前发送的消息自动排队，连接建立后发出。消息解析分发由 {@link MessageDispatcher} 处理。
 */
public class NapcatWebSocketClient {

    private static final int CLOSE_NORMAL = 1000;

    private final JavaPlugin plugin;
    private final ConfigurationManager cfg;
    private final String wsUrl;
    private final int reconnectDelay;
    private final long connectTimeout;
    private final String authToken;
    private final MessageDispatcher dispatcher;

    private final CopyOnWriteArraySet<GroupEntry> groupListeners = new CopyOnWriteArraySet<>();
    private final Queue<PendingMessage> pendingQueue = new ConcurrentLinkedQueue<>();

    private HttpClient httpClient;
    private WebSocket webSocket;
    @Getter
    private volatile boolean connected = false;
    private volatile boolean autoReconnect = true;
    private volatile boolean reconnectScheduled = false;

    private ExecutorService executor;
    private int reconnectTaskId = -1;

    private BiConsumer<String, Object[]> debugConsumer;

    public void setDebugConsumer(BiConsumer<String, Object[]> consumer) {
        this.debugConsumer = consumer;
        dispatcher.setDebugConsumer(consumer);
    }

    public NapcatWebSocketClient(JavaPlugin plugin, ConfigurationManager cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.wsUrl = cfg.getWsUrl();
        this.reconnectDelay = cfg.getReconnectDelay();
        this.connectTimeout = cfg.getNapcatConnectTimeout();
        this.authToken = cfg.getNapcatToken();
        this.dispatcher = new MessageDispatcher(plugin, cfg, executor, groupListeners);
    }

    // ==================== 调试 ====================

    private void debug(String key, Object... args) {
        if (debugConsumer != null) debugConsumer.accept(key, args);
    }

    public void setDatabaseManager(DatabaseManager db) {
        dispatcher.setDatabaseManager(db);
    }

    // ==================== 监听器注册 ====================

    public void addGroup(long groupId, IGroupMessageListener listener) {
        groupListeners.add(new GroupEntry(groupId, listener));
        cfg.logInfo("group-listener-added", groupId);
    }

    public void addGroups(Collection<Long> groupIds, IGroupMessageListener listener) {
        groupIds.forEach(gid -> addGroup(gid, listener));
    }

    public boolean removeGroup(long groupId) {
        boolean removed = groupListeners.removeIf(e -> e.groupId() == groupId);
        if (removed) cfg.logInfo("group-listener-removed", groupId);
        return removed;
    }

    public Set<Long> getMonitoredGroups() {
        Set<Long> groups = new HashSet<>();
        groupListeners.forEach(e -> groups.add(e.groupId()));
        return groups;
    }

    // ==================== 连接管理 ====================

    public void connect() {
        if (connected) return;
        ensureExecutor();
        httpClient = HttpClient.newHttpClient();
        doConnect();
    }

    public void reconnect() {
        autoReconnect = true;
        connected = false;
        cancelReconnectTask();
        try { if (webSocket != null) webSocket.sendClose(CLOSE_NORMAL, "Manual reconnect"); } catch (Exception ignored) {}
        ensureExecutor();
        doConnect();
    }

    private void ensureExecutor() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "Napcat-WS");
                t.setDaemon(true);
                return t;
            });
        }
        dispatcher.updateExecutor(executor);
    }

    private void doConnect() {
        try {
            cfg.logInfo("ws-connecting", wsUrl);
            var builder = httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(connectTimeout));
            if (authToken != null && !authToken.isEmpty()) {
                builder.header("Authorization", "Bearer " + authToken);
            }
            builder.buildAsync(URI.create(wsUrl), new WsListener())
                    .orTimeout(connectTimeout, TimeUnit.SECONDS)
                    .thenAccept(ws -> {
                        this.webSocket = ws;
                        this.connected = true;
                        cfg.logInfo("ws-connected", wsUrl);
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
        cancelReconnectTask();
        try { if (webSocket != null) webSocket.sendClose(CLOSE_NORMAL, "Plugin shutting down"); } catch (Exception ignored) {}
        if (executor != null) executor.shutdownNow();
        cfg.logInfo("ws-disconnected");
    }

    private void scheduleReconnect() {
        if (!autoReconnect || !plugin.isEnabled() || reconnectScheduled) return;
        reconnectScheduled = true;
        cfg.logInfo("ws-reconnecting", reconnectDelay);
        reconnectTaskId = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            reconnectScheduled = false;
            reconnectTaskId = -1;
            if (autoReconnect && !connected) doConnect();
        }, reconnectDelay * 20L).getTaskId();
    }

    private void cancelReconnectTask() {
        if (reconnectTaskId != -1) {
            Bukkit.getScheduler().cancelTask(reconnectTaskId);
            reconnectTaskId = -1;
        }
    }

    // ==================== 发送 ====================

    public void sendGroupMessage(long groupId, String message) {
        if (connected && webSocket != null) {
            executor.execute(() -> doSend(groupId, message));
        } else {
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
        webSocket.sendText(JSON.toJSONString(payload), true)
                .thenRun(() -> {
                    PendingMessage next = pendingQueue.poll();
                    if (next != null) doSend(next.groupId, next.message);
                })
                .exceptionally(ex -> {
                    cfg.logWarning("ws-send-failed", groupId, ex.getMessage());
                    return null;
                });
    }

    private void flushPendingQueue() {
        PendingMessage pm = pendingQueue.poll();
        if (pm != null) doSend(pm.groupId, pm.message);
    }

    // ==================== WebSocket 监听器 ====================

    private class WsListener implements Listener {
        private final StringBuilder buf = new StringBuilder();

        @Override
        public void onOpen(WebSocket ws) {
            cfg.logInfo("ws-opened");
            debug("debug-ws-open");
            ensureExecutor();
        }

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            buf.append(data);
            if (last) {
                String full = buf.toString();
                buf.setLength(0);
                if (executor != null && !executor.isShutdown()) {
                    executor.execute(() -> dispatcher.dispatch(full));
                }
            }
            return Listener.super.onText(ws, data, last);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int code, String reason) {
            connected = false;
            String r = reason != null ? reason : "";
            cfg.logInfo("ws-closed", code, r);
            debug("debug-ws-close", code, r);
            scheduleReconnect();
            return Listener.super.onClose(ws, code, reason);
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            cfg.logWarning("ws-error", error.getMessage());
            debug("debug-ws-error", error.getMessage());
        }
    }

    // ==================== 内部实体 ====================

    private record PendingMessage(long groupId, String message) {}
}
