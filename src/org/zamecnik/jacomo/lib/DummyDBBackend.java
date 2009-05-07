package org.zamecnik.jacomo.lib;

//import bot.Logger.Contact;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Bohouš
 */
public class DummyDBBackend implements DBBackend {

    public int enableContact(String contact, String name) {
        System.out.println("enable contact: " + contact + "(" + name + ")");
        return 0;
    }

    public void disableContact(String contact) {
        System.out.println("disable contact: " + contact);
    }

    public void changeContactPresence(
            String contact,
            Date date,
            PresenceStatus status,
            String statusDesctiption) {
        System.out.println("status change: ");
        System.out.println("  contact: " + contact);
        System.out.println("  date: " + date);
        System.out.println("  status: " + status);
        System.out.println("  status desctiption: " + statusDesctiption);
    }

    public void updateContactName(String contact, String name) {
        System.out.println("update contact name: " + contact + ", " + name);
    }

    public void changeBotPresence(boolean online) {
        System.out.println("change own presence: " + (online ? "ONLINE" : "OFFLINE"));
    }

    public List<Contact> getContactsList() {
        return new ArrayList<Contact>();
    }

    public List<PresenceChange> getBotPresenceChangesList() {
        return new ArrayList<PresenceChange>();
    }

    public List<PresenceChange> getPresenceChangesList() {
        return new ArrayList<PresenceChange>();
    }

    public List<PresenceChange> getContactPresenceChangesList(int contactId) {
        return new ArrayList<PresenceChange>();
    }

    public void dispose() {
    }

}
