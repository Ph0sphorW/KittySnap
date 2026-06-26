package org.icarus.kittysnap.napcat.onebot;

import java.util.Map;

public class OB11MessageImage extends OB11SegmentBase {

    public static final String TYPE = "image";

    /**
     * fastjson2 反序列化用
     */
    @SuppressWarnings("unused")
    public OB11MessageImage() {
    }

    public OB11MessageImage(Map<String, Object> data) {
        super(TYPE, data);
    }

    /**
     * 获取图片 URL
     */
    public String getUrl() {
        return getDataString("url");
    }

    /**
     * 获取图片摘要
     */
    public String getSummary() {
        return getDataString("summary");
    }
}
