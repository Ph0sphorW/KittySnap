package org.icarus.kittysnap.napcat.ob11;

import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * OB11 消息段基类，持有 type 和 data
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
     * 根据 JSONObject 创建对应类型的 OB11Segment 实例
     *
     * @param obj fastjson2 JSONObject，包含 type 和 data 字段
     * @return 对应的 OB11Segment 子类实例
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
            default -> new OB11MessageUnknown(type, data);
        };
    }

    @Override
    public String toString() {
        return "OB11Segment{type='" + type + "', data=" + data + "}";
    }
}
