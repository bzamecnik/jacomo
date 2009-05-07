package org.zamecnik.jacomo.lib;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

/**
 *
 * @author Bohouš
 */
public class JavaDBBackend implements DBBackend {

    public JavaDBBackend() throws JacomoException {
        String dbName = System.getProperty("jacomo.dbName");
        if ((dbName == null) || dbName.isEmpty()) {
            throw new JacomoException("No database specified.");
        }
        String strUrl = "jdbc:derby:" + dbName + ";create=true";
        String homeDir = System.getProperty("jacomo.homeDir", ".");
        System.out.println("home directory:" + homeDir);

        // Create the home directory if it doesn't exist
        // Note: Derby will create that directory automatically
        //File homeDirFile = new File(homeDir);
        //homeDirFile.mkdirs();
        
        // Note: derby.system.home property must be set BEFORE loading the
        // JavaDB (Derby) driver
        System.setProperty("derby.system.home", homeDir);

        loadDriver();
        
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
                    "SELECT id, jid, name FROM Contacts WHERE archived = ?");

            stmtSelectContactByJid = dbConnection.prepareStatement(
                    "SELECT id, archived FROM Contacts WHERE jid = ?");

            stmtSelectBotPresenceChanges = dbConnection.prepareStatement(
                    "SELECT time, online FROM BotPresenceLog ORDER BY time");

            stmtSelectContactsPresenceChanges = dbConnection.prepareStatement(
                    "SELECT contactId, time, online, status, statusDesc " +
                    "    FROM ContactPresenceLog ORDER BY contactId, time");

            stmtSelectContactsPresenceChangesById = dbConnection.prepareStatement(
                    "SELECT contactId, time, online, status, statusDesc " +
                    "    FROM ContactPresenceLog WHERE contactId = ? " +
                    "    ORDER BY contactId, time");

            stmtUpdateContactArchived = dbConnection.prepareStatement(
                    "UPDATE Contacts SET archived = ? WHERE id = ?");

            stmtUpdateContactName = dbConnection.prepareStatement(
                    "UPDATE Contacts SET name = ? WHERE id = ?");

            stmtInsertContactPresenceChange = dbConnection.prepareStatement(
                    "INSERT INTO ContactPresenceLog " +
                    "   (contactId, time, online, status, statusDesc) " +
                    "VALUES (?, ?, ?, ?, ?)");

            stmtInsertBotPresenceChange = dbConnection.prepareStatement(
                    "INSERT INTO BotPresenceLog (time, online) VALUES (?, ?)");
        } catch (SQLException ex) {
            //ex.printStackTrace();
            throw new JacomoException("Problem with database: " + ex.getMessage());
        }
    }

    void loadDriver() throws JacomoException {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            System.out.println("Loaded JavaDB driver.");
        } catch (ClassNotFoundException ex) {
            throw new JacomoException(
                    "Can't load JavaDB driver. Check the CLASSPATH." +
                    ex.getMessage());
        } catch (InstantiationException ex) {
            throw new JacomoException("Can't instantiate JavaDB driver." +
                    ex.getMessage());
        } catch (IllegalAccessException ex) {
            throw new JacomoException("Can't access JavaDB driver." +
                    ex.getMessage());
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
                // 'Y' = archived, 'N' = active
                "archived   CHAR(1) CONSTRAINT archived_CK " +
                "    CHECK (archived IN ('Y', 'N')))";

        // log of online presence changes of contacts
        String strCreateTableContactPresenceLog =
                "CREATE TABLE ContactPresenceLog (" +
                "contactId    INTEGER NOT NULL " +
                "    CONSTRAINT Contacts_FK REFERENCES Contacts (id), " +
                "time   TIMESTAMP NOT NULL, " +
                "online   CHAR(1) CONSTRAINT contact_online_CK " +
                "    CHECK (online IN ('Y', 'N')), " +
                "status   VARCHAR(20) NOT NULL, " +
                "statusDesc  LONG VARCHAR ) "; // status description

        // log of online presence of JaCoMo itself
        String strCreateTableBotPresenceLog =
                "CREATE TABLE BotPresenceLog (" +
                "time   TIMESTAMP NOT NULL PRIMARY KEY, " +
                // 'Y' = on-line, 'N' = off-line
                "online   CHAR(1) CONSTRAINT bot_online_CK " +
                "    CHECK (online IN ('Y', 'N')))";

        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute(strCreateTableContacts);
            statement.execute(strCreateTableContactPresenceLog);
            statement.execute(strCreateTableBotPresenceLog);
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
                stmtInsertContact.setString(2, name);
                stmtInsertContact.setString(3, "N");
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

    public void changeContactPresence(
            String contact,
            Date date,
            PresenceStatus status,
            String statusDesctiption) {
        int id = getContactId(contact);
        System.out.println("id: " + id);
        if (id != -1) {
            try {
                stmtInsertContactPresenceChange.clearParameters();
                stmtInsertContactPresenceChange.setInt(1, id);
                stmtInsertContactPresenceChange.setTimestamp(2, new Timestamp(date.getTime()));
                stmtInsertContactPresenceChange.setString(3, status.isOnline(status) ? "Y" : "N");
                stmtInsertContactPresenceChange.setString(4, status.toString());
                stmtInsertContactPresenceChange.setString(5, statusDesctiption);
                stmtInsertContactPresenceChange.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void changeBotPresence(boolean online) {
        Date date = Calendar.getInstance().getTime();
        System.out.println("change in bot presence: " +
                (online ? "ONLINE" : "OFFLINE") + ", "
                + DateFormat.getDateTimeInstance().format(date));
        try {
            stmtInsertBotPresenceChange.clearParameters();
            stmtInsertBotPresenceChange.setTimestamp(1, new Timestamp(date.getTime()));
            stmtInsertBotPresenceChange.setString(2, online ? "Y" : "N");
            stmtInsertBotPresenceChange.executeUpdate();
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

    public List<Contact> getContactsList() {
        List<Contact> contacts = new ArrayList<Contact>();
        try {
            stmtSelectContacts.clearParameters();
            stmtSelectContacts.setString(1, "N"); // not archived
            ResultSet rs = stmtSelectContacts.executeQuery();
            while (rs.next()) {
                contacts.add(new Contact(
                        rs.getString(2), // jid
                        rs.getString(3), // name
                        rs.getInt(1))); // id
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return contacts;
    }

    /**
     * Get alist of presence changes of the bot itself.
     * @return
     */
    public List<PresenceChange> getBotPresenceChangesList() {
        List<PresenceChange> presenceChanges = new ArrayList<PresenceChange>();
        try {
            stmtSelectBotPresenceChanges.clearParameters(); // ?
            ResultSet rs = stmtSelectBotPresenceChanges.executeQuery();
            while (rs.next()) {
                presenceChanges.add(new PresenceChange(
                    new Date(rs.getTimestamp(1).getTime()), // time
                    PresenceStatus.fromBoolean(rs.getString(2).equals("Y")) // status
                    ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return presenceChanges;
    }

    /**
     * Get a list of presence changes of all the contacts.
     * @return
     */
    public List<PresenceChange> getPresenceChangesList() {
        List<PresenceChange> presenceChanges = new ArrayList<PresenceChange>();
        try {
            stmtSelectContactsPresenceChanges.clearParameters(); // ?
            ResultSet rs = stmtSelectContactsPresenceChanges.executeQuery();
            while (rs.next()) {
                presenceChanges.add(new PresenceChange(
                    new Date(rs.getTimestamp(2).getTime()), // time
                    PresenceStatus.fromString(rs.getString(4)), // status
                    rs.getInt(1), // contact id
                    rs.getString(3) // status description
                    ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return presenceChanges;
    }

    /**
     * Get a list of presence changes of a specified contact.
     * @return
     */
    public List<PresenceChange> getContactPresenceChangesList(int contactId) {
        List<PresenceChange> presenceChanges = new ArrayList<PresenceChange>();
        try {
            stmtSelectContactsPresenceChangesById.clearParameters(); // ?
            stmtSelectContactsPresenceChangesById.setInt(1, contactId);
            ResultSet rs = stmtSelectContactsPresenceChangesById.executeQuery();
            while (rs.next()) {
                presenceChanges.add(new PresenceChange(
                    new Date(rs.getTimestamp(2).getTime()), // time
                    PresenceStatus.fromString(rs.getString(4)), // status
                    rs.getInt(1), // contact id
                    rs.getString(3) // status description
                    ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return presenceChanges;
    }

    public void dispose() {
        System.out.println("JavaDBBackend dispose()");
        try {
            dbConnection.close();
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
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
    PreparedStatement stmtInsertContactPresenceChange;
    PreparedStatement stmtInsertBotPresenceChange;
    PreparedStatement stmtSelectContacts;
    PreparedStatement stmtSelectContactByJid;
    PreparedStatement stmtSelectBotPresenceChanges;
    PreparedStatement stmtSelectContactsPresenceChanges;
    PreparedStatement stmtSelectContactsPresenceChangesById;
    PreparedStatement stmtUpdateContactArchived;
    PreparedStatement stmtUpdateContactName;
}
