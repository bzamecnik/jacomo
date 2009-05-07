package org.zamecnik.jacomo;

import java.util.Properties;
import javax.swing.SwingUtilities;
import org.zamecnik.jacomo.bot.*;
import org.zamecnik.jacomo.lib.DBBackend;
import org.zamecnik.jacomo.stats.*;
import org.zamecnik.jacomo.lib.JacomoException;
import org.zamecnik.jacomo.lib.JavaDBBackend;

/**
 *
 * @author Bohouš
 */
public class JacomoApplication {

    private JacomoApplication() {
        botApp = new BotApp();
    }
    private static JacomoApplication singletonInstance;

    public static JacomoApplication getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new JacomoApplication();
        }
        return singletonInstance;
    }

    public static void main(String[] args) {

        // TODO: run either Bot deamon or Stats GUI based on arguments
//        if (args.length > 0) {
//            if (args[0].equals("-bot")) {
//                runBot();
//            } else if (args[0].equals("-stats")) {
//                runStats();
//            }
//        }

        // TODO: get stored properties from a file
        // TODO: get stored contact filter configuration

        initProperties();

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    void runBot() {
        initProperties();

        System.out.println("JaCoMo bot");
        Logger logger = null;
        try {
            try {
                logger = new Logger(dbBackend);
            } catch (JacomoException ex) {
                System.err.println("Can't create Logger: " + ex.getMessage());
                return;
            }
            try {
                logger.login();
            } catch (JacomoException ex) {
                System.err.println("Can't log in to the jabber server: " + ex.getMessage());
                return;
            }
            logger.start();

            System.out.println("Type \"exit\" to shut down the program.");
            while (!System.console().readLine().startsWith("exit")) {
            }

            // TODO: how to implement the loop?

            logger.stop();
            logger.logout();
        } finally {
            logger.dispose();
        }
    }

    void runStats() {
        initProperties();

        System.out.println("JaCoMo stats");

    //presenceManager.show();

    //Interpreter contactsCount = new ContactsCount(presenceManager);
    //ContactsCount.Result result = (ContactsCount.Result)contactsCount.interpret();
    //System.out.println("ContactsCount: " + result.getContactsCount());

    //        Collection<IntervalList> intervalLists = presenceManager.getAllPresenceIntervals();
    //
    //        Histogram histogram = Histogram.hourHistogram;
    //        Quantizer quantizer = Quantizer.hourQuantizer;
    ////        Histogram histogram = Histogram.weekdayHistogram;
    ////        Quantizer quantizer = Quantizer.weekdayQuantizer;
    //
    //        int[] quantizationSums = quantizer.quantizeAndSum(intervalLists);
    //        System.out.println("quantization sums:");
    //        for (int i = 0; i < quantizationSums.length; i++) {
    //            System.out.print(quantizationSums[i] + ", ");
    //        }
    //        System.out.println();
    //
    //        double[] scaledHistogramResult = histogram.computeScaledHistogram(quantizationSums);
    //        System.out.println("histogram:");
    //        for (int i = 0; i < scaledHistogramResult.length; i++) {
    //            System.out.print(scaledHistogramResult[i] + ", ");
    //        }
    //        System.out.println();

    }

    static void initProperties() {
        String homeDir = System.getProperty("user.home", ".") + "/.jacomo";
        System.setProperty("jacomo.homeDir", homeDir);

//        // load properties from a file:
//        Properties p = new Properties();
//        String propFile = homeDir + "/jacomo-config.properties";
//        try {
//            p.load(new FileReader(propFile));
//        } catch (FileNotFoundException ex) {
//            System.err.println("Properties file " + propFile + " not found.");
//            System.exit(1);
//        } catch (IOException ex) {
//            System.err.println("Error loading properties file: " + ex.getMessage());
//            System.exit(1);
//        }
//        p.putAll(System.getProperties());
//        System.setProperties(p);
    }

    public static void setJabberProperties(
            String server, String user, String passsword) {
        Properties props = new Properties();
        props.putAll(System.getProperties());
        props.setProperty("jacomo.jabberServer", server);
        props.setProperty("jacomo.jabberUser", user);
        props.setProperty("jacomo.jabberPassword", passsword);
        String dbName = new String();
        if (!server.isEmpty() && !user.isEmpty()) {
            dbName = user + "_" + server;
        }
        props.setProperty("jacomo.dbName", dbName);
        System.setProperties(props);
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
    private DBBackend dbBackend;
    private BotApp botApp;
    private StatsApp statsApp;

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
}
