package com.conductor.tenant.api;

import com.conductor.shared.middleware.tenant.TenantContext;
import com.conductor.tenant.api.dto.CreateTenantRequest;
import com.conductor.tenant.api.dto.PageResponse;
import com.conductor.tenant.api.dto.TenantResponse;
import com.conductor.tenant.api.dto.UpdateTenantRequest;
import com.conductor.tenant.domain.Tenant;
import com.conductor.tenant.service.TenantService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

  private final TenantService tenantService;

  public TenantController(TenantService tenantService) {
    this.tenantService = tenantService;
  }

  @PostMapping
  @PreAuthorize("hasRole('ROLE_PLATFORM_ADMIN')")
  public ResponseEntity<TenantResponse> createTenant(
      @Valid @RequestBody CreateTenantRequest request) {
    Tenant tenant =
        tenantService.createTenant(
            request.tenantKey(),
            request.displayName(),
            request.legalName(),
            request.domain(),
            request.timezone(),
            request.locale(),
            request.defaultCurrency());
    return ResponseEntity.status(HttpStatus.CREATED).body(TenantResponse.from(tenant));
  }

  @GetMapping
  @PreAuthorize("hasRole('ROLE_PLATFORM_ADMIN')")
  public ResponseEntity<PageResponse<TenantResponse>> listTenants(
      @RequestParam(name = "limit", defaultValue = "20") int limit,
      @RequestParam(name = "starting_after", required = false) String startingAfter) {

    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException("Limit must be between 1 and 100");
    }

    UUID startingAfterUuid = null;
    if (startingAfter != null && !startingAfter.trim().isEmpty()) {
      try {
        byte[] decoded = Base64.getDecoder().decode(startingAfter);
        startingAfterUuid = UUID.fromString(new String(decoded, StandardCharsets.UTF_8));
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid starting_after cursor format");
      }
    }

    // Query limit + 1 to check if there is a next page
    List<Tenant> tenants = tenantService.listTenants(startingAfterUuid, limit + 1);
    boolean hasMore = tenants.size() > limit;
    List<Tenant> responseList = hasMore ? tenants.subList(0, limit) : tenants;

    List<TenantResponse> data = responseList.stream().map(TenantResponse::from).toList();

    String nextCursor = null;
    if (hasMore && !responseList.isEmpty()) {
      UUID lastId = responseList.get(responseList.size() - 1).getId();
      nextCursor =
          Base64.getEncoder().encodeToString(lastId.toString().getBytes(StandardCharsets.UTF_8));
    }

    return ResponseEntity.ok(
        PageResponse.<TenantResponse>builder()
            .data(data)
            .count(data.size())
            .hasMore(hasMore)
            .nextCursor(nextCursor)
            .build());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ROLE_PLATFORM_ADMIN', 'ROLE_TENANT_ADMIN')")
  public ResponseEntity<TenantResponse> getTenant(@PathVariable UUID id) {
    UUID activeTenantId = TenantContext.getCurrentTenantId();
    if (activeTenantId != null && !activeTenantId.equals(id)) {
      return ResponseEntity.notFound().build();
    }

    return tenantService
        .getTenant(id)
        .map(tenant -> ResponseEntity.ok(TenantResponse.from(tenant)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasAnyRole('ROLE_PLATFORM_ADMIN', 'ROLE_TENANT_ADMIN')")
  public ResponseEntity<TenantResponse> updateTenant(
      @PathVariable UUID id, @Valid @RequestBody UpdateTenantRequest request) {
    UUID activeTenantId = TenantContext.getCurrentTenantId();
    if (activeTenantId != null && !activeTenantId.equals(id)) {
      return ResponseEntity.notFound().build();
    }

    Tenant tenant =
        tenantService.updateTenant(
            id,
            request.displayName(),
            request.legalName(),
            request.domain(),
            request.timezone(),
            request.locale(),
            request.defaultCurrency());
    return ResponseEntity.ok(TenantResponse.from(tenant));
  }

  @PostMapping("/{id}/deactivate")
  @PreAuthorize("hasRole('ROLE_PLATFORM_ADMIN')")
  public ResponseEntity<Void> deactivateTenant(@PathVariable UUID id) {
    tenantService.deactivateTenant(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/activate")
  @PreAuthorize("hasRole('ROLE_PLATFORM_ADMIN')")
  public ResponseEntity<Void> activateTenant(@PathVariable UUID id) {
    tenantService.activateTenant(id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ROLE_PLATFORM_ADMIN')")
  public ResponseEntity<Void> deleteTenant(@PathVariable UUID id) {
    tenantService.deleteTenant(id);
    return ResponseEntity.noContent().build();
  }
}
