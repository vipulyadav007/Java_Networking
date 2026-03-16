import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Encryption utility for secure message exchange
 * Uses AES-256 encryption for data protection
 */
public class EncryptionUtil {
    private static final String ALGORITHM = "AES";

    private SecretKey secretKey;

    /**
     * Initialize encryption with a password-derived key
     * UPDATED: Pads or truncates key to valid AES length (32 bytes for AES-256 to match server)
     */
    public EncryptionUtil(String password) throws Exception {

        byte[] keyBytes = password.getBytes("UTF-8");

        byte[] paddedKey = new byte[32];
        if (keyBytes.length >= 32) {
            System.arraycopy(keyBytes, 0, paddedKey, 0, 32);
        } else {
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);

        }

        this.secretKey = new SecretKeySpec(paddedKey, ALGORITHM);
    }

    /**
     * Encrypt a string message
     */
    public String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Decrypt an encrypted message
     */
    public String decrypt(String encryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, "UTF-8");
    }

    /**
     * Encrypt binary data
     */
    public byte[] encryptBytes(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    /**
     * Decrypt binary data
     */
    public byte[] decryptBytes(byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedData);
    }

    /**
     * Hash a password for secure storage/comparison
     */
    public static String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
