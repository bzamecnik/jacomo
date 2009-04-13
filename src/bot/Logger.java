package bot;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;

/**
 *
 * @author Bohouš
 */
public class Logger {

    public Logger() throws JacomoException {
        System.out.println("Logger()");
        state = LoggerState.NOT_PREPARED;

        contactsCache = new java.util.HashSet<String>();
        contactFilter = new ContactFilter();
        // sample filter data:
        contactFilter.addBlacklistKeyword("tv.jabbim.cz");
        contactFilter.addBlacklistKeyword("weather.netlab.cz");
        contactFilter.addBlacklistKeyword("live@jabbim.cz"); // whole JID

        //dbBackend = new DummyDBBackend();
        dbBackend = new JavaDBBackend();

        String jabberServer = System.getProperty("jacomo.bot.jabberServer");
        System.out.println("jabber server: " + jabberServer);
        if (jabberServer == null) {
            throw new JacomoException("jacomo.bot.jabberServer is not specified.");
        }
        jabberConnection = new XMPPConnection(jabberServer);

        rosterListener = new RosterListener() {

            public void entriesAdded(Collection<String> addresses) {
                for (String jid : addresses) {
                    jid = stripJID(jid);
                    if (contactFilter.filter(jid)) {
                        System.out.println("entry added: " + jid);
                        enableContact(jid);
                    }
                }
            }

            public void entriesDeleted(Collection<String> addresses) {
                for (String jid : addresses) {
                    jid = stripJID(jid);
                    if (contactFilter.filter(jid)) {
                        System.out.println("entry deleted: " + jid);
                        disableContact(jid);
                    }
                }
            }

            public void entriesUpdated(Collection<String> addresses) {
                for (String jid : addresses) {
                    jid = stripJID(jid);
                    if (contactFilter.filter(jid)) {
                        System.out.println("entry updated: " + jid);
                        updateContactName(jid);
                    }
                }
            }

            public void presenceChanged(Presence presence) {
                Date date = Calendar.getInstance().getTime();
                String jid = stripJID(presence.getFrom());
                if ((jid != null) && contactFilter.filter(jid)) {
                    PresenceStatus status =
                            (presence.getType() == Presence.Type.available)
                            ? PresenceStatus.ONLINE : PresenceStatus.OFFLINE;
                    String statusDesctiption = presence.getStatus();
                    System.out.println("status changed: " + jid + " at " +
                            date + ": " + status + " (" + statusDesctiption + ")");
                    changeContactStatus(jid, date, status, statusDesctiption);
                }
            }
        };
    }

    /**
     * Start logging.
     */
    public void start() {
        System.out.println("Logger.start()");
        if (state == LoggerState.PREPARED) {
            registerJabberHandlers();
            state = LoggerState.RUNNING;
        }
        // write ONLINE to own presence log
        dbBackend.changeBotPresence(true);
    }

    /**
     * Stop logging.
     */
    public void stop() {
        System.out.println("Logger.stop()");
        if (state == LoggerState.RUNNING) {
            unregisterJabberHandlers();
            state = LoggerState.PREPARED;
        }
        // write OFFLINE to own presence log
        dbBackend.changeBotPresence(false);
    }

    /**
     * Start a Jabber session.
     */
    public void login() throws JacomoException {
        System.out.println("Logger.login()");
        try {
            jabberConnection.connect();
            jabberConnection.login(
                    System.getProperty("jacomo.bot.jabberUser"),
                    System.getProperty("jacomo.bot.jabberPassword"));
        } catch (XMPPException ex) {
            throw new JacomoException(ex);
        }

        // Synchronize contacts in roster with contacts in the database.
        // - read contacts from roster
        //   - filter active contacts -> C1 set
        // - enable C1
        //   - these also get loaded contacts to contactsCache
        Set<String> rosterActiveContacts = new HashSet<String>();
        Roster roster = jabberConnection.getRoster();
        for (RosterEntry entry : roster.getEntries()) {
            String jid = stripJID(entry.getUser());
            if (contactFilter.filter(jid)) {
                rosterActiveContacts.add(jid);
                enableContact(jid, entry.getName());
            }
        }
        // - read active (non-archived) contacts from database -> C2 set
        // convert list to set to allow set operations
        Set<String> dbActiveContacts = new HashSet<String>();
        List<String> dbActiveContactsList = dbBackend.getContactsList();
        for (String contact : dbActiveContactsList) {
            dbActiveContacts.add(contact);
        }
        // - disable (C2 - C1) set
        dbActiveContacts.removeAll(rosterActiveContacts);
        for (String contact : dbActiveContacts){
            disableContact(contact);
        }

        state = LoggerState.PREPARED;
    }

    /**
     * Stop a Jabber session.
     */
    public void logout() {
        System.out.println("Logger.logout()");
        if ((state == LoggerState.PREPARED) && jabberConnection.isConnected()) {
            jabberConnection.disconnect();
            state = LoggerState.NOT_PREPARED;
        }
    }

    /**
     * Add a new contact or enable an archived one.
     */
    void enableContact(String contact, String name) {
        if (contact != null) {
            dbBackend.enableContact(contact, name);
            contactsCache.add(contact);
        }
    }

    void enableContact(String contact) {
        Roster roster = jabberConnection.getRoster();
        RosterEntry entry = roster.getEntry(contact);
        enableContact(contact, (entry != null) ? entry.getName() : "");
    }

    /**
     * Disable a contact, ie. archive it.
     */
    void disableContact(String contact) {
        if (contact != null) {
            dbBackend.disableContact(contact);
            contactsCache.remove(contact);
        }
    }

    boolean contactExists(String contact) {
        return contactsCache.contains(contact);
    }

    void updateContactName(String contact) {
        if (contact != null) {
            Roster roster = jabberConnection.getRoster();
            RosterEntry entry = roster.getEntry(contact);
            if (entry != null) {
                System.out.println("update contact name: " + contact + ", " + entry.getName());
                dbBackend.updateContactName(contact, entry.getName());
            }
        }
    }

    /**
     * Store a change in status of a contact.
     */
    void changeContactStatus(
            String contact,
            Date date,
            PresenceStatus status,
            String statusDesctiption) {
        if (!contactExists(contact)) {
            enableContact(contact);
        }
        dbBackend.changeContactStatus(contact, date, status, statusDesctiption);
    }

    /**
     * Register handlers to events from a Jabber session.
     */
    void registerJabberHandlers() {
        Roster roster = jabberConnection.getRoster();
        roster.addRosterListener(rosterListener);
    }

    /**
     * Unregister handlers to events from a Jabber session.
     */
    void unregisterJabberHandlers() {
        Roster roster = jabberConnection.getRoster();
        roster.removeRosterListener(rosterListener);
    }

    /**
     * Strip the resource part from a JID.
     */
    String stripJID(String jid) {
        return jid.replaceFirst("/.*", "");
    }

//  public class Contact {
//    public String jid;
////    public int id;
//    
//    public Contact(String jid) {
//      this.jid = jid;
//    }
//    
////    public Contact(String jid, int id) {
////      this.jid = jid;
////      this.id = id;
////    }
//    
//    @Override
//    public boolean equals(Object o) {
//      if (!(o instanceof Contact)) {
//        return false;
//      }
//      Contact other = (Contact)o;
//      return jid.equals(other.jid);
//    }
//
//    @Override
//    public int hashCode() {
//      int hash = 5;
//      hash = 89 * hash + (this.jid != null ? this.jid.hashCode() : 0);
//      return hash;
//    }
//  }
    public enum PresenceStatus {

        OFFLINE,
        ONLINE; // use more modes: see Presence.Mode

        public boolean isOnline(PresenceStatus status) {
            return status != OFFLINE;
        }
    }

    public enum LoggerState {

        RUNNING,
        PREPARED,
        NOT_PREPARED,
        //ERROR
    }
    DBBackend dbBackend;
    XMPPConnection jabberConnection; // Jabber backend
    //java.util.Map<Integer, Contact> contactsCache;
    //java.util.Set<Contact> contactsCache;
    // a cache of contacts that haven't been archived (ie. disabled)
    Set<String> contactsCache;
    LoggerState state;
    RosterListener rosterListener;
    ContactFilter contactFilter;
}
