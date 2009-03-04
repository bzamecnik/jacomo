package bot;

import java.util.Properties;

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
    Properties props = new Properties();

    props.put("jacomo.jabberServer", "jabber.cz");
    props.put("jacomo.jabberUser", "bohous");
    props.put("jacomo.jabberPassword", "elensila");
    
    props.put("jacomo.homeDir",
            System.getProperty("user.home", ".") + "/.jacomo");
    
    props.put("jacomo.dbName",
            props.getProperty("jacomo.bot.jabberUser") + "_"+ 
            props.getProperty("jacomo.bot.jabberServer")
        );
    
    System.setProperties(props);
  }
  
}
