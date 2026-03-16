# Financial Data Client Application

## Overview
A robust Java client application that connects to a Socket Server for financial data queries and ISO 8583 balance enquiries.

## Features

### 1. **Configuration Management**
- Loads settings from `client.properties`
- Configurable server IP, port, username, and password

### 2. **Resilient Connection**
- **Auto-Reconnect**: Automatically attempts to reconnect every 5 seconds if connection is lost
- **Keep-Alive**: Sends heartbeat signals every 10 seconds to maintain connection
- Thread-safe connection handling

### 3. **Startup Sequence**
1. Connects to server at configured IP:PORT
2. Sends credentials in format `USER:PASS`
3. Receives binary CSV file (`Data_20251211.csv`) from server
4. Saves file locally as `downloaded_data.csv`
5. Displays first 5 rows to console as proof of receipt

### 4. **Interactive Console UI**
The application provides a simple command-line interface:

```
>> <TICKER>     - Search for company by ticker symbol (e.g., NBK, AAPL)
>> ISO          - Send ISO 8583 Balance Enquiry (0x0800)
>> HELP         - Display help menu
>> EXIT         - Exit application
```

### 5. **Operations**

#### Ticker Search
- User enters company ticker (e.g., "NBK")
- Client sends ticker to server
- Displays Security Name and Price information

#### ISO 8583 Balance Check
- User enters "ISO"
- Client constructs binary packet with header `0x0800` (Balance Enquiry)
- Sends binary message to server
- Receives binary response
- Checks for `0x0810` response (success indicator)
- Displays "Balance Enquiry Successful" or failure message
- Shows hex dump of request and response

### 6. **Logging**
All operations are logged to `client_messages.log`:
- Outgoing commands
- Incoming responses
- Connection events
- Errors

## Project Structure

```
ClientApplication/
├── client.properties           # Configuration file
├── src/
│   ├── Main.java              # Main entry point and console loop
│   ├── ClientConfig.java      # Configuration loader
│   ├── ConnectionManager.java # Connection and network operations
│   ├── ISO8583Message.java    # ISO 8583 message builder
│   └── ClientLogger.java      # Logging utility
├── downloaded_data.csv        # Downloaded data file (created at runtime)
└── client_messages.log        # Log file (created at runtime)
```

## Configuration

Edit `client.properties`:

```properties
SERVER_IP=127.0.0.1
SERVER_PORT=8888
USER=admin
PASS=password123
```

## Usage

### Running the Application

1. **In IntelliJ IDEA:**
   - Open the project
   - Run `Main.java`

2. **From Command Line:**
   ```bash
   cd src
   javac *.java
   cd ..
   java -cp src Main
   ```

### Example Session

```
╔════════════════════════════════════════╗
║  FINANCIAL DATA CLIENT APPLICATION    ║
║           Version 1.0                  ║
╚════════════════════════════════════════╝

✓ Configuration loaded from client.properties
  Server: 127.0.0.1:8888
  User: admin

Connecting to server...
✓ Authentication successful

========== RECEIVING DATA FILE ==========
File size: 2048 bytes
✓ Data saved to: downloaded_data.csv

--- First 5 Rows ---
Symbol,Security Name,Market Price,Change
NBK,National Bank of Kuwait,1.25,+0.05
AAPL,Apple Inc,150.00,-2.50
MSFT,Microsoft Corp,320.00,+5.00
GOOGL,Alphabet Inc,140.00,+1.25
---------------------------

==================================================
INTERACTIVE CONSOLE
==================================================
Enter Company Ticker (e.g., NBK) or 'ISO' for Balance Check
Type 'EXIT' to quit
==================================================

>> NBK

Searching for ticker: NBK

--- Ticker Details ---
Security Name: National Bank of Kuwait
Market Price: 1.25
Change: +0.05
---------------------

>> ISO

Initiating ISO 8583 Balance Enquiry...

--- Sending ISO 8583 Balance Enquiry ---
Message Type: 0x0800 (Balance Enquiry)
Hex Dump:
08 00 31 32 31 32 31 35 33 30 31 35 10 31 32 33
34 35 36 37 38 39 30

--- Server Response ---
Response Size: 2 bytes
Hex Dump:
08 10

✓ Balance Enquiry Successful
----------------------

>> EXIT

Exiting application...
Goodbye!
```

## Protocol Details

### Authentication
- Format: `USER:PASS` sent as UTF-8 string
- Expected response: `AUTH_SUCCESS`

### Data File Transfer
1. Server sends file size as 4-byte integer
2. Server sends file data as byte array
3. Client saves and displays first 5 rows

### Ticker Query
- Client sends ticker symbol as UTF-8 string
- Server responds with company details

### ISO 8583 Balance Enquiry
- Client sends `"ISO8583"` marker
- Client sends message length (4 bytes)
- Client sends binary message starting with `0x0800`
- Server responds with length + binary data
- Success response starts with `0x0810`

### Heartbeat
- Sent every 10 seconds
- Format: UTF-8 string `"HEARTBEAT"`
- Server can ignore or acknowledge

## Error Handling

- **Connection Lost**: Automatic reconnection every 5 seconds
- **Authentication Failed**: Logs error and continues reconnect attempts
- **Network Errors**: Logged with full details
- **Invalid Commands**: Gracefully handled with user feedback

## Thread Safety

- Synchronized output operations prevent data corruption
- Atomic boolean flags for connection state
- Daemon threads for background operations (heartbeat, reconnect)

## Requirements

- Java 8 or higher
- Network connectivity to server
- Write permissions for log and data files

## Notes

- Binary data operations use `DataInputStream`/`DataOutputStream`
- CSV file encoding: UTF-8
- All timestamps in logs use format: `yyyy-MM-dd HH:mm:ss`
- ISO 8583 implementation is simplified for demonstration

## Troubleshooting

**Connection Refused:**
- Verify server is running
- Check `SERVER_IP` and `SERVER_PORT` in config
- Verify firewall settings

**Authentication Failed:**
- Verify `USER` and `PASS` credentials
- Check server logs for authentication errors

**No Data Received:**
- Check server has data file available
- Verify binary protocol compatibility
- Review `client_messages.log` for details


