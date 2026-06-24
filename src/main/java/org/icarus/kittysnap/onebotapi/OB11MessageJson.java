package org.icarus.kittysnap.onebotapi;

import java.util.Map;

/**
 * 卡片消息
 */
public class OB11MessageJson extends OB11SegmentBase {

    public static final String TYPE = "json";

    @SuppressWarnings("unused")
    public OB11MessageJson() {}

    public OB11MessageJson(Map<String, Object> data) {
        super(TYPE, data);
    }
}
