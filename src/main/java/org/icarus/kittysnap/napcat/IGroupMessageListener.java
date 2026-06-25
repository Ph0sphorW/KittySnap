package org.icarus.kittysnap.napcat;

import org.icarus.kittysnap.napcat.onebotapi.OB11Message;

@FunctionalInterface
public interface IGroupMessageListener {

    /**
     * 监听到符合条件的群消息时回调
     * @param message  解析后的 Napcat 消息对象
     * @param groupId  群号
     * @param userId   发送者 QQ
     * @param content  消息纯文本内容
     */
    void onGroupMessage(OB11Message message, long groupId, long userId, String content);
}
