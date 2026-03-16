import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ClientConfig {
    private final String serverIp;
    private final int serverPort;
    private final String user;
    private final String pass;
    private final boolean encryptionEnabled;
    private final String encryptionKey;

    public ClientConfig(String configFile) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
        }

        this.serverIp = props.getProperty("SERVER_IP");
        this.serverPort = Integer.parseInt(props.getProperty("SERVER_PORT"));
        this.user = props.getProperty("USER");
        this.pass = props.getProperty("PASS");
        this.encryptionEnabled = Boolean.parseBoolean(props.getProperty("ENCRYPTION_ENABLED", "false"));
        this.encryptionKey = props.getProperty("ENCRYPTION_KEY", "DefaultKey123");
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }
}
