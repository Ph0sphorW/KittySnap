package org.icarus.kittysnap.handler.handlers;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.icarus.kittysnap.config.MessagesConfig;
import org.icarus.kittysnap.onebotapi.OB11MessageJson;

/**
 * 卡片消息处理器，提取内嵌 URL
 */
public class CardHandler {

    public static String handleJson(OB11MessageJson seg, MessagesConfig m) {
        String raw = seg.getDataString("data");
        if (raw == null || raw.isEmpty()) return m.getSegment().getCardText();

        String url = extractUrl(raw);
        if (url == null) return m.getSegment().getCardText();

        String safeUrl = url.replace("'", "\\'");
        String hover = url.length() <= 60 ? url : url.substring(0, 60) + "...";
        return "<click:open_url:'" + safeUrl + "'>"
                + "<hover:show_text:'<gray>" + hover + "</gray>'>"
                + m.getSegment().getCardText()
                + "</hover></click>";
    }

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
        for (String field : new String[]{"qqdocurl", "url", "jumpUrl", "link", "jump_url"}) {
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
