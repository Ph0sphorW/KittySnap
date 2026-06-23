package org.icarus.kittySnap.napcat;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

/**
 * OneBot 标准消息对象模型 <p>
 * 用于解析 Napcat 通过 WebSocket 推送的 JSON 消息
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

    private String message;

    private long time;

    @JSONField(name = "message_seq")
    private String messageSeq;

    private Anonymous anonymous;

    private Sender sender;

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
                ", rawMessage='" + rawMessage + '\'' +
                '}';
    }
}
