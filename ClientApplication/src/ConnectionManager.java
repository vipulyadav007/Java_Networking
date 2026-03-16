import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Connection Manager - Handles all network communication with the server
 * Supports encryption, auto-reconnect, and optimized data transfer
 */
public class ConnectionManager {
    private final ClientConfig config;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Thread reconnectThread;
    private EncryptionUtil encryption;

    private static final int RECONNECT_INTERVAL = 5000; // 5 seconds
    private static final String DOWNLOADED_FILE = "downloaded_data.csv";
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final int BUFFER_SIZE = 8192;

    public ConnectionManager(ClientConfig config) {
        this.config = config;

        if (config.isEncryptionEnabled()) {
            try {
                this.encryption = new EncryptionUtil(config.getEncryptionKey());
                ClientLogger.logInfo("Encryption enabled (AES-256)");
            } catch (Exception e) {
                ClientLogger.logError("Failed to initialize encryption: " + e.getMessage());
                System.err.println("Warning: Encryption initialization failed, using plain text");
            }
        }
    }

    public boolean connect() {
        try {
            ClientLogger.logInfo("Attempting to connect to " + config.getServerIp() + ":" + config.getServerPort());
            socket = new Socket(config.getServerIp(), config.getServerPort());
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(30000);

            input = new DataInputStream(new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE));
            output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE));

            connected.set(true);
            ClientLogger.logInfo("Connected successfully");

            // Perform handshake
            if (performHandshake()) {
                receiveDataFile();
                return true;
            } else {
                disconnect();
                return false;
            }

        } catch (IOException e) {
            ClientLogger.logError("Connection failed: " + e.getMessage());
            connected.set(false);
            return false;
        }
    }

    /**
     * Performs authentication handshake with the server
     */
    private boolean performHandshake() {
        try {
            System.out.println("✓ Sending authentication...");

            String credentials = config.getUser() + ":" + config.getPass();
            String toSend = credentials;

            // Encrypt if enabled
            if (encryption != null) {
                try {
                    toSend = encryption.encrypt(credentials);
                    System.out.println("✓ Credentials encrypted (AES-256)");
                    ClientLogger.logOutgoing("ENCRYPTED_CREDENTIALS: " + config.getUser() + ":********");
                } catch (Exception e) {
                    ClientLogger.logError("Encryption failed, sending plain text: " + e.getMessage());
                    System.out.println("Encryption failed, using plain text");
                }
            }

            output.writeUTF(toSend);
            output.flush();

            if (encryption == null) {
                ClientLogger.logOutgoing("CREDENTIALS: " + config.getUser() + ":********");
                System.out.println("✓ Credentials sent: " + config.getUser() + ":********");
            }

            String encryptedResponse = input.readUTF();
            String response = encryptedResponse;

            if (encryption != null) {
                try {
                    response = encryption.decrypt(encryptedResponse);
                    System.out.println("✓ Response decrypted successfully");
                    ClientLogger.logIncoming("DECRYPTED_AUTH_RESPONSE: " + response);
                } catch (Exception e) {
                    ClientLogger.logError("Decryption failed: " + e.getMessage());
                    System.out.println(" Warning: Could not decrypt response - " + e.getMessage());
                    System.out.println("   This might indicate a key mismatch between client and server");

                }
            } else {
                ClientLogger.logIncoming("AUTH_RESPONSE: " + response);
            }

            System.out.println("Server response: " + response);

            if (response != null && (response.startsWith("200") || response.contains("OK") || response.contains("SUCCESS"))) {
                System.out.println("✓ Authentication successful");
                return true;
            } else {
                System.out.println("✗ Authentication failed: " + response);
                return false;
            }

        } catch (SocketTimeoutException e) {
            ClientLogger.logError("Authentication timeout: " + e.getMessage());
            System.out.println("✗ Authentication timeout");
            return false;
        } catch (IOException e) {
            ClientLogger.logError("Handshake failed: " + e.getMessage());
            System.out.println("✗ Handshake error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Receives the binary CSV file from server with optimized buffering
     */
    private void receiveDataFile() {
        try {
            System.out.println("\n========== RECEIVING DATA FILE ==========");


            int fileSize = input.readInt();
            ClientLogger.logIncoming("File size: " + fileSize + " bytes");
            System.out.println("File size: " + fileSize + " bytes");

            if (fileSize > 0 && fileSize < 100_000_000) // Max 100MB
            {

                byte[] fileData = new byte[fileSize];
                int totalRead = 0;
                int bytesRead;
                int chunkSize = Math.min(BUFFER_SIZE, fileSize);

                while (totalRead < fileSize) {
                    int remaining = fileSize - totalRead;
                    int toRead = Math.min(chunkSize, remaining);
                    bytesRead = input.read(fileData, totalRead, toRead);

                    if (bytesRead == -1) {
                        throw new IOException("Unexpected end of stream");
                    }

                    totalRead += bytesRead;

                    if (fileSize > 1_000_000 && totalRead % 1_000_000 == 0) {
                        int progress = (int) ((totalRead * 100.0) / fileSize);
                        System.out.print("\rDownloading... " + progress + "%");
                    }
                }

                if (fileSize > 1_000_000) {
                    System.out.println("\rDownloading... 100%");
                }

                if (encryption != null) {
                    try {
                        fileData = encryption.decryptBytes(fileData);
                        System.out.println("✓ Data decrypted");
                        ClientLogger.logInfo("Data decrypted successfully");
                    } catch (Exception e) {
                        ClientLogger.logError("Decryption failed: " + e.getMessage());
                        System.out.println("Could not decrypt data, saving as-is");
                    }
                }

                try (FileOutputStream fos = new FileOutputStream(DOWNLOADED_FILE);
                     BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                    bos.write(fileData);
                    bos.flush();
                    System.out.println("✓ Data saved to: " + DOWNLOADED_FILE);
                    ClientLogger.logInfo("Data file saved: " + DOWNLOADED_FILE + " (" + fileSize + " bytes)");
                }

                displayFirstRows(fileData, 15);

            } else if (fileSize >= 100_000_000) {
                System.out.println("✗ File too large: " + fileSize + " bytes (max 100MB)");
                ClientLogger.logError("File size exceeds maximum: " + fileSize);
            } else {
                System.out.println("No data file available.");
            }


            String completionMsg = input.readUTF();
            ClientLogger.logIncoming("SERVER: " + completionMsg);


            String readyMsg = input.readUTF();
            ClientLogger.logIncoming("SERVER: " + readyMsg);
            System.out.println("✓ Server is ready for queries");

            System.out.println("========================================\n");

        } catch (IOException e) {
            ClientLogger.logError("Failed to receive data file: " + e.getMessage());
            System.out.println("✗ Warning: Could not receive data file from server");
            System.out.println("  Error: " + e.getMessage());
        }
    }

    /**
     * Displays the first N rows of CSV data
     */
    private void displayFirstRows(byte[] data, int numRows) {
        try {
            String csvData = new String(data, StandardCharsets.UTF_8);
            String[] lines = csvData.split("\n");

            System.out.println("\n--- First " + numRows + " Rows ---");
            int rowsToDisplay = Math.min(numRows, lines.length);
            for (int i = 0; i < rowsToDisplay; i++) {
                System.out.println(lines[i]);
            }
            System.out.println("---------------------------\n");

        } catch (Exception e) {
            ClientLogger.logError("Failed to display rows: " + e.getMessage());
        }
    }

    /**
     * Starts auto-reconnect mechanism with exponential backoff
     */
    public void startAutoReconnect() {
        reconnectThread = new Thread(() -> {
            int attempts = 0;
            while (running.get() && attempts < MAX_RECONNECT_ATTEMPTS) {
                if (!connected.get()) {
                    System.out.println("\n⟳ Attempting to reconnect... (Attempt " + (attempts + 1) + "/" + MAX_RECONNECT_ATTEMPTS + ")");
                    if (connect()) {
                        System.out.println("✓ Reconnected successfully!\n");
                        attempts = 0; // Reset counter on success
                    } else {
                        attempts++;
                        int delay = Math.min(RECONNECT_INTERVAL * attempts, 30000); // Max 30 seconds
                        System.out.println("✗ Reconnection failed. Retrying in " + (delay / 1000) + " seconds...");
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                } else {
                    attempts = 0;
                    try {
                        Thread.sleep(RECONNECT_INTERVAL);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }

            if (attempts >= MAX_RECONNECT_ATTEMPTS) {
                System.out.println("\n✗ Max reconnection attempts reached. Please restart the application.");
                ClientLogger.logError("Max reconnection attempts reached");
            }
        }, "ReconnectThread");
        reconnectThread.setDaemon(true);
        reconnectThread.start();
    }

    /**
     * Searches for a company by ticker symbol
     * Supports encrypted communication
     */
    public void searchByTicker(String ticker) {
        if (!connected.get()) {
            System.out.println("✗ Not connected to server");
            return;
        }

        try {
            String toSend = ticker;

            if (encryption != null) {
                try {
                    toSend = encryption.encrypt(ticker);
                } catch (Exception e) {
                    ClientLogger.logError("Encryption failed for ticker: " + e.getMessage());
                }
            }

            synchronized (output) {
                output.writeUTF(toSend);
                output.flush();
                ClientLogger.logOutgoing("TICKER_SEARCH: " + ticker);
            }


            String response = input.readUTF();


            if (encryption != null && response != null) {
                try {
                    response = encryption.decrypt(response);
                } catch (Exception e) {
                }
            }

            ClientLogger.logIncoming("TICKER_RESULT: " + response);

            System.out.println("\n--- Ticker Details ---");
            System.out.println(response);
            System.out.println("---------------------\n");

        } catch (IOException e) {
            ClientLogger.logError("Ticker search failed: " + e.getMessage());
            System.out.println("✗ Search failed: " + e.getMessage());
            handleDisconnection();
        }
    }

    /**
     * Sends ISO 8583 Balance Enquiry message
     */
    public void checkBalance() {
        if (!connected.get()) {
            System.out.println("✗ Not connected to server");
            return;
        }

        try {
            synchronized (output) {
                output.writeUTF("ISO8583");
                output.flush();
                ClientLogger.logOutgoing("ISO8583 command sent");
            }

            socket.setSoTimeout(5000); // 5 second timeout
            String readyMsg = input.readUTF();
            socket.setSoTimeout(30000); // Reset to 30 seconds

            ClientLogger.logIncoming("ISO_READY response: " + readyMsg);

            if (!"ISO_READY".equals(readyMsg)) {
                System.out.println("✗ Server not ready for ISO message. Received: " + readyMsg);
                return;
            }

            System.out.println("✓ Server ready for ISO message");

            // Create ISO 8583 message with header 0x0800
            byte[] isoMessage = ISO8583Message.createBalanceEnquiry(null);

            System.out.println("\n--- Sending ISO 8583 Balance Enquiry ---");
            System.out.println("Message Type: 0x0800 (Balance Enquiry)");
            System.out.println("Hex Dump:");
            System.out.println(ISO8583Message.toHexString(isoMessage));


            synchronized (output) {
                output.write(isoMessage[0]);
                output.write(isoMessage[1]);
                output.flush();
            }
            ClientLogger.logOutgoing("ISO8583 Header sent: " + String.format("%02X %02X", isoMessage[0], isoMessage[1]));


            byte[] responseHeader = new byte[2];
            input.readFully(responseHeader);
            ClientLogger.logIncoming("ISO8583 Response Header: " + String.format("%02X %02X", responseHeader[0], responseHeader[1]));

            String responseMessage = input.readUTF();
            ClientLogger.logIncoming("ISO8583 Response Message: " + responseMessage);

            String statusMsg = input.readUTF();
            ClientLogger.logIncoming("ISO8583_STATUS: " + statusMsg);

            System.out.println("\n--- Server Response ---");
            System.out.println("Response Header: " + ISO8583Message.toHexString(responseHeader));
            System.out.println("Message: " + responseMessage);

            if (ISO8583Message.isBalanceResponseSuccess(responseHeader)) {
                System.out.println("\n✓ Balance Enquiry Successful");
            } else {
                System.out.println("\n✗ Balance Enquiry Failed");
            }
            System.out.println("----------------------\n");

        } catch (SocketTimeoutException e) {
            ClientLogger.logError("ISO operation timeout: " + e.getMessage());
            System.out.println("✗ Operation timeout");
        } catch (IOException e) {
            ClientLogger.logError("Balance check failed: " + e.getMessage());
            System.out.println("✗ Balance check failed: " + e.getMessage());
            handleDisconnection();
        }
    }

    /**
     * Handles disconnection event
     */
    private void handleDisconnection() {
        if (connected.get()) {
            connected.set(false);
            System.out.println("\n-- Connection lost!");
            ClientLogger.logError("Connection lost");
            disconnect();
        }
    }

    /**
     * Closes the connection gracefully
     */
    public void disconnect() {
        connected.set(false);

        try {
            if (output != null) {
                output.writeUTF("EXIT");
                output.flush();
            }
            closeResources();
            ClientLogger.logInfo("Disconnected from server");
        } catch (Exception e) {
            ClientLogger.logError("Error during disconnect: " + e.getMessage());
        }
    }

    /**
     * Close all resources properly
     */
    private void closeResources() {
        try { if (output != null) output.close(); } catch (Exception ignored) {}
        try { if (input != null) input.close(); } catch (Exception ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception ignored) {}
    }

    /**
     * Shuts down the connection manager
     */
    public void shutdown() {
        running.set(false);
        disconnect();
        if (reconnectThread != null) {
            reconnectThread.interrupt();
            try {
                reconnectThread.join(1000); // Wait max 1 second
            } catch (InterruptedException ignored) {}
        }
    }

    public boolean isConnected() {
        return connected.get();
    }
}
