package org.icarus.kittysnap.napcat.handler.handlers;

import org.icarus.kittysnap.config.KittySnapConfig;
import org.icarus.kittysnap.config.MessagesConfig;
import org.icarus.kittysnap.database.DatabaseManager;
import org.icarus.kittysnap.utils.OriginalMessage;
import org.icarus.kittysnap.napcat.handler.SegmentHandler;
import org.icarus.kittysnap.napcat.onebot.OB11MessageReply;
import org.icarus.kittysnap.napcat.onebot.OB11Segment;
import org.icarus.kittysnap.utils.BuildResult;
import org.icarus.kittysnap.utils.HandleResult;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
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
        List<Component> clickComponents = null;
        for (var seg : segments) {
            if (!(seg instanceof OB11MessageReply)) {
                HandleResult r = handler.handle(seg, groupId, false);
                sb.append(r.text());
                if (r.clickComponents() != null) {
                    if (clickComponents == null) clickComponents = new ArrayList<>();
                    clickComponents.addAll(r.clickComponents());
                }
            }
        }
        String content = sb.toString().trim();

        if (replyId > 0 && db != null) {
            content = lookupReplyPrefix(replyId, groupId, db, config, snapConfig) + " " + content;
        }
        return new BuildResult(replyId, content, clickComponents);
    }

    private static String lookupReplyPrefix(long replyMessageId,
                                            long groupId,
                                            DatabaseManager db,
                                            MessagesConfig config,
                                            KittySnapConfig snapConfig) {
        OriginalMessage original = db.queryOriginalMessage(groupId, replyMessageId);
        if (original != null) {
            String sender = original.senderName().replace("<", "\\<");
            String summary = original.summary(snapConfig.getChatForward().getMaximumReplyingSummaryLength()).replace("<", "\\<");
            return config.getSegment().getReplyFormat().formatted(sender, summary);
        }
        return config.getSegment().getReplyUnknown();
    }
}
