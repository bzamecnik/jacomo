package bot;

/**
 *
 * @author Bohouš
 */
public class BotApplication {

  public static void main(String[] args) {
    initProperties();
        
    System.out.println("JaCoMo");
    Logger logger = new Logger();
    logger.login();
    logger.start();
    
    System.out.println("Type \"exit\" to shut down the program.");
    while(!System.console().readLine().startsWith("exit")) {}
    
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
    System.setProperty("jacomo.bot.jabberUser", "bohous");
    System.setProperty("jacomo.bot.jabberPassword", "elensila");
    
    System.setProperty("jacomo.homeDir",
            System.getProperty("user.home", ".") + "/.jacomo");
    
    System.setProperty("jacomo.dbName",
            System.getProperty("jacomo.bot.jabberUser")// + "_"+
            //props.getProperty("jacomo.bot.jabberServer")
        );
  }
  
}
