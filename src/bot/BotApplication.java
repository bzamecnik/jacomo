package bot;

/**
 *
 * @author Bohouš
 */
public class BotApplication {

  public static void main(String[] args) {
    System.out.println("JaCoMo");
    Logger logger = new Logger();
    logger.login();
    logger.start();
    
    System.out.println("Type \"exit\" to shut down the program.");
    while(!System.console().readLine().startsWith("exit")) {}
    
    logger.stop();
    logger.logout();
  }
  
}
