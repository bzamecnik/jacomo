package org.zamecnik.jacomo;

import javax.swing.SwingUtilities;
import org.zamecnik.jacomo.bot.*;
import org.zamecnik.jacomo.lib.*;
import org.zamecnik.jacomo.stats.*;

/**
 *
 * @author Bohouš
 */
public class JacomoApplication {

    private JacomoApplication() {
        botApp = new BotApp();
    }

    public static JacomoApplication getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new JacomoApplication();
        }
        return singletonInstance;
    }

    public static void main(String[] args) {
        // TODO: get stored properties from a file
        // TODO: get stored contact filter configuration

        PropertiesHelper.initProperties();

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    public boolean startDatabase() {
        try {
            dbBackend = new JavaDBBackend();
        } catch (JacomoException ex) {
            System.err.println("Can't connect to database: " + ex.getMessage());
            return false;
        }
        botApp.setDbBackend(dbBackend);
        statsApp = new StatsApp(dbBackend);
        return true;
    }

    public void stopDatabase() {
        if (dbBackend != null) {
            dbBackend.dispose();
            dbBackend = null;
            statsApp = null;
        }
    }

    public void dispose() {
        System.out.println("JacomoApplication dispose()");
        botApp.dispose();
        stopDatabase();
    }

    /**
     * @return the botApp
     */
    public BotApp getBotApp() {
        return botApp;
    }

    /**
     * @return the statsApp
     */
    public StatsApp getStatsApp() {
        return statsApp;
    }
    private static JacomoApplication singletonInstance;
    private DBBackend dbBackend;
    private BotApp botApp;
    private StatsApp statsApp;
}
