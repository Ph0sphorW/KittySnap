package org.icarus.kittysnap.napcat.onebotapi;

import com.alibaba.fastjson2.JSONObject;

import java.util.Map;

import static com.alibaba.fastjson2.JSON.parseArray;

/**
 * 标准消息片段接口
 * <p>
 * 包含 type 和 data
 */
public interface OB11Segment {

    /**
     * 段类型
     */
    String getType();

    /**
     * 段数据
     */
    Map<String, Object> getData();

    /**
     * 从 data 中获取指定键的字符串值
     */
    default String getDataString(String key) {
        Object v = getData() != null ? getData().get(key) : null;
        return v != null ? v.toString() : null;
    }

    /**
     * 从 data 中获取指定键的长整型数值
     */
    default long getDataLong(String key) {
        Object v = getData() != null ? getData().get(key) : null;
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }
}
