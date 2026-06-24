package org.icarus.kittysnap.napcat.ob11.handler;

import org.icarus.kittysnap.database.DatabaseManager;
import org.icarus.kittysnap.napcat.NapcatWebSocketClient;
import org.icarus.kittysnap.napcat.ob11.*;

import java.util.List;

/**
 * OB11 消息段 → MiniMessage 展示文本 处理器。
 * <p>
 * 每种段类型的渲染规则：
 * <ul>
 *   <li>{@code text} → 原文（&lt; 已转义）</li>
 *   <li>{@code at} → {@code @群名片/QQ号}</li>
 *   <li>{@code face} → {@code [表情名]}</li>
 *   <li>{@code image} → {@code [图片]} / {@code [动画表情]}</li>
 *   <li>{@code json} → {@code [卡片消息]}</li>
 *   <li>{@code markdown} → {@code [Markdown消息]}</li>
 *   <li>{@code reply} → 由 {@link ReplyFormatter} 统一处理</li>
 *   <li>未知 → {@code [其它消息]}</li>
 * </ul>
 */
public class OB11SegmentHandler {

    private final NapcatWebSocketClient napcatClient;

    public OB11SegmentHandler(NapcatWebSocketClient napcatClient) {
        this.napcatClient = napcatClient;
    }

    /** 将单个段转为展示文本（已有转义处理） */
    public String handle(OB11Segment segment, long groupId) {
        return switch (segment) {
            case OB11MessageText t -> esc(t.getText());
            case OB11MessageAt at -> handleAt(at, groupId);
            case OB11MessageFace face -> "[" + FaceMapper.getName(face.getFaceId()) + "]";
            case OB11MessageImage img -> handleImage(img);
            case OB11MessageReply r -> "";
            case OB11MessageJson j -> "[卡片消息]";
            case OB11MessageMarkdown m -> "[Markdown消息]";
            case OB11MessageUnknown u -> "[其它消息]";
            default -> "";
        };
    }

    /** 处理段列表，自动合并 reply 前缀，返回完整展示文本 */
    public BuildResult buildDisplay(List<OB11Segment> segments, long groupId, DatabaseManager db) {
        return ReplyFormatter.build(segments, groupId, db, this);
    }

    // ==================== 内部 ====================

    private String handleAt(OB11MessageAt at, long groupId) {
        if (at.isAtAll()) return "@全体成员";
        String target = at.getTarget();
        if (target == null || target.isEmpty()) return "";

        long uid = at.getTargetUserId();
        if (uid > 0) {
            String name = napcatClient.queryGroupMemberName(groupId, uid);
            if (name != null) return "@" + esc(name);
        }
        return "@" + target;
    }

    private static String handleImage(OB11MessageImage img) {
        String s = img.getSummary();
        return (s != null && !s.isEmpty()) ? "[动画表情]" : "[图片]";
    }

    private static String esc(String s) {
        return s != null ? s.replace("<", "\\<") : "";
    }
}
