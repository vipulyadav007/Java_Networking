import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class ServerConfig {
    private static final Logger logger = Logger.getLogger(ServerConfig.class.getName());
    private int serverPort;
    private int timeoutMs;
    private String csvFilePath;

    public ServerConfig(String configFilePath) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            props.load(fis);
            this.serverPort = Integer.parseInt(props.getProperty("SERVER_PORT", "8080"));
            this.timeoutMs = Integer.parseInt(props.getProperty("TIMEOUT_MS", "90000"));
            this.csvFilePath = props.getProperty("CSV_FILE_PATH", "C:/Users/raovi/Downloads/JavaAssignment2025/Data_20251211.csv");
            logger.info("Server configuration loaded successfully");
        }
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public String getCsvFilePath() {
        return csvFilePath;
    }
}

