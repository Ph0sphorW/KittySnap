package org.icarus.kittysnap.napcat.onebot;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OneBot 标准消息对象模型
 */
@Getter
@Setter
public class OB11Message {

    @JSONField(name = "post_type")
    private String postType;

    @JSONField(name = "message_type")
    private String messageType;

    @JSONField(name = "message_id")
    private long messageId;

    @JSONField(name = "group_id")
    private long groupId;

    /**
     * 发送者 QQ 号，user_id
     */
    @JSONField(name = "user_id")
    private long senderId;

    @JSONField(name = "raw_message")
    private String rawMessage;

    /**
     * 消息片段列表
     * 不走 json 序列化，走下面解析
     */
    private List<OB11Segment> segments = Collections.emptyList();

    private long time;

    @JSONField(name = "message_seq")
    private String messageSeq;

    private Sender sender;

    // ---------- 消息片段解析 ----------

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
        switch (messageField) {
            case null -> {
                this.segments = Collections.emptyList();
                return;
            }
            // 包装为 text 段
            case String text -> {
                this.segments = Collections.singletonList(
                        new OB11MessageText(Collections.singletonMap("text", text))
                );
                return;
            }
            // 数组格式
            case JSONArray arr -> {
                List<OB11Segment> list = getOB11Segments(arr);
                this.segments = Collections.unmodifiableList(list);
                return;
            }
            default -> {
            }
        }

        // 未知类型处理
        this.segments = Collections.emptyList();
    }

    public static @NonNull List<OB11Segment> getOB11Segments(JSONArray arr) {
        List<OB11Segment> list = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            JSONObject segObj = arr.getJSONObject(i);
            if (segObj != null) {
                OB11SegmentBase seg = OB11SegmentBase.fromJSON(segObj);
                if (seg != null) list.add(seg);
            }
        }
        return list;
    }

    // ---------- 内部类 ----------

    @Getter
    @Setter
    public static class Sender {
        @JSONField(name = "user_id")
        private long userId;
        private String nickname;
        private String card;
        private String role;

        /**
         * 优先群名片，其次昵称
         */
        public String getDisplayName() {
            return (card != null && !card.isEmpty()) ? card : nickname;
        }

        @Override
        public String toString() {
            return "Sender{userId=" + userId + ", displayName='" + getDisplayName() + "', role='" + role + "'}";
        }
    }

    /**
     * 脑子没病的话，不要用这个
     *
     * @return 神必的 NapcatMessage 文本对象
     */
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
