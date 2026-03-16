import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class UserAuthenticator {
    private static final Logger logger = Logger.getLogger(UserAuthenticator.class.getName());
    private Properties users;

    public UserAuthenticator(String usersFilePath) throws IOException {
        users = new Properties();
        try (FileInputStream fis = new FileInputStream(usersFilePath)) {
            users.load(fis);
            logger.info("User database loaded with " + users.size() + " users");
        }
    }

    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        String storedPassword = users.getProperty(username);
        boolean authenticated = password.equals(storedPassword);
        if (authenticated) {
            logger.info("Authentication successful for user: " + username);
        } else {
            logger.warning("Authentication failed for user: " + username);
        }
        return authenticated;
    }
}

