package com.conductor.integrations.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CredentialEncryptor {

    private static final Logger log = LoggerFactory.getLogger(CredentialEncryptor.class);
    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final byte[] secretKey;

    public CredentialEncryptor(@Value("${INTEGRATION_ENCRYPTION_KEY:defaultValueDefaultValueDefaultVa}") String keyString) {
        byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) {
            log.warn("INTEGRATION_ENCRYPTION_KEY must be exactly 32 bytes long for AES-256 GCM. Resizing to 32 bytes.");
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            this.secretKey = padded;
        } else {
            this.secretKey = keyBytes;
        }
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, AES);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] encryptedMessage = new byte[iv.length + cipherText.length];

            System.arraycopy(iv, 0, encryptedMessage, 0, iv.length);
            System.arraycopy(cipherText, 0, encryptedMessage, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(encryptedMessage);
        } catch (Exception e) {
            log.error("Failed to encrypt credentials", e);
            throw new RuntimeException("Encryption error", e);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null) {
            return null;
        }
        try {
            byte[] encryptedMessage = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedMessage, 0, iv, 0, iv.length);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, AES);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

            byte[] cipherText = new byte[encryptedMessage.length - iv.length];
            System.arraycopy(encryptedMessage, iv.length, cipherText, 0, cipherText.length);

            byte[] decrypted = cipher.doFinal(cipherText);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to decrypt credentials", e);
            throw new RuntimeException("Decryption error", e);
        }
    }
}
