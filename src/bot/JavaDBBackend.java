package bot;

//import bot.Logger.Contact;
import bot.Logger.PresenceStatus;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 *
 * @author Bohouš
 */
public class JavaDBBackend implements DBBackend {

    public JavaDBBackend() throws JacomoException {
        loadDriver();

        String dbName = System.getProperty("jacomo.dbName");
        String strUrl = "jdbc:derby:" + dbName + ";create=true";
        String homeDir = System.getProperty("jacomo.homeDir", ".");
        // create the home directory if it doesn't exist
        File homeDirFile = new File(homeDir);
        homeDirFile.mkdirs();
        //System.setProperty("derby.system.home", homeDir);

        dbConnection = null;

        try {
            // make a connection
            dbConnection = DriverManager.getConnection(strUrl);

            // create tables if we have created a new database
            ResultSet rs;
            DatabaseMetaData md = dbConnection.getMetaData();
            System.out.println("database URL: " + md.getURL());
            rs = md.getTables(null, "APP", "Contacts".toUpperCase(), null);
            if (!rs.next()) {
                System.out.println("creating tables");
                createTables();
            }

            // prepare some SQL statements for frequent usage
            stmtInsertContact = dbConnection.prepareStatement(
                    "INSERT INTO Contacts " +
                    "   (jid, name, archived) " +
                    "VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);

            stmtSelectContacts =  dbConnection.prepareStatement(
                    "SELECT jid FROM Contacts WHERE archived = ?");

            stmtSelectContactByJid = dbConnection.prepareStatement(
                    "SELECT id, archived FROM Contacts WHERE jid = ?");

            stmtUpdateContactArchived = dbConnection.prepareStatement(
                    "UPDATE Contacts SET archived = ? WHERE id = ?");

            stmtUpdateContactName = dbConnection.prepareStatement(
                    "UPDATE Contacts SET name = ? WHERE id = ?");

            stmtInsertContactStatusChange = dbConnection.prepareStatement(
                    "INSERT INTO StatusChanges " +
                    "   (contactId, time, status, statusDesc) " +
                    "VALUES (?, ?, ?, ?)");

            stmtInsertOwnPresenceChange = dbConnection.prepareStatement(
                    "INSERT INTO OwnPresenceLog (time, online) VALUES (?, ?)");
        } catch (SQLException ex) {
            //ex.printStackTrace();
            throw new JacomoException("Problem with database: " + ex.getMessage());
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
                // TODO: use CONSTRAINT CHECK
                "archived   CHAR(1) )"; // 'Y' = archived, 'N' = active

        // log of online presence changes of contacts
        String strCreateTableStatusChanges =
                "CREATE TABLE StatusChanges (" +
                "contactId    INTEGER NOT NULL " +
                "CONSTRAINT Contacts_FK REFERENCES Contacts (id), " +
                "time   TIMESTAMP NOT NULL, " +
                "status   VARCHAR(20) NOT NULL, " +
                "statusDesc  LONG VARCHAR ) "; // status description

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

    public int enableContact(String contact, String name) {
        System.out.println("enable contact: " + contact + " (" + name + ")");
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
            System.out.println("id: " + id + ", archived:" + archived);
            if (id != -1) {
                if (archived) {
                    //   - yes: set 'archived' to 'N'
                    stmtUpdateContactArchived.clearParameters();
                    stmtUpdateContactArchived.setString(1, "N");
                    stmtUpdateContactArchived.setInt(2, id);
                    stmtUpdateContactArchived.executeUpdate();
                }
            } else {
                if (name == null) {
                    name = "";
                }
                //   - no: insert the contact into the table and get the generated 'id'
                stmtInsertContact.clearParameters();
                stmtInsertContact.setString(1, contact);
                stmtInsertContact.setString(2, name); // TODO: set name
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
        // look into Contacts table if the contact is already there
        int id = getContactId(contact);
        System.out.println("id: " + id);
        if (id != -1) {
            // - yes: set 'archived' to 'Y'
            try {
                stmtUpdateContactArchived.clearParameters();
                stmtUpdateContactArchived.setString(1, "Y");
                stmtUpdateContactArchived.setInt(2, id);
                stmtUpdateContactArchived.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            // - no: nothing (probably report an error)
        }
    }

    public void changeContactStatus(
            String contact,
            Date date,
            PresenceStatus status,
            String statusDesctiption) {
        int id = getContactId(contact);
        System.out.println("id: " + id);
        if (id != -1) {
            try {
                stmtInsertContactStatusChange.clearParameters();
                stmtInsertContactStatusChange.setInt(1, id);
                stmtInsertContactStatusChange.setTimestamp(2, new Timestamp(date.getTime()));
                stmtInsertContactStatusChange.setString(3, status.isOnline(status) ? "Y" : "N");
                stmtInsertContactStatusChange.setString(4, statusDesctiption);
                stmtInsertContactStatusChange.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void changeOwnPresence(boolean online) {
        Date date = Calendar.getInstance().getTime();
        System.out.println("change own presence: " +
                (online ? "ONLINE" : "OFFLINE") + ", " + date.getTime());
        try {
            stmtInsertOwnPresenceChange.clearParameters();
            stmtInsertOwnPresenceChange.setTimestamp(1, new Timestamp(date.getTime()));
            stmtInsertOwnPresenceChange.setString(2, online ? "Y" : "N");
            stmtInsertOwnPresenceChange.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void updateContactName(String contact, String name) {
        int id = getContactId(contact);
        if (id != -1) {
            try {
                stmtUpdateContactName.clearParameters();
                stmtUpdateContactName.setString(1, name);
                stmtUpdateContactName.setInt(2, id);
                stmtUpdateContactName.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public List<String> getContactsList() {
        List<String> contacts = new ArrayList<String>();
        try {
            stmtSelectContacts.clearParameters();
            stmtSelectContacts.setString(1, "N"); // not archived
            ResultSet rs = stmtSelectContacts.executeQuery();
            if (rs.next()) {
                contacts.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return contacts;
    }

    int getContactId(String contact) {
        int id = -1;
        try {
            stmtSelectContactByJid.clearParameters();
            stmtSelectContactByJid.setString(1, contact);
            ResultSet rs = stmtSelectContactByJid.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return id;
    }
    Connection dbConnection;
    PreparedStatement stmtInsertContact;
    PreparedStatement stmtInsertContactStatusChange;
    PreparedStatement stmtInsertOwnPresenceChange;
    PreparedStatement stmtSelectContacts;
    PreparedStatement stmtSelectContactByJid;
    PreparedStatement stmtUpdateContactArchived;
    PreparedStatement stmtUpdateContactName;
    
}
