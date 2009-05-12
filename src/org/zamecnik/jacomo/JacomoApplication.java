package org.zamecnik.jacomo;

import javax.swing.SwingUtilities;
import org.zamecnik.jacomo.bot.*;
import org.zamecnik.jacomo.lib.*;
import org.zamecnik.jacomo.stats.*;

/**
 * JaCoMo Application acts as a main application class. It holds references
 * to bot and stats part (BotApplication and StatsApplication instances).
 * The GUI is started using the {@link #main(String[] args)} function.
 * <p>
 * JacomoApplication is a singleton class and reference to its only instance
 * can be obtained by {@link #getInstance()} function.
 * <p>
 * JacomoApplication instance holds a reference to database connection resource
 * shared by the bot and stats part. The database connection is configured in
 * the GUI and until it is done so bot and stats parts can't be used. After
 * usage the should be disposed using {@link #dispose()} member function to free
 * resources.
 * @author Bohumir Zamecnik
 */
public class JacomoApplication {

    /**
     * Private constructor. To be called by {@link #getInstance()} only.
     */
    private JacomoApplication() {
    }

    /**
     * Get singleton instance. Create the instance on the first time calling.
     * @return JacomoApplication singleton instance
     */
    public static JacomoApplication getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new JacomoApplication();
        }
        return singletonInstance;
    }

    /**
     * Main function.
     * @param args
     */
    public static void main(String[] args) {
        // TODO: get stored contact filter configuration

        PropertiesHelper.initProperties();

        // run GUI in the Event Dispatching Thread
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    /**
     * Start database. Start the database backend, prepare bot application and
     * stats application.
     * @return true if it started ok and is prepared
     */
    public boolean startDatabase() {
        try {
            dbBackend = new JavaDBBackend();
        } catch (JacomoException ex) {
            System.err.println("Can't connect to database: " + ex.getMessage());
            return false;
        }
        if (botApp == null) {
            botApp = new BotApplication(dbBackend);
        } else {
            botApp.setDbBackend(dbBackend);
        }
        statsApp = new StatsApplication(dbBackend);
        return true;
    }

    /**
     * Stop database. Stop bot and stats application an then database.
     */
    public void stopDatabase() {
        if (dbBackend != null) {
            botApp.shutdown();
            dbBackend.dispose();
            dbBackend = null;
            statsApp = null;
        }
    }

    /**
     * Dispose the application and free resources.
     */
    public void dispose() {
        System.out.println("JacomoApplication dispose()");
        if (botApp != null) {
            botApp.shutdown();
        }
        stopDatabase();
    }

    /**
     * Get bot application instance.
     * @return bot application instance
     */
    public BotApplication getBotApp() {
        return botApp;
    }

    /**
     * Get stats application instance.
     * @return stats application instance
     */
    public StatsApplication getStatsApp() {
        return statsApp;
    }
    /** JacomoApplication singleton instance */
    private static JacomoApplication singletonInstance;
    /** Database backend resource */
    private DBBackend dbBackend;
    /** Bot application */
    private BotApplication botApp;
    /** Stats application */
    private StatsApplication statsApp;
}
