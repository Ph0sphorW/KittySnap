package org.icarus.kittysnap.napcat;

/**
 * 群号 → 监听器 的绑定关系。
 */
public record GroupEntry(long groupId, IGroupMessageListener listener) {}
