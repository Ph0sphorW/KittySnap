package org.icarus.kittysnap.napcat.handler.handlers;

import org.icarus.kittysnap.config.KittySnapConfig;
import org.icarus.kittysnap.config.MessagesConfig;
import org.icarus.kittysnap.database.DatabaseManager;
import org.icarus.kittysnap.database.MessageRepository;
import org.icarus.kittysnap.napcat.handler.SegmentHandler;
import org.icarus.kittysnap.napcat.onebotapi.OB11MessageReply;
import org.icarus.kittysnap.napcat.onebotapi.OB11Segment;

import java.util.List;

/**
 * 输出: {@code [回复 发送者: 摘要] 回复内容}
 */
public class ReplyFormatter {

    /**
     * 提取第一条 reply 的消息 ID，没有返回 0
     */
    public static long extractReplyId(List<OB11Segment> segments) {
        for (var seg : segments) {
            if (seg instanceof OB11MessageReply r) return r.getReplyId();
        }
        return 0;
    }

    /**
     * 构建文本，查不到原消息时显示"[回复 未知消息]"
     */
    public static BuildResult build(List<OB11Segment> segments, long groupId,
                                    DatabaseManager db, SegmentHandler handler, MessagesConfig config, KittySnapConfig snapConfig) {
        long replyId = extractReplyId(segments);
        StringBuilder sb = new StringBuilder();
        for (var seg : segments) {
            if (!(seg instanceof OB11MessageReply)) {
                sb.append(handler.handle(seg, groupId));
            }
        }
        String content = sb.toString().trim();

        if (replyId > 0 && db != null) {
            content = lookupReplyPrefix(replyId, groupId, db, config, snapConfig) + " " + content;
        }
        return new BuildResult(replyId, content);
    }

    private static String lookupReplyPrefix(long replyMessageId,
                                            long groupId,
                                            DatabaseManager db,
                                            MessagesConfig config,
                                            KittySnapConfig snapConfig) {
        MessageRepository.OriginalMessage original = db.queryOriginalMessage(groupId, replyMessageId);
        if (original != null) {
            String sender = original.senderName().replace("<", "\\<");
            String summary = original.summary(snapConfig.getChatForward().getMaximumReplyingSummaryLength()).replace("<", "\\<");
            return config.getSegment().getReplyFormat().formatted(sender, summary);
        }
        return config.getSegment().getReplyUnknown();
    }
}
