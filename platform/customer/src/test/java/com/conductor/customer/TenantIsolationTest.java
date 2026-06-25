package com.conductor.customer;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conductor.customer.api.CustomerController;
import com.conductor.customer.domain.Customer;
import com.conductor.customer.service.CustomerMergeService;
import com.conductor.customer.service.CustomerSearchService;
import com.conductor.customer.service.CustomerService;
import com.conductor.customer.service.IdentityResolutionService;
import com.conductor.shared.customer.CustomerStatus;
import com.conductor.shared.middleware.tenant.TenantContext;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerController.class)
class TenantIsolationTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CustomerService customerService;

  @MockBean private CustomerMergeService customerMergeService;

  @MockBean private CustomerSearchService customerSearchService;

  @MockBean private IdentityResolutionService identityResolutionService;

  @MockBean private com.conductor.customer.service.ContactService contactService;

  @MockBean private com.conductor.customer.service.ConsentService consentService;

  @MockBean private com.conductor.customer.service.TagService tagService;

  @MockBean private com.conductor.customer.service.SegmentService segmentService;

  @MockBean private com.conductor.customer.service.CustomerTimelineService timelineService;

  @MockBean private com.conductor.shared.messaging.NatsConnectionManager natsConnectionManager;

  @MockBean private com.conductor.shared.messaging.EventConsumer eventConsumer;

  @MockBean private com.conductor.shared.messaging.EventObservability eventObservability;

  @MockBean private io.micrometer.core.instrument.MeterRegistry meterRegistry;

  @MockBean private com.conductor.shared.middleware.tenant.TenantFilterAspect tenantFilterAspect;

  @MockBean private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

  private UUID tenantId;

  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
    TenantContext.setCurrentTenantId(tenantId);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  @WithMockUser(roles = "TENANT_ADMIN")
  void givenActiveTenant_whenGetOwnCustomer_thenOk() throws Exception {
    UUID customerId = UUID.randomUUID();
    Customer customer = new Customer();
    customer.setId(customerId);
    customer.setTenantId(tenantId);
    customer.setDisplayName("John Doe");
    customer.setStatus(CustomerStatus.ACTIVE);

    when(customerService.findById(customerId)).thenReturn(Optional.of(customer));

    mockMvc
        .perform(
            get("/api/v1/customers/" + customerId)
                .header("X-Tenant-ID", tenantId.toString())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "TENANT_ADMIN")
  void givenActiveTenant_whenGetCrossTenantCustomer_thenReturn404() throws Exception {
    UUID crossTenantCustomerId = UUID.randomUUID();

    // CustomerService returns empty optional due to row-level tenant filtering
    when(customerService.findById(crossTenantCustomerId)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            get("/api/v1/customers/" + crossTenantCustomerId)
                .header("X-Tenant-ID", tenantId.toString())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void givenUnauthenticatedUser_whenGetCustomer_thenReturn401Or403() throws Exception {
    UUID customerId = UUID.randomUUID();
    mockMvc
        .perform(get("/api/v1/customers/" + customerId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }
}
