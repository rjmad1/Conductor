package com.conductor.customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.conductor.customer.domain.Customer;
import com.conductor.customer.domain.CustomerContact;
import com.conductor.customer.repository.CustomerContactRepository;
import com.conductor.customer.repository.CustomerRepository;
import com.conductor.customer.repository.CustomerSegmentRepository;
import com.conductor.customer.repository.CustomerTagRepository;
import com.conductor.customer.service.CustomerSearchService;
import com.conductor.customer.service.IdentityResolutionService;
import com.conductor.shared.customer.ContactType;
import com.conductor.shared.middleware.tenant.TenantContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class CustomerSearchServiceTest {

  @Mock private CustomerRepository customerRepository;

  @Mock private CustomerContactRepository contactRepository;

  @Mock private CustomerTagRepository tagRepository;

  @Mock private CustomerSegmentRepository segmentRepository;

  @Mock private IdentityResolutionService identityResolutionService;

  private CustomerSearchService customerSearchService;
  private UUID tenantId;

  @BeforeEach
  void setUp() {
    customerSearchService =
        new CustomerSearchService(
            customerRepository,
            contactRepository,
            tagRepository,
            segmentRepository,
            identityResolutionService);
    tenantId = UUID.randomUUID();
    TenantContext.setCurrentTenantId(tenantId);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void testSearchByPhone() {
    String phone = "+1234567890";
    String normalized = "+1234567890";
    String hash = "phoneHash";

    when(identityResolutionService.normalizePhone(phone)).thenReturn(normalized);
    when(identityResolutionService.hashPhone(normalized)).thenReturn(hash);

    UUID customerId = UUID.randomUUID();
    CustomerContact contact = new CustomerContact();
    contact.setCustomerId(customerId);

    when(contactRepository.findByTypeAndValueHash(ContactType.PHONE, hash))
        .thenReturn(Optional.of(contact));

    Customer customer = new Customer();
    customer.setId(customerId);
    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

    List<Customer> results = customerSearchService.searchByPhone(phone);

    assertEquals(1, results.size());
    assertEquals(customerId, results.get(0).getId());
  }

  @Test
  void testSearchByEmail() {
    String email = "test@example.com";
    String normalized = "test@example.com";
    String hash = "emailHash";

    when(identityResolutionService.normalizeEmail(email)).thenReturn(normalized);
    when(identityResolutionService.hashEmail(normalized)).thenReturn(hash);

    UUID customerId = UUID.randomUUID();
    CustomerContact contact = new CustomerContact();
    contact.setCustomerId(customerId);

    when(contactRepository.findByTypeAndValueHash(ContactType.EMAIL, hash))
        .thenReturn(Optional.of(contact));

    Customer customer = new Customer();
    customer.setId(customerId);
    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

    List<Customer> results = customerSearchService.searchByEmail(email);

    assertEquals(1, results.size());
    assertEquals(customerId, results.get(0).getId());
  }

  @Test
  void testSearchByName() {
    String query = "John";
    Customer customer = new Customer();
    customer.setId(UUID.randomUUID());

    when(customerRepository.searchByDisplayName(tenantId.toString(), "John"))
        .thenReturn(List.of(customer));

    List<Customer> results = customerSearchService.searchByName(query);

    assertEquals(1, results.size());
    assertEquals(customer.getId(), results.get(0).getId());
  }

  @Test
  void testSearchByTag() {
    UUID tagId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    Customer customer = new Customer();
    customer.setId(customerId);

    when(tagRepository.findCustomerIdsByTagId(tagId)).thenReturn(List.of(customerId));
    when(customerRepository.findAllById(List.of(customerId))).thenReturn(List.of(customer));

    List<Customer> results = customerSearchService.searchByTag(tagId);

    assertEquals(1, results.size());
    assertEquals(customerId, results.get(0).getId());
  }

  @Test
  void testSearchBySegment() {
    UUID segmentId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    Customer customer = new Customer();
    customer.setId(customerId);

    when(segmentRepository.findCustomerIdsBySegmentId(segmentId)).thenReturn(List.of(customerId));
    when(customerRepository.findAllById(List.of(customerId))).thenReturn(List.of(customer));

    List<Customer> results = customerSearchService.searchBySegment(segmentId);

    assertEquals(1, results.size());
    assertEquals(customerId, results.get(0).getId());
  }

  @Test
  void testMultiSearchDeduplication() {
    String phone = "+1234567890";
    String normalized = "+1234567890";
    String hash = "phoneHash";

    when(identityResolutionService.normalizePhone(phone)).thenReturn(normalized);
    when(identityResolutionService.hashPhone(normalized)).thenReturn(hash);

    UUID customerId = UUID.randomUUID();
    CustomerContact contact = new CustomerContact();
    contact.setCustomerId(customerId);

    when(contactRepository.findByTypeAndValueHash(ContactType.PHONE, hash))
        .thenReturn(Optional.of(contact));

    Customer customer = new Customer();
    customer.setId(customerId);
    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

    // When searching by name as well, return the same customer
    when(customerRepository.searchByDisplayName(tenantId.toString(), "John"))
        .thenReturn(List.of(customer));

    List<Customer> results = customerSearchService.search(phone, null, "John", null, null);

    // Should return only 1 customer because of de-duplication
    assertEquals(1, results.size());
    assertEquals(customerId, results.get(0).getId());
  }
}
