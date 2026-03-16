import java.io.*;
import java.net.Socket;

/**
 * Test Client to demonstrate the Server functionality
 *
 * Features tested:
 * 1. Authentication
 * 2. Binary CSV file reception
 * 3. Ticker queries (NBK, KIB, ZAIN, etc.)
 * 4. ISO 8583 binary message handling
 */
public class TestClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Financial Data Server - Test Client  ");
        System.out.println("========================================\n");

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("✓ Connected to server: " + SERVER_HOST + ":" + SERVER_PORT);

            // ===== STEP 1: AUTHENTICATION =====
            System.out.println("\n--- STEP 1: Authentication ---");
            String credentials = "admin:admin123";
            writer.println(credentials);
            System.out.println("Sent credentials: " + credentials);

            String authResponse = reader.readLine();
            System.out.println("Server response: " + authResponse);

            if (!authResponse.startsWith("200")) {
                System.err.println("Authentication failed!");
                return;
            }

            // ===== STEP 2: RECEIVE BINARY CSV DATA =====
            System.out.println("\n--- STEP 2: Receiving Binary CSV Data ---");

            // Wait for CSV_DUMP_START notification
            String csvStartMsg = reader.readLine();
            System.out.println("Server notification: " + csvStartMsg);

            if ("CSV_DUMP_START".equals(csvStartMsg)) {
                // Read binary data
                int csvLength = dis.readInt();
                byte[] csvData = new byte[csvLength];
                dis.readFully(csvData);

                String csvContent = new String(csvData);
                System.out.println("Received CSV data (" + csvLength + " bytes):");
                System.out.println("--- CSV Content Start ---");
                System.out.println(csvContent);
                System.out.println("--- CSV Content End ---");

                // Wait for completion message
                String csvCompleteMsg = reader.readLine();
                System.out.println("Server notification: " + csvCompleteMsg);
            }

            // Read "READY" message
            String readyMsg = reader.readLine();
            System.out.println("\nServer says: " + readyMsg);

            // ===== STEP 3: QUERY TICKERS =====
            System.out.println("\n--- STEP 3: Querying Security Tickers ---");

            String[] testTickers = {"NBK", "KIB", "ZAIN", "INVALID_TICKER", "GBK", "MABANEE"};

            for (String ticker : testTickers) {
                System.out.println("\nQuerying ticker: " + ticker);
                writer.println(ticker);
                String response = reader.readLine();
                System.out.println("Response: " + response);
            }

            // ===== STEP 4: ISO 8583 TEST (BONUS) =====
            System.out.println("\n--- STEP 4: ISO 8583 Balance Enquiry Test ---");
            writer.println("ISO8583");

            // Wait for ISO_READY signal
            String isoReady = reader.readLine();
            System.out.println("Server says: " + isoReady);

            if ("ISO_READY".equals(isoReady)) {
                // Send ISO 8583 header 0x0800
                byte[] iso8583Header = {(byte)0x08, (byte)0x00};
                dos.write(iso8583Header);
                dos.flush();
                System.out.println("Sent ISO 8583 Balance Enquiry (header: 0x0800)");

                // Read response
                byte[] responseHeader = new byte[2];
                dis.readFully(responseHeader);
                System.out.printf("Received response header: 0x%02X%02X\n", responseHeader[0], responseHeader[1]);

                String isoResponse = dis.readUTF();
                System.out.println("ISO 8583 Response: " + isoResponse);

                String isoConfirm = reader.readLine();
                System.out.println("Server confirmation: " + isoConfirm);
            }

            // ===== STEP 5: EXIT =====
            System.out.println("\n--- STEP 5: Disconnecting ---");
            writer.println("EXIT");
            String exitResponse = reader.readLine();
            System.out.println("Server response: " + exitResponse);

            System.out.println("\n========================================");
            System.out.println("  Test Client Completed Successfully!  ");
            System.out.println("========================================");

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
