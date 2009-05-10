package org.zamecnik.jacomo.stats;

import java.util.Collection;
import org.zamecnik.jacomo.lib.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Presence intervals manager. The purpose is to get the logging data from
 * database, interpret them as time intervals and prepare it for usage by other
 * objects. Contact information can be obtained here too.
 * @author Bohumir Zamecnik
 */
public class PresenceManager {

    /**
     * PresenceManager constructor. The database backend resource is not owned
     * here.
     * @param dbBackend database backend resource
     */
    public PresenceManager(DBBackend dbBackend) {
        this.dbBackend = dbBackend;
        botPresenceIntervals = new IntervalList();
        contactIntervals = new HashMap<Integer, IntervalList>();
        contacts = new HashMap<Integer, Contact>();
    }

    /**
     * Reload data from database and interpret them again.
     */
    public void reload() {
        // load bot presence changes
        List<PresenceChange> presenceList = dbBackend.getBotPresenceChangesList();

        // interpret the changes as time intervals
        boolean onlineCurrentStatus = false;
        boolean onlinePreviousStatus = false;
        for (PresenceChange change : presenceList) {
            onlineCurrentStatus = change.getStatus().isOnline(change.getStatus());
            if (onlineCurrentStatus != onlinePreviousStatus) {
                botPresenceIntervals.add(change.getTime());
            }
            onlinePreviousStatus = onlineCurrentStatus;
        }

        // load contacts
        List<Contact> contactList = dbBackend.getContactsList();
        for (Contact contact : contactList) {
            contacts.put(contact.getId(), contact);
        }

        // load contact presence changes
        // interpret them as time intervals (intersected by bot presence)

        // TODO: possible optimization:
        //   - get changes in one SQL query using getPresenceChangesList()
        //   - separate change sets for each contact here
        for (int contactId : contacts.keySet()) {
            presenceList = dbBackend.getContactPresenceChangesList(contactId);
            onlinePreviousStatus = false;
            IntervalList currentIntervalList = new IntervalList();
            for (PresenceChange change : presenceList) {
                onlineCurrentStatus = change.getStatus().isOnline(change.getStatus());
                if (onlineCurrentStatus != onlinePreviousStatus) {
                    currentIntervalList.add(change.getTime());
                }
                onlinePreviousStatus = onlineCurrentStatus;
            }
            contactIntervals.put(contactId,
                    IntervalList.intersect(currentIntervalList, botPresenceIntervals));
        }

    // TODO: register in bot for update notification (?)
    }

    /**
     * Get contacts with database id's.
     * @return map: database id -> contact
     */
    public Map<Integer, Contact> getContacts() {
        return Collections.unmodifiableMap(contacts);
    }

    /**
     * Get the number of contacts.
     * @return the number of contacts
     */
    public int getContactsCount() {
        return contacts.size();
    }

    /**
     * Get a list of bot presence intervals.
     * @return bot presence intervals
     */
    public IntervalList getBotPresenceIntervals() {
        // TODO: this should give read-only access
        return botPresenceIntervals;
    }

    /**
     * Get a list of presence intervals of a specified contact.
     * @param contactId database id of a contact
     * @return presence intervals of a contact
     */
    public IntervalList getContactPresenceIntervals(int contactId) {
        // TODO: this should give read-only access
        return contactIntervals.get(contactId);
    }

    /**
     * Get a read-only collection of presence intervals of all contacts.
     * @return collection of presence intervals of all contacts
     */
    public Collection<IntervalList> getAllPresenceIntervals() {
        // give read-only access
        return Collections.unmodifiableCollection(contactIntervals.values());
    }

    /**
     * Get a read-only map of contacts and their presence intervals.
     * @return map of contacts and presence intervals
     */
    public Map<Contact, IntervalList> getPresenceIntervalsWithContacts() {
        Map<Contact, IntervalList> map = new HashMap<Contact, IntervalList>();
        for (Entry<Integer, IntervalList> entry : contactIntervals.entrySet()) {
            Contact contact = contacts.get(entry.getKey());
            map.put(contact, entry.getValue());
        }
        // give read-only access
        return Collections.unmodifiableMap(map);
    }

//    // DEBUG
//    public void show() {
//        System.out.println("Presence Manager");
//        System.out.println("Bot presence intervals");
//        System.out.println(botPresenceIntervals.toString());
//        System.out.println();
//
//        System.out.println("Contacts (size: " + contacts.size() + "):");
//        for (Map.Entry<Integer, Contact> entry : contacts.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//
//        }
//        System.out.println();
//
//        System.out.println("Contact presence intervals:");
//        for (Map.Entry<Integer, IntervalList> entry : contactIntervals.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//
//        }
//        System.out.println();
//    }
    /** Presence intervals of JaCoMo bot. */
    IntervalList botPresenceIntervals;
    /** Map: contact database id -> its interval list. */
    Map<Integer, IntervalList> contactIntervals;
    /** Map: contact database id -> contact information. */
    Map<Integer, Contact> contacts;
    /** Database backend resource. Not owned here. */
    DBBackend dbBackend;
}
