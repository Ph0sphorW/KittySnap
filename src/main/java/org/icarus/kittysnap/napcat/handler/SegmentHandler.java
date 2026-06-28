package org.icarus.kittysnap.napcat.handler;

import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.config.MessagesConfig;
import org.icarus.kittysnap.config.KittySnapConfig;
import org.icarus.kittysnap.database.DatabaseManager;
import org.icarus.kittysnap.napcat.handler.handlers.*;
import org.icarus.kittysnap.utils.BuildResult;
import org.icarus.kittysnap.utils.HandleResult;
import org.icarus.kittysnap.napcat.handler.handlers.image.ImageHandler;
import org.icarus.kittysnap.napcat.NapcatWebSocketClient;
import org.icarus.kittysnap.napcat.onebot.*;

import java.util.ArrayList;
import java.util.List;

import static org.icarus.kittysnap.utils.Escaper.escape;

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
public record SegmentHandler(NapcatWebSocketClient napcatClient, ConfigurationManager cfg) {

    /**
     * 将单个段转为展示文本（已有转义处理）
     */
    public HandleResult handle(OB11Segment segment, long groupId, boolean inForwarding) {
        MessagesConfig messages = cfg.getMessages();
        KittySnapConfig snapConfig = cfg.getConfig();
        return switch (segment) {
            case OB11MessageText t -> new HandleResult(escape(t.getText()));
            case OB11MessageAt at -> new HandleResult(AtFormatter.handleAt(napcatClient, at, groupId, messages));
            case OB11MessageFace face -> {
                String fn = QQFaceMapper.getName(face.getFaceId());
                yield new HandleResult(fn != null ? "[" + fn + "]" : "[表情" + face.getFaceId() + "]");
            }
            case OB11MessageImage img -> inForwarding
                    ? new HandleResult(messages.getSegment().getImageText())
                    : ImageHandler.handleImage(img, cfg);
            case OB11MessageJson json -> new HandleResult(inForwarding
                    ? messages.getSegment().getCardText()
                    : CardHandler.handleJson(json, messages));
            case OB11MessageForward forward -> new HandleResult(inForwarding
                    ? messages.getSegment().getForwardText()
                    : ForwardHandler.handleForward(forward, groupId, this, messages, snapConfig));
            case OB11MessageFile file -> new HandleResult(FileHandler.fileHandler(file, messages));
            case OB11MessageMarkdown ignored -> new HandleResult(messages.getSegment().getMarkdownText());
            case OB11MessageUnknown ignored -> new HandleResult(messages.getSegment().getUnknownText());
            default -> new HandleResult("");
        };
    }

    /**
     * 合并 reply，一并返回再完整展示文本
     */
    public BuildResult buildDisplay(List<OB11Segment> segments, long groupId, DatabaseManager db) {
        return ReplyFormatter.build(segments, groupId, db, this, cfg.getMessages(), cfg.getConfig());
    }
}
