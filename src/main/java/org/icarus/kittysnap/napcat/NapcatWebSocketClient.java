package org.icarus.kittysnap.napcat;

import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.database.DatabaseManager;
import org.icarus.kittysnap.handler.OB11SegmentHandler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public class NapcatWebSocketClient {

    private final JavaPlugin plugin;
    private final ConfigurationManager cfg;

    final ListenerManager listeners;
    final MessageSender sender;
    final MessageDispatcher dispatcher;
    final ConcurrentHashMap<String, CompletableFuture<JSONObject>> pendingApiCalls = new ConcurrentHashMap<>();

    private HttpClient httpClient;
    private WebSocket webSocket;
    @Getter
    volatile boolean connected = false;
    private volatile boolean autoReconnect = true;
    private volatile boolean reconnectScheduled = false;
    volatile ExecutorService executor;
    private int reconnectTaskId = -1;

    public NapcatWebSocketClient(JavaPlugin plugin, ConfigurationManager cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.listeners = new ListenerManager(cfg);
        this.dispatcher = new MessageDispatcher(plugin, cfg, listeners.entries());
        this.sender = new MessageSender(cfg, pendingApiCalls, () -> connected, () -> webSocket, () -> executor);
    }

    // -------------------- 调试 --------------------

    public void setDebugConsumer(BiConsumer<String, Object[]> consumer) {
        dispatcher.setDebugConsumer(consumer);
    }

    public void setDatabaseManager(DatabaseManager db) {
        dispatcher.setDatabaseManager(db);
    }

    public void setSegmentHandler(OB11SegmentHandler handler) {
        dispatcher.setSegmentHandler(handler);
    }

    // -------------------- 监听器管理 --------------------

    public void addGroup(long groupId, IGroupMessageListener listener) {
        listeners.add(groupId, listener);
    }

    public void addGroups(Collection<Long> gids, IGroupMessageListener l) {
        listeners.addAll(gids, l);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean removeGroup(long groupId) {
        return listeners.remove(groupId);
    }

    public void removeListenersByType(Class<? extends IGroupMessageListener> t) {
        listeners.removeByType(t);
    }

    public Set<Long> getMonitoredGroups() {
        return listeners.getMonitoredGroups();
    }

    // -------------------- 连接 --------------------

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
        try {
            if (webSocket != null) webSocket.sendClose(1000, "Manual reconnect");
        } catch (Exception ignored) {
            // 滚木
        }
        ensureExecutor();
        doConnect();
    }

    public void disconnect() {
        autoReconnect = false;
        connected = false;
        cancelReconnectTask();
        try {
            if (webSocket != null) webSocket.sendClose(1000, "Plugin shutting down");
        } catch (Exception ignored) {
        }
        if (executor != null) executor.shutdownNow();
        cfg.logInfo("ws-disconnected");
    }

    private void doConnect() {
        ensureExecutor();
        try {
            cfg.logInfo("ws-connecting", cfg.getWsUrl());
            var builder = httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(cfg.getNapcatConnectTimeout()));
            String token = cfg.getNapcatToken();
            if (token != null && !token.isEmpty()) builder.header("Authorization", "Bearer " + token);
            builder.buildAsync(URI.create(cfg.getWsUrl()),
                            new WsListener(this, cfg, plugin, dispatcher, pendingApiCalls))
                    .orTimeout(cfg.getNapcatConnectTimeout(), TimeUnit.SECONDS)
                    .thenAccept(ws -> {
                        this.webSocket = ws;
                        this.connected = true;
                        cfg.logInfo("ws-connected", cfg.getWsUrl());
                        sender.flushPending();
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

    void scheduleReconnect() {
        if (!autoReconnect || !plugin.isEnabled() || reconnectScheduled) return;
        reconnectScheduled = true;
        cfg.logInfo("ws-reconnecting", cfg.getReconnectDelay());
        reconnectTaskId = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            reconnectScheduled = false;
            reconnectTaskId = -1;
            if (autoReconnect && !connected) doConnect();
        }, cfg.getReconnectDelay() * 20L).getTaskId();
    }

    // -------------------- 消息发送 --------------------

    public void sendGroupMessage(long groupId, String message) {
        sender.sendGroupMessage(groupId, message);
    }

    public JSONObject sendActionSync(String action, JSONObject params, long timeout) {
        return sender.sendActionSync(action, params, timeout);
    }

    public String queryGroupMemberName(long groupId, long userId) {
        return sender.queryGroupMemberName(groupId, userId);
    }

    // ==================== 内部 ====================

    void ensureExecutor() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "Napcat-WS");
                t.setDaemon(true);
                return t;
            });
        }
    }

    private void cancelReconnectTask() {
        if (reconnectTaskId != -1) {
            Bukkit.getScheduler().cancelTask(reconnectTaskId);
            reconnectTaskId = -1;
        }
    }
}
