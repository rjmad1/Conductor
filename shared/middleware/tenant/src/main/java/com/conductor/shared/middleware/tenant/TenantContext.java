package com.conductor.shared.middleware.tenant;

import java.util.UUID;

public final class TenantContext {

  private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();
  private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

  private TenantContext() {}

  public static UUID getCurrentTenantId() {
    return CURRENT_TENANT.get();
  }

  public static void setCurrentTenantId(UUID tenantId) {
    CURRENT_TENANT.set(tenantId);
  }

  public static String getCurrentUserId() {
    return CURRENT_USER.get();
  }

  public static void setCurrentUserId(String userId) {
    CURRENT_USER.set(userId);
  }

  public static void clear() {
    CURRENT_TENANT.remove();
    CURRENT_USER.remove();
  }
}
