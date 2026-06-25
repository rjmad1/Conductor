package com.conductor.shared.execution.provider;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reusable utility class for webhook signature verification and timestamp-based replay protection.
 */
public class WebhookVerifier {

  private static final Logger log = LoggerFactory.getLogger(WebhookVerifier.class);

  /**
   * Verifies that the signature matches the payload encrypted with the shared secret. Supports Hex,
   * Base64, and prefix-prepended signature strings.
   */
  public static boolean verifySignature(
      String payload, String signature, String secret, String algorithm) {
    if (payload == null || signature == null || secret == null) {
      return false;
    }
    try {
      String hmacAlg = algorithm != null ? algorithm : "HmacSHA256";
      SecretKeySpec signingKey =
          new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), hmacAlg);
      Mac mac = Mac.getInstance(hmacAlg);
      mac.init(signingKey);
      byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

      String hexHmac = bytesToHex(rawHmac);
      String base64Hmac = Base64.getEncoder().encodeToString(rawHmac);

      return signature.equalsIgnoreCase(hexHmac)
          || signature.equalsIgnoreCase(base64Hmac)
          || signature.equalsIgnoreCase("sha256=" + hexHmac)
          || signature.contains(hexHmac)
          || signature.contains(base64Hmac);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      log.error("Webhook signature verification failed due to cryptographic error", e);
      return false;
    }
  }

  /**
   * Verifies that the event timestamp is within a specific tolerance interval to prevent replay
   * attacks.
   */
  public static boolean verifyTimestamp(long timestampMs, long toleranceMs) {
    long now = System.currentTimeMillis();
    return Math.abs(now - timestampMs) <= toleranceMs;
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
