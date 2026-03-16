import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

/**
 *  CSVDataCache with thread-safe operations
 * - Uses ConcurrentHashMap for thread-safe concurrent access
 * - Lazy loading support
 * - Memory-efficient data storage
 */
public class CSVDataCache {
    private static final Logger logger = Logger.getLogger(CSVDataCache.class.getName());
    private final Map<String, String> dataCache;
    private final String csvFilePath;
    private volatile boolean loaded = false;

    public CSVDataCache(String csvFilePath) throws IOException {
        this.csvFilePath = csvFilePath;
        this.dataCache = new ConcurrentHashMap<>();
        loadData();
    }

    /**
     * Load CSV data into cache
     */
    private void loadData() throws IOException {
        long startTime = System.currentTimeMillis();
        int recordCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath), 8192)) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                if (isHeader) {
                    isHeader = false;
                    logger.info("CSV Header: " + line);
                    continue;
                }

                // Use proper CSV parsing to handle quoted fields and commas
                String[] parts = parseCSVLine(line);
                if (parts.length >= 13) {
                    String securityTicker = parts[1].trim();  // This is our KEY
                    String securityName = parts[2].trim();
                    String sector = parts[3].trim();
                    String closingPrice = parts[8].trim().replaceAll("\"", "").replaceAll(",", "");
                    String marketCap = parts[12].trim().replaceAll("\"", "");

                    // Format: "TICKER | SECURITY_NAME | Sector: SECTOR | Price: PRICE"
                    String formattedData = String.format("%s | %s | Sector: %s | Price: %s  | Market Cap: %s",
                            securityTicker, securityName, sector, closingPrice , marketCap);

                    dataCache.put(securityTicker, formattedData);
                    recordCount++;
                }
            }
        }

        loaded = true;
        long duration = System.currentTimeMillis() - startTime;
        logger.info("CSV data loaded into cache: " + recordCount + " records in " + duration + "ms");
    }

    /**
     * Properly parse CSV line handling quoted fields and commas within values
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                // Keep the quote in the field
                currentField.append(c);
            } else if (c == ',' && !inQuotes) {
                // End of field
                result.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // Add the last field
        result.add(currentField.toString());

        return result.toArray(new String[0]);
    }

    /**
     * Thread-safe lookup with null safety
     */
    public String lookup(String securityTicker) {
        if (!loaded) {
            logger.warning("Cache not fully loaded yet");
            return null;
        }

        if (securityTicker == null || securityTicker.isEmpty()) {
            return null;
        }

        String result = dataCache.get(securityTicker.trim().toUpperCase());

        if (result != null) {
            logger.fine("Cache HIT for ticker: " + securityTicker);
        } else {
            // Try case-insensitive search as fallback
            for (Map.Entry<String, String> entry : dataCache.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(securityTicker)) {
                    logger.fine("Cache HIT (case-insensitive) for ticker: " + securityTicker);
                    return entry.getValue();
                }
            }
            logger.fine("Cache MISS for ticker: " + securityTicker);
        }

        return result;
    }

    public String getCsvFilePath() {
        return csvFilePath;
    }

    public int getRecordCount() {
        return dataCache.size();
    }

    public Map<String, String> getAllData() {
        return new ConcurrentHashMap<>(dataCache);
    }

    /**
     * Check if cache is loaded
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Get cache statistics
     */
    public String getStatistics() {
        return String.format("Cache: %d records, Loaded: %s, Memory: ~%d KB",
                dataCache.size(),
                loaded ? "Yes" : "No",
                (dataCache.size() * 150) / 1024); // Rough estimate
    }
}
