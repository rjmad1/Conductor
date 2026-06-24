package com.conductor.identity;

import com.conductor.shared.security.HmacValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HmacValidatorTest {

    private final String secret = "super_secret_webhook_key";
    private final String payload = "{\"event\":\"test\",\"data\":\"hello\"}";
    
    // Pre-computed hex signature for the payload with the secret
    private final String validSignature = "62f0d9ba9234c83a13c78c61b8216e3a09c6e7be61a630e21bb16b5622e2e36e";

    @Test
    void givenValidPayloadAndSignature_whenValidated_thenReturnTrue() {
        assertTrue(HmacValidator.isValidSignature(payload.getBytes(), validSignature, secret));
    }

    @Test
    void givenInvalidSignature_whenValidated_thenReturnFalse() {
        assertFalse(HmacValidator.isValidSignature(payload.getBytes(), "wrong_signature", secret));
    }

    @Test
    void givenInvalidSecret_whenValidated_thenReturnFalse() {
        assertFalse(HmacValidator.isValidSignature(payload.getBytes(), validSignature, "wrong_secret"));
    }

    @Test
    void givenNullParameters_whenValidated_thenReturnFalse() {
        assertFalse(HmacValidator.isValidSignature(null, validSignature, secret));
        assertFalse(HmacValidator.isValidSignature(payload.getBytes(), null, secret));
        assertFalse(HmacValidator.isValidSignature(payload.getBytes(), validSignature, null));
    }
}
