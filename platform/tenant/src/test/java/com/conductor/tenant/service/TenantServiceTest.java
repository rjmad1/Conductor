package com.conductor.tenant.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import com.conductor.tenant.domain.Tenant;
import com.conductor.tenant.domain.TenantStatus;
import com.conductor.tenant.repository.TenantRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

  @Mock private TenantRepository tenantRepository;
  @Mock private KeycloakAdminService keycloakAdminService;
  @Mock private NatsEventPublisher eventPublisher;
  @Mock private AuditLogger auditLogger;
  @Mock private TenantMetrics tenantMetrics;

  private TenantService tenantService;

  @BeforeEach
  void setUp() {
    tenantService =
        new TenantService(
            tenantRepository, keycloakAdminService, eventPublisher, auditLogger, tenantMetrics);
  }

  @Test
  void whenCreateTenant_givenValidData_thenSucceeds() {
    String tenantKey = "test-tenant";
    String displayName = "Test Tenant Display Name";
    String domain = "test.conductor.io";

    when(tenantRepository.findByTenantKey(tenantKey)).thenReturn(Optional.empty());
    when(tenantRepository.findByDomain(domain)).thenReturn(Optional.empty());

    Tenant mockSaved = new Tenant();
    mockSaved.setId(UUID.randomUUID());
    mockSaved.setTenantKey(tenantKey);
    mockSaved.setDisplayName(displayName);
    mockSaved.setDomain(domain);
    mockSaved.setStatus(TenantStatus.ACTIVE);

    when(tenantRepository.save(any(Tenant.class))).thenReturn(mockSaved);

    Tenant result =
        tenantService.createTenant(
            tenantKey, displayName, "Legal Name", domain, "UTC", "en_US", "USD");

    assertNotNull(result);
    assertEquals(tenantKey, result.getTenantKey());
    assertEquals(displayName, result.getDisplayName());
    assertEquals(domain, result.getDomain());
    assertEquals(TenantStatus.ACTIVE, result.getStatus());

    verify(keycloakAdminService).createTenantRealm(anyString());
    verify(eventPublisher).publishEvent(eq("tenant"), eq("profile"), eq("created"), anyString());
    verify(auditLogger).logEvent(eq("CREATE"), anyString(), eq("SUCCESS"), anyString());
    verify(tenantMetrics).recordTenantCreated();
  }

  @Test
  void whenCreateTenant_givenDuplicateKey_thenThrowsException() {
    String tenantKey = "duplicate";
    when(tenantRepository.findByTenantKey(tenantKey)).thenReturn(Optional.of(new Tenant()));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            tenantService.createTenant(
                tenantKey, "Name", "Legal", "domain.com", "UTC", "en_US", "USD"));
  }

  @Test
  void whenCreateTenant_givenDuplicateDomain_thenThrowsException() {
    String domain = "duplicate.com";
    when(tenantRepository.findByTenantKey(anyString())).thenReturn(Optional.empty());
    when(tenantRepository.findByDomain(domain)).thenReturn(Optional.of(new Tenant()));

    assertThrows(
        IllegalArgumentException.class,
        () -> tenantService.createTenant("key", "Name", "Legal", domain, "UTC", "en_US", "USD"));
  }

  @Test
  void whenUpdateTenant_givenDeletedTenant_thenThrowsException() {
    UUID tenantId = UUID.randomUUID();
    Tenant deletedTenant = new Tenant();
    deletedTenant.setStatus(TenantStatus.DELETED);

    when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(deletedTenant));

    assertThrows(
        IllegalStateException.class,
        () ->
            tenantService.updateTenant(
                tenantId, "New Name", "New Legal", "new.com", "UTC", "en_US", "USD"));
  }

  @Test
  void whenDeactivateTenant_givenDeletedTenant_thenThrowsException() {
    UUID tenantId = UUID.randomUUID();
    Tenant deletedTenant = new Tenant();
    deletedTenant.setStatus(TenantStatus.DELETED);

    when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(deletedTenant));

    assertThrows(IllegalStateException.class, () -> tenantService.deactivateTenant(tenantId));
  }
}
