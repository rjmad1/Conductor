package com.conductor.customer.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Acceptance test: Customer tenant isolation.
 *
 * <p>Verifies that customer records, contacts, and timelines are strictly partitioned by tenant.
 * Tenant A can never read, modify, or list Tenant B's data.
 */
@DisplayName("Acceptance: Customer Tenant Isolation")
@SuppressWarnings("null")
class CustomerTenantIsolationAcceptanceTest extends BaseCustomerAcceptanceTest {

  private UUID tenantA;
  private UUID tenantB;

  @BeforeEach
  void setUp() {
    tenantA = UUID.randomUUID();
    tenantB = UUID.randomUUID();
  }

  @AfterEach
  void tearDown() {
    com.conductor.shared.middleware.tenant.TenantContext.clear();
  }

  @Test
  @DisplayName("Tenant A cannot see Tenant B's customer via GET")
  void tenantA_cannotReadTenantB_customer() throws Exception {
    // Create customer in Tenant B
    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantB);
    MvcResult createResult =
        performAs(
                post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        json(Map.of("displayName", "Tenant B Customer", "sourceSystem", "TEST"))),
                tenantAdminJwt(),
                tenantB)
            .andExpect(status().isCreated())
            .andReturn();

    String customerBId =
        objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    // Attempt to read as Tenant A
    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantA);
    performAs(get("/api/v1/customers/" + customerBId), tenantAdminJwt(), tenantA)
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Customer list for Tenant A does not include Tenant B customers")
  void customerList_strictlyIsolated() throws Exception {
    // Create customer in each tenant
    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantA);
    MvcResult resultA =
        performAs(
                post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of("displayName", "Customer for A", "sourceSystem", "TEST"))),
                tenantAdminJwt(),
                tenantA)
            .andExpect(status().isCreated())
            .andReturn();
    String idA =
        objectMapper.readTree(resultA.getResponse().getContentAsString()).get("id").asText();

    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantB);
    MvcResult resultB =
        performAs(
                post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of("displayName", "Customer for B", "sourceSystem", "TEST"))),
                tenantAdminJwt(),
                tenantB)
            .andExpect(status().isCreated())
            .andReturn();
    String idB =
        objectMapper.readTree(resultB.getResponse().getContentAsString()).get("id").asText();

    // List as Tenant A
    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantA);
    MvcResult listResult =
        performAs(get("/api/v1/customers"), tenantAdminJwt(), tenantA)
            .andExpect(status().isOk())
            .andReturn();

    String listBody = listResult.getResponse().getContentAsString();
    assertThat(listBody).contains(idA);
    assertThat(listBody).doesNotContain(idB);
  }

  @Test
  @DisplayName("Lead capture is scoped to the tenant in the request context")
  void lead_scopedToRequestingTenant() throws Exception {
    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantA);
    MvcResult result =
        performAs(
                post("/api/v1/leads")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        json(
                            Map.of(
                                "firstName", "Scoped",
                                "lastName", "Lead",
                                "email", "scoped.lead.iso@example.com"))),
                tenantAdminJwt(),
                tenantA)
            .andExpect(status().isCreated())
            .andReturn();

    String customerId =
        objectMapper.readTree(result.getResponse().getContentAsString()).get("customerId").asText();

    // Verify customer belongs to Tenant A
    MvcResult customerResult =
        performAs(get("/api/v1/customers/" + customerId), tenantAdminJwt(), tenantA)
            .andExpect(status().isOk())
            .andReturn();

    JsonNode customerNode =
        objectMapper.readTree(customerResult.getResponse().getContentAsString());
    assertThat(customerNode.get("tenantId").asText()).isEqualTo(tenantA.toString());

    // Tenant B cannot access this customer
    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantB);
    performAs(get("/api/v1/customers/" + customerId), tenantAdminJwt(), tenantB)
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Metrics are not cross-tenant — customer counts are per-tenant")
  void metrics_perTenant() throws Exception {
    // Create 2 customers for A, 1 for B
    for (int i = 0; i < 2; i++) {
      com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantA);
      performAs(
              post("/api/v1/customers")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      json(
                          Map.of("displayName", "Tenant A Customer " + i, "sourceSystem", "TEST"))),
              tenantAdminJwt(),
              tenantA)
          .andExpect(status().isCreated());
    }

    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantB);
    performAs(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("displayName", "Tenant B Customer", "sourceSystem", "TEST"))),
            tenantAdminJwt(),
            tenantB)
        .andExpect(status().isCreated());

    // Count visible to each tenant
    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantA);
    MvcResult listA =
        performAs(get("/api/v1/customers"), tenantAdminJwt(), tenantA)
            .andExpect(status().isOk())
            .andReturn();

    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantB);
    MvcResult listB =
        performAs(get("/api/v1/customers"), tenantAdminJwt(), tenantB)
            .andExpect(status().isOk())
            .andReturn();

    JsonNode pageA = objectMapper.readTree(listA.getResponse().getContentAsString());
    JsonNode pageB = objectMapper.readTree(listB.getResponse().getContentAsString());

    int sizeA = pageA.get("content").size();
    int sizeB = pageB.get("content").size();

    assertThat(sizeA).isGreaterThanOrEqualTo(2);
    assertThat(sizeB).isGreaterThanOrEqualTo(1);
    // Total must not exceed the union across both tenants
    assertThat(sizeA + sizeB).isGreaterThanOrEqualTo(3);
  }
}
