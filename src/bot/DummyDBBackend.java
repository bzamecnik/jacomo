/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bot;

//import bot.Logger.Contact;
import bot.Logger.PresenceStatus;
import java.util.Date;

/**
 *
 * @author Bohouš
 */
public class DummyDBBackend implements DBBackend {

  public int enableContact(String contact) {
    System.out.println("enable contact: " + contact);
    return 0;
  }

  public void disableContact(String contact) {
    System.out.println("disable contact: " + contact);
  }

  public void changeContactStatus(String contact, Date date, PresenceStatus status, String statusDesctiption) {
    System.out.println("status change: ");
    System.out.println("  contact: " + contact);
    System.out.println("  date: " + date);
    System.out.println("  status: " + status);
    System.out.println("  status desctiption: " + statusDesctiption);
  }

}
