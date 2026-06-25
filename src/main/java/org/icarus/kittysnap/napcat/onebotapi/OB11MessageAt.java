package org.icarus.kittysnap.napcat.onebotapi;

import java.util.Map;

public class OB11MessageAt extends OB11SegmentBase {

    public static final String TYPE = "at";

    /**
     * fastjson2 反序列化用
     */
    @SuppressWarnings("unused")
    public OB11MessageAt() {
    }

    public OB11MessageAt(Map<String, Object> data) {
        super(TYPE, data);
    }

    /**
     * 获取 @ 的目标标识
     */
    public String getTarget() {
        return getDataString("qq");
    }

    /**
     * 是否为 @全体成员
     */
    public boolean isAtAll() {
        return "all".equals(getTarget());
    }

    /**
     * 获取 @ 的目标 QQ 号
     */
    public long getTargetUserId() {
        return getDataLong("qq");
    }
}
