package org.zamecnik.jacomo;

import org.zamecnik.jacomo.bot.*;
import org.zamecnik.jacomo.stats.*;
import org.zamecnik.jacomo.stats.interpret.*;
import org.zamecnik.jacomo.lib.JacomoException;

import java.util.Calendar;
import java.text.DateFormat;

/**
 *
 * @author Bohouš
 */
public class JacomoApplication {

    public static void main(String[] args) {
//        Calendar cal = Calendar.getInstance();
//        //System.out.println("now:" + DateFormat.getDateTimeInstance().format(cal.getTime()));
//        System.out.println("now:" + cal.getTime().getTime());
//        //cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
//        cal.set(Calendar.AM_PM, 0);
//        cal.set(Calendar.HOUR, 0);
//        cal.set(Calendar.MINUTE, 0);
//        cal.set(Calendar.SECOND, 0);
//        cal.set(Calendar.MILLISECOND, 0);
//        System.out.println("start of week:" + DateFormat.getDateTimeInstance().format(cal.getTime()));
//        System.out.println("start of week:" + cal.getTime().getTime());

        // TODO: run either Bot deamon or Stats GUI based on arguments
        if (args.length > 0) {
            if (args[0].equals("-bot")) {
                runBot();
            } else if (args[0].equals("-stats")) {
                runStats();
                //org.zamecnik.jacomo.stats.Gui.main(args);
            }
        }
    }

    static void runBot() {
        initProperties();

        System.out.println("JaCoMo bot");
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

    static void runStats() {
        initProperties();

        System.out.println("JaCoMo stats");
        PresenceManager presenceManager = null;
        try {
            presenceManager = new PresenceManager();
        } catch (JacomoException ex) {
            System.err.println("Can't create PresenceManager: " + ex.getMessage());
            return;
        }
        presenceManager.initialize();
        //presenceManager.show();

        //Interpreter contactsCount = new ContactsCount(presenceManager);
        //ContactsCount.Result result = (ContactsCount.Result)contactsCount.interpret();
        //System.out.println("ContactsCount: " + result.getContactsCount());

        Histogram histogram = Histogram.hourHistogram;
        histogram.setIntervalLists(presenceManager.getAllPresenceIntervals());
        //((Histogram)histogram).setIntervals(presenceManager.getContactPresenceIntervals(71));
        Histogram.Result result = (Histogram.Result)histogram.interpret();
        System.out.println("histogram: " + result.toString());
    }

    static void initProperties() {
        //// possibly load properties from a file:
        // Properties p = new Properties(System.getProperties());
        // FileInputStream propFile = new FileInputStream("config.txt");
        // p.load()
        // System.setProperties(p);

        System.setProperty("jacomo.bot.jabberServer", "jabber.cz");
        //System.setProperty("jacomo.bot.jabberUser", "jacomobot");
        //System.setProperty("jacomo.bot.jabberPassword", "comchabo");
        System.setProperty("jacomo.bot.jabberUser", "bohous");
        System.setProperty("jacomo.bot.jabberPassword", "elensila");

        System.setProperty("jacomo.homeDir",
                System.getProperty("user.home", ".") + "/.jacomo");

        System.setProperty("jacomo.dbName",
                System.getProperty("jacomo.bot.jabberUser") + "_" +
                System.getProperty("jacomo.bot.jabberServer"));
    }
}
