package com.conductor.customer.api;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conductor.customer.domain.Customer;
import com.conductor.customer.service.*;
import com.conductor.shared.customer.ContactType;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
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

@WebMvcTest(LeadController.class)
@WithMockUser(roles = {"TENANT_ADMIN"})
class LeadControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CustomerService customerService;
  @MockBean private ContactService contactService;
  @MockBean private IdentityResolutionService identityResolutionService;
  @MockBean private CustomerMergeService customerMergeService;
  @MockBean private NatsEventPublisher eventPublisher;
  @MockBean private ConsentService consentService;
  @MockBean private SegmentService segmentService;
  @MockBean private TagService tagService;
  @MockBean private CustomerTimelineService customerTimelineService;
  @MockBean private CustomerSearchService customerSearchService;

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
  void testCreateLead_NewCustomer() throws Exception {
    String payload =
        "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\",\"phone\":\"+1234567890\"}";

    when(identityResolutionService.resolveByEmail("john.doe@example.com"))
        .thenReturn(Optional.empty());
    when(identityResolutionService.resolveByPhone("+1234567890")).thenReturn(Optional.empty());

    Customer customer = new Customer();
    UUID customerId = UUID.randomUUID();
    customer.setId(customerId);
    customer.setFirstName("John");
    customer.setLastName("Doe");

    when(customerService.createCustomer(
            eq("John"), eq("Doe"), eq("John Doe"), any(), eq("LEAD_CAPTURE")))
        .thenReturn(customer);

    mockMvc
        .perform(
            post("/api/v1/leads")
                .with(csrf())
                .header("X-Tenant-ID", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.customerId").value(customerId.toString()))
        .andExpect(jsonPath("$.status").value("CREATED"));

    verify(customerService)
        .createCustomer(eq("John"), eq("Doe"), eq("John Doe"), any(), eq("LEAD_CAPTURE"));
    verify(contactService)
        .addContact(
            eq(customerId),
            eq(ContactType.EMAIL),
            eq("john.doe@example.com"),
            anyString(),
            eq(true));
    verify(contactService)
        .addContact(
            eq(customerId), eq(ContactType.PHONE), eq("+1234567890"), anyString(), eq(true));
  }
}
