# 🚀 How to Run the Financial Data Client Application

This guide will walk you through running the Financial Data Client Application step by step.

---


## 🏗️ Step 1: Compile the Application

### Option A: Using Command Line

Navigate to the project directory:

```bash
cd C:\Users\raovi\Downloads\pratice\ClientApplication
```

Compile all Java files:

```bash
javac -d bin src/*.java
```

### Option B: Using IntelliJ IDEA (Recommended)

1. Open IntelliJ IDEA
2. Click **File → Open**
3. Navigate to `ClientApplication` folder and click **OK**
4. IntelliJ will automatically compile the project

---

## ▶️ Step 2: Run the Application

### Option A: Using IntelliJ IDEA (Easiest)

1. Locate `Main.java` in the `src` folder
2. Right-click on `Main.java`
3. Select **Run 'Main.main()'**

![IntelliJ Run Configuration](docs/intellij-run.png)

### Option B: Using Command Line

```bash
java -cp bin Main
```

---

## 📺 Step 3: Application Startup

When the application starts successfully, you'll see:

Application Startup

```
╔════════════════════════════════════════╗
║  FINANCIAL DATA CLIENT APPLICATION     ║
║           Version 1.0                  ║
╚════════════════════════════════════════╝

-- Configuration loaded from client.properties
  Server: 127.0.0.1:8080
  User: admin

Connecting to server...
✓ Sending authentication...
✓ Credentials encrypted (AES-256)
✓ Response decrypted successfully
Server response: 200 OK
✓ Authentication successful

========== RECEIVING DATA FILE ==========
File size: 7952 bytes
✓ Data decrypted
```

The application will:
1. ✅ Load configuration from `client.properties`
2. ✅ Connect to the server
3. ✅ Encrypt and send authentication credentials (AES-256)
4. ✅ Receive authentication response
5. ✅ Download the CSV data file
6. ✅ Decrypt the data
7. ✅ Save it as `downloaded_data.csv`

---

## 🎮 Step 4: Using the Interactive Console

After successful startup, you'll see the interactive console:

![Interactive Console](docs/interactive-console.png)

```
==================================================
INTERACTIVE CONSOLE
==================================================
Enter Company Ticker (e.g., NBK) or 'ISO' for Balance Check
Type 'EXIT' to quit
==================================================

>>
```

---

## 💼 Available Commands

### 1️⃣ **Ticker Search**

Search for company stock information by entering a ticker symbol:

**Example:**

```
>> NBK
```

**Output:**

![Ticker Search Example](docs/ticker-search.png)

```
Searching for ticker: NBK

--- Ticker Details ---
FOUND: NBK | NATIONAL BANK OF KUWAIT | Sector: BANKS | Price: 1039.00 | Market Cap: 8,742,765,046
-------------------

>>
```

---

### 2️⃣ **ISO 8583 Balance Enquiry**

Check account balance using ISO 8583 protocol:

**Command:**

```
>> ISO
```

**Output:**

![ISO Balance Enquiry](docs/iso-balance.png)

```
Initiating ISO 8583 Balance Enquiry...
✓ Server ready for ISO message

--- Sending ISO 8583 Balance Enquiry ---
Message Type: 0x0800 (Balance Enquiry)
Hex Dump:
08 00 31 32 31 33 32 31 32 39 33 31 10 31 32 33
34 35 36 37 38 39 30 31 32 33 34 35 36

--- Server Response ---
Response Header: 08 10
Message: Balance: $10,000.00

✓ Balance Enquiry Successful
-------------------

>>
```

**Message Structure:**
- **0x0800**: Balance Enquiry Request (MTI)
- **0x0810**: Balance Enquiry Response (MTI)
- The hex dump shows the encrypted binary ISO 8583 message

---

### 3️⃣ **Help Menu**

Display all available commands:

**Command:**

```
>> HELP
```

**Output:**

```
┌────────────────────────────────────────────┐
│               HELP MENU                     │
├─────────────────────────────────────────────┤
│ <TICKER>  - Search for company ticker       │
│             Example: NBK, AAPL, MSFT        │
│                                             │
│ ISO       - Send ISO 8583 Balance Enquiry   │
│             (0x0800 request)                │
│                                             │
│ HELP      - Display this help menu          │
│ EXIT      - Exit the application            │
└─────────────────────────────────────────────┘
```

---

### 4️⃣ **Exit Application**

**Command:**

```
>> EXIT
```

**Output:**

```
Exiting application...

Goodbye!
```

The application will:
- Close the server connection gracefully
- Stop the auto-reconnect thread
- Save all logs to `client_messages.log`
- Clean up resources

---

## 🔄 Auto-Reconnect Feature

If the connection to the server is lost, the application will automatically attempt to reconnect:

```
✗ Connection lost
⟳ Attempting to reconnect in 5 seconds...
⟳ Attempting to reconnect in 10 seconds...
✓ Reconnected successfully!
```

**Reconnection Strategy:**
- Attempt 1: 5 seconds delay
- Attempt 2: 10 seconds delay
- Attempt 3: 15 seconds delay
- Attempt 4: 20 seconds delay
- Attempt 5+: 30 seconds delay (max)

---

## 📁 Output Files

After running the application, you'll find these files:

### 1. `downloaded_data.csv`
The CSV file received from the server containing market data.

**Sample content:**
```csv
Ticker,Security Name,Sector,Price,Market Cap
NBK,NATIONAL BANK OF KUWAIT,BANKS,1039.00,8742765046
AAPL,Apple Inc.,Technology,189.95,2950000000000
MSFT,Microsoft Corporation,Technology,380.50,2830000000000
```

### 2. `client_messages.log`
Detailed log of all operations.

**Sample content:**
```
[2025-12-13 10:30:45] INFO: Application started
[2025-12-13 10:30:45] INFO: Configuration loaded from client.properties
[2025-12-13 10:30:45] INFO: Encryption enabled: true
[2025-12-13 10:30:45] INFO: Connecting to 127.0.0.1:8080
[2025-12-13 10:30:45] INFO: Connected successfully
[2025-12-13 10:30:45] INFO: Authentication successful
[2025-12-13 10:30:46] INFO: Received file: 7952 bytes
[2025-12-13 10:30:47] INFO: Ticker search: NBK
[2025-12-13 10:30:48] INFO: ISO 8583 balance enquiry sent
[2025-12-13 10:31:00] INFO: Application terminated
```

---

## ⚠️ Troubleshooting

### Problem: "Connection refused"

**Cause:** Server is not running or wrong configuration

**Solution:**
1. Verify the server is running
2. Check `SERVER_IP` and `SERVER_PORT` in `client.properties`
3. Ensure firewall allows the connection
4. Try pinging the server: `ping 127.0.0.1`

---

### Problem: "Authentication failed"

**Cause:** Incorrect username or password

**Solution:**
1. Verify `USER` and `PASS` in `client.properties`
2. Check server logs for authentication errors
3. Ensure credentials match server configuration

---

### Problem: "Encryption initialization failed"

**Cause:** Invalid encryption key or JCE not available

**Solution:**
1. Verify JDK 8+ is installed (includes JCE by default)
2. Check `ENCRYPTION_KEY` in `client.properties`
3. Ensure key is at least 16 characters
4. Remove any leading/trailing spaces from the key

---

### Problem: "Could not decrypt response"

**Cause:** Encryption key mismatch between client and server

**Solution:**
1. Verify `ENCRYPTION_KEY` matches the server's key **exactly**
2. Check for extra spaces or special characters
3. Confirm both client and server have `ENCRYPTION_ENABLED=true`
4. Restart both client and server after changing keys

---

### Problem: Application freezes at "Connecting to server..."

**Cause:** Network timeout or server not responding

**Solution:**
1. Check network connectivity
2. Verify server is running and accepting connections
3. Check firewall/antivirus settings
4. Try a different port if 8080 is blocked
5. Wait for auto-reconnect (will retry automatically)

---

## 📊 Performance Tips

### For Faster File Downloads:
- Ensure stable network connection
- Run client and server on the same network
- Use wired connection instead of Wi-Fi when possible

### For Better Reliability:
- Enable encryption for secure communication
- Monitor `client_messages.log` for issues
- Keep the auto-reconnect feature enabled

---

## 🎓 Example Session

Here's a complete example of running the application:

```bash
# Step 1: Navigate to project directory
cd C:\Users\raovi\Downloads\pratice\ClientApplication

# Step 2: Compile (if not using IDE)
javac -d bin src/*.java

# Step 3: Run
java -cp bin Main
```

**Console Output:**
```
╔════════════════════════════════════════╗
║  FINANCIAL DATA CLIENT APPLICATION     ║
║           Version 1.0                  ║
╚════════════════════════════════════════╝

-- Configuration loaded from client.properties
  Server: 127.0.0.1:8080
  User: admin

Connecting to server...
✓ Authentication successful
✓ Data saved to: downloaded_data.csv

==================================================
INTERACTIVE CONSOLE
==================================================

>> NBK
FOUND: NBK | NATIONAL BANK OF KUWAIT | Price: 1039.00

>> ISO
✓ Balance Enquiry Successful
Message: Balance: $10,000.00

>> EXIT
Exiting application...
Goodbye!
```

---

## 📞 Additional Support

If you encounter issues not covered in this guide:

1. Check `client_messages.log` for detailed error information
2. Verify all prerequisites are met
3. Ensure server is running and properly configured
4. Review the [README.md](README.md) for feature details
5. Consult [OPTIMIZATION_GUIDE.md](OPTIMIZATION_GUIDE.md) for advanced topics

---

## ✅ Quick Checklist

Before running the application, ensure:

- [ ] Java JDK 8+ is installed
- [ ] Server application is running
- [ ] `client.properties` is configured
- [ ] Encryption keys match (if encryption enabled)
- [ ] Network connection is stable
- [ ] Firewall allows the connection

---

**🎉 You're all set! Happy trading!**

---

**Last Updated:** December 13, 2025  
**Application Version:** 1.0

