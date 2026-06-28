package com.conductor.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTenantRequest {
  @NotBlank private String tenantKey;

  @NotBlank private String displayName;

  @NotBlank private String legalName;

  @NotBlank private String domain;

  private String timezone;
  private String locale;
  private String defaultCurrency;
}
