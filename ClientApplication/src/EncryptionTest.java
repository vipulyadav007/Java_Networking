/**
 * Quick test to verify encryption matches server
 */
public class EncryptionTest {
    public static void main(String[] args) {
        try {
            String key = "MySecretKey12345MySecretKey12345";
            String credentials = "admin:admin123";

            System.out.println("Testing encryption compatibility...");
            System.out.println("Key: " + key);
            System.out.println("Key length: " + key.length() + " bytes");
            System.out.println("Plain text: " + credentials);

            EncryptionUtil util = new EncryptionUtil(key);
            String encrypted = util.encrypt(credentials);

            System.out.println("\nEncrypted: " + encrypted);
            System.out.println("Expected:  9FBuLs1tR3kCduhUqTz7LA==");
            System.out.println("Match: " + encrypted.equals("9FBuLs1tR3kCduhUqTz7LA=="));

            // Try decrypting
            String decrypted = util.decrypt(encrypted);
            System.out.println("\nDecrypted: " + decrypted);
            System.out.println("Correct: " + decrypted.equals(credentials));

            // Show key bytes
            byte[] keyBytes = key.getBytes("UTF-8");
            System.out.println("\nKey bytes length: " + keyBytes.length);
            System.out.print("First 32 bytes: ");
            for (int i = 0; i < Math.min(32, keyBytes.length); i++) {
                System.out.print(String.format("%02X ", keyBytes[i]));
            }
            System.out.println();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

