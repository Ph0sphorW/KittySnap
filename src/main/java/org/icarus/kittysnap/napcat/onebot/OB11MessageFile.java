package org.icarus.kittysnap.napcat.onebot;

import java.util.Map;

/**
 * 文件类型，仅解析文件名
 */
public class OB11MessageFile extends OB11SegmentBase {

    public static final String TYPE = "file";

    /**
     * fastjson2 反序列化用
     */
    @SuppressWarnings("unused")
    public OB11MessageFile() {
    }

    public OB11MessageFile(Map<String, Object> data) {
        super(TYPE, data);
    }

    public String getFileName() {
        return this.getData().get("file").toString();
    }
}
