package org.zamecnik.jacomo.bot;

import org.zamecnik.jacomo.lib.JacomoException;

/**
 *
 * @author Bohouš
 */
public class BotApplication {

    public static void main(String[] args) {
        // TODO: run either Bot deamon or Stats GUI based on arguments
        runBot();
    }

    static void runBot() {
        initProperties();

        System.out.println("JaCoMo");
        Logger logger = null;
        try {
            logger = new Logger();
        } catch (JacomoException ex) {
            System.err.println("Can't create Logger: " + ex.getMessage());
            return;
        }
        try {
            logger.login();
        } catch (JacomoException ex) {
            System.err.println("Can't log in to the jabber server: "
                    + ex.getMessage());
            return;
        }
        logger.start();

        System.out.println("Type \"exit\" to shut down the program.");
        while (!System.console().readLine().startsWith("exit")) {
        }

        // TODO: how to implement the loop?

        logger.stop();
        logger.logout();
    }

    static void initProperties() {
        //// possibly load properties from a file:
        // Properties p = new Properties(System.getProperties());
        // FileInputStream propFile = new FileInputStream("config.txt");
        // p.load()
        // System.setProperties(p);

        System.setProperty("jacomo.bot.jabberServer", "jabber.cz");
        System.setProperty("jacomo.bot.jabberUser", "jacomobot");
        System.setProperty("jacomo.bot.jabberPassword", "comchabo");

        System.setProperty("jacomo.homeDir",
                System.getProperty("user.home", ".") + "/.jacomo");

        System.setProperty("jacomo.dbName",
                System.getProperty("jacomo.bot.jabberUser") + "_" +
                System.getProperty("jacomo.bot.jabberServer"));
    }
}
