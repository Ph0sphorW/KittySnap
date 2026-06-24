package org.icarus.kittysnap.napcat;

/** 待发送的群消息 */
record PendingMessage(long groupId, String message) {}
