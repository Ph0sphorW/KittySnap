package org.icarus.kittysnap.onebotapi;

import java.util.Map;

public class OB11MessageFace extends OB11SegmentBase {

    public static final String TYPE = "face";

    /**
     * fastjson2 反序列化用
     */
    @SuppressWarnings("unused")
    public OB11MessageFace() {
    }

    public OB11MessageFace(Map<String, Object> data) {
        super(TYPE, data);
    }

    /**
     * 获取表情 ID
     */
    public long getFaceId() {
        return getDataLong("id");
    }
}
