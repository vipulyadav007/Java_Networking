import java.io.IOException;
import java.util.logging.*;

public class ServerLogger {
    public static void setupLogger() throws IOException {
        Logger rootLogger = Logger.getLogger("");


        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }


        FileHandler fileHandler = new FileHandler("server_activities.log", true);
        fileHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(fileHandler);

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(consoleHandler);

        rootLogger.setLevel(Level.INFO);
    }
}

