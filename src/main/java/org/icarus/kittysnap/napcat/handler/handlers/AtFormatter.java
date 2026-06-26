package org.icarus.kittysnap.napcat.handler.handlers;

import org.icarus.kittysnap.config.MessagesConfig;
import org.icarus.kittysnap.napcat.NapcatWebSocketClient;
import org.icarus.kittysnap.napcat.onebot.OB11MessageAt;

import static org.icarus.kittysnap.utils.Escaper.escape;

public class AtFormatter {
    public static String handleAt(NapcatWebSocketClient napcatClient, OB11MessageAt at, long groupId, MessagesConfig m) {
        if (at.isAtAll()) return m.getSegment().getAtAll();
        String target = at.getTarget();
        if (target == null || target.isEmpty()) return "";

        long uid = at.getTargetUserId();
        if (uid > 0) {
            String name = napcatClient.queryGroupMemberName(groupId, uid);
            if (name != null) return m.getSegment().getAtPrefix() + escape(name);
        }
        return m.getSegment().getAtPrefix() + target;
    }
}
