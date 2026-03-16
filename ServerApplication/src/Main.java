import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 *  Main Server Application
 * - Shutdown support
 * - JVM shutdown hook for cleanup
 * - logging
 * - Performance monitoring
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final int THREAD_POOL_SIZE = 15;
    private static ExecutorService threadPool;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown signal received, cleaning up...");
            shutdown();
        }));

        try {

            ServerLogger.setupLogger();
            logger.info("========================================");
            logger.info("Server Application Starting...");
            logger.info("With AES-256 Encryption and GZIP Compression");
            logger.info("========================================");


            ServerConfig config = new ServerConfig("server.properties");
            logger.info("Server Port: " + config.getServerPort());
            logger.info("Timeout: " + config.getTimeoutMs() + "ms");
            logger.info("CSV File: " + config.getCsvFilePath());

            // Load user authentication
            UserAuthenticator authenticator = new UserAuthenticator("users.properties");


            long startTime = System.currentTimeMillis();
            CSVDataCache dataCache = new CSVDataCache(config.getCsvFilePath());
            long loadTime = System.currentTimeMillis() - startTime;
            logger.info("CSV cache loaded with " + dataCache.getRecordCount() + " records in " + loadTime + "ms");
            logger.info(dataCache.getStatistics());

            threadPool = Executors.newFixedThreadPool(
                THREAD_POOL_SIZE,
                runnable -> {
                    Thread t = new Thread(runnable);
                    t.setDaemon(false);
                    t.setName("ClientHandler-" + System.currentTimeMillis());
                    return t;
                }
            );
            logger.info("Thread pool initialized with " + THREAD_POOL_SIZE + " threads");

            // Start server socket
            serverSocket = new ServerSocket(config.getServerPort());
            serverSocket.setReuseAddress(true);
            serverSocket.setPerformancePreferences(0, 1, 2);
            logger.info("========================================");
            logger.info("Server started successfully on port " + config.getServerPort());
            logger.info("--AES-256 Encryption: ENABLED");
            logger.info("--GZIP Compression: ENABLED");
            logger.info("--Optimizations: TCP NoDelay, KeepAlive, 8KB Buffers");
            logger.info("Waiting for client connections...");
            logger.info("========================================");

            int clientCount = 0;

            // Accept client connections
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientCount++;

                    logger.info("Client #" + clientCount + " accepted from: " +
                        clientSocket.getInetAddress().getHostAddress());

                    ClientHandler handler = new ClientHandler(
                        clientSocket,
                        authenticator,
                        dataCache,
                        config.getTimeoutMs()
                    );
                    threadPool.execute(handler);

                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        logger.severe("Error accepting client connection: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            logger.severe("Fatal error: " + e.getMessage());
            logException(e);
            System.exit(1);
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e.getMessage());
            logException(e);
            System.exit(1);
        } finally {
            shutdown();
        }
    }

    /**
     * Graceful shutdown
     */
    private static void shutdown() {
        logger.info("Initiating graceful shutdown...");

        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                logger.info("Server socket closed");
            } catch (IOException e) {
                logger.warning("Error closing server socket: " + e.getMessage());
            }
        }

        // Shutdown thread pool
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                    logger.warning("Thread pool forced shutdown");
                } else {
                    logger.info("Thread pool shutdown completed");
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        logger.info("========================================");
        logger.info("Server shutdown complete");
        logger.info("========================================");
    }

    /**
     * Log exception properly
     */
    private static void logException(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        logger.severe(sw.toString());
    }
}