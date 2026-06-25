package com.conductor.tenant.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.conductor.tenant.BaseTenantIntegrationTest;
import com.conductor.tenant.domain.Tenant;
import com.conductor.tenant.domain.TenantStatus;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class TenantRepositoryTest extends BaseTenantIntegrationTest {

  @Autowired private TenantRepository tenantRepository;

  @BeforeEach
  void setUp() {
    tenantRepository.deleteAll();
  }

  @Test
  void whenSaveTenant_thenSucceedsAndGeneratesIdAndVersion() {
    Tenant tenant = new Tenant();
    tenant.setTenantKey("t-1");
    tenant.setDisplayName("Tenant 1");
    tenant.setDomain("t1.com");
    tenant.setStatus(TenantStatus.ACTIVE);
    tenant.setTimezone("UTC");
    tenant.setLocale("en_US");
    tenant.setDefaultCurrency("USD");

    Tenant saved = tenantRepository.save(tenant);

    assertNotNull(saved.getId());
    assertNotNull(saved.getVersion());
    assertEquals(0L, saved.getVersion());
    assertEquals("t-1", saved.getTenantKey());
  }

  @Test
  void whenSaveDuplicateTenantKey_thenThrowsException() {
    Tenant t1 = new Tenant();
    t1.setTenantKey("dup-key");
    t1.setDisplayName("Tenant 1");
    t1.setDomain("t1.com");
    tenantRepository.save(t1);

    Tenant t2 = new Tenant();
    t2.setTenantKey("dup-key");
    t2.setDisplayName("Tenant 2");
    t2.setDomain("t2.com");

    assertThrows(DataIntegrityViolationException.class, () -> tenantRepository.save(t2));
  }

  @Test
  void whenOptimisticLockingTriggered_thenThrowsException() {
    Tenant tenant = new Tenant();
    tenant.setTenantKey("opt-key");
    tenant.setDisplayName("Tenant");
    tenant.setDomain("opt.com");
    Tenant saved = tenantRepository.save(tenant);

    Tenant copy1 = tenantRepository.findById(saved.getId()).orElseThrow();
    Tenant copy2 = tenantRepository.findById(saved.getId()).orElseThrow();

    copy1.setDisplayName("Name 1");
    tenantRepository.save(copy1);

    copy2.setDisplayName("Name 2");
    assertThrows(ObjectOptimisticLockingFailureException.class, () -> tenantRepository.save(copy2));
  }

  @Test
  void whenQueryingPaged_thenReturnsCorrectResultsExcludingDeleted() {
    Tenant t1 = new Tenant();
    t1.setTenantKey("key-1");
    t1.setDisplayName("T1");
    t1.setDomain("t1.com");
    tenantRepository.save(t1);

    Tenant t2 = new Tenant();
    t2.setTenantKey("key-2");
    t2.setDisplayName("T2");
    t2.setDomain("t2.com");
    t2.setStatus(TenantStatus.DEACTIVATED);
    tenantRepository.save(t2);

    Tenant t3 = new Tenant();
    t3.setTenantKey("key-3");
    t3.setDisplayName("T3");
    t3.setDomain("t3.com");
    t3.setStatus(TenantStatus.DELETED);
    tenantRepository.save(t3);

    // List all
    List<Tenant> paged = tenantRepository.findAllPaged(null, PageRequest.of(0, 10));
    assertEquals(2, paged.size()); // key-1 and key-2, key-3 (deleted) is excluded

    // Cursor pagination check
    List<Tenant> page1 = tenantRepository.findAllPaged(null, PageRequest.of(0, 1));
    assertEquals(1, page1.size());
    UUID firstId = page1.get(0).getId();

    List<Tenant> page2 = tenantRepository.findAllPaged(firstId, PageRequest.of(0, 1));
    assertEquals(1, page2.size());
    assertNotEquals(firstId, page2.get(0).getId());
  }
}
