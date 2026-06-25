package com.conductor.tenant.api;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conductor.shared.middleware.tenant.TenantContext;
import com.conductor.tenant.BaseTenantIntegrationTest;
import com.conductor.tenant.api.dto.CreateTenantRequest;
import com.conductor.tenant.api.dto.UpdateTenantRequest;
import com.conductor.tenant.domain.Tenant;
import com.conductor.tenant.domain.TenantStatus;
import com.conductor.tenant.repository.TenantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class TenantControllerTest extends BaseTenantIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private TenantRepository tenantRepository;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    tenantRepository.deleteAll();
    TenantContext.clear();
  }

  @Test
  void whenCreateTenant_withValidPlatformAdminToken_thenReturns201() throws Exception {
    CreateTenantRequest request =
        new CreateTenantRequest(
            "valid-key",
            "Display Name",
            "Legal Name Ltd",
            "valid-domain.com",
            "Asia/Kolkata",
            "en_IN",
            "INR");

    mockMvc
        .perform(
            post("/api/v1/tenants")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.tenantKey", is("valid-key")))
        .andExpect(jsonPath("$.displayName", is("Display Name")))
        .andExpect(jsonPath("$.domain", is("valid-domain.com")))
        .andExpect(jsonPath("$.status", is("ACTIVE")))
        .andExpect(jsonPath("$.timezone", is("Asia/Kolkata")))
        .andExpect(jsonPath("$.locale", is("en_IN")))
        .andExpect(jsonPath("$.defaultCurrency", is("INR")));

    List<Tenant> list = tenantRepository.findAll();
    assertEquals(1, list.size());
    assertEquals("valid-key", list.get(0).getTenantKey());
  }

  @Test
  void whenCreateTenant_withInvalidKey_thenReturns400() throws Exception {
    CreateTenantRequest request =
        new CreateTenantRequest(
            "invalid key spaces",
            "Display Name",
            "Legal Name Ltd",
            "valid-domain.com",
            "UTC",
            "en_US",
            "USD");

    mockMvc
        .perform(
            post("/api/v1/tenants")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title", is("Validation Failed")))
        .andExpect(jsonPath("$.status", is(400)))
        .andExpect(
            jsonPath(
                "$.extensions.tenantKey",
                containsString("tenantKey must contain only alphanumeric characters")));
  }

  @Test
  void whenGetTenant_byPlatformAdmin_thenReturns200() throws Exception {
    Tenant t = new Tenant();
    t.setTenantKey("platform-get");
    t.setDisplayName("Name");
    t.setDomain("pget.com");
    Tenant saved = tenantRepository.save(t);

    mockMvc
        .perform(
            get("/api/v1/tenants/" + saved.getId())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(saved.getId().toString())));
  }

  @Test
  void whenGetTenant_byTenantAdminCrossTenant_thenReturns404() throws Exception {
    Tenant t = new Tenant();
    t.setTenantKey("cross-tenant-key");
    t.setDisplayName("Name");
    t.setDomain("cross.com");
    Tenant saved = tenantRepository.save(t);

    UUID anotherTenantId = UUID.randomUUID();
    TenantContext.setCurrentTenantId(anotherTenantId);

    mockMvc
        .perform(
            get("/api/v1/tenants/" + saved.getId())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN")))
                .header("X-Tenant-ID", anotherTenantId.toString()))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenPatchTenant_withValidFields_thenReturns200() throws Exception {
    Tenant t = new Tenant();
    t.setTenantKey("patch-test");
    t.setDisplayName("Original Name");
    t.setDomain("patch.com");
    Tenant saved = tenantRepository.save(t);

    UpdateTenantRequest request =
        new UpdateTenantRequest(
            "Updated Name", "Updated Legal", "patch-new.com", "GMT", "en_GB", "GBP");

    mockMvc
        .perform(
            patch("/api/v1/tenants/" + saved.getId())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayName", is("Updated Name")))
        .andExpect(jsonPath("$.legalName", is("Updated Legal")))
        .andExpect(jsonPath("$.domain", is("patch-new.com")))
        .andExpect(jsonPath("$.timezone", is("GMT")))
        .andExpect(jsonPath("$.locale", is("en_GB")))
        .andExpect(jsonPath("$.defaultCurrency", is("GBP")));
  }

  @Test
  void whenDeactivateTenant_thenStatusChanges() throws Exception {
    Tenant t = new Tenant();
    t.setTenantKey("deact-test");
    t.setDisplayName("Name");
    t.setDomain("deact.com");
    Tenant saved = tenantRepository.save(t);

    mockMvc
        .perform(
            post("/api/v1/tenants/" + saved.getId() + "/deactivate")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN"))))
        .andExpect(status().isNoContent());

    Tenant updated = tenantRepository.findById(saved.getId()).orElseThrow();
    assertEquals(TenantStatus.DEACTIVATED, updated.getStatus());
  }

  @Test
  void whenDeleteTenant_thenStatusIsDeletedAndSoftDeleted() throws Exception {
    Tenant t = new Tenant();
    t.setTenantKey("delete-test");
    t.setDisplayName("Name");
    t.setDomain("delete.com");
    Tenant saved = tenantRepository.save(t);

    mockMvc
        .perform(
            delete("/api/v1/tenants/" + saved.getId())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN"))))
        .andExpect(status().isNoContent());

    Tenant updated = tenantRepository.findById(saved.getId()).orElseThrow();
    assertEquals(TenantStatus.DELETED, updated.getStatus());
    assertNotNull(updated.getDeletedAt());
  }

  @Test
  void whenListTenants_withCursor_thenSucceeds() throws Exception {
    Tenant t1 = new Tenant();
    t1.setTenantKey("t-list-1");
    t1.setDisplayName("T1");
    t1.setDomain("tlist1.com");
    tenantRepository.save(t1);

    Tenant t2 = new Tenant();
    t2.setTenantKey("t-list-2");
    t2.setDisplayName("T2");
    t2.setDomain("tlist2.com");
    tenantRepository.save(t2);

    mockMvc
        .perform(
            get("/api/v1/tenants?limit=1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.hasMore", is(true)))
        .andExpect(jsonPath("$.nextCursor", notNullValue()));
  }
}
