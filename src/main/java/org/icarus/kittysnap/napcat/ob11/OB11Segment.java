package org.icarus.kittysnap.napcat.ob11;

import java.util.Map;

/**
 * OneBot 11 标准消息段接口
 * <p>
 * 每个消息段包含 type（段类型）和 data（段数据）。
 */
public interface OB11Segment {

    /** 段类型，如 "text"、"reply"、"face"、"at"、"image" */
    String getType();

    /** 段数据，键值对形式 */
    Map<String, Object> getData();

    /** 从 data 中获取指定键的字符串值 */
    default String getDataString(String key) {
        Object v = getData() != null ? getData().get(key) : null;
        return v != null ? v.toString() : null;
    }

    /** 从 data 中获取指定键的 long 值 */
    default long getDataLong(String key) {
        Object v = getData() != null ? getData().get(key) : null;
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
        }
        return 0;
    }
}
