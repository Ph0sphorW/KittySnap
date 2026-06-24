package org.icarus.kittysnap.onebotapi;

import java.util.Map;

/**
 * Markdown 消息段
 * <p>
 * data 字段: {"content": "markdown内容"}
 */
public class OB11MessageMarkdown extends OB11SegmentBase {

    public static final String TYPE = "markdown";

    public OB11MessageMarkdown() {}

    public OB11MessageMarkdown(Map<String, Object> data) {
        super(TYPE, data);
    }
}
