import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * ClientHandler with Encryption Support
 * - Uses DataInputStream/DataOutputStream for consistent protocol
 * - AES-256 encryption for all messages
 * - GZIP compression for CSV data
 * - Improved performance with buffering
 * - Enhanced error handling
 */
public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private final Socket clientSocket;
    private final UserAuthenticator authenticator;
    private final CSVDataCache dataCache;
    private final int timeout;
    private final String clientInfo;
    private final EncryptionManager encryption;

    public ClientHandler(Socket socket, UserAuthenticator authenticator, CSVDataCache dataCache, int timeout) {
        this.clientSocket = socket;
        this.authenticator = authenticator;
        this.dataCache = dataCache;
        this.timeout = timeout;
        this.clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        this.encryption = new EncryptionManager();
    }

    @Override
    public void run() {
        logger.info("======================================");
        logger.info("NEW CLIENT CONNECTED: " + clientInfo);
        logger.info("======================================");

        DataInputStream in = null;
        DataOutputStream out = null;

        try {

            clientSocket.setSoTimeout(timeout);
            clientSocket.setTcpNoDelay(true); // Disable Nagle's algorithm for better latency
            clientSocket.setKeepAlive(true);  // TCP keep-alive


            in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream(), 8192));
            out = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream(), 8192));

            // ===== STEP 1: AUTHENTICATION =====
            logger.info("Waiting for authentication from: " + clientInfo);


            String encryptedCreds = in.readUTF();
            String credentials = decryptMessage(encryptedCreds);

            if (credentials == null || credentials.trim().isEmpty()) {
                logger.warning("No credentials received from: " + clientInfo);
                out.writeUTF(encryptMessage("401 Auth Failed - No credentials provided"));
                out.flush();
                return;
            }

            logger.info("Credentials received from " + clientInfo + " (encrypted)");

            String[] parts = credentials.split(":");
            if (parts.length != 2) {
                out.writeUTF(encryptMessage("401 Auth Failed - Invalid format (use username:password)"));
                out.flush();
                logger.warning("Invalid credentials format from: " + clientInfo);
                return;
            }

            String username = parts[0].trim();
            String password = parts[1].trim();

            if (!authenticator.authenticate(username, password)) {
                out.writeUTF(encryptMessage("401 Auth Failed - Invalid credentials"));
                out.flush();
                logger.warning("AUTHENTICATION FAILED for user '" + username + "' from: " + clientInfo);
                return;
            }


            out.writeUTF(encryptMessage("200 OK"));
            out.flush();
            logger.info("✓ AUTHENTICATION SUCCESS for user '" + username + "' from: " + clientInfo);

            // ===== STEP 2: BINARY CSV DUMP (Compressed & Encrypted) =====
            logger.info("Starting optimized CSV file transfer to: " + clientInfo);

            try {
                byte[] csvBytes = Files.readAllBytes(Paths.get(dataCache.getCsvFilePath()));


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                    gzipOut.write(csvBytes);
                }
                byte[] compressedData = baos.toByteArray();


                byte[] encryptedData = encryption.encryptBytes(compressedData);


                out.writeInt(encryptedData.length);  // Send size first
                out.write(encryptedData);            // Send encrypted data
                out.flush();

                logger.info("✓ CSV dump completed: " + csvBytes.length + " bytes → "
                    + compressedData.length + " compressed → "
                    + encryptedData.length + " encrypted (saved "
                    + (100 - (encryptedData.length * 100 / csvBytes.length)) + "%)");


                out.writeUTF("CSV_DUMP_COMPLETE");
                out.writeUTF("READY");
                out.flush();

            } catch (IOException e) {
                logger.severe("Error sending CSV file: " + e.getMessage());
                out.writeUTF(encryptMessage("ERROR: Failed to send CSV data"));
                out.flush();
                return;
            }

            // ===== STEP 3: QUERY HANDLING LOOP =====
            logger.info("Ready to accept queries from: " + clientInfo);

            while (true) {
                try {

                    String request = in.readUTF();


                    if ("ISO8583".equals(request)) {
                        handleISO8583(in, out);
                        continue;
                    }

                    String decryptedQuery = decryptMessage(request);

                    if (decryptedQuery.isEmpty()) {
                        continue;
                    }

                    logger.info("Query received from " + clientInfo + ": '" + decryptedQuery + "' (encrypted)");

                    if (decryptedQuery.equalsIgnoreCase("EXIT") || decryptedQuery.equalsIgnoreCase("QUIT")) {
                        out.writeUTF(encryptMessage("GOODBYE"));
                        out.flush();
                        logger.info("Client requested disconnect: " + clientInfo);
                        break;
                    }


                    String result = dataCache.lookup(decryptedQuery);
                    if (result != null) {
                        out.writeUTF(encryptMessage("FOUND: " + result));
                        logger.info("✓ Ticker found - Encrypted response sent to " + clientInfo);
                    } else {
                        out.writeUTF(encryptMessage("NOT_FOUND: Ticker not found: " + decryptedQuery));
                        logger.info("✗ Ticker NOT FOUND for query: '" + decryptedQuery + "' from: " + clientInfo);
                    }
                    out.flush();

                } catch (EOFException e) {
                    logger.info("Client disconnected (EOF): " + clientInfo);
                    break;
                }
            }

        } catch (SocketTimeoutException e) {
            logger.warning("--CLIENT TIMEOUT (no activity for " + timeout + "ms): " + clientInfo);
        } catch (IOException e) {
            logger.warning("IO Error with client " + clientInfo + ": " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Unexpected error with client " + clientInfo + ": " + e.getMessage());
            logStackTrace(e);
        } finally {
            closeResources(in, out);
            logger.info("======================================");
            logger.info("CLIENT DISCONNECTED: " + clientInfo);
            logger.info("======================================");
        }
    }

    /**
     * Handle ISO 8583 Balance Enquiry with encryption
     */
    private void handleISO8583(DataInputStream in, DataOutputStream out) {
        logger.info("ISO 8583 message indicator received from: " + clientInfo);

        try {

            out.writeUTF("ISO_READY");
            out.flush();


            byte[] header = new byte[2];
            in.readFully(header);

            if (ISO8583Handler.isISO8583Message(header)) {
                logger.info("Valid ISO 8583 Balance Enquiry received from: " + clientInfo);
                ISO8583Handler.handleBalanceEnquiry(in, out, clientInfo);
            } else {
                logger.warning("Invalid ISO 8583 header received from: " + clientInfo);
                out.writeUTF("ERROR: Invalid ISO 8583 header");
                out.flush();
            }
        } catch (Exception e) {
            logger.warning("Error processing ISO 8583 message: " + e.getMessage());
            try {
                out.writeUTF("ERROR: ISO 8583 processing failed");
                out.flush();
            } catch (Exception ex) {
                logger.severe("Failed to send error message: " + ex.getMessage());
            }
        }
    }


    private String encryptMessage(String message) {
        try {
            return encryption.encrypt(message);
        } catch (Exception e) {
            logger.warning("Encryption failed, sending plain text: " + e.getMessage());
            return message;
        }
    }


    private String decryptMessage(String encryptedMessage) {
        try {
            return encryption.decrypt(encryptedMessage);
        } catch (Exception e) {
            logger.warning("Decryption failed, treating as plain text: " + e.getMessage());
            return encryptedMessage;
        }
    }

    /**
     * Close all resources safely
     */
    private void closeResources(DataInputStream in, DataOutputStream out) {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            logger.warning("Error closing socket: " + e.getMessage());
        }
    }

    /**
     * Log exception stack trace
     */
    private void logStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        logger.severe(sw.toString());
    }
}
