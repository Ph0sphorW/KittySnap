package org.icarus.kittysnap.napcat.handler;

import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.config.MessagesConfig;
import org.icarus.kittysnap.config.KittySnapConfig;
import org.icarus.kittysnap.database.DatabaseManager;
import org.icarus.kittysnap.napcat.handler.handlers.BuildResult;
import org.icarus.kittysnap.napcat.handler.handlers.AtFormatter;
import org.icarus.kittysnap.napcat.handler.handlers.CardHandler;
import org.icarus.kittysnap.napcat.handler.handlers.image.ImageHandler;
import org.icarus.kittysnap.napcat.handler.handlers.QQFaceMapper;
import org.icarus.kittysnap.napcat.handler.handlers.ReplyFormatter;
import org.icarus.kittysnap.napcat.NapcatWebSocketClient;
import org.icarus.kittysnap.napcat.onebotapi.*;

import java.util.List;

import static org.icarus.kittysnap.napcat.handler.Escaper.escape;
import static org.icarus.kittysnap.napcat.handler.Escaper.hoverSafe;

public record SegmentHandler(NapcatWebSocketClient napcatClient, ConfigurationManager cfg) {

    public String handle(OB11Segment segment, long groupId) {
        MessagesConfig config = cfg.getMessages();
        KittySnapConfig snapConfig = cfg.getConfig();
        return switch (segment) {
            case OB11MessageText t -> escape(t.getText());
            case OB11MessageAt at -> AtFormatter.handleAt(napcatClient, at, groupId, config);
            case OB11MessageFace face -> {
                String fn = QQFaceMapper.getName(face.getFaceId());
                yield fn != null ? "[" + fn + "]" : "[表情" + face.getFaceId() + "]";
            }
            case OB11MessageImage img -> ImageHandler.handleImage(img, config, cfg.getConfig());
            case OB11MessageJson json -> CardHandler.handleJson(json, config);
            case OB11MessageMarkdown ignored -> config.getSegment().getMarkdownText();
            case OB11MessageUnknown ignored -> config.getSegment().getUnknownText();
            // 新版 QQ 对原生转发消息不再附加 content，因此无法梗概
            case OB11MessageForward ignored -> config.getSegment().getForwardText();
            default -> "";
        };
    }

    /** 将段列表渲染为纯文本（无 MiniMessage 标签），适合嵌入 hover */
    public String buildContent(List<OB11Segment> segments, long groupId) {
        StringBuilder sb = new StringBuilder();
        for (OB11Segment seg : segments) {
            if (seg instanceof OB11MessageText t) {
                String txt = t.getText();
                if (txt != null) sb.append(hoverSafe(txt));
            } else if (seg instanceof OB11MessageFace face) {
                String fn = QQFaceMapper.getName(face.getFaceId());
                sb.append("[").append(fn != null ? fn : "表情" + face.getFaceId()).append("]");
            } else {
                sb.append("[消息]");
            }
        }
        String text = sb.toString().trim();
        return text.isEmpty() ? "(消息)" : text;
    }

    public BuildResult buildDisplay(List<OB11Segment> segments, long groupId, DatabaseManager db) {
        return ReplyFormatter.build(segments, groupId, db, this, cfg.getMessages(), cfg.getConfig());
    }
}
