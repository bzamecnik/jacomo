package org.zamecnik.jacomo.lib;

//import bot.Logger.Contact;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Bohouš
 */
public interface DBBackend {

    /**
     * Store a change in status of a contact.
     */
    void changeContactPresence(
            String contact,
            Date date,
            PresenceStatus status,
            String statusDesctiption);

    /**
     * Update contact name.
     */
    void updateContactName(String contact, String name);

    /**
     * Add a new contact or enable an archived one.
     * @return id of the contact
     */
    int enableContact(String contact, String name);

    /**
     * Disable a contact, ie. archive it.
     */
    void disableContact(String contact);

    /**
     * Write to own presence log.
     */
    void changeBotPresence(boolean online);

    /**
     * Get list of contacts stored in the database.
     */
    List<Contact> getContactsList();

    List<PresenceChange> getBotPresenceChangesList();

    List<PresenceChange> getPresenceChangesList();

    List<PresenceChange> getContactPresenceChangesList(int contactId);

    void dispose();
}
