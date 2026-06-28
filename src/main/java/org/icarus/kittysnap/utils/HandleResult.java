package org.icarus.kittysnap.utils;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 单个 OB11 段处理结果。
 * @param text            MiniMessage 兼容的展示文本
 * @param clickComponents 可点击的组件列表（含 ClickEvent.callback），通常为图片预览
 */
public record HandleResult(String text, @Nullable List<Component> clickComponents) {

    public HandleResult(String text) {
        this(text, null);
    }
}
