package org.icarus.kittysnap.napcat;

import org.icarus.kittysnap.config.ConfigurationManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ListenerManager {

    private final CopyOnWriteArraySet<GroupEntry> groupListeners = new CopyOnWriteArraySet<>();
    private final ConfigurationManager cfg;

    public ListenerManager(ConfigurationManager cfg) {
        this.cfg = cfg;
    }

    public void add(long groupId, IGroupMessageListener listener) {
        groupListeners.add(new GroupEntry(groupId, listener));
        cfg.logInfo("group.listener-added", groupId);
    }

    public void addAll(Collection<Long> groupIds, IGroupMessageListener listener) {
        groupIds.forEach(gid -> add(gid, listener));
    }

    public boolean remove(long groupId) {
        boolean r = groupListeners.removeIf(e -> e.groupId() == groupId);
        if (r) cfg.logInfo("group.listener-removed", groupId);
        return r;
    }

    public void removeByType(Class<? extends IGroupMessageListener> type) {
        groupListeners.removeIf(e -> type.isInstance(e.listener()));
    }

    public Set<Long> getMonitoredGroups() {
        Set<Long> groups = new HashSet<>();
        groupListeners.forEach(e -> groups.add(e.groupId()));
        return groups;
    }

    public CopyOnWriteArraySet<GroupEntry> entries() {
        return groupListeners;
    }
}
