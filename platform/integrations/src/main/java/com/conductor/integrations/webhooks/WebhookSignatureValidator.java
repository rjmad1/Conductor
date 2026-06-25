package com.conductor.integrations.webhooks;

import com.conductor.shared.security.HmacValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class WebhookSignatureValidator {

    private static final Logger log = LoggerFactory.getLogger(WebhookSignatureValidator.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    public boolean validateShopify(byte[] body, String signatureBase64, String secret) {
        if (body == null || signatureBase64 == null || secret == null) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKey);
            byte[] computedHash = mac.doFinal(body);
            String computedSignature = Base64.getEncoder().encodeToString(computedHash);

            return MessageDigest.isEqual(
                    signatureBase64.getBytes(StandardCharsets.UTF_8),
                    computedSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Shopify signature validation error", e);
            return false;
        }
    }

    public boolean validateRazorpay(byte[] body, String signatureHex, String secret) {
        return HmacValidator.isValidSignature(body, signatureHex, secret);
    }

    /**
     * Validates Zoho webhook using HMAC-SHA256.
     * Zoho sends the signature as a hex-encoded HMAC-SHA256 of the raw body keyed with the shared secret.
     */
    public boolean validateZoho(byte[] body, String signatureHex, String secret) {
        if (body == null || signatureHex == null || secret == null) {
            return false;
        }
        return HmacValidator.isValidSignature(body, signatureHex, secret);
    }
}
