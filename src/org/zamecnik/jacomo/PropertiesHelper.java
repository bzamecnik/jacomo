package org.zamecnik.jacomo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Bohouš
 */
public class PropertiesHelper {

    public static void initProperties() {
        String homeDir = new File(System.getProperty("user.home", ".") + File.separator + ".jacomo").getPath();
        System.setProperty("jacomo.homeDir", homeDir);
        loadProperties();
    }

    public static void setJabberProperties(
            String server, String user, String passsword) {
        System.setProperty("jacomo.jabberServer", server);
        System.setProperty("jacomo.jabberUser", user);
        System.setProperty("jacomo.jabberPassword", passsword);
        setDBName(server, user);
    }

    static void setDBName(String server, String user) {
        String dbName = new String();
        if (!server.isEmpty() && !user.isEmpty()) {
            dbName = user + "_" + server;
        }
        System.setProperty("jacomo.dbName", dbName);
    }

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

    private static final String CONFIG_FILE = "config.properties";
}
