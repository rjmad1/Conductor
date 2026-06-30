package com.conductor.integrations;

import static org.junit.jupiter.api.Assertions.*;

import com.conductor.integrations.framework.EncryptionKeyValidator;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

public class EncryptionKeyValidatorTest {

  @Test
  void customKey_noActiveProfile_passes() {
    MockEnvironment env = new MockEnvironment();
    EncryptionKeyValidator v = new EncryptionKeyValidator("customKey_32bytes_______________!", env);
    assertDoesNotThrow(v::afterPropertiesSet);
  }

  @Test
  void defaultKey_testProfile_passes() {
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles("test");
    EncryptionKeyValidator v = new EncryptionKeyValidator("defaultValueDefaultValueDefaultVa", env);
    assertDoesNotThrow(v::afterPropertiesSet, "Default key must be allowed in the test profile");
  }

  @Test
  void defaultKey_devProfile_passes() {
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles("dev");
    EncryptionKeyValidator v = new EncryptionKeyValidator("defaultValueDefaultValueDefaultVa", env);
    assertDoesNotThrow(v::afterPropertiesSet);
  }

  @Test
  void defaultKey_noProfile_fails() {
    MockEnvironment env = new MockEnvironment();
    // No active profile — simulates a production startup without a profile override
    EncryptionKeyValidator v = new EncryptionKeyValidator("defaultValueDefaultValueDefaultVa", env);
    IllegalStateException ex =
        assertThrows(
            IllegalStateException.class,
            v::afterPropertiesSet,
            "Application must refuse to start with the default key outside dev/test");
    assertTrue(ex.getMessage().contains("INTEGRATION_ENCRYPTION_KEY"));
  }

  @Test
  void defaultKey_prodProfile_fails() {
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles("prod");
    EncryptionKeyValidator v = new EncryptionKeyValidator("defaultValueDefaultValueDefaultVa", env);
    assertThrows(IllegalStateException.class, v::afterPropertiesSet);
  }

  @Test
  void defaultKey_productionProfile_fails() {
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles("production");
    EncryptionKeyValidator v = new EncryptionKeyValidator("defaultValueDefaultValueDefaultVa", env);
    assertThrows(IllegalStateException.class, v::afterPropertiesSet);
  }
}
