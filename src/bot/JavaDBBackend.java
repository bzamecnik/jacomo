package bot;

//import bot.Logger.Contact;
import bot.Logger.PresenceStatus;
import java.util.Date;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 *
 * @author Bohouš
 */
public class JavaDBBackend implements DBBackend {

  public JavaDBBackend() {
    loadDriver();
    dbConnection = null;
    String dbName = System.getProperty("jacomo.dbName");
    String strUrl = "jdbc:derby:" + dbName + ";create=true";
    System.setProperty("derby.system.home", System.getProperty("jacomo.homeDir", "."));
    try {
      // make a connection
      dbConnection = DriverManager.getConnection(strUrl);
    
      // create tables if we have created a new database
      ResultSet rs;
      DatabaseMetaData md = dbConnection.getMetaData();
      rs = md.getTables(null, dbName, "StatusChanges", null);
      if(!rs.next()) {
        createTables();
      }
      
      // prepare some SQL statements for freqent usage
      stmtInsertContact = dbConnection.prepareStatement(
        "INSERT INTO Contacts " +
        "   (jid, name, archived) " +
        "VALUES (?, ?, ?)",
        Statement.RETURN_GENERATED_KEYS);
      
      stmtSelectContactByJid = dbConnection.prepareStatement(
        "SELECT (id, archived) FROM Contacts WHERE jid = ?");
      
      stmtUpdateContactArchived = dbConnection.prepareStatement(
        "UPDATE Contacts SET (archived) VALUES (?) WHERE id = ?");
      
      stmtInsertStatusChange = dbConnection.prepareStatement(
        "INSERT INTO StatusChanges " +
        "   (contactId, time, status, statusDesc) " +
        "VALUES (?, ?, ?, ?)");
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    
    
  }
  
  void loadDriver() {
    try {
      Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
      System.out.println("Loaded JavaDB driver.");
    } catch (ClassNotFoundException ex) {
      System.err.println("Can't load JavaDB driver. Check the CLASSPATH.");
      ex.printStackTrace(System.err);
    } catch (InstantiationException ex) {
      System.err.println("Can't instantiate JavaDB driver.");
      ex.printStackTrace(System.err);
    } catch (IllegalAccessException ex) {
      System.err.println("Can't access JavaDB driver.");
      ex.printStackTrace(System.err);
    }
  }
  
  void createTables() {
    // contact details
    String strCreateTableContacts =
        "CREATE TABLE Contacts (" +
        "id    INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY " +
        "      (START WITH 1, INCREMENT BY 1), " +
        "jid   VARCHAR(100) NOT NULL UNIQUE, " +
        "name  VARCHAR(100), " +
        "archived   CHAR(1) )"; // 'Y' = archived, 'N' = active
    
    // log of online presence changes of contacts
    String strCreateTableStatusChanges =
        "CREATE TABLE StatusChanges (" +
        "contactId    INTEGER NOT NULL FOREIGN KEY " +
        "time   TIMESTAMP NOT NULL, " +
        "status   VARCHAR(20) NOT NULL, " +
        "statusDesc  LONG VARCHAR )"; // status description
    
    // log of online presence of JaCoMo itself
    String strCreateTableOwnPresenceLog =
        "CREATE TABLE OwnPresenceLog (" +
        "time   TIMESTAMP NOT NULL PRIMARY KEY, " +
        "online   CHAR(1) )"; // 'Y' = on-line, 'N' = off-line
    
    Statement statement = null;
    try {
        statement = dbConnection.createStatement();
        statement.execute(strCreateTableContacts);
        statement.execute(strCreateTableStatusChanges);
        statement.execute(strCreateTableOwnPresenceLog);
        // or use:
        //statement.addBatch(...)
        //statement.executeBatch()
    } catch (SQLException ex) {
        ex.printStackTrace();
    }

  }
  
  public int enableContact(String contact) {
    // - look into Contacts table if we already have this contact
    int id = -1;
    boolean archived = false;
    try {
        stmtSelectContactByJid.clearParameters();
        stmtSelectContactByJid.setString(1, contact);
        ResultSet rs = stmtSelectContactByJid.executeQuery();
        if (rs.next()) {
            id = rs.getInt(1);
            archived = rs.getString(2).equals("Y");
        }
        if ((id != -1) && archived) {
    //   - yes: set 'archived' to 'N'
          stmtUpdateContactArchived.clearParameters();
          stmtUpdateContactArchived.setString(1, "N");
          stmtUpdateContactArchived.setInt(1, id);
          stmtUpdateContactArchived.executeUpdate();
        } else {
    //   - no: insert the contact into the table and get the generated 'id'
          stmtInsertContact.clearParameters();
          stmtInsertContact.setString(1, contact);
          stmtInsertContact.setString(2, ""); // TODO: set name
          stmtInsertContact.setString(3, "N"); // TODO: set name
          stmtInsertContact.executeUpdate();
          rs = stmtInsertContact.getGeneratedKeys();
          if (rs.next()) {
              id = rs.getInt(1);
          }
        }
    } catch (SQLException ex) {
       ex.printStackTrace();
    }
    return id;
  }

  public void disableContact(String contact) {
    // - look into Contacts table if we already have this contact
    int id = -1;
    try {
        stmtSelectContactByJid.clearParameters();
        stmtSelectContactByJid.setString(1, contact);
        ResultSet rs = stmtSelectContactByJid.executeQuery();
        if (rs.next()) {
            id = rs.getInt(1);
        }
        if (id != -1) {
    //   - yes: set 'archived' to 'Y'
          stmtUpdateContactArchived.clearParameters();
          stmtUpdateContactArchived.setString(1, "Y");
          stmtUpdateContactArchived.setInt(1, id);
          stmtUpdateContactArchived.executeUpdate();
        } else {
    //   - no: nothing (probably report an error)
        }
    } catch (SQLException ex) {
       ex.printStackTrace();
    }
  }

  public void changeContactStatus(String contact, Date date, PresenceStatus status, String statusDesctiption) {
    int id = -1;
    try {
        stmtSelectContactByJid.clearParameters();
        stmtSelectContactByJid.setString(1, contact);
        ResultSet rs = stmtSelectContactByJid.executeQuery();
        if (rs.next()) {
            id = rs.getInt(1);
        }
        if (id == -1) {
          id = enableContact(contact);
        }
        stmtInsertStatusChange.clearParameters();
        stmtInsertStatusChange.setInt(1, id);
        stmtInsertStatusChange.setTimestamp(2, new Timestamp(date.getTime()));
        stmtInsertStatusChange.setString(3, status.isOnline(status) ? "Y" : "N");
        stmtInsertStatusChange.setString(4, statusDesctiption);
        
    } catch (SQLException ex) {
       ex.printStackTrace();
    }
  }

  Connection dbConnection;
  
  PreparedStatement stmtInsertContact;
  PreparedStatement stmtSelectContactByJid;
  PreparedStatement stmtUpdateContactArchived;
  PreparedStatement stmtInsertStatusChange;

}
