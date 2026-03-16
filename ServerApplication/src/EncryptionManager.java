import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.logging.Logger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Encryption Manager - Handles AES encryption/decryption
 * Uses AES-256 encryption for secure message exchange
 */
public class EncryptionManager {
    private static final Logger logger = Logger.getLogger(EncryptionManager.class.getName());
    private static final String ALGORITHM = "AES";
    //The hardcoded key is not secure for prod use
    private static final String SECRET_KEY_STRING = "MySecretKey12345MySecretKey12345";

    private SecretKey secretKey;

    public EncryptionManager() {
        try {

            byte[] keyBytes = SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8);


            if (keyBytes.length != 32) {
                keyBytes = Arrays.copyOf(keyBytes, 32);
            }

            this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            logger.info("Encryption initialized with AES-256 (key length: " + keyBytes.length + " bytes)");
        } catch (Exception e) {
            logger.severe("Failed to initialize encryption: " + e.getMessage());
            e.printStackTrace();
        }
    }

//    public static SecretKey generateKey() throws Exception {
//        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
//        keyGen.init(KEY_SIZE, new SecureRandom());
//        return keyGen.generateKey();
//    }

    public String encrypt(String plainText) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public byte[] encryptBytes(byte[] plainData) throws Exception {
        if (plainData == null || plainData.length == 0) {
            return plainData;
        }

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(plainData);
    }


    public byte[] decryptBytes(byte[] encryptedData) throws Exception {
        if (encryptedData == null || encryptedData.length == 0) {
            return encryptedData;
        }

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedData);
    }

}
