package bot;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;

/**
 *
 * @author Bohouš
 */
public class Logger {

  public Logger() {
    System.out.println("Logger()");
    state = LoggerState.NOT_PREPARED;
    
    contactsCache = new java.util.HashSet<String>();
    
    dbBackend = new DummyDBBackend();
    //dbBackend = new JavaDBBackend();
    
    // TODO: pass there a server name from configuration
    jabberConnection = new XMPPConnection("jabber.cz");
    
    rosterListener = new RosterListener() {
        public void entriesAdded(Collection<String> addresses) {
          for (String address : addresses) {
            enableContact(address);
          }
        }
        public void entriesDeleted(Collection<String> addresses) {
          for (String address : addresses) {
            disableContact(address);
          }
        }
        public void entriesUpdated(Collection<String> addresses) {
          // nothing
        }
        public void presenceChanged(Presence presence) {
          Date date = Calendar.getInstance().getTime();
          String contact = presence.getFrom();
          if (contact != null) {
            PresenceStatus status =
                    ((presence.getType() == Presence.Type.available)
                    ? PresenceStatus.ONLINE : PresenceStatus.OFFLINE);
            String statusDesctiption = presence.getStatus();
            changeContactStatus(contact, date, status, statusDesctiption);
          }
        }
    };
  }
  
  /**
   * Start logging.
   */
  public void start() {
    System.out.println("Logger.start()");
    if(state == LoggerState.PREPARED) {
      registerJabberHandlers();
      state = LoggerState.RUNNING;
    }
  }
  
  /**
   * Stop logging.
   */
  public void stop() {
    System.out.println("Logger.stop()");
    if(state == LoggerState.RUNNING) {
      unregisterJabberHandlers();
      state = LoggerState.PREPARED;
    }
  }
  
  /**
   * Start a Jabber session.
   */
  public void login() {
    System.out.println("Logger.login()");
    try {
      jabberConnection.connect();
      // TODO: pass there login details from configuration
      jabberConnection.login("bohous", "elensila");
    } catch(XMPPException e) {
      System.out.println(e);
      return;
    }
    
    // TODO: synchronize contacts in roster with contacts in the database
    // TODO: load contacts from database to contactsCache
    
    // for now just load contacts from the roster to the cache
    Roster roster = jabberConnection.getRoster();
    for (RosterEntry entry : roster.getEntries()) {
      enableContact(entry.getUser());
    }
    
    state = LoggerState.PREPARED;
  }
  
  /**
   * Stop a Jabber session.
   */
  public void logout() {
    System.out.println("Logger.logout()");
    if ((state == LoggerState.PREPARED) && jabberConnection.isConnected())
    {
      jabberConnection.disconnect();
      state = LoggerState.NOT_PREPARED;
    }
  }
  
  /**
   * Add a new contact or enable an archived one.
   */
  void enableContact(String contact) {
    if (contact != null) {
      dbBackend.enableContact(contact);
      contactsCache.add(contact);
    }
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
  
  /**
   * Store a change in status of a contact.
   */
  void changeContactStatus(
          String contact,
          Date date,
          PresenceStatus status,
          String statusDesctiption
          )
  {
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
  
  
//  public class Contact {
//    public String name;
////    public int id;
//    
//    public Contact(String name) {
//      this.name = name;
//    }
//    
////    public Contact(String name, int id) {
////      this.name = name;
////      this.id = id;
////    }
//    
//    @Override
//    public boolean equals(Object o) {
//      if (!(o instanceof Contact)) {
//        return false;
//      }
//      Contact other = (Contact)o;
//      return name.equals(other.name);
//    }
//
//    @Override
//    public int hashCode() {
//      int hash = 5;
//      hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
//      return hash;
//    }
//  }
  
  public enum PresenceStatus {
    OFFLINE,
    ONLINE
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
  // a cache of contact that haven't been archive (ie. disabled)
  Set<String> contactsCache;
  LoggerState state;
  
  RosterListener rosterListener;
}
