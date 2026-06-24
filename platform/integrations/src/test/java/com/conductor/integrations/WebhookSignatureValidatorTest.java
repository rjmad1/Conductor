package com.conductor.integrations;

import com.conductor.integrations.webhooks.WebhookSignatureValidator;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

public class WebhookSignatureValidatorTest {

    @Test
    public void testShopifySignature() {
        WebhookSignatureValidator validator = new WebhookSignatureValidator();
        byte[] body = "{\"shop\":\"mock.myshopify.com\"}".getBytes(StandardCharsets.UTF_8);
        String secret = "shopify-secret";
        
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] computedHash = mac.doFinal(body);
            String validSignature = java.util.Base64.getEncoder().encodeToString(computedHash);

            assertTrue(validator.validateShopify(body, validSignature, secret));
            assertFalse(validator.validateShopify(body, "invalid-sig", secret));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testRazorpaySignature() {
        WebhookSignatureValidator validator = new WebhookSignatureValidator();
        byte[] body = "{\"event\":\"payment.captured\"}".getBytes(StandardCharsets.UTF_8);
        String secret = "razorpay-secret";

        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] computedHash = mac.doFinal(body);

            StringBuilder hexString = new StringBuilder();
            for (byte b : computedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String validSignature = hexString.toString();

            assertTrue(validator.validateRazorpay(body, validSignature, secret));
            assertFalse(validator.validateRazorpay(body, "invalid-sig", secret));
        } catch (Exception e) {
            fail(e);
        }
    }
}
