package org.icarus.kittysnap.napcat.onebot;

import java.util.Map;

/**
 * 需要注意，新版 QQ 不再提供 content 字段，因此无法梗概。
 */
public class OB11MessageForward extends OB11SegmentBase {

    public static final String TYPE = "forward";

    /**
     * fastjson2 反序列化用
     */
    @SuppressWarnings("unused")
    public OB11MessageForward() {
    }

    public OB11MessageForward(Map<String, Object> data) {
        super(TYPE, data);
    }
}
