package org.icarus.kittysnap.napcat.onebot;

import java.util.Map;

/**
 * 未知类型消息
 * <p>
 * 有些没做的也会识别成未知
 */
public class OB11MessageUnknown extends OB11SegmentBase {

    /**
     * fastjson2 反序列化用
     */
    @SuppressWarnings("unused")
    public OB11MessageUnknown() {
    }

    public OB11MessageUnknown(String type, Map<String, Object> data) {
        super(type, data);
    }
}
