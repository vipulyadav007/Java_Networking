# Multi-Threaded Socket Server Application

## 📋 Project Overview
A production-ready, multi-threaded Java Socket Server that manages client connections, authenticates users, and serves financial data from a CSV file. Built with Java 17 (compatible with Java 8+).

## 🎯 Key Features

### 1. Configuration & Setup
- **server.properties**: Configures server port, timeout, and CSV file path
- **users.properties**: Stores valid username=password pairs for authentication
- **CSV Data Caching**: Loads financial data into memory on startup for fast access with proper CSV parsing

### 2. Connection Lifecycle
- Multi-threaded architecture supporting concurrent clients
- Thread pool with 10 worker threads
- Socket timeout (30 seconds default)
- Graceful connection handling with try-with-resources

### 3. Data Protocol (Binary & Plain Text)

#### Feature A: Initial Binary Sync
- After successful authentication, the entire CSV file is sent as binary data
- Uses `DataOutputStream` for efficient binary streaming

#### Feature B: Ticker Query Handling
- Clients can query by **Security Ticker** (e.g., "NBK", "KIB", "ZAIN")
- Returns formatted data: `TICKER | NAME | Sector: SECTOR | Price: PRICE`
- **Proper CSV Parsing**: Handles quoted fields and comma-separated numbers correctly
- Handles non-existing tickers with "NOT_FOUND" response

#### Feature C: ISO 8583 Support (Bonus)
- Detects binary header `0x0800` (Balance Enquiry)
- Responds with `0x0810` (Success Response)
- Mock balance data for demonstration

### 4. Comprehensive Logging
- All activities logged to `server_activities.log`
- Logs: Server start, Client connections (IP), Authentication results, Queries, Disconnections
- Console output for real-time monitoring

## 📁 Project Structure

```
ServerApplication/
├── Data_20251211.csv          # Financial data (Security tickers)
├── server.properties           # Server configuration
├── users.properties            # User credentials
├── ServerApplication.iml       # IntelliJ project file
├── server_activities.log       # Generated log file
└── src/
    ├── Main.java              # Server entry point
    ├── ClientHandler.java     # Handles individual client connections
    ├── CSVDataCache.java      # Parses and caches CSV data
    ├── ServerConfig.java      # Loads server configuration
    ├── UserAuthenticator.java # Validates user credentials
    ├── ServerLogger.java      # Configures logging
    ├── ISO8583Handler.java    # ISO 8583 message handler
    └── TestClient.java        # Test client for demonstration
```

## 📊 CSV Data Structure

The CSV file contains financial security data with **6 columns**:

| Column | Description | Example | Used As Key? |
|--------|-------------|---------|--------------|
| **Security Code** | Unique numeric identifier | 10001 | ❌ |
| **Security Ticker** | Short ticker code | NBK | ✅ **PRIMARY KEY** |
| **Security Name** | Full company name | NATIONAL BANK OF KUWAIT | ❌ |
| **Sector** | Business sector | Banking | ❌ |
| **Closing Price** | Latest closing price | 1040.00 | ❌ |
| **Market Cap** | Market capitalization | 7200000000 | ❌ |

### Sample Data:
```csv
Security Code,Security Ticker,Security Name,Sector,Closing Price,Market Cap
10001,NBK,NATIONAL BANK OF KUWAIT,Banking,1040.00,7200000000
10006,ZAIN,MOBILE TELECOMMUNICATIONS COMPANY,Telecommunications,542.00,3800000000
10008,AGILITY,AGILITY PUBLIC WAREHOUSING,Logistics,820.00,5600000000
```

### Key Implementation Details:
- **Column Indexing**: Security Ticker (index 1), Security Name (index 2), Sector (index 3), Closing Price (index 4)
- **Proper CSV Parsing**: Uses custom `parseCSVLine()` method to handle quoted fields with commas
- **Number Formatting**: Automatically removes quotes and commas from numeric values
- **Lookup Key**: Security Ticker is used as the HashMap key for O(1) lookups

## 🔧 Configuration Files

### server.properties
```properties
SERVER_PORT=8080
TIMEOUT_MS=30000
CSV_FILE_PATH=Data_20251211.csv
```

### users.properties
```properties
admin=admin123
Vipul=password1
Vipul2=password2
testuser=test@123
client=client123
```

## 🚀 How to Run

### 1. Compile the Server
```bash
cd C:\Users\raovi\Downloads\pratice\ServerApplication\src
javac *.java
```

### 2. Start the Server
```bash
java Main
```

Expected output:
```
========================================
Server Application Starting...
========================================
Server Port: 8080
Timeout: 30000ms
CSV File: Data_20251211.csv
CSV cache loaded with 10 records
========================================
Server started successfully on port 8080
Waiting for client connections...
========================================
```

### 3. Run Test Client (in a separate terminal)
```bash
java TestClient
```

## 📡 Client Protocol

### Step 1: Authentication
Send credentials in format: `username:password`
```
Client: admin:admin123
Server: 200 OK
```

### Step 2: Receive Binary CSV Data
Server automatically sends the entire CSV file as binary data after successful authentication.

### Step 3: Query Tickers
Send Security Ticker codes to query:
```
Client: NBK
Server: FOUND: NBK | NATIONAL BANK OF KUWAIT | Sector: Banking | Price: 1040.00

Client: ZAIN
Server: FOUND: ZAIN | MOBILE TELECOMMUNICATIONS COMPANY | Sector: Telecommunications | Price: 542.00

Client: INVALID_TICKER
Server: NOT_FOUND - Ticker not found: INVALID_TICKER
```

### Step 4: ISO 8583 (Optional)
```
Client: ISO8583
Client: [Binary: 0x0800]
Server: [Binary: 0x0810 + Response Data]
Server: ISO8583_SUCCESS
```

### Step 5: Disconnect
```
Client: EXIT
Server: GOODBYE - Connection closing
```

## 🛡️ Exception Safety

- **Try-with-resources**: All I/O resources are automatically closed
- **Timeout handling**: Disconnects idle clients after configured timeout
- **Graceful error handling**: All exceptions logged, connections closed properly
- **Thread safety**: Each client handled in separate thread

## 📝 Logging Examples

```log
INFO: Server Application Starting...
INFO: CSV cache loaded with 10 records
INFO: Server started successfully on port 8080
INFO: NEW CLIENT CONNECTED: 127.0.0.1:54321
INFO: ✓ AUTHENTICATION SUCCESS for user 'admin' from: 127.0.0.1:54321
INFO: ✓ Binary CSV dump completed: 612 bytes sent to: 127.0.0.1:54321
INFO: Query received from 127.0.0.1:54321: 'NBK'
INFO: Cache HIT for ticker: NBK
INFO: ✓ Ticker found - Response sent to 127.0.0.1:54321: NBK | NATIONAL BANK OF KUWAIT | Sector: Banking | Price: 1040.00
INFO: CLIENT DISCONNECTED: 127.0.0.1:54321
```

## 🧪 Testing

The included `TestClient.java` demonstrates all features:
1. ✓ Connects to server
2. ✓ Authenticates with valid credentials
3. ✓ Receives binary CSV data dump
4. ✓ Queries multiple tickers (both valid and invalid)
5. ✓ Tests ISO 8583 binary message handling
6. ✓ Gracefully disconnects

### Expected Output:
```
Searching for ticker: NBK
--- Ticker Details ---
FOUND: NBK | NATIONAL BANK OF KUWAIT | Sector: Banking | Price: 1040.00
---------------------

Searching for ticker: ZAIN
--- Ticker Details ---
FOUND: ZAIN | MOBILE TELECOMMUNICATIONS COMPANY | Sector: Telecommunications | Price: 542.00
---------------------
```

## 🔒 Security Features

- Credential validation against users.properties
- Failed authentication closes connection immediately
- Client IP logging for audit trail
- Timeout protection against idle/malicious connections

## 🎓 Technical Highlights

- **Multi-threading**: ExecutorService with fixed thread pool
- **Binary & Text Protocol**: Supports both DataOutputStream and PrintWriter
- **In-Memory Caching**: HashMap for O(1) ticker lookups
- **Proper CSV Parsing**: Custom parser handles quoted fields and commas within values
- **Logging Framework**: java.util.logging with file and console handlers
- **Clean Architecture**: Separation of concerns across multiple classes

## 📌 Available Tickers

Query these Security Tickers from the sample data:

| Ticker | Company Name | Sector | Price |
|--------|--------------|--------|-------|
| **NBK** | National Bank of Kuwait | Banking | 1,040.00 |
| **GBK** | Gulf Bank | Banking | 285.00 |
| **KIB** | Kuwait International Bank | Banking | 281.00 |
| **ABK** | Al Ahli Bank of Kuwait | Banking | 295.00 |
| **BOUBYAN** | Boubyan Bank | Banking | 720.00 |
| **ZAIN** | Mobile Telecommunications Company | Telecommunications | 542.00 |
| **OOREDOO** | Ooredoo Telecom | Telecommunications | 485.00 |
| **AGILITY** | Agility Public Warehousing | Logistics | 820.00 |
| **HUMANSOFT** | Humansoft Holding | Technology | 1,250.00 |
| **MABANEE** | Mabanee Company | Real Estate | 650.00 |

## 🐛 Troubleshooting

### Issue: Price showing as "1" instead of full value
**Cause**: CSV file has comma-separated numbers (e.g., "1,040.00") that break with simple split()  
**Solution**: ✅ Fixed - Now uses proper CSV parser that handles quoted fields

### Issue: Wrong price displayed
**Cause**: Reading wrong column index (Previous Closing Price vs Closing Price)  
**Solution**: ✅ Verify column mapping matches your CSV structure

### Issue: Data not loading
**Check**:
1. CSV file exists at path specified in `server.properties`
2. CSV has correct column structure (minimum 5 columns required)
3. Check `server_activities.log` for parsing errors

## 🛠️ Requirements

- Java 8 or higher (Tested with Java 17)
- IntelliJ IDEA (optional, can run from command line)
- Windows/Linux/Mac OS

## 📄 License

Educational/Practice Project - Free to use and modify

---

**Author**: Vipul  
**Date**: December 13, 2025  
**Version**: 1.1 - Fixed CSV parsing for proper data handling
