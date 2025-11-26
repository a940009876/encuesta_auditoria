package mx.com.ado.auditoria.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import mx.com.ado.auditoria.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for encrypting and decrypting survey responses.
 */
@Service
public class EncryptionService {

    private static final Logger LOG = LoggerFactory.getLogger(EncryptionService.class);
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    private final ApplicationProperties applicationProperties;

    public EncryptionService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Encrypts the given plain text using AES encryption.
     *
     * @param plainText the text to encrypt
     * @return the encrypted text in Base64 format
     * @throws EncryptionException if encryption fails
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            SecretKeySpec secretKeySpec = generateSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            LOG.error("Error encrypting data", e);
            throw new EncryptionException("Error al encriptar los datos", e);
        }
    }

    /**
     * Decrypts the given encrypted text using AES decryption.
     *
     * @param encryptedText the encrypted text in Base64 format
     * @return the decrypted plain text
     * @throws EncryptionException if decryption fails
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            SecretKeySpec secretKeySpec = generateSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.error("Error decrypting data", e);
            throw new EncryptionException("Error al desencriptar los datos", e);
        }
    }

    /**
     * Generates a secret key from the configured secret key string.
     * Uses SHA-256 to hash the secret key and takes the first 16 bytes for AES-128.
     *
     * @return the SecretKeySpec for AES encryption
     * @throws Exception if key generation fails
     */
    private SecretKeySpec generateSecretKey() throws Exception {
        String secretKey = applicationProperties.getEncryption().getSecretKey();
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = secretKey.getBytes(StandardCharsets.UTF_8);
        key = sha.digest(key);
        // AES-128 requires 16 bytes key
        byte[] key128 = new byte[16];
        System.arraycopy(key, 0, key128, 0, 16);
        return new SecretKeySpec(key128, ALGORITHM);
    }

    /**
     * Exception thrown when encryption or decryption fails.
     */
    public static class EncryptionException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

