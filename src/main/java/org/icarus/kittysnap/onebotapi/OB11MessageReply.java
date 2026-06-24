package org.icarus.kittysnap.onebotapi;

import java.util.Map;

/**
 * 回复消息段
 * <p>
 * data 字段: {"id": "被回复消息的 message_id"}
 */
public class OB11MessageReply extends OB11SegmentBase {

    public static final String TYPE = "reply";

    /** fastjson2 反序列化用 */
    @SuppressWarnings("unused")
    public OB11MessageReply() {}

    public OB11MessageReply(Map<String, Object> data) {
        super(TYPE, data);
    }

    /** 获取被回复消息的 message_id */
    public long getReplyId() {
        return getDataLong("id");
    }
}
