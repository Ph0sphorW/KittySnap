package org.icarus.kittysnap.napcat.handler;

import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.config.MessagesConfig;
import org.icarus.kittysnap.database.DatabaseManager;
import org.icarus.kittysnap.utils.BuildResult;
import org.icarus.kittysnap.napcat.handler.handlers.AtFormatter;
import org.icarus.kittysnap.napcat.handler.handlers.CardHandler;
import org.icarus.kittysnap.napcat.handler.handlers.image.ImageHandler;
import org.icarus.kittysnap.napcat.handler.handlers.QQFaceMapper;
import org.icarus.kittysnap.napcat.handler.handlers.ReplyFormatter;
import org.icarus.kittysnap.napcat.NapcatWebSocketClient;
import org.icarus.kittysnap.napcat.onebot.*;

import java.util.List;

import static org.icarus.kittysnap.utils.Escaper.escape;

public record SegmentHandler(NapcatWebSocketClient napcatClient, ConfigurationManager cfg) {

    public String handle(OB11Segment segment, long groupId) {
        MessagesConfig config = cfg.getMessages();
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
            case OB11MessageForward ignored -> config.getSegment().getForwardText();
            default -> "";
        };
    }

    public BuildResult buildDisplay(List<OB11Segment> segments, long groupId, DatabaseManager db) {
        return ReplyFormatter.build(segments, groupId, db, this, cfg.getMessages(), cfg.getConfig());
    }
}
