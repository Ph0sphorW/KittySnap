package org.icarus.kittysnap.onebotapi;

import java.util.Map;

/**
 * JSON / 卡片消息段
 * <p>
 * data 字段: {"data": "卡片JSON内容"}
 */
public class OB11MessageJson extends OB11SegmentBase {

    public static final String TYPE = "json";

    public OB11MessageJson() {}

    public OB11MessageJson(Map<String, Object> data) {
        super(TYPE, data);
    }
}
