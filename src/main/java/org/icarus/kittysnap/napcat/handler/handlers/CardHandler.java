package org.icarus.kittysnap.napcat.handler.handlers;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.icarus.kittysnap.config.MessagesConfig;
import org.icarus.kittysnap.napcat.onebot.OB11MessageJson;

/**
 * 卡片消息处理器，提取内嵌 URL
 */
public class CardHandler {

    public static String handleJson(OB11MessageJson segment, MessagesConfig config) {
        String raw = segment.getDataString("data");
        if (raw == null || raw.isEmpty()) return config.getSegment().getCardText();

        String url = extractUrl(raw);
        if (url == null) return config.getSegment().getCardText();

        String safeUrl = url.replace("'", "\\'");
        String hover = url.length() <= 60 ? url : url.substring(0, 60) + "...";
        return "<click:open_url:" + safeUrl + ">" + "<hover:show_text:'<gray>" + config.getSegment().getCardPreviewHover() + hover + "</gray>'>" + config.getSegment().getCardText() + "</hover></click>";
    }

    private static final String[] URL_FIELDS = {"qqdocurl", "url", "jumpUrl", "link", "jump_url"};

    /**
     * 从卡片 JSON 字符串中提取第一个可用的链接
     */
    private static String extractUrl(String raw) {
        try {
            JSONObject root = JSON.parseObject(raw);
            JSONObject meta = root.getJSONObject("meta");
            if (meta == null) return tryDirectUrl(root);

            // 遍历 URL
            for (String key : meta.keySet()) {
                JSONObject obj = meta.getJSONObject(key);
                if (obj == null) continue;

                String url = tryPickUrl(obj);
                if (url != null) return url;
            }

            return tryDirectUrl(root);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String tryPickUrl(JSONObject obj) {
        for (String field : URL_FIELDS) {
            String v = obj.getString(field);
            if (v != null && (v.startsWith("http://") || v.startsWith("https://"))) return v;
        }
        return null;
    }

    /**
     * 尝试从根对象直接取 url 字段
     */
    private static String tryDirectUrl(JSONObject root) {
        return tryPickUrl(root);
    }
}
