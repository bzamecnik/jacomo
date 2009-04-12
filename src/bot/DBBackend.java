package bot;

//import bot.Logger.Contact;
import bot.Logger.PresenceStatus;
import java.util.Date;

/**
 *
 * @author Bohouš
 */
public interface DBBackend {
     
  /**
   * Store a change in status of a contact.
   */
  void changeContactStatus(
          String contact,
          Date date,
          PresenceStatus status,
          String statusDesctiption
          );
  
  /**
   * Add a new contact or enable an archived one.
   * @return id of the contact
   */
  int enableContact(String contact, String name);
  
  /**
   * Disable a contact, ie. archive it.
   */
  void disableContact(String contact);
  
//  /**
//   * Get list of contacts stored in the database.
//   */
//  public List<Contact> getContactsList();
  
}
