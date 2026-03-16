import java.io.*;
import java.net.Socket;
import java.util.zip.GZIPInputStream;

/**
 * Optimized Test Client with Encryption Support
 * Tests all encrypted features:
 * 1. Encrypted authentication
 * 2. Compressed & encrypted CSV reception
 * 3. Encrypted ticker queries
 * 4. Encrypted ISO 8583 transactions
 */
public class EncryptedTestClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final EncryptionManager encryption = new EncryptionManager();

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║  🔒 ENCRYPTED TEST CLIENT                     ║");
        System.out.println("║     AES-256 + GZIP Compression                 ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("✓ Connected to server: " + SERVER_HOST + ":" + SERVER_PORT);

            // ===== STEP 1: ENCRYPTED AUTHENTICATION =====
            System.out.println("\n--- STEP 1: Encrypted Authentication ---");
            String credentials = "admin:admin123";
            String encryptedCredentials = encryption.encrypt(credentials);

            System.out.println("Plain credentials: " + credentials);
            // Fix: Only show substring if length is sufficient
            if (encryptedCredentials.length() > 40) {
                System.out.println("Encrypted credentials: " + encryptedCredentials.substring(0, 40) + "...");
            } else {
                System.out.println("Encrypted credentials: " + encryptedCredentials);
            }

            writer.println(encryptedCredentials);

            String authResponse = reader.readLine();
            String decryptedAuth = encryption.decrypt(authResponse);
            System.out.println("Server response (decrypted): " + decryptedAuth);

            if (!decryptedAuth.equals("200 OK")) {
                System.err.println("✗ Authentication failed!");
                return;
            }
            System.out.println("✓ Authentication successful (encrypted)");

            // ===== STEP 2: RECEIVE COMPRESSED & ENCRYPTED CSV =====
            System.out.println("\n--- STEP 2: Receiving Compressed & Encrypted CSV ---");

            String csvStartMsg = reader.readLine();
            String decryptedStart = encryption.decrypt(csvStartMsg);
            System.out.println("Server notification: " + decryptedStart);

            if (decryptedStart.equals("CSV_DUMP_START")) {
                int encryptedLength = dis.readInt();
                byte[] encryptedData = new byte[encryptedLength];
                dis.readFully(encryptedData);

                System.out.println("Received encrypted data: " + encryptedLength + " bytes");

                byte[] compressedData = encryption.decryptBytes(encryptedData);
                System.out.println("Decrypted to compressed: " + compressedData.length + " bytes");

                // Decompress data
                ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
                GZIPInputStream gzipIn = new GZIPInputStream(bais);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzipIn.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }

                byte[] originalData = baos.toByteArray();
                String csvContent = new String(originalData);

                int compressionRatio = 100 - (compressedData.length * 100 / originalData.length);

                System.out.println("Decompressed to original: " + originalData.length + " bytes");
                System.out.println("📊 Compression ratio: " + compressionRatio + "%");
                System.out.println("🔒 Encryption: AES-256");

                System.out.println("\n--- CSV Content Preview (first 3 lines) ---");
                String[] lines = csvContent.split("\n");
                for (int i = 0; i < Math.min(3, lines.length); i++) {
                    System.out.println(lines[i]);
                }
                System.out.println("... (" + (lines.length - 3) + " more lines)");


                String csvComplete = encryption.decrypt(reader.readLine());
                System.out.println("\nServer notification: " + csvComplete);
            }

            String ready = encryption.decrypt(reader.readLine());
            System.out.println("Server notification: " + ready);
            System.out.println("✓ Encrypted CSV transfer completed");

            // ===== STEP 3: ENCRYPTED TICKER QUERIES =====
            System.out.println("\n--- STEP 3: Encrypted Ticker Queries ---");

            String[] testTickers = {"NBK", "ZAIN", "KIB", "INVALID_TICKER", "BOUBYAN"};

            for (String ticker : testTickers) {
                System.out.println("\n📤 Querying ticker: " + ticker);

                String encryptedQuery = encryption.encrypt(ticker);
                System.out.println("   Encrypted query length: " + encryptedQuery.length() + " chars");
                writer.println(encryptedQuery);

                String encryptedResponse = reader.readLine();
                String response = encryption.decrypt(encryptedResponse);

                if (response.startsWith("FOUND:")) {
                    System.out.println("✓ " + response.substring(7));
                } else if (response.startsWith("NOT_FOUND:")) {
                    System.out.println("✗ " + response.substring(11));
                }
            }

            // ===== STEP 4: ENCRYPTED ISO 8583 =====
            System.out.println("\n--- STEP 4: Encrypted ISO 8583 Balance Enquiry ---");

            String encryptedISO = encryption.encrypt("ISO8583");
            writer.println(encryptedISO);

            String isoReady = encryption.decrypt(reader.readLine());
            System.out.println("Server response: " + isoReady);

            if (isoReady.equals("ISO_READY")) {
                // Send encrypted ISO 8583 header
                byte[] iso8583Header = {(byte)0x08, (byte)0x00};
                byte[] encryptedHeader = encryption.encryptBytes(iso8583Header);

                dos.writeInt(encryptedHeader.length);
                dos.write(encryptedHeader);
                dos.flush();

                System.out.println("📤 Sent encrypted ISO 8583 request (0x0800)");

                // Read encrypted response
                int responseLength = dis.readInt();
                byte[] encryptedISResponse = new byte[responseLength];
                dis.readFully(encryptedISResponse);

                byte[] decryptedResponse = encryption.decryptBytes(encryptedISResponse);

                System.out.printf("📥 Received encrypted response (%d bytes)\n", responseLength);
                System.out.printf("   Decrypted response: 0x%02X%02X\n",
                    decryptedResponse[0], decryptedResponse[1]);

                String message = new String(decryptedResponse, 2, decryptedResponse.length - 2);
                System.out.println("   Message: " + message);

                String isoConfirm = encryption.decrypt(reader.readLine());
                System.out.println("   Server confirmation: " + isoConfirm);
                System.out.println("✓ Encrypted ISO 8583 transaction completed");
            }

            // ===== STEP 5: DISCONNECT =====
            System.out.println("\n--- STEP 5: Encrypted Disconnect ---");
            String encryptedExit = encryption.encrypt("EXIT");
            writer.println(encryptedExit);

            String exitResponse = encryption.decrypt(reader.readLine());
            System.out.println("Server response: " + exitResponse);

            System.out.println("\n╔════════════════════════════════════════════════╗");
            System.out.println("║  ✓ ALL ENCRYPTED TESTS COMPLETED!             ║");
            System.out.println("║                                               ║");
            System.out.println("║  Security Features Tested:                    ║");
            System.out.println("║  • AES-256 Encryption on all messages         ║");
            System.out.println("║  • GZIP Compression for large data            ║");
            System.out.println("║  • Binary encryption for ISO 8583             ║");
            System.out.println("╚════════════════════════════════════════════════╝");

        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
