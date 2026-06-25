package com.conductor.integrations.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Fails application startup when the encryption key is the well-known insecure default
 * and the active Spring profile is not dev/test/local.
 *
 * Set INTEGRATION_ENCRYPTION_KEY to a cryptographically-random 32-byte value before
 * deploying to any non-development environment.
 */
@Component
public class EncryptionKeyValidator implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(EncryptionKeyValidator.class);
    // The default value baked into CredentialEncryptor — never acceptable in production
    static final String INSECURE_DEFAULT_PREFIX = "defaultValue";

    private final String encryptionKey;
    private final Environment environment;

    public EncryptionKeyValidator(
            @Value("${INTEGRATION_ENCRYPTION_KEY:defaultValueDefaultValueDefaultVa}") String encryptionKey,
            Environment environment) {
        this.encryptionKey = encryptionKey;
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() {
        if (!encryptionKey.startsWith(INSECURE_DEFAULT_PREFIX)) {
            return; // custom key — OK
        }

        String[] activeProfiles = environment.getActiveProfiles();
        boolean isSafeProfile = Arrays.stream(activeProfiles)
                .anyMatch(p -> p.contains("test") || p.contains("dev") || p.contains("local"));

        if (!isSafeProfile) {
            String msg = "FATAL: INTEGRATION_ENCRYPTION_KEY is the insecure default. " +
                    "Set a cryptographically-random 32-byte key via the INTEGRATION_ENCRYPTION_KEY " +
                    "environment variable before starting the application in a non-development profile.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        log.warn("INTEGRATION_ENCRYPTION_KEY is using the insecure default. " +
                "This is only acceptable in dev/test environments.");
    }
}
