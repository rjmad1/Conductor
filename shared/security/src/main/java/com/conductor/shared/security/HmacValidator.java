package com.conductor.shared.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HmacValidator {

    private static final Logger log = LoggerFactory.getLogger(HmacValidator.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    private HmacValidator() {
    }

    public static boolean isValidSignature(byte[] payload, String signature, String secret) {
        if (payload == null || signature == null || secret == null) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA256);
            mac.init(secretKey);
            byte[] computedHash = mac.doFinal(payload);
            
            // Convert computed hash to hex string representation
            StringBuilder hexString = new StringBuilder();
            for (byte b : computedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // Perform constant-time comparison to prevent timing attacks
            byte[] signatureBytes = signature.getBytes();
            byte[] computedBytes = hexString.toString().getBytes();

            return MessageDigest.isEqual(signatureBytes, computedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error executing HMAC validation", e);
            return false;
        }
    }
}
