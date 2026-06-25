package com.conductor.customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.conductor.customer.domain.Customer;
import com.conductor.customer.exception.CustomerNotFoundException;
import com.conductor.customer.repository.CustomerIdentifierRepository;
import com.conductor.customer.repository.CustomerRepository;
import com.conductor.customer.service.CustomerService;
import com.conductor.customer.service.CustomerTimelineService;
import com.conductor.shared.customer.CustomerStatus;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

  @Mock private CustomerRepository customerRepository;

  @Mock private CustomerIdentifierRepository identifierRepository;

  @Mock private CustomerTimelineService timelineService;

  @Mock private NatsEventPublisher eventPublisher;

  @Mock private AuditLogger auditLogger;

  private CustomerService customerService;

  @BeforeEach
  void setUp() {
    customerService =
        new CustomerService(
            customerRepository, identifierRepository, timelineService, eventPublisher, auditLogger);
  }

  @Test
  void testCreateCustomer() {
    when(customerRepository.save(any(Customer.class)))
        .thenAnswer(
            invocation -> {
              Customer c = invocation.getArgument(0);
              c.setId(UUID.randomUUID());
              return c;
            });

    Customer customer = customerService.createCustomer("John", "Doe", "John Doe", "ext-123", "CRM");

    assertNotNull(customer);
    assertNotNull(customer.getId());
    assertEquals("John", customer.getFirstName());
    assertEquals("Doe", customer.getLastName());
    assertEquals("John Doe", customer.getDisplayName());
    assertEquals("ext-123", customer.getExternalId());
    assertEquals("CRM", customer.getSourceSystem());
    assertEquals(CustomerStatus.ACTIVE, customer.getStatus());

    verify(timelineService).record(any(UUID.class), any(), anyString(), anyString(), anyString());
    verify(eventPublisher).publishEvent(anyString(), anyString(), anyString(), anyString());
    verify(auditLogger).logEvent(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testFindById() {
    UUID id = UUID.randomUUID();
    Customer customer = new Customer();
    customer.setId(id);

    when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

    Optional<Customer> found = customerService.findById(id);
    assertTrue(found.isPresent());
    assertEquals(id, found.get().getId());
  }

  @Test
  void testFindAll() {
    PageRequest pageRequest = PageRequest.of(0, 10);
    Customer c1 = new Customer();
    Customer c2 = new Customer();
    when(customerRepository.findAllActive(pageRequest)).thenReturn(new PageImpl<>(List.of(c1, c2)));

    Page<Customer> page = customerService.findAll(pageRequest);
    assertEquals(2, page.getTotalElements());
  }

  @Test
  void testUpdateCustomer() {
    UUID id = UUID.randomUUID();
    Customer customer = new Customer();
    customer.setId(id);
    customer.setFirstName("John");
    customer.setLastName("Doe");
    customer.setDisplayName("John Doe");

    when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
    when(customerRepository.save(any(Customer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Customer updated = customerService.updateCustomer(id, "Jane", "Doe", "Jane Doe");

    assertEquals("Jane", updated.getFirstName());
    assertEquals("Jane Doe", updated.getDisplayName());
    verify(timelineService).record(eq(id), any(), anyString(), anyString(), any());
  }

  @Test
  void testDeactivateCustomer() {
    UUID id = UUID.randomUUID();
    Customer customer = new Customer();
    customer.setId(id);
    customer.setStatus(CustomerStatus.ACTIVE);

    when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
    when(customerRepository.save(any(Customer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    customerService.deactivateCustomer(id);

    assertEquals(CustomerStatus.INACTIVE, customer.getStatus());
  }

  @Test
  void testSoftDeleteCustomer() {
    UUID id = UUID.randomUUID();
    Customer customer = new Customer();
    customer.setId(id);
    customer.setStatus(CustomerStatus.ACTIVE);

    when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
    when(customerRepository.save(any(Customer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    customerService.softDeleteCustomer(id);

    assertEquals(CustomerStatus.DELETED, customer.getStatus());
    assertNotNull(customer.getDeletedAt());
  }

  @Test
  void testArchiveCustomer() {
    UUID id = UUID.randomUUID();
    Customer customer = new Customer();
    customer.setId(id);
    customer.setStatus(CustomerStatus.ACTIVE);

    when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
    when(customerRepository.save(any(Customer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    customerService.archiveCustomer(id);

    assertEquals(CustomerStatus.ARCHIVED, customer.getStatus());
  }

  @Test
  void testRequireCustomer_ThrowsNotFound() {
    UUID id = UUID.randomUUID();
    when(customerRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(CustomerNotFoundException.class, () -> customerService.requireCustomer(id));
  }
}
