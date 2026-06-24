package org.icarus.kittysnap.napcat.ob11;

import java.util.Map;

/**
 * 未知类型消息段（兜底用）
 * <p>
 * 当接收到 OB11 标准中未定义的 segment type 时使用此类型存储原始 type 和 data。
 */
public class OB11MessageUnknown extends OB11SegmentBase {

    /** fastjson2 反序列化用 */
    @SuppressWarnings("unused")
    public OB11MessageUnknown() {}

    public OB11MessageUnknown(String type, Map<String, Object> data) {
        super(type, data);
    }
}
