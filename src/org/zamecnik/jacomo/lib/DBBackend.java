package org.zamecnik.jacomo.lib;

import java.util.Date;
import java.util.List;

/**
 * Database backend interface. The backend is used both in bot and stats part
 * of JaCoMo. The purpose is to make an abstract interface to various database
 * operations and offer a possibility to suppport multiple implementations.
 * @author Bohumir Zamecnik
 */
public interface DBBackend {

    /**
     * Store a change in presence status of a contact.
     * @param contactJID identification of a contact (Jabber Id)
     * @param date date of the change
     * @param status new presence status
     * @param statusDescription description of the new status (optional)
     */
    void changeContactPresence(
            String contactJID,
            Date date,
            PresenceStatus status,
            String statusDescription);

    /**
     * Update contact name.
     * @param contactJID identification of a contact (Jabber Id)
     * @param name new contact name
     */
    void updateContactName(String contactJID, String name);

    /**
     * Add a new contact or enable an archived one.
     * @return id of the contact
     */
    int enableContact(String contact, String name);

    /**
     * Disable a contact. In fact the contact is not deleted, but rather
     * archived. So if it is in future enabled again, the presence records
     * associated with the contact are preserved.
     * @param contactJID identification of a contact (Jabber Id)
     */
    void disableContact(String contactJID);

    /**
     * Write a record to bot presence log.
     * @param online presence changed to: true = online, false = offline
     */
    void changeBotPresence(boolean online);

    /**
     * Get a list of contacts stored in the database.
     * @return list of contacts
     */
    List<Contact> getContactsList();

    /**
     * Get a list of presence changes of the bot.
     * @return list of contacts
     */
    List<PresenceChange> getBotPresenceChangesList();

    /**
     * Get a list of presence changes of all contacts. The list doesn't contain
     * record of archived contacts.
     * @return list of presence changes
     */
    List<PresenceChange> getPresenceChangesList();

    /**
     * Get a list of presence changes of a specified contact. If such a contact
     * is not presence in the database return an empty list.
     * @param contactId database id of a contact
     * @return list of presence changes
     */
    List<PresenceChange> getContactPresenceChangesList(int contactId);

    /**
     * Dispose the instance freeing all resources holded by it.
     * Disposed instance should not be used anymore.
     */
    void dispose();
}
