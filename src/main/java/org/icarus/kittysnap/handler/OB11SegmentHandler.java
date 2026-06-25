package org.icarus.kittysnap.handler;

import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.config.MessagesConfig;
import org.icarus.kittysnap.database.DatabaseManager;
import org.icarus.kittysnap.handler.handlers.BuildResult;
import org.icarus.kittysnap.handler.handlers.AtFormatter;
import org.icarus.kittysnap.handler.handlers.ImageHandler;
import org.icarus.kittysnap.handler.handlers.QQFaceMapper;
import org.icarus.kittysnap.handler.handlers.ReplyFormatter;
import org.icarus.kittysnap.napcat.NapcatWebSocketClient;
import org.icarus.kittysnap.onebotapi.*;

import java.util.List;

import static org.icarus.kittysnap.handler.Escape.esc;

/**
 * 每种类型的渲染规则：
 * <ul>
 *   <li>{@code text} → 原文（&lt; 已转义）</li>
 *   <li>{@code at} → {@code @群名片/QQ号}</li>
 *   <li>{@code face} → {@code [表情名]}</li>
 *   <li>{@code image} → {@code [图片]} / {@code [动画表情]}</li>
 *   <li>{@code json} → {@code [卡片消息]} </li>
 *   <li>{@code markdown} → {@code [Markdown消息]} (不会处理 markdown 消息，太过复杂)</li>
 *   <li>{@code reply} → 由 {@link ReplyFormatter} 最后处理，这个比较特殊</li>
 *   <li>未知 → {@code [其它消息]}</li>
 * </ul>
 */
public record OB11SegmentHandler(NapcatWebSocketClient napcatClient, ConfigurationManager cfg) {

    /**
     * 将单个段转为展示文本（已有转义处理）
     */
    public String handle(OB11Segment segment, long groupId) {
        MessagesConfig m = cfg.getMessages();
        return switch (segment) {
            case OB11MessageText t -> esc(t.getText());
            case OB11MessageAt at -> AtFormatter.handleAt(napcatClient, at, groupId, m);
            // TODO 更完善的表情列表
            case OB11MessageFace face -> "[" + QQFaceMapper.getName(face.getFaceId()) + "]";
            case OB11MessageImage img -> ImageHandler.handleImage(img, m);
            // TODO 卡片消息网址解析
            case OB11MessageJson ignored -> m.getSegmentCardText();
            case OB11MessageMarkdown ignored -> m.getSegmentMarkdownText();
            case OB11MessageUnknown ignored -> m.getSegmentUnknownText();
            default -> "";
        };
    }

    /**
     * 合并 reply，一并返回再完整展示文本
     */
    public BuildResult buildDisplay(List<OB11Segment> segments, long groupId, DatabaseManager db) {
        return ReplyFormatter.build(segments, groupId, db, this, cfg.getMessages());
    }
}
