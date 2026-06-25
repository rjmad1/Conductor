package com.conductor.tenant.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
    @NotBlank(message = "tenantKey is required")
        @Size(max = 255, message = "tenantKey must not exceed 255 characters")
        @Pattern(
            regexp = "^[a-zA-Z0-9-_]+$",
            message =
                "tenantKey must contain only alphanumeric characters, hyphens, or underscores")
        String tenantKey,
    @NotBlank(message = "displayName is required")
        @Size(max = 255, message = "displayName must not exceed 255 characters")
        String displayName,
    @Size(max = 255, message = "legalName must not exceed 255 characters") String legalName,
    @NotBlank(message = "domain is required")
        @Size(max = 255, message = "domain must not exceed 255 characters")
        String domain,
    String timezone,
    String locale,
    String defaultCurrency) {}
