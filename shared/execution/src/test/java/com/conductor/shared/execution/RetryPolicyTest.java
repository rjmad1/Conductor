package com.conductor.shared.execution;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RetryPolicy value object.
 */
class RetryPolicyTest {

    @Test
    void defaultValuesAreCorrect() {
        RetryPolicy policy = RetryPolicy.builder().build();

        assertThat(policy.getMaxAttempts()).isEqualTo(3);
        assertThat(policy.getInitialIntervalSeconds()).isEqualTo(2L);
        assertThat(policy.getBackoffCoefficient()).isEqualTo(2.0);
        assertThat(policy.getMaxIntervalSeconds()).isEqualTo(120L);
    }

    @Test
    void customValuesOverrideDefaults() {
        RetryPolicy policy = RetryPolicy.builder()
                .maxAttempts(5)
                .initialIntervalSeconds(10)
                .backoffCoefficient(1.5)
                .maxIntervalSeconds(60)
                .build();

        assertThat(policy.getMaxAttempts()).isEqualTo(5);
        assertThat(policy.getInitialIntervalSeconds()).isEqualTo(10L);
        assertThat(policy.getBackoffCoefficient()).isEqualTo(1.5);
        assertThat(policy.getMaxIntervalSeconds()).isEqualTo(60L);
    }

    @Test
    void auditStringContainsKeyFields() {
        RetryPolicy policy = RetryPolicy.builder().maxAttempts(3).build();
        String audit = policy.toAuditString();

        assertThat(audit).contains("maxAttempts=3");
        assertThat(audit).contains("initialInterval=");
        assertThat(audit).contains("backoff=");
    }
}
