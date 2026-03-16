# 📘 USER GUIDE - Financial Data Server Application

## 📋 Table of Contents
1. [Overview](#overview)
2. [System Requirements](#system-requirements)
3. [Configuration](#configuration)
4. [Running the Application](#running-the-application)
5. [Testing & Usage](#testing--usage)
6. [Troubleshooting](#troubleshooting)

---

## 🎯 Overview

Multi-threaded TCP Socket Server that serves financial security data from CSV files with:
- Multi-threaded architecture (10 concurrent clients)
- User authentication
- Binary CSV data transmission
- Ticker query service
- ISO 8583 support
- Complete logging

---

## 💻 System Requirements

- **Java**: JDK 8+ (Tested with Java 17)
- **OS**: Windows, Linux, or macOS
- **RAM**: 512 MB minimum

---

## ⚙️ Configuration

### server.properties
```properties
SERVER_PORT=8080
TIMEOUT_MS=50000
CSV_FILE_PATH=C:/Users/raovi/Downloads/JavaAssignment2025/Data_20251211.csv
```

⚠️ **Important**: Update `CSV_FILE_PATH` to your actual file location (use forward slashes `/`)

### users.properties
```properties
admin=admin123
Vipul=password1
testuser=test@123
```

Format: `username=password` (one per line)

---

## 🚀 Running the Application

### Method 1: Using IntelliJ IDEA (Easiest)

1. **Open Project**: File → Open → Select `ServerApplication` folder
2. **Run Server**: Right-click `src/Main.java` → Run 'Main.main()'
3. **Run Test Client**: Right-click `src/TestClient.java` → Run 'TestClient.main()'

**Expected Server Output:**
```
INFO: Server Application Starting...
INFO: With AES-256 Encryption and GZIP Compression
INFO: Server Port: 8080
INFO: CSV data loaded into cache: 139 records
INFO: Server started successfully on port 8080
```

**Expected Client Output:**
```
Authenticating as: admin
Authentication successful!
CSV data received: XXXX bytes
Searching for ticker: NBK
FOUND: NBK | NATIONAL BANK OF KUWAIT | Sector: BANKS | Price: 1040.00
```

---

### Method 2: Using Command Line

#### Windows
```bash
# Navigate to project
cd C:\Users\raovi\Downloads\pratice\ServerApplication\src

# Compile
javac *.java

# Run Server (Terminal 1)
java Main

# Run Test Client (Terminal 2 - new window)
java TestClient
```

#### Linux/Mac
```bash
cd ~/Downloads/ServerApplication/src
javac *.java
java Main              # Terminal 1
java TestClient        # Terminal 2
```

---

## 🧪 Testing & Usage

### Available Tickers (Examples)
- `NBK` - National Bank of Kuwait (Banking)
- `ZAIN` - Mobile Telecommunications (Telecom)
- `AGILITY` - Agility Public Warehousing (Logistics)
- `GBK`, `KIB`, `ABK`, `BOUBYAN` (Banking)
- `OOREDOO`, `HUMANSOFT`, `MABANEE`

*139 securities available in total*

### Test Scenarios

**1. Basic Connection**
- Start server → Start TestClient → Verify authentication & CSV received

**2. Query Tickers**
- TestClient automatically queries NBK and ZAIN
- Server returns: `FOUND: TICKER | NAME | Sector: X | Price: Y`

**3. ISO 8583 Balance Enquiry**
- TestClient tests ISO 8583 binary protocol
- Server responds with success message

**4. Multiple Clients**
- Run multiple TestClient instances simultaneously
- All connect independently

---

## 🔍 Troubleshooting

### "Address already in use"
Change port in `server.properties`:
```properties
SERVER_PORT=8081
```

### "CSV File not found"
Update path in `server.properties` with forward slashes:
```properties
CSV_FILE_PATH=C:/Users/YourName/Downloads/Data_20251211.csv
```

### "Could not find or load main class Main"
Ensure you're in the `src` folder:
```bash
cd src
java Main
```

### Authentication fails
- Verify credentials in `users.properties`
- Check username/password are correct
- Remember: case-sensitive!

### Port 8080 blocked?
Check if another application is using port 8080:
```bash
netstat -ano | findstr :8080    # Windows
lsof -i :8080                    # Linux/Mac
```

---

## 📝 Important Notes

1. **Run server first** before starting clients
2. **Keep server running** while testing
3. **CSV file path** must use forward slashes (`/`)
4. **Logs**: Check `server_activities.log` for detailed activity

---

## 📊 Technical Details

### Protocol Flow
```
1. Client connects → Sends credentials
2. Server authenticates → Sends "200 OK"
3. Server sends binary CSV data
4. Client queries tickers
5. Server responds with data
6. Client disconnects
```

### Features
- **Multi-threading**: Fixed thread pool (10 threads)
- **Authentication**: Validates against users.properties
- **CSV Caching**: In-memory HashMap for O(1) lookup
- **Binary Protocol**: Efficient data transmission
- **Logging**: All activities logged to file

---

## 📄 Project Structure

```
ServerApplication/
├── server.properties          # Server config
├── users.properties          # User credentials
├── Data_20251211.csv        # Financial data
├── src/
│   ├── Main.java            # Server entry point
│   ├── ClientHandler.java   # Client handler
│   ├── CSVDataCache.java    # Data cache
│   ├── ServerConfig.java    # Config loader
│   ├── UserAuthenticator.java
│   ├── ServerLogger.java
│   ├── ISO8583Handler.java
│   ├── EncryptionManager.java
│   └── TestClient.java      # Test client
└── server_activities.log    # Generated logs
```

---

**Version**: 1.1 | **Date**: December 2025 | **Type**: Educational Project

For detailed information, see README.md
