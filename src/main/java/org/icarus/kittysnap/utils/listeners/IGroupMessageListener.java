package org.icarus.kittysnap.utils.listeners;

import net.kyori.adventure.text.Component;
import org.icarus.kittysnap.napcat.onebot.OB11Message;
import org.jspecify.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface IGroupMessageListener {

    /**
     * 监听到符合条件的群消息时回调
     * @param message         解析后的 Napcat 消息对象
     * @param groupId         群号
     * @param userId          发送者 QQ
     * @param content         消息展示文本
     * @param clickComponents 可点击组件列表（如含 ClickEvent.callback 的图片），可为 null
     */
    void onGroupMessage(OB11Message message, long groupId, long userId, String content,
                        @Nullable List<Component> clickComponents);
}
