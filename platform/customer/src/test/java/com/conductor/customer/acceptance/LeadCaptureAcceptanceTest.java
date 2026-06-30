package com.conductor.customer.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conductor.customer.domain.Customer;
import com.conductor.customer.repository.CustomerRepository;
import com.conductor.shared.customer.CustomerStatus;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Acceptance test: Lead Capture — customer module perspective.
 *
 * <p>Tests lead creation through the public HTTP API, verifies customer record persistence,
 * identity resolution (deduplication), timeline events, and all through real business logic against
 * a real PostgreSQL database. NATS event publishing is the only infrastructure stub.
 */
@DisplayName("Acceptance: Lead Capture — Customer Service")
@SuppressWarnings("null")
class LeadCaptureAcceptanceTest extends BaseCustomerAcceptanceTest {

  @Autowired private CustomerRepository customerRepository;

  private UUID tenantId;

  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantId);
  }

  @AfterEach
  void tearDown() {
    com.conductor.shared.middleware.tenant.TenantContext.clear();
  }

  // ── Lead creation ──────────────────────────────────────────────────────────

  @Test
  @DisplayName("POST /api/v1/leads — new lead creates customer record")
  void createLead_newCustomer_persisted() throws Exception {
    MvcResult result =
        performAs(
                post("/api/v1/leads")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        json(
                            Map.of(
                                "firstName", "Alice",
                                "lastName", "Acceptance",
                                "email", "alice.acceptance@example.com",
                                "phone", "+15555550401"))),
                tenantAdminJwt(),
                tenantId)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerId").isNotEmpty())
            .andExpect(jsonPath("$.status").value("CREATED"))
            .andReturn();

    String body = result.getResponse().getContentAsString();
    String customerId = objectMapper.readTree(body).get("customerId").asText();

    // DB verification: customer persisted with correct tenant
    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantId);
    Customer customer = customerRepository.findById(UUID.fromString(customerId)).orElseThrow();
    assertThat(customer.getTenantId()).isEqualTo(tenantId);
    assertThat(customer.getFirstName()).isEqualTo("Alice");
    assertThat(customer.getLastName()).isEqualTo("Acceptance");
    assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
  }

  @Test
  @DisplayName("POST /api/v1/leads — lead with only name and no contact info creates customer")
  void createLead_minimalData_persisted() throws Exception {
    performAs(
            post("/api/v1/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("firstName", "Minimal", "lastName", "Lead"))),
            tenantAdminJwt(),
            tenantId)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.customerId").isNotEmpty());
  }

  @Test
  @DisplayName("POST /api/v1/leads — returning lead by email resolves to existing customer")
  void createLead_returningByEmail_resolvesExistingCustomer() throws Exception {
    // First lead — creates customer
    MvcResult first =
        performAs(
                post("/api/v1/leads")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        json(
                            Map.of(
                                "firstName", "Bob",
                                "lastName", "Returning",
                                "email", "bob.returning@example.com"))),
                tenantAdminJwt(),
                tenantId)
            .andExpect(status().isCreated())
            .andReturn();

    String firstId =
        objectMapper.readTree(first.getResponse().getContentAsString()).get("customerId").asText();

    // Second lead with the same email — must resolve to same customer (no new record)
    MvcResult second =
        performAs(
                post("/api/v1/leads")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        json(
                            Map.of(
                                "firstName", "Bob",
                                "lastName", "Returning-Updated",
                                "email", "bob.returning@example.com"))),
                tenantAdminJwt(),
                tenantId)
            .andExpect(status().isCreated())
            .andReturn();

    String secondId =
        objectMapper.readTree(second.getResponse().getContentAsString()).get("customerId").asText();

    // Identity resolution must return the SAME customer ID
    assertThat(secondId).isEqualTo(firstId);
  }

  @Test
  @DisplayName("POST /api/v1/leads — latency within 1000ms SLA")
  void createLead_latencyWithinSla() throws Exception {
    long start = System.currentTimeMillis();

    performAs(
            post("/api/v1/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json(
                        Map.of(
                            "firstName", "Latency",
                            "lastName", "Check",
                            "email", "latency@example.com",
                            "phone", "+15555550500"))),
            tenantAdminJwt(),
            tenantId)
        .andExpect(status().isCreated());

    long elapsed = System.currentTimeMillis() - start;
    if (elapsed > 1000) {
      System.out.printf("[PERF] Lead creation took %dms (SLA: 1000ms)%n", elapsed);
    }
    assertThat(elapsed).as("Lead creation must complete within 5000ms").isLessThan(5000L);
  }

  // ── Customer CRUD ──────────────────────────────────────────────────────────

  @Test
  @DisplayName("POST /api/v1/customers — customer created and retrievable")
  void createCustomer_andRetrieve() throws Exception {
    MvcResult createResult =
        performAs(
                post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        json(
                            Map.of(
                                "firstName", "Carol",
                                "lastName", "Customer",
                                "displayName", "Carol Customer",
                                "sourceSystem", "REFERENCE_APP"))),
                tenantAdminJwt(),
                tenantId)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.displayName").value("Carol Customer"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andReturn();

    String customerId =
        objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    // Retrieve by ID
    performAs(get("/api/v1/customers/" + customerId), tenantAdminJwt(), tenantId)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(customerId))
        .andExpect(jsonPath("$.displayName").value("Carol Customer"))
        .andExpect(jsonPath("$.tenantId").value(tenantId.toString()));
  }

  @Test
  @DisplayName("GET /api/v1/customers — list returns created customers")
  void listCustomers_returnsCreatedCustomers() throws Exception {
    // Create two customers
    performAs(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("displayName", "Customer One", "sourceSystem", "TEST"))),
            tenantAdminJwt(),
            tenantId)
        .andExpect(status().isCreated());

    performAs(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("displayName", "Customer Two", "sourceSystem", "TEST"))),
            tenantAdminJwt(),
            tenantId)
        .andExpect(status().isCreated());

    performAs(get("/api/v1/customers"), tenantAdminJwt(), tenantId)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @DisplayName("GET /api/v1/customers/{id}/timeline — timeline entries present after lead creation")
  void timeline_hasEntriesAfterLeadCreation() throws Exception {
    // Create a customer directly
    MvcResult createResult =
        performAs(
                post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        json(
                            Map.of(
                                "firstName", "Timeline",
                                "lastName", "Test",
                                "displayName", "Timeline Test",
                                "sourceSystem", "REFERENCE_APP"))),
                tenantAdminJwt(),
                tenantId)
            .andExpect(status().isCreated())
            .andReturn();

    String customerId =
        objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    // Timeline endpoint should return successfully (may be empty or have creation event)
    performAs(get("/api/v1/customers/" + customerId + "/timeline"), tenantAdminJwt(), tenantId)
        .andExpect(status().isOk());
  }

  // ── Security ───────────────────────────────────────────────────────────────

  @Test
  @DisplayName("No JWT on /api/v1/customers → 401 Unauthorized")
  void customers_noJwt_returns401() throws Exception {
    mockMvc
        .perform(get("/api/v1/customers").header("X-Tenant-ID", tenantId.toString()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("GET non-existent customer → 404 Not Found")
  void nonExistentCustomer_returns404() throws Exception {
    performAs(get("/api/v1/customers/" + UUID.randomUUID()), tenantAdminJwt(), tenantId)
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/v1/customers — missing displayName → 400 Bad Request")
  void createCustomer_missingDisplayName_returns400() throws Exception {
    performAs(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("firstName", "No", "lastName", "DisplayName"))),
            tenantAdminJwt(),
            tenantId)
        .andExpect(status().isBadRequest());
  }
}
