package com.conductor.shared.customer;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA AttributeConverter that encrypts/decrypts Tier-1 PII fields (phone, email, name) using
 * AES-256-GCM before storing to PostgreSQL.
 *
 * <p>Security compliance: SG-006 — Customer PII must be encrypted at rest. Key source:
 * ${CUSTOMER_PII_ENCRYPTION_KEY} environment variable (32-byte Base64 encoded).
 *
 * <p>Ciphertext format: Base64(IV[12] || TAG[16] || CIPHERTEXT) IV is randomly generated per
 * encryption. Authentication tag is included (GCM provides AEAD).
 *
 * <p>ponytail: Env-var key is acceptable for MVP. Upgrade path: replace key loading with AWS KMS
 * `GenerateDataKey` call and cache the DEK per application lifecycle.
 */
@Converter
public class PiiEncryptedConverter implements AttributeConverter<String, String> {

  private static final Logger log = LoggerFactory.getLogger(PiiEncryptedConverter.class);

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final String KEY_ALGORITHM = "AES";
  private static final int GCM_IV_LENGTH = 12; // 96-bit IV (NIST recommended)
  private static final int GCM_TAG_BITS = 128; // 128-bit authentication tag
  private static final String ENV_KEY_NAME = "CUSTOMER_PII_ENCRYPTION_KEY";

  private final SecretKeySpec secretKey;
  private final SecureRandom secureRandom;

  public PiiEncryptedConverter() {
    this.secureRandom = new SecureRandom();
    String base64Key = System.getenv(ENV_KEY_NAME);
    if (base64Key == null || base64Key.isBlank()) {
      // Fail-fast: PII cannot be stored without encryption
      throw new IllegalStateException(
          "Environment variable '"
              + ENV_KEY_NAME
              + "' is not set. "
              + "Customer PII encryption cannot be initialised. "
              + "Set a 32-byte Base64-encoded AES-256 key.");
    }
    byte[] keyBytes = Base64.getDecoder().decode(base64Key);
    if (keyBytes.length != 32) {
      throw new IllegalStateException(
          "'"
              + ENV_KEY_NAME
              + "' must decode to exactly 32 bytes (256 bits). "
              + "Got "
              + keyBytes.length
              + " bytes.");
    }
    this.secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
  }

  @Override
  public String convertToDatabaseColumn(String plaintext) {
    if (plaintext == null) {
      return null;
    }
    try {
      byte[] iv = new byte[GCM_IV_LENGTH];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
      byte[] ciphertext =
          cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

      // Prepend IV to ciphertext (IV || CIPHERTEXT+TAG)
      byte[] combined = new byte[GCM_IV_LENGTH + ciphertext.length];
      System.arraycopy(iv, 0, combined, 0, GCM_IV_LENGTH);
      System.arraycopy(ciphertext, 0, combined, GCM_IV_LENGTH, ciphertext.length);

      return Base64.getEncoder().encodeToString(combined);
    } catch (Exception e) {
      log.error("Failed to encrypt PII field", e);
      throw new IllegalStateException("PII encryption failed", e);
    }
  }

  @Override
  public String convertToEntityAttribute(String encoded) {
    if (encoded == null) {
      return null;
    }
    try {
      byte[] combined = Base64.getDecoder().decode(encoded);
      byte[] iv = new byte[GCM_IV_LENGTH];
      System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);

      byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];
      System.arraycopy(combined, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
      byte[] plaintext = cipher.doFinal(ciphertext);

      return new String(plaintext, java.nio.charset.StandardCharsets.UTF_8);
    } catch (Exception e) {
      log.error("Failed to decrypt PII field", e);
      throw new IllegalStateException("PII decryption failed", e);
    }
  }
}
