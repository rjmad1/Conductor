package com.conductor.tenant.api.dto;

import jakarta.validation.constraints.Size;

public record UpdateTenantRequest(
    @Size(max = 255, message = "displayName must not exceed 255 characters") String displayName,
    @Size(max = 255, message = "legalName must not exceed 255 characters") String legalName,
    @Size(max = 255, message = "domain must not exceed 255 characters") String domain,
    String timezone,
    String locale,
    String defaultCurrency) {}
