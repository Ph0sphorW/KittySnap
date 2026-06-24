package org.icarus.kittysnap.napcat.ob11;

import java.util.Map;

/**
 * 图片消息段
 * <p>
 * data 字段: {"file": "文件名", "url": "图片URL", "summary": "图片摘要"}
 */
public class OB11MessageImage extends OB11SegmentBase {

    public static final String TYPE = "image";

    /** fastjson2 反序列化用 */
    @SuppressWarnings("unused")
    public OB11MessageImage() {}

    public OB11MessageImage(Map<String, Object> data) {
        super(TYPE, data);
    }

    /** 获取图片文件名 */
    public String getFile() {
        return getDataString("file");
    }

    /** 获取图片 URL */
    public String getUrl() {
        return getDataString("url");
    }

    /** 获取图片摘要（Napcat 扩展字段） */
    public String getSummary() {
        return getDataString("summary");
    }
}
