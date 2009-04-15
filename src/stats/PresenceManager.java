package stats;

import lib.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Bohouš
 */
public class PresenceManager {
    IntervalList botPresenceIntervals;
    Map<Integer, IntervalList> contactIntervals;
    Map<Integer, Contact> contacts;

    DBBackend dbBackend;

    public PresenceManager() throws JacomoException{
        dbBackend = new JavaDBBackend();
        botPresenceIntervals = new IntervalList();
        contactIntervals = new HashMap<Integer, IntervalList>();
        contacts = new HashMap<Integer, Contact>();
    }

    public void initialize() {
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
}