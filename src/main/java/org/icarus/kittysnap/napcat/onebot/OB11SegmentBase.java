package org.icarus.kittysnap.napcat.onebot;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OB11 消息段基类
 */
@Getter
@Setter
public abstract class OB11SegmentBase implements OB11Segment {

    private String type;
    private Map<String, Object> data;

    protected OB11SegmentBase() {}

    protected OB11SegmentBase(String type, Map<String, Object> data) {
        this.type = type;
        this.data = data != null ? data : Collections.emptyMap();
    }

    /**
     * 分类器
     */
    public static OB11SegmentBase fromJSON(JSONObject obj) {
        if (obj == null) return null;
        String type = obj.getString("type");
        if (type == null) return null;

        JSONObject dataObj = obj.getJSONObject("data");
        Map<String, Object> data = dataObj != null ? new HashMap<>(dataObj) : new HashMap<>();

        return switch (type) {
            case "text" -> new OB11MessageText(data);
            case "reply" -> new OB11MessageReply(data);
            case "face" -> new OB11MessageFace(data);
            case "at" -> new OB11MessageAt(data);
            case "image" -> new OB11MessageImage(data);
            case "json" -> new OB11MessageJson(data);
            case "markdown" -> new OB11MessageMarkdown(data);
            case "forward" -> new OB11MessageForward(data);
            case "file" -> new OB11MessageFile(data);
            default -> new OB11MessageUnknown(type, data);
        };
    }

    /** 将 JSONArray 中的段定义批量解析为 OB11Segment 列表 */
    public static List<OB11Segment> fromJSONArray(JSONArray arr) {
        if (arr == null || arr.isEmpty()) return Collections.emptyList();
        List<OB11Segment> list = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            OB11SegmentBase seg = fromJSON(arr.getJSONObject(i));
            if (seg != null) list.add(seg);
        }
        return list;
    }

    @Override
    public String toString() {
        return "OB11Segment{type='" + type + "', data=" + data + "}";
    }
}
