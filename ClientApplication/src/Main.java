import java.util.Scanner;


public class Main {
    private static final String CONFIG_FILE = "client.properties";

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║  FINANCIAL DATA CLIENT APPLICATION     ║");
        System.out.println("║           Version 1.0                  ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        ConnectionManager connectionManager = null;
        Scanner scanner = new Scanner(System.in);

        try {

            ClientConfig config = new ClientConfig(CONFIG_FILE);
            ClientLogger.logInfo("Application started");
            System.out.println("-- Configuration loaded from " + CONFIG_FILE);
            System.out.println("  Server: " + config.getServerIp() + ":" + config.getServerPort());
            System.out.println("  User: " + config.getUser());


            connectionManager = new ConnectionManager(config);


            System.out.println("\nConnecting to server...");
            if (!connectionManager.connect()) {
                System.out.println("✗ Initial connection failed. Starting auto-reconnect...");
            }

            connectionManager.startAutoReconnect();

            runConsoleLoop(scanner, connectionManager);

        } catch (Exception e) {
            System.err.println("✗ Fatal error: " + e.getMessage());
            ClientLogger.logError("Fatal error: " + e.getMessage());
            e.printStackTrace();
        } finally {

            if (connectionManager != null) {
                connectionManager.shutdown();
            }
            scanner.close();
            ClientLogger.logInfo("Application terminated");
            System.out.println("\nGoodbye!");
        }
    }

    private static void runConsoleLoop(Scanner scanner, ConnectionManager connectionManager) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("INTERACTIVE CONSOLE");
        System.out.println("=".repeat(50));
        System.out.println("Enter Company Ticker (e.g., NBK) or 'ISO' for Balance Check");
        System.out.println("Type 'EXIT' to quit");
        System.out.println("=".repeat(50) + "\n");

        boolean running = true;

        while (running) {
            System.out.print(">> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String command = input.toUpperCase();

            switch (command) {
                case "EXIT":
                case "QUIT":
                case "Q":
                    System.out.println("\nExiting application...");
                    running = false;
                    break;

                case "ISO":
                case "ISO8583":
                    handleISOBalanceCheck(connectionManager);
                    break;

                case "HELP":
                case "?":
                    displayHelp();
                    break;

                default:
                    handleTickerSearch(connectionManager, input);
                    break;
            }
        }
    }

    /**
     * Handles ticker search request
     */
    private static void handleTickerSearch(ConnectionManager connectionManager, String ticker) {
        if (!connectionManager.isConnected()) {
            System.out.println("✗ Not connected to server. Waiting for reconnection...");
            return;
        }

        System.out.println("\nSearching for ticker: " + ticker);
        connectionManager.searchByTicker(ticker);
    }

    /**
     * Handles ISO 8583 Balance Enquiry
     */
    private static void handleISOBalanceCheck(ConnectionManager connectionManager) {
        if (!connectionManager.isConnected()) {
            System.out.println("✗ Not connected to server. Waiting for reconnection...");
            return;
        }

        System.out.println("\nInitiating ISO 8583 Balance Enquiry...");
        connectionManager.checkBalance();
    }

    /**
     * Displays help information
     */
    private static void displayHelp() {
        System.out.println("\n┌─────────────────────────────────────────────┐");
        System.out.println("│               HELP MENU                     │");
        System.out.println("├─────────────────────────────────────────────┤");
        System.out.println("│ <TICKER>  - Search for company ticker       │");
        System.out.println("│             Example: NBK, AAPL, MSFT        │");
        System.out.println("│                                             │");
        System.out.println("│ ISO       - Send ISO 8583 Balance Enquiry   │");
        System.out.println("│             (0x0800 request)                │");
        System.out.println("│                                             │");
        System.out.println("│ HELP      - Display this help menu          │");
        System.out.println("│ EXIT      - Exit the application            │");
        System.out.println("└─────────────────────────────────────────────┘\n");
    }
}