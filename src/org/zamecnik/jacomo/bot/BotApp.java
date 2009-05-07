package org.zamecnik.jacomo.bot;

import org.zamecnik.jacomo.lib.DBBackend;
import org.zamecnik.jacomo.lib.JacomoException;

/**
 *
 * @author Bohouš
 */
public class BotApp {
    public boolean startJabber() {
        if (dbBackend == null) {
            return false;
        }
        if (logger != null) {
            stopJabber();
        }
        try {
            logger = new Logger(dbBackend);
        } catch (JacomoException ex) {
            System.err.println("Can't create Logger: " + ex.getMessage());
            return false;
        }
        try {
            logger.login();
        } catch (JacomoException ex) {
            System.err.println("Can't log in to the jabber server: " + ex.getMessage());
            return false;
        }
        return true;
    }

    public void stopJabber() {
        if (logger != null) {
            logger.logout();
            logger = null;
        }
    }

    public void startLogging() {
        if (logger != null) {
            logger.start();
        }
    }

    public void stopLogging() {
        if (logger != null) {
            logger.stop();
        }
    }

    public void dispose() {
        System.out.println("BotApp dispose()");
        stopLogging();
        stopJabber();
    }

    private Logger logger;
    private DBBackend dbBackend;

    /**
     * @param dbBackend the dbBackend to set
     */
    public void setDbBackend(DBBackend dbBackend) {
        this.dbBackend = dbBackend;
    }
}
