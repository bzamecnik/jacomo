package org.zamecnik.jacomo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Properties helper. A helper class used for properties handling:
 * setting, initializing, loading from and saving to a config file.
 * @author Bohumir Zamecnik
 */
public class PropertiesHelper {

    /**
     * Initialize properties. Set {@code jacomo.homeDir} property and load
     * properties from a config file.
     */
    public static void initProperties() {
        String homeDir = new File(System.getProperty("user.home", ".") + File.separator + ".jacomo").getPath();
        System.setProperty("jacomo.homeDir", homeDir);
        loadProperties();
    }

    /**
     * Set Jabber specific properties. Also set a proper database name.
     * @param server Jabber server set to {@code jacomo.jabberServer} property
     * @param user Jabber account user name set to {@code jacomo.jabberUser} property
     * @param password Jabber account password set to {@code jacomo.jabberPassword} property
     */
    public static void setJabberProperties(
            String server, String user, String password) {
        System.setProperty("jacomo.jabberServer", server);
        System.setProperty("jacomo.jabberUser", user);
        System.setProperty("jacomo.jabberPassword", password);
        setDBName(server, user);
    }

    /**
     * Compute a database name and set it as a property.
     * The database name is in form "USER_SERVER", eg. john.doe_jabber.com
     * and is set to {@code jacomo.dbName} property.
     * @param server Jabber server
     * @param user Jabber account user name
     */
    static void setDBName(String server, String user) {
        String dbName = new String();
        if (!server.isEmpty() && !user.isEmpty()) {
            dbName = user + "_" + server;
        }
        System.setProperty("jacomo.dbName", dbName);
    }

    /**
     * Load properties from a config file.
     * The name of config file is specified in {@code CONFIG_FILE} and the
     * directory is specified in the {@code jacomo.homeDir} property.
     * If the file doesn't exist no properties are loaded.
     */
    public static void loadProperties() {
        // load properties from a file
        Properties loadedProps = new Properties();
        Properties systemProps = new Properties();
        File propFile = new File(System.getProperty("jacomo.homeDir")
                + File.separator + CONFIG_FILE);
        try {
            loadedProps.load(new FileInputStream(propFile));
            systemProps.putAll(System.getProperties());
            // loaded properties have higher priority
            systemProps.putAll(loadedProps);
            System.setProperties(systemProps);
        } catch (FileNotFoundException ex) {
            System.err.println("Properties file " + propFile + " not found.");
        } catch (IOException ex) {
            System.err.println("Error loading properties file: " + ex.getMessage());
        }
        setDBName(System.getProperty("jacomo.jabberServer",""),
                System.getProperty("jacomo.jabberUser",""));
    }

    /**
     * Save properties to a config file.
     * The name of config file is specified in {@code CONFIG_FILE} and the
     * directory is specified in the {@code jacomo.homeDir} property.
     * The file is automatically created if it doesn't exist.
     */
    public static void saveProperties() {
        // save properties to a file
        Properties props = new Properties();
        props.setProperty("jacomo.jabberServer",
                System.getProperty("jacomo.jabberServer"));
        props.setProperty("jacomo.jabberUser",
                System.getProperty("jacomo.jabberUser"));
        // TODO: really store the password???
        props.setProperty("jacomo.jabberPassword",
                System.getProperty("jacomo.jabberPassword"));
        File propFile = new File(System.getProperty("jacomo.homeDir")
                + File.separator + CONFIG_FILE);
        try {
            propFile.createNewFile();
            props.store(new FileOutputStream(propFile), null);
        } catch (FileNotFoundException ex) {
            System.err.println("Properties file " + propFile + " not found.");
        } catch (IOException ex) {
            System.err.println("Error saving properties file: " + ex.getMessage());
        }
    }

    /** Config file name. */
    private static final String CONFIG_FILE = "config.properties";
}
