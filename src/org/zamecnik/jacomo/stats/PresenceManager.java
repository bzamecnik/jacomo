package org.zamecnik.jacomo.stats;

import java.util.Collection;
import org.zamecnik.jacomo.lib.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Bohouš
 */
public class PresenceManager {

    public PresenceManager(DBBackend dbBackend) {
        this.dbBackend = dbBackend;
        botPresenceIntervals = new IntervalList();
        contactIntervals = new HashMap<Integer, IntervalList>();
        contacts = new HashMap<Integer, Contact>();
    }

    public void refresh() {
        // - load contact bot presence changes
        List<PresenceChange> presenceList = dbBackend.getBotPresenceChangesList();

        //   - interpret them as time intervals
        boolean onlineCurrentStatus = false;
        boolean onlinePreviousStatus = false;
        for (PresenceChange change : presenceList) {
            onlineCurrentStatus = change.getStatus().isOnline(change.getStatus());
            if (onlineCurrentStatus != onlinePreviousStatus) {
                botPresenceIntervals.add(change.getTime());
            }
            onlinePreviousStatus = onlineCurrentStatus;
        }

        // - load contacts
        List<Contact> contactList = dbBackend.getContactsList();
        for (Contact contact : contactList) {
            contacts.put(contact.getId(), contact);
        }

        // - load contact presence changes
        //   - interpret them as time intervals (intersected by bot presence)

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

    // - register in bot for update notification
    }

    public Map getContacts() {
        return Collections.unmodifiableMap(contacts);
    }

    public IntervalList getBotPresenceIntervals() {
        // TODO: this should give read-only access
        return botPresenceIntervals;
    }

    public IntervalList getContactPresenceIntervals(int contactId) {
        // TODO: this should give read-only access
        return contactIntervals.get(contactId);
    }

    public Collection<IntervalList> getAllPresenceIntervals() {
        // give read-only access
        return Collections.unmodifiableCollection(contactIntervals.values());
    }

    public Map<Contact, IntervalList> getPresenceIntervalsWithContacts() {
        // give read-only access
        Map<Contact, IntervalList> map = new HashMap<Contact, IntervalList>();
        for (Entry<Integer, IntervalList> entry : contactIntervals.entrySet()) {
            Contact contact = contacts.get(entry.getKey());
            map.put(contact, entry.getValue());
        }
        return Collections.unmodifiableMap(map);
    }

    public void dispose() {
        dbBackend.dispose();
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
    IntervalList botPresenceIntervals;
    Map<Integer, IntervalList> contactIntervals;
    Map<Integer, Contact> contacts;
    DBBackend dbBackend;
}
