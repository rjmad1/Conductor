package com.conductor.shared.middleware.tenant;

import java.util.UUID;

public final class TenantContextValidator {

    private TenantContextValidator() {
    }

    public static UUID getRequiredTenantId() {
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Active tenant context is required but was not found in the current thread");
        }
        return tenantId;
    }

    public static String getRequiredUserId() {
        String userId = TenantContext.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("Active user context is required but was not found in the current thread");
        }
        return userId;
    }
}
