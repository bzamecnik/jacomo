package org.zamecnik.jacomo.stats;

import org.zamecnik.jacomo.stats.*;

/**
 *
 * @author Bohouš
 */
public class ContactsCount {
    public ContactsCount(PresenceManager presenceManager) {
        this.presenceManager = presenceManager;
    }

    public int getContactsCount() {
        return presenceManager.getContacts().size();
    }

    PresenceManager presenceManager;
}
