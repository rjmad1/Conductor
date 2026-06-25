package com.conductor.integrations;

import com.conductor.integrations.framework.CredentialEncryptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WebhookSecretEncryptionTest {

    private static final String KEY_32 = "testKeytestKeytestKeytestKeytest";
    private final CredentialEncryptor encryptor = new CredentialEncryptor(KEY_32);

    @Test
    void webhookSecret_encryptedAtRest_decryptsToOriginal() {
        String rawSecret = "whsec_abcdef1234567890";
        String encrypted = encryptor.encrypt(rawSecret);
        assertNotEquals(rawSecret, encrypted, "Stored value must not be the plaintext secret");
        assertEquals(rawSecret, encryptor.decrypt(encrypted), "Decrypted value must match original");
    }

    @Test
    void twoEncryptionsOfSameSecret_produceDifferentCiphertext() {
        String rawSecret = "whsec_same_secret";
        String enc1 = encryptor.encrypt(rawSecret);
        String enc2 = encryptor.encrypt(rawSecret);
        assertNotEquals(enc1, enc2, "Random IV must produce distinct ciphertexts on each call");
        assertEquals(rawSecret, encryptor.decrypt(enc1));
        assertEquals(rawSecret, encryptor.decrypt(enc2));
    }

    @Test
    void wrongKey_failsDecryption() {
        String rawSecret = "whsec_sensitive";
        String encrypted = encryptor.encrypt(rawSecret);
        CredentialEncryptor wrongKey = new CredentialEncryptor("wrongKeywrongKeywrongKeywrongKey");
        assertThrows(RuntimeException.class, () -> wrongKey.decrypt(encrypted),
                "Decryption with wrong key must throw, not return garbage");
    }
}
