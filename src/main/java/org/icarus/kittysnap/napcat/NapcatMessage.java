package org.icarus.kittysnap.napcat;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.icarus.kittysnap.napcat.ob11.OB11MessageText;
import org.icarus.kittysnap.napcat.ob11.OB11Segment;
import org.icarus.kittysnap.napcat.ob11.OB11SegmentBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OneBot 标准消息对象模型 <p>
 * 用于解析 Napcat 通过 WebSocket 推送的 JSON 消息。
 * <p>
 * 本对象持有 {@link #segments} 字段，即 OB11 标准消息段数组"message"的解析结果，
 * 由 {@link #parseMessageSegments(JSONObject)} 从原始 JSON 中手动解析并填充。
 */
@Getter
@Setter
public class NapcatMessage {

    @JSONField(name = "post_type")
    private String postType;

    @JSONField(name = "message_type")
    private String messageType;

    @JSONField(name = "sub_type")
    private String subType;

    @JSONField(name = "message_id")
    private long messageId;

    @JSONField(name = "group_id")
    private long groupId;

    /** 发送者 QQ 号（OneBot 字段: user_id） */
    @JSONField(name = "user_id")
    private long senderId;

    /** 发送者昵称（OneBot 字段: nickname） */
    @JSONField(name = "nickname")
    private String senderNickname;

    @JSONField(name = "raw_message")
    private String rawMessage;

    /**
     * OB11 标准消息段列表，从 JSON 的 "message" 数组解析而来。
     * 不由 fastjson2 自动反序列化（"message" 在 JSON 中可能是数组或字符串），
     * 由 {@link #parseMessageSegments(JSONObject)} 手动填充。
     */
    private List<OB11Segment> segments = Collections.emptyList();

    private long time;

    @JSONField(name = "message_seq")
    private String messageSeq;

    private Anonymous anonymous;

    private Sender sender;

    // ========== OB11 消息段解析 ==========

    /**
     * 从 OneBot JSON 根对象的 "message" 字段中解析 OB11 消息段列表，并设置到本对象中。
     * <p>
     * "message" 字段有两种可能格式：
     * <ul>
     *   <li>JSON 数组 {@code [{"type":"text","data":{"text":"hi"}}]} — 标准 OB11 数组格式</li>
     *   <li>字符串 {@code "hi"} — 仅含纯文本的简写格式</li>
     * </ul>
     * 此方法兼容处理两种格式，对于字符串格式会包装为单个 text 段。
     *
     * @param root OneBot JSON 根对象
     */
    public void parseMessageSegments(JSONObject root) {
        Object messageField = root.get("message");
        if (messageField == null) {
            this.segments = Collections.emptyList();
            return;
        }

        // 处理字符串格式: "message": "hello" → 包装为一个 text 段
        if (messageField instanceof String text) {
            this.segments = Collections.singletonList(
                    new OB11MessageText(Collections.singletonMap("text", text))
            );
            return;
        }

        // 处理数组格式: "message": [{"type":"text","data":{"text":"hello"}}]
        if (messageField instanceof JSONArray arr) {
            List<OB11Segment> list = new ArrayList<>(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                JSONObject segObj = arr.getJSONObject(i);
                if (segObj != null) {
                    OB11SegmentBase seg = OB11SegmentBase.fromJSON(segObj);
                    if (seg != null) list.add(seg);
                }
            }
            this.segments = Collections.unmodifiableList(list);
            return;
        }

        // 兜底：未知类型
        this.segments = Collections.emptyList();
    }

    // ========== 内部类 ==========

    @Getter
    @Setter
    public static class Anonymous {
        private long id;
        private String name;
        @JSONField(name = "flag")
        private String uniqueIdentifier;

        @Override
        public String toString() {
            return "Anonymous{id=" + id + ", name='" + name + "'}";
        }
    }

    @Getter
    @Setter
    public static class Sender {
        @JSONField(name = "user_id")
        private long userId;
        private String nickname;
        private String card;
        private String role;

        /**
         * 获取展示名称：优先群名片，其次昵称
         */
        public String getDisplayName() {
            return (card != null && !card.isEmpty()) ? card : nickname;
        }

        public boolean isOwner() {
            return "owner".equals(role);
        }

        public boolean isAdmin() {
            return "admin".equals(role) || isOwner();
        }

        @Override
        public String toString() {
            return "Sender{userId=" + userId + ", displayName='" + getDisplayName() + "', role='" + role + "'}";
        }
    }

    // ========== 快捷方法 ==========

    /**
     * 判断是否为群消息
     */
    public boolean isGroupMessage() {
        return "message".equals(postType) && "group".equals(messageType);
    }

    /**
     * 判断是否为指定群的消息
     */
    public boolean isFromGroup(long groupId) {
        return isGroupMessage() && this.groupId == groupId;
    }

    /**
     * 判断是否为指定群的消息（可变参，任一匹配即可）
     */
    public boolean isFromAnyGroup(long... groupIds) {
        if (!isGroupMessage()) return false;
        for (long gid : groupIds) {
            if (this.groupId == gid) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "NapcatMessage{" +
                "postType='" + postType + '\'' +
                ", messageType='" + messageType + '\'' +
                ", groupId=" + groupId +
                ", userId=" + senderId +
                ", segments=" + segments +
                '}';
    }
}
