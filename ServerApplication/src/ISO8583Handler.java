import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class ISO8583Handler {
    private static final Logger logger = Logger.getLogger(ISO8583Handler.class.getName());
    private static final byte[] BALANCE_ENQUIRY_HEADER = {0x08, 0x00};
    private static final byte[] SUCCESS_RESPONSE_HEADER = {0x08, 0x10};

    public static boolean isISO8583Message(byte[] data) {
        if (data.length < 2) {
            return false;
        }
        return data[0] == BALANCE_ENQUIRY_HEADER[0] && data[1] == BALANCE_ENQUIRY_HEADER[1];
    }

    public static void handleBalanceEnquiry(DataInputStream dis, DataOutputStream dos, String clientInfo) throws IOException {
        logger.info("ISO 8583 Balance Enquiry received from: " + clientInfo);

        // Read the remaining ISO 8583 message data
        // Header (0x0800) was already read and validated in ClientHandler

        // Read available data (timestamp and account number from client)
        int available = dis.available();
        if (available > 0) {
            byte[] messageData = new byte[Math.min(available, 256)];
            dis.readFully(messageData);
            logger.info("Received " + messageData.length + " bytes of ISO 8583 data");
        }

        logger.info("Processing balance enquiry for account");

        // Send success response header (0x0810)
        dos.write(SUCCESS_RESPONSE_HEADER);

        // Send response data
        dos.writeUTF("Balance: $10,000.00");
        dos.writeUTF("SUCCESS");
        dos.flush();

        logger.info("✓ ISO 8583 Balance Enquiry response sent to: " + clientInfo);
    }
}
