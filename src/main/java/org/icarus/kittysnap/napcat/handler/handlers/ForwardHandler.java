package org.icarus.kittysnap.napcat.handler.handlers;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.icarus.kittysnap.config.KittySnapConfig;
import org.icarus.kittysnap.config.MessagesConfig;
import org.icarus.kittysnap.napcat.handler.SegmentHandler;
import org.icarus.kittysnap.napcat.onebot.OB11MessageForward;
import org.icarus.kittysnap.napcat.onebot.OB11Segment;
import org.icarus.kittysnap.napcat.onebot.OB11SegmentBase;

import java.util.List;

/**
 *
 */
public class ForwardHandler {
    public static String handleForward(OB11MessageForward forwardedMessage,
                                       long groupId,
                                       SegmentHandler handler,
                                       MessagesConfig messages,
                                       KittySnapConfig snapConfig) {
        int previewLines = snapConfig.getChatForward().getMaximumForwardingSummaryLines();

        String id = forwardedMessage.getDataString("id");
        if (id == null || id.isEmpty()) return messages.getSegment().getForwardText();

        JSONArray msgs = (JSONArray) forwardedMessage.getData().get("content");
        if (msgs == null || msgs.isEmpty()) return messages.getSegment().getForwardText();

        // 构建 hover 预览：发送者昵称: 内容摘要（每行一条）
        StringBuilder preview = new StringBuilder();
        int limit = Math.min(msgs.size(), previewLines);
        for (int i = 0; i < limit; i++) {
            StringBuilder line = new StringBuilder();
            JSONObject message = msgs.getJSONObject(i);
            JSONObject sender = message.getJSONObject("sender");
            String name = sender != null ? sender.getString("nickname") : "QQ用户";
            line.append("<#0099FF>")
                    .append(name)
                    .append("</#0099FF>: ");
            String displayMessage = renderContent(message.getJSONArray("message"), groupId, handler);
            line.append("<gray>")
                    .append(displayMessage)
                    .append("</gray>");
            if ( i != limit - 1) line.append("\n");
            preview.append(line);
        }
        if (msgs.size() > previewLines) {
            preview.append("\n<gray>... 共").append(msgs.size()).append("条消息</gray>");
        }

        return "<hover:show_text:'" + preview + "'>" + messages.getSegment().getForwardText() + "</hover>";
    }

    /**
     * 将 OB11 segment 数组渲染为一行摘要文本
     */
    private static String renderContent(JSONArray content, long groupId, SegmentHandler handler) {
        if (content == null || content.isEmpty()) return "";
        List<OB11Segment> segments = OB11SegmentBase.fromJSONArray(content);
        StringBuilder sb = new StringBuilder();
        for (OB11Segment s : segments) {
            sb.append(handler.handle(s, groupId, true));
        }
        return sb.toString().trim();
    }
}
