package org.icarus.kittysnap.handler.handlers;

import org.icarus.kittysnap.config.MessagesConfig;
import org.icarus.kittysnap.database.DatabaseManager;
import org.icarus.kittysnap.database.MessageRepository;
import org.icarus.kittysnap.handler.OB11SegmentHandler;
import org.icarus.kittysnap.onebotapi.OB11MessageReply;
import org.icarus.kittysnap.onebotapi.OB11Segment;

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
                                    DatabaseManager db, OB11SegmentHandler handler, MessagesConfig m) {
        long replyId = extractReplyId(segments);
        StringBuilder sb = new StringBuilder();
        for (var seg : segments) {
            if (!(seg instanceof OB11MessageReply)) {
                sb.append(handler.handle(seg, groupId));
            }
        }
        String content = sb.toString().trim();

        if (replyId > 0 && db != null) {
            content = lookupReplyPrefix(replyId, groupId, db, m) + " " + content;
        }
        return new BuildResult(replyId, content);
    }

    private static String lookupReplyPrefix(long replyMessageId, long groupId, DatabaseManager db, MessagesConfig m) {
        MessageRepository.OriginalMessage original = db.queryOriginalMessage(groupId, replyMessageId);
        if (original != null) {
            String sender = original.senderName().replace("<", "\\<");
            String summary = original.summary(6).replace("<", "\\<");
            return m.getSegmentReplyFormat().formatted(sender, summary);
        }
        return m.getSegmentReplyUnknown();
    }
}
