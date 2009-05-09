package org.zamecnik.jacomo.stats;

/**
 * Contacts count statistic. A simple statistic over logged data. Get the number
 * of active contacts (not archived ones).
 * @author Bohumir Zamecnik
 */
public class ContactsCount {

    /**
     * ContactsCount constructor.
     * @param presenceManager presence manager holding the data
     */
    public ContactsCount(PresenceManager presenceManager) {
        this.presenceManager = presenceManager;
    }

    /**
     * Get the number of active contacts (not archived ones).
     * @return number of active contacts
     */
    public int getContactsCount() {
        return presenceManager.getContacts().size();
    }
    PresenceManager presenceManager;
}
