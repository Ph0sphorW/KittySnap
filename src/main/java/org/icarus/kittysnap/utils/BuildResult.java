package org.icarus.kittysnap.utils;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * OB11 段列表处理结果
 */
public record BuildResult(long replyMessageId, String displayContent, @Nullable List<Component> clickComponents) {

    public BuildResult(long replyMessageId, String displayContent) {
        this(replyMessageId, displayContent, null);
    }

    public List<Component> clickComponents() {
        return clickComponents != null ? clickComponents : Collections.emptyList();
    }

    public boolean hasClickComponents() {
        return clickComponents != null && !clickComponents.isEmpty();
    }
}
