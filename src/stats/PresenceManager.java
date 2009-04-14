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
        List<PresenceChange> botPresenceList = dbBackend.getBotPresenceChangesList();

        //   - interpret them as time intervals
        boolean botOnlineCurrentStatus = false;
        boolean botOnlinePreviousStatus = false;
        for (PresenceChange change : botPresenceList) {
            botOnlineCurrentStatus = change.getStatus().isOnline(change.getStatus());
            if (botOnlineCurrentStatus != botOnlinePreviousStatus) {
                botPresenceIntervals.add(change.getTime());
            }
            botOnlinePreviousStatus = botOnlineCurrentStatus;
        }

        // - load contacts
        List<Contact> contactList = dbBackend.getContactsList();
        for (Contact contact : contactList) {
            contacts.put(contact.getId(), contact);
        }

        // - load contact presence changes
        //   - interpret them as time intervals (intersected by bot presence)

        // - register in bot for update notification
    }
}
