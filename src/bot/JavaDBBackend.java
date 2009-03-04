package bot;

//import bot.Logger.Contact;
import bot.Logger.PresenceStatus;
import java.util.Date;

/**
 *
 * @author Bohouš
 */
public class JavaDBBackend implements DBBackend {

  public int enableContact(String contact) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void disableContact(String contact) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void changeContactStatus(String contact, Date date, PresenceStatus status, String statusDesctiption) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
