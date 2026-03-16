import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ISO8583Message {


    public static final short MTI_BALANCE_REQUEST = 0x0800;
    public static final short MTI_BALANCE_RESPONSE = 0x0810;

    /**
     * Creates a mock ISO 8583 Balance Enquiry message (MTI 0x0800)
     * This is a simplified implementation for demonstration purposes
     */
    public static byte[] createBalanceEnquiry(String accountNumber) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // Message Type Indicator (MTI) - 0x0800 for Balance Enquiry
        dos.writeShort(MTI_BALANCE_REQUEST);

        // Add some mock fields
        // Field: Transmission Date and Time
        String transmissionDateTime = new java.text.SimpleDateFormat("MMddHHmmss").format(new java.util.Date());
        dos.writeBytes(transmissionDateTime);

        // Field: Account number (mock)
        String pan = accountNumber != null ? accountNumber : "1234567890123456";
        dos.writeByte(pan.length());
        dos.writeBytes(pan);

        dos.flush();
        return baos.toByteArray();
    }

    /**
     * Checks if the response is a successful balance response (0x0810)
     */
    public static boolean isBalanceResponseSuccess(byte[] response) {
        if (response == null || response.length < 2) {
            return false;
        }

        // Read first 2 bytes as short
        short mti = (short) (((response[0] & 0xFF) << 8) | (response[1] & 0xFF));
        return mti == MTI_BALANCE_RESPONSE;
    }

    /**
     * Converts byte array to hexadecimal string for display
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0 && i % 16 == 0) {
                sb.append("\n");
            }
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString();
    }
}
