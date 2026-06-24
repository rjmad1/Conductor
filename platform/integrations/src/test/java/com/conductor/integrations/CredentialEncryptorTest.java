package com.conductor.integrations;

import com.conductor.integrations.framework.CredentialEncryptor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CredentialEncryptorTest {

    @Test
    public void testEncryptAndDecrypt() {
        CredentialEncryptor encryptor = new CredentialEncryptor("12345678901234567890123456789012");
        String secret = "shopify-super-secret-api-key";

        String encrypted = encryptor.encrypt(secret);
        assertNotNull(encrypted);
        assertNotEquals(secret, encrypted);

        String decrypted = encryptor.decrypt(encrypted);
        assertEquals(secret, decrypted);
    }
}
