package org.zamecnik.jacomo.bot;

import org.zamecnik.jacomo.lib.*;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;

/**
 * BotApplication act as a Jabber bot which logs the contacts' presence changes.
 * It uses Smack library for Jabber stuff. To work properly, it must connect to
 * two backends at first: a database and a Jabber account. Then it is possible
 * to start logging. It gets presence change events from the Jabber server and
 * stores it into the database.
 * <p>
 * It is possible to use a filter to decide which
 * contacts to exclude.
 * <p>
 * A BotApplication instance holds a XMPPConnection resource and also an
 * instance of {@link ContactFilter}.
 * <p>
 * You can start/stop jabber connection and logging separately. So it is
 * possible to pause logging without leaving Jabber server. Or you can use
 * convenience function {@link #startup()} and {@link #shutdown()}.
 * <p>
 * @author Bohumir Zamecnik
 */
public class BotApplication {

    public BotApplication(DBBackend dbBackend) {
        System.out.println("BotApplication()");
        state = LoggerState.NOT_PREPARED;

        contactsCache = new java.util.HashSet<String>();
        contactFilter = new ContactFilter();
        // sample filter data:
        contactFilter.addBlacklistKeyword("tv.jabbim.cz");
        contactFilter.addBlacklistKeyword("weather.netlab.cz");
        contactFilter.addBlacklistKeyword("live@jabbim.cz"); // whole JID

        this.dbBackend = dbBackend;

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
                changeContactPresence(presence);
            }
        };
    }

    /**
     * Start logging.
     */
    public void start() {
        if (state == LoggerState.PREPARED) {
            System.out.println("BotApplication.start()");
            registerJabberHandlers();
            state = LoggerState.RUNNING;
            // log contacts being currently online
            Roster roster = jabberConnection.getRoster();
            for (RosterEntry entry : roster.getEntries()) {
                String jid = stripJID(entry.getUser());
                Presence presence = roster.getPresence(jid);
                if (presence.isAvailable()) {
                    changeContactPresence(presence);
                }
            }
            // write ONLINE to own presence log
            dbBackend.changeBotPresence(true);
        }
    }

    /**
     * Stop logging.
     */
    public void stop() {
        if (state == LoggerState.RUNNING) {
            System.out.println("BotApplication.stop()");
            unregisterJabberHandlers();
            state = LoggerState.PREPARED;
            // log contacts being currently online as they would have disconnected
            Roster roster = jabberConnection.getRoster();
            for (RosterEntry entry : roster.getEntries()) {
                String jid = stripJID(entry.getUser());
                Presence presence = roster.getPresence(jid);
                if (presence.isAvailable()) {
                    // treat it as offline
                    presence.setType(Presence.Type.unavailable);
                    changeContactPresence(presence);
                }
            }
            // write OFFLINE to own presence log
            dbBackend.changeBotPresence(false);
        }
    }

    /**
     * Start a Jabber session.
     * @return true if succesfully connected to Jabber server
     */
    public boolean login() {
        if (state != LoggerState.NOT_PREPARED) {
            return false;
        }
        System.out.println("BotApplication.login()");
        String jabberServer = System.getProperty("jacomo.jabberServer");
        System.out.println("jabber server: " + jabberServer);
        if (jabberServer == null) {
            //throw new JacomoException("jacomo.jabberServer is not specified.");
            System.err.println("jacomo.jabberServer is not specified.");
            return false;
        }
        jabberConnection = new XMPPConnection(jabberServer);
        try {
            jabberConnection.connect();
            jabberConnection.login(
                    System.getProperty("jacomo.jabberUser"),
                    System.getProperty("jacomo.jabberPassword"));
        } catch (XMPPException ex) {
            //throw new JacomoException(ex);
            System.err.println("Can't log in to the jabber server: " + ex.getMessage());
            return false;
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
        List<Contact> dbActiveContactsList = dbBackend.getContactsList();
        for (Contact contact : dbActiveContactsList) {
            dbActiveContacts.add(contact.getJid());
        }
        // - disable (C2 - C1) set
        dbActiveContacts.removeAll(rosterActiveContacts);
        for (String contact : dbActiveContacts){
            disableContact(contact);
        }

        state = LoggerState.PREPARED;
        return true;
    }

    /**
     * Stop a Jabber session.
     */
    public void logout() {
        System.out.println("BotApplication.logout()");
        if (state == LoggerState.RUNNING) {
            stop();
        }
        if ((state == LoggerState.PREPARED) && jabberConnection.isConnected()) {
            jabberConnection.disconnect();
            state = LoggerState.NOT_PREPARED;
        }
    }

    public void startup() {
        if(login()) {
            start();
        }
    }

    public void shutdown() {
        stop();
        logout();
    }

    /**
     * Set database backend.
     * @param dbBackend the database backend to set
     */
    public void setDbBackend(DBBackend dbBackend) {
        this.dbBackend = dbBackend;
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
    void changeContactPresence(
            String contact,
            Date date,
            PresenceStatus status,
            String statusDesctiption) {
        if (!contactExists(contact)) {
            enableContact(contact);
        }
        dbBackend.changeContactPresence(contact, date, status, statusDesctiption);
    }

    void changeContactPresence(Presence presence) {
        Date date = Calendar.getInstance().getTime();
        String jid = stripJID(presence.getFrom());
        if ((jid != null) && contactFilter.filter(jid)) {
            PresenceStatus status = PresenceStatus.fromJabberPresence(presence);
            String statusDesctiption = presence.getStatus();
            System.out.println("status changed: " + jid + " at " +
                    date + ": " + status + " (" + statusDesctiption + ")");
            changeContactPresence(jid, date, status, statusDesctiption);
        }
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
     * Strip the resource part from a Jabber Id.
     * @param jid Jabber Id
     * @return Jid without the resource part
     */
    String stripJID(String jid) {
        return jid.replaceFirst("/.*", "");
    }

    /**
     * Bot application logger state.
     */
    public enum LoggerState {

        /** Connected to Jabber and logging. */
        RUNNING,
        /** Connected to Jabber and prepared to log. */
        PREPARED,
        /** Not connected to Jabber and not prepared to log. */
        NOT_PREPARED,
        //ERROR
    }
    /** Database backend. The resource is not owned here. */
    DBBackend dbBackend;
    /** Jabber backend. The resource is owned here. */
    XMPPConnection jabberConnection;
    //java.util.Map<Integer, Contact> contactsCache;
    //java.util.Set<Contact> contactsCache;
    /** A cache of contacts that haven't been archived (ie. disabled). */
    Set<String> contactsCache;
    /** Current logger state. */
    LoggerState state;
    /** Event handler for Jabber roster. */
    RosterListener rosterListener;
    /** Filter for excluding unwanted contacts from being logged. */
    ContactFilter contactFilter;
}
