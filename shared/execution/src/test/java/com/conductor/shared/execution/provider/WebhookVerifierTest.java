package com.conductor.shared.execution.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WebhookVerifierTest {

  private final String payload = "{\"event\":\"order.created\",\"id\":\"ord_123\"}";
  private final String secret = "super-secret-key-123";
  private final String validHexSignature =
      "e9b447b69385578b032cc22aa3acb87dcf0fcba4d6399ceb9fa13cd48dbff339";
  // Base64 representation of raw bytes
  private final String validBase64Signature = "6bRHtpOFV4sDLMIqo6y4fc8Py6TWOZzrn6E81I2/8zk=";

  @Test
  void testVerifySignatureHexSuccess() {
    assertTrue(WebhookVerifier.verifySignature(payload, validHexSignature, secret, "HmacSHA256"));
  }

  @Test
  void testVerifySignatureBase64Success() {
    assertTrue(
        WebhookVerifier.verifySignature(payload, validBase64Signature, secret, "HmacSHA256"));
  }

  @Test
  void testVerifySignatureWithPrefixSuccess() {
    assertTrue(
        WebhookVerifier.verifySignature(
            payload, "sha256=" + validHexSignature, secret, "HmacSHA256"));
  }

  @Test
  void testVerifySignatureIncorrectSecretFailure() {
    assertFalse(
        WebhookVerifier.verifySignature(payload, validHexSignature, "wrong-secret", "HmacSHA256"));
  }

  @Test
  void testVerifySignatureIncorrectPayloadFailure() {
    assertFalse(
        WebhookVerifier.verifySignature(
            "different-payload", validHexSignature, secret, "HmacSHA256"));
  }

  @Test
  void testVerifyTimestampSuccess() {
    long currentTimestamp = System.currentTimeMillis();
    // Verify within 5 minutes (300,000 ms) tolerance
    assertTrue(WebhookVerifier.verifyTimestamp(currentTimestamp - 10000, 300000));
    assertTrue(WebhookVerifier.verifyTimestamp(currentTimestamp + 10000, 300000));
  }

  @Test
  void testVerifyTimestampOutsideToleranceFailure() {
    long currentTimestamp = System.currentTimeMillis();
    // Verify outside 5 seconds (5000 ms) tolerance
    assertFalse(WebhookVerifier.verifyTimestamp(currentTimestamp - 10000, 5000));
  }
}
