import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientLogger {
    private static final String LOG_FILE = "client_messages.log";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static synchronized void log(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = dateFormat.format(new Date());
            writer.println("[" + timestamp + "] " + message);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to write to log: " + e.getMessage());
        }
    }

    public static void logOutgoing(String message) {
        log("OUTGOING: " + message);
    }

    public static void logIncoming(String message) {
        log("INCOMING: " + message);
    }

    public static void logError(String message) {
        log("ERROR: " + message);
    }

    public static void logInfo(String message) {
        log("INFO: " + message);
    }
}

