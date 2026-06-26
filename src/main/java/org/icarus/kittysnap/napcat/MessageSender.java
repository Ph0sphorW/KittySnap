package org.icarus.kittysnap.napcat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.utils.PendingMessage;

import java.net.http.WebSocket;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class MessageSender {

    private final ConfigurationManager cfg;
    private final Queue<PendingMessage> pendingQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<String, CompletableFuture<JSONObject>> pendingApiCalls;
    private final Supplier<Boolean> connected;
    private final Supplier<WebSocket> webSocket;
    private final Supplier<ExecutorService> executor;

    public MessageSender(ConfigurationManager cfg,
                         ConcurrentHashMap<String, CompletableFuture<JSONObject>> pendingApiCalls,
                         Supplier<Boolean> connected,
                         Supplier<WebSocket> webSocket, Supplier<ExecutorService> executor) {
        this.cfg = cfg;
        this.pendingApiCalls = pendingApiCalls;
        this.connected = connected;
        this.webSocket = webSocket;
        this.executor = executor;
    }

    /**
     * 发送群消息，未连接则排队
     */
    public void sendGroupMessage(long groupId, String message) {
        if (connected.get() && webSocket.get() != null) {
            executor.get().execute(() -> doSend(groupId, message));
        } else {
            pendingQueue.add(new PendingMessage(groupId, message));
        }
    }

    private void doSend(long groupId, String message) {
        WebSocket ws = webSocket.get();
        if (!connected.get() || ws == null) {
            pendingQueue.add(new PendingMessage(groupId, message));
            return;
        }
        JSONObject payload = new JSONObject();
        payload.put("action", "send_group_msg");
        JSONObject params = new JSONObject();
        params.put("group_id", groupId);
        params.put("message", message);
        payload.put("params", params);
        ws.sendText(JSON.toJSONString(payload), true)
                .thenRun(() -> {
                    PendingMessage next = pendingQueue.poll();
                    if (next != null) doSend(next.groupId(), next.message());
                })
                .exceptionally(ex -> {
                    cfg.logWarning("websocket.send-failed", groupId, ex.getMessage());
                    return null;
                });
    }

    /**
     * 一并发送积压的待发消息
     */
    public void flushPending() {
        PendingMessage pm;
        while ((pm = pendingQueue.poll()) != null) {
            doSend(pm.groupId(), pm.message());
        }
    }

    /**
     * 同步 API 调用等待响应
     */
    public JSONObject sendActionSync(String action, JSONObject params, long timeout) {
        WebSocket ws = webSocket.get();
        if (!connected.get() || ws == null) return null;
        String echo = UUID.randomUUID().toString();
        CompletableFuture<JSONObject> future = new CompletableFuture<>();
        pendingApiCalls.put(echo, future);

        JSONObject payload = new JSONObject();
        payload.put("action", action);
        if (params != null) payload.put("params", params);
        payload.put("echo", echo);

        try {
            ws.sendText(JSON.toJSONString(payload), true);
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            pendingApiCalls.remove(echo);
            return null;
        }
    }

    /**
     * 查询群成员群名片 / 昵称
     */
    public String queryGroupMemberName(long groupId, long userId) {
        JSONObject params = new JSONObject();
        params.put("group_id", groupId);
        params.put("user_id", userId);
        JSONObject resp = sendActionSync("get_group_member_info", params, 3000);
        if (resp == null) return null;
        JSONObject data = resp.getJSONObject("data");
        if (data == null) return null;
        String card = data.getString("card");
        return (card != null && !card.isEmpty()) ? card : data.getString("nickname");
    }

    public ConcurrentHashMap<String, CompletableFuture<JSONObject>> pendingApiCalls() {
        return pendingApiCalls;
    }
}
