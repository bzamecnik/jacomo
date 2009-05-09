    package org.zamecnik.jacomo.lib;

import org.jivesoftware.smack.packet.*;

/**
 * Presence status representation.
 * <p>
 * See Presence.Mode in Smack library
 * @author Bohumir Zamecnik
 */
public enum PresenceStatus {

    /** Offline. */
    OFFLINE,
    /** Online and available. */
    AVAILABLE,
    /** Online, but away.*/
    AWAY,
    /** Online, but away for a long time. */
    XA,
    /** Online, but don't disturb. */
    DND;

    /**
     * Check if presence is regarded as online.
     * @param status presence status being checked
     * @return true if regarded as online
     */
    public boolean isOnline(PresenceStatus status) {
        return status != OFFLINE;
    }

    /**
     * Check if presence is regarded as away.
     * @param status presence status being checked
     * @return true if regarded as away
     */
    public boolean isAway(PresenceStatus status) {
        return (status == AWAY) || (status == XA) || (status == DND);
    }

    /**
     * Create presence status from a string form. Default is OFFLINE.
     * @param status string form of status
     * @return JaCoMo presence status
     */
    public static PresenceStatus fromString(String status) {
        try {
            return valueOf(status);
        } catch(IllegalArgumentException ex) {
            return OFFLINE; //default
        }
    }

    /**
     * Create presence status from a Smack Jabber presence.
     * @param presence Smack presence
     * @return JaCoMo presence status
     */
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

    /**
     * Create presence status from a boolean.
     * @param online boolean online/offline status
     * @return JaCoMo presence status, AVAILABLE on true, OFFLINE on false
     */
    public static PresenceStatus fromBoolean(boolean online) {
        return online ? PresenceStatus.AVAILABLE : PresenceStatus.OFFLINE;
    }
}
