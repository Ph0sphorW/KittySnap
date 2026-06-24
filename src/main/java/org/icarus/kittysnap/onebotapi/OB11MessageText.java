package org.icarus.kittysnap.onebotapi;

import java.util.Map;

public class OB11MessageText extends OB11SegmentBase {

    public static final String TYPE = "text";

    /**
     * fastjson2 反序列化用
     */
    @SuppressWarnings("unused")
    public OB11MessageText() {
    }

    public OB11MessageText(Map<String, Object> data) {
        super(TYPE, data);
    }

    /**
     * 获取文本内容
     */
    public String getText() {
        return getDataString("text");
    }
}
