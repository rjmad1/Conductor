package com.conductor.tenant.api.dto;

import com.conductor.tenant.domain.Tenant;
import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
    UUID id,
    String tenantKey,
    String displayName,
    String legalName,
    String domain,
    String status,
    String timezone,
    String locale,
    String defaultCurrency,
    Instant createdAt,
    Instant updatedAt,
    Long version) {
  public static TenantResponse from(Tenant tenant) {
    return new TenantResponse(
        tenant.getId(),
        tenant.getTenantKey(),
        tenant.getDisplayName(),
        tenant.getLegalName(),
        tenant.getDomain(),
        tenant.getStatus().name(),
        tenant.getTimezone(),
        tenant.getLocale(),
        tenant.getDefaultCurrency(),
        tenant.getCreatedAt(),
        tenant.getUpdatedAt(),
        tenant.getVersion());
  }
}
