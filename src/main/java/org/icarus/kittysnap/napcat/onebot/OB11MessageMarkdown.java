package org.icarus.kittysnap.napcat.onebot;

import java.util.Map;

/**
 * Markdown 消息
 **/
public class OB11MessageMarkdown extends OB11SegmentBase {

    public static final String TYPE = "markdown";

    @SuppressWarnings("unused")
    public OB11MessageMarkdown() {
    }

    public OB11MessageMarkdown(Map<String, Object> data) {
        super(TYPE, data);
    }
}
