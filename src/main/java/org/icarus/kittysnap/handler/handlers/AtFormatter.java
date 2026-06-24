package org.icarus.kittysnap.handler.handlers;

import org.icarus.kittysnap.napcat.NapcatWebSocketClient;
import org.icarus.kittysnap.onebotapi.OB11MessageAt;

import static org.icarus.kittysnap.handler.Escape.esc;

public class AtFormatter {
    public static String handleAt(NapcatWebSocketClient napcatClient, OB11MessageAt at, long groupId) {
        if (at.isAtAll()) return "@全体成员";
        String target = at.getTarget();
        if (target == null || target.isEmpty()) return "";

        long uid = at.getTargetUserId();
        if (uid > 0) {
            String name = napcatClient.queryGroupMemberName(groupId, uid);
            if (name != null) return "@" + esc(name);
        }
        return "@" + target;
    }
}
