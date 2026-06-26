package org.icarus.kittysnap.utils;

/**
 * 原始消息查询结果
 */
public record OriginalMessage(String senderName, String rawMessage) {
    /**
     * 用于展示的摘要文本，maxLen 为最大长度
     */
    public String summary(int maxLen) {
        if (rawMessage == null || rawMessage.isEmpty()) return "";
        if (rawMessage.length() <= maxLen) return rawMessage;
        return rawMessage.substring(0, maxLen) + "...";
    }
}
