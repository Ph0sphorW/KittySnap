package org.icarus.kittysnap.napcat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.bukkit.plugin.java.JavaPlugin;
import org.icarus.kittysnap.config.ConfigurationManager;

import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

class WebsocketListener implements WebSocket.Listener {

    private final StringBuilder buf = new StringBuilder();
    private final NapcatWebSocketClient client;
    private final ConfigurationManager cfg;
    private final JavaPlugin plugin;
    private final MessageDispatcher dispatcher;
    private final ConcurrentHashMap<String, CompletableFuture<JSONObject>> pendingApiCalls;
    private final int truncateLen;

    WebsocketListener(NapcatWebSocketClient client, ConfigurationManager cfg, JavaPlugin plugin,
                      MessageDispatcher dispatcher,
                      ConcurrentHashMap<String, CompletableFuture<JSONObject>> pendingApiCalls) {
        this.client = client;
        this.cfg = cfg;
        this.plugin = plugin;
        this.dispatcher = dispatcher;
        this.pendingApiCalls = pendingApiCalls;
        this.truncateLen = cfg.getNapcatDebugTruncateLength();
    }

    @Override
    public void onOpen(WebSocket ws) {
        cfg.logInfo("websocket.opened");
        // 确保 executor 可用（安全网：万一 doConnect 时的 executor 后续被关闭）
        client.ensureExecutor();
        // ★ 关键：请求接收帧数据
        ws.request(1);
        plugin.getLogger().fine("[WS-LISTENER] onOpen: WebSocket 连接已打开，已请求第一帧数据");
    }

    @Override
    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
        buf.append(data);
        if (last) {
            String full = buf.toString();
            buf.setLength(0);

            if (!routeEcho(full)) {
                String preview = full.length() > truncateLen ? full.substring(0, truncateLen) + "..." : full;
                plugin.getLogger().fine("[WS-RECV] ← " + preview);

                client.ensureExecutor();
                ExecutorService exec = client.executor;
                if (exec != null && !exec.isShutdown()) {
                    exec.execute(() -> dispatcher.dispatch(full));
                } else {
                    plugin.getLogger().warning("[WS-RECV] executor 不可用，消息被丢弃");
                }
            }
        }

        ws.request(1);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket ws, int code, String reason) {
        client.connected = false;
        cfg.logInfo("websocket.closed", code, reason != null ? reason : "");
        plugin.getLogger().fine("[WS-LISTENER] onClose: WebSocket 已关闭, code=" + code);
        client.scheduleReconnect();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onError(WebSocket ws, Throwable error) {
        client.connected = false;
        // 清理 pending API 调用，避免 future 永不过期
        for (var entry : pendingApiCalls.entrySet()) {
            entry.getValue().completeExceptionally(error);
        }
        pendingApiCalls.clear();
        cfg.logWarning("websocket.error", error.getMessage());
        plugin.getLogger().log(Level.WARNING, "[WS-LISTENER] onError: " + error.getMessage() + " — 已触发重连", error);
        client.scheduleReconnect();
    }

    private boolean routeEcho(String json) {
        try {
            JSONObject root = JSON.parseObject(json);
            String echo = root.getString("echo");
            if (echo == null || echo.isEmpty()) return false;
            CompletableFuture<JSONObject> f = pendingApiCalls.remove(echo);
            if (f != null) {
                f.complete(root);
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
