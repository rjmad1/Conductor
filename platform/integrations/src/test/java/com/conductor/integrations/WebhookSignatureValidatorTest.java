package com.conductor.integrations;

import com.conductor.integrations.webhooks.WebhookSignatureValidator;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class WebhookSignatureValidatorTest {

    private final WebhookSignatureValidator validator = new WebhookSignatureValidator();

    // ── Shopify ──────────────────────────────────────────────────────────────

    @Test
    void shopify_validSignature_returnsTrue() throws Exception {
        byte[] body = "{\"shop\":\"mock.myshopify.com\"}".getBytes(StandardCharsets.UTF_8);
        String secret = "shopify-secret";
        String sig = computeShopifySignature(body, secret);
        assertTrue(validator.validateShopify(body, sig, secret));
    }

    @Test
    void shopify_invalidSignature_returnsFalse() {
        byte[] body = "{\"shop\":\"mock.myshopify.com\"}".getBytes(StandardCharsets.UTF_8);
        assertFalse(validator.validateShopify(body, "invalid-sig", "shopify-secret"));
    }

    @Test
    void shopify_nullBody_returnsFalse() {
        assertFalse(validator.validateShopify(null, "sig", "secret"));
    }

    @Test
    void shopify_nullSignature_returnsFalse() {
        assertFalse(validator.validateShopify("body".getBytes(), null, "secret"));
    }

    // ── Razorpay ─────────────────────────────────────────────────────────────

    @Test
    void razorpay_validSignature_returnsTrue() throws Exception {
        byte[] body = "{\"event\":\"payment.captured\"}".getBytes(StandardCharsets.UTF_8);
        String secret = "razorpay-secret";
        String sig = computeHmacHex(body, secret);
        assertTrue(validator.validateRazorpay(body, sig, secret));
    }

    @Test
    void razorpay_invalidSignature_returnsFalse() {
        byte[] body = "{\"event\":\"payment.captured\"}".getBytes(StandardCharsets.UTF_8);
        assertFalse(validator.validateRazorpay(body, "deadbeef", "razorpay-secret"));
    }

    // ── Zoho ─────────────────────────────────────────────────────────────────

    @Test
    void zoho_validHmacSignature_returnsTrue() throws Exception {
        byte[] body = "{\"lead\":\"new\"}".getBytes(StandardCharsets.UTF_8);
        String secret = "zoho-secret";
        String sig = computeHmacHex(body, secret);
        assertTrue(validator.validateZoho(body, sig, secret),
                "Zoho validation must compute HMAC, not compare signature to secret");
    }

    @Test
    void zoho_secretPassedAsSignature_returnsFalse() {
        // Regression: old implementation returned true when signature == secret.
        // This must now fail.
        byte[] body = "{\"lead\":\"new\"}".getBytes(StandardCharsets.UTF_8);
        String secret = "zoho-secret";
        assertFalse(validator.validateZoho(body, secret, secret),
                "Passing the secret itself as the signature must be rejected");
    }

    @Test
    void zoho_invalidSignature_returnsFalse() {
        byte[] body = "{\"lead\":\"new\"}".getBytes(StandardCharsets.UTF_8);
        assertFalse(validator.validateZoho(body, "invalidsig", "zoho-secret"));
    }

    @Test
    void zoho_nullSignature_returnsFalse() {
        assertFalse(validator.validateZoho("body".getBytes(), null, "secret"));
    }

    @Test
    void zoho_nullSecret_returnsFalse() {
        assertFalse(validator.validateZoho("body".getBytes(), "sig", null));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String computeShopifySignature(byte[] body, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(body));
    }

    private static String computeHmacHex(byte[] body, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(body);
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
