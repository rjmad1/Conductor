package com.conductor.tenant;

import com.conductor.shared.middleware.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void givenTenantId_whenSetContext_thenRetrieveCorrectId() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setCurrentTenantId(tenantId);
        assertEquals(tenantId, TenantContext.getCurrentTenantId());
    }

    @Test
    void givenUserId_whenSetContext_thenRetrieveCorrectId() {
        String userId = "user-123-uuid";
        TenantContext.setCurrentUserId(userId);
        assertEquals(userId, TenantContext.getCurrentUserId());
    }

    @Test
    void whenCleared_thenRetrieveNull() {
        TenantContext.setCurrentTenantId(UUID.randomUUID());
        TenantContext.setCurrentUserId("user-123");
        TenantContext.clear();
        assertNull(TenantContext.getCurrentTenantId());
        assertNull(TenantContext.getCurrentUserId());
    }
}
