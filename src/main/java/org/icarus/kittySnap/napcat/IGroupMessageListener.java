package org.icarus.kittySnap.napcat;

/**
 * 群消息监听器接口
 * 实现此接口以处理来自指定群聊的消息
 */
@FunctionalInterface
public interface IGroupMessageListener {

    /**
     * 当监听到符合条件的群消息时回调
     * @param message  解析后的 Napcat 消息对象
     * @param groupId  群号
     * @param userId   发送者 QQ
     * @param content  消息纯文本内容
     */
    void onGroupMessage(NapcatMessage message, long groupId, long userId, String content);
}
