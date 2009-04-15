package org.zamecnik.jacomo.lib;

import org.jivesoftware.smack.packet.*;

/**
 *
 * @author Bohouš
 */
public enum PresenceStatus {

    // see Presence.Mode in Smack library
    OFFLINE,
    AVAILABLE,
    AWAY,
    XA, // extended away
    DND; // don't disturb

    public boolean isOnline(PresenceStatus status) {
        return status != OFFLINE;
    }

    public boolean isAway(PresenceStatus status) {
        return (status == AWAY) || (status == XA) || (status == DND);
    }

    public static PresenceStatus fromString(String status) {
        try {
            return valueOf(status);
        } catch(IllegalArgumentException ex) {
            return OFFLINE; //default
        }
    }

    public static PresenceStatus fromJabberPresence(Presence presence) {
        if (!presence.isAvailable()){
            return OFFLINE;
        }
        if (presence.isAway()) {
            Presence.Mode mode = presence.getMode();
            switch (mode) {
                case away: return AWAY;
                case xa: return XA;
                case dnd: return DND;
            }
        }
        return AVAILABLE; // available, free for chat
    }

    public static PresenceStatus fromBoolean(boolean online) {
        return online ? PresenceStatus.AVAILABLE : PresenceStatus.OFFLINE;
    }
}
