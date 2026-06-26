package org.icarus.kittysnap.utils.listeners;

import org.icarus.kittysnap.KittySnap;
import org.icarus.kittysnap.napcat.onebot.OB11Message;

public class LoggingGroupListener implements IGroupMessageListener {
    private final KittySnap plugin;

    public LoggingGroupListener(KittySnap plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGroupMessage(OB11Message message, long groupId, long userId, String content) {
        plugin.getLogger().info(plugin.getConfigManager().logGroupMsgFormat(groupId, userId, content));
    }
}
