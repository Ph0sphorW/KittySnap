package org.icarus.kittysnap.utils.listeners;

import net.kyori.adventure.text.Component;
import org.icarus.kittysnap.KittySnap;
import org.icarus.kittysnap.napcat.onebot.OB11Message;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class LoggingGroupListener implements IGroupMessageListener {
    private final KittySnap plugin;

    public LoggingGroupListener(KittySnap plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGroupMessage(OB11Message message, long groupId, long userId, String content,
                               @Nullable List<Component> clickComponents) {
        plugin.getLogger().info(plugin.getConfigManager().logGroupMsgFormat(groupId, userId, content));
    }
}
