package org.icarus.kittysnap.utils;

/**
 * 待发送的群消息
 */
public record PendingMessage(long groupId, String message) {
}
