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

/** Napcat WebSocket 事件监听器（处理 onOpen/onText/onClose/onError） */
class WsListener implements WebSocket.Listener {

    private final StringBuilder buf = new StringBuilder();
    private final NapcatWebSocketClient client;
    private final ConfigurationManager cfg;
    private final JavaPlugin plugin;
    private final MessageDispatcher dispatcher;
    private final ConcurrentHashMap<String, CompletableFuture<JSONObject>> pendingApiCalls;

    WsListener(NapcatWebSocketClient client, ConfigurationManager cfg, JavaPlugin plugin,
               MessageDispatcher dispatcher,
               ConcurrentHashMap<String, CompletableFuture<JSONObject>> pendingApiCalls) {
        this.client = client;
        this.cfg = cfg;
        this.plugin = plugin;
        this.dispatcher = dispatcher;
        this.pendingApiCalls = pendingApiCalls;
    }

    @Override
    public void onOpen(WebSocket ws) {
        cfg.logInfo("ws-opened");
        client.onWsOpen();
        ws.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
        buf.append(data);
        if (!last) return null;

        String full = buf.toString();
        buf.setLength(0);

        if (routeEcho(full)) return null;

        plugin.getLogger().info("[WS-RECV] ← " + (full.length() > 300 ? full.substring(0, 300) + "..." : full));
        ExecutorService exec = client.executor();
        if (exec != null && !exec.isShutdown()) {
            exec.execute(() -> dispatcher.dispatch(full));
        } else {
            plugin.getLogger().warning("[WS-RECV] executor 不可用，消息被丢弃");
        }
        return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket ws, int code, String reason) {
        client.onWsClosed();
        cfg.logInfo("ws-closed", code, reason != null ? reason : "");
        client.scheduleReconnect();
        return null;
    }

    @Override
    public void onError(WebSocket ws, Throwable error) {
        cfg.logWarning("ws-error", error.getMessage());
        client.onWsError(error.getMessage());
    }

    private boolean routeEcho(String json) {
        try {
            JSONObject root = JSON.parseObject(json);
            String echo = root.getString("echo");
            if (echo == null || echo.isEmpty()) return false;
            CompletableFuture<JSONObject> f = pendingApiCalls.remove(echo);
            if (f != null) { f.complete(root); return true; }
        } catch (Exception ignored) {}
        return false;
    }
}
