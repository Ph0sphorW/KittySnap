package org.icarus.kittysnap.handler;

/**
 * OB11 段列表处理结果：被回复消息 ID + 最终展示文本。
 */
public record BuildResult(long replyMessageId, String displayContent) {
}
