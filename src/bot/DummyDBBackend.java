package bot;

//import bot.Logger.Contact;
import bot.Logger.PresenceStatus;
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

    public void changeContactStatus(
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

    public List<String> getContactsList() {
        return new ArrayList<String>();
    }
}
