package com.conductor.customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.conductor.customer.domain.Customer;
import com.conductor.customer.domain.CustomerContact;
import com.conductor.customer.domain.CustomerIdentifier;
import com.conductor.customer.repository.CustomerContactRepository;
import com.conductor.customer.repository.CustomerIdentifierRepository;
import com.conductor.customer.repository.CustomerRepository;
import com.conductor.customer.service.ScheduledAnonymizationTask;
import com.conductor.shared.customer.ContactType;
import com.conductor.shared.customer.CustomerStatus;
import com.conductor.shared.middleware.tenant.TenantContext;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ScheduledAnonymizationTaskTest {

  @Mock private CustomerRepository customerRepository;

  @Mock private CustomerContactRepository contactRepository;

  @Mock private CustomerIdentifierRepository identifierRepository;

  private ScheduledAnonymizationTask task;

  @BeforeEach
  void setUp() {
    task =
        new ScheduledAnonymizationTask(customerRepository, contactRepository, identifierRepository);
    TenantContext.clear();
  }

  @Test
  void testAnonymizationProcess_WithDeletedCustomers_Succeeds() {
    UUID tenantId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Customer customer = new Customer();
    customer.setId(customerId);
    customer.setTenantId(tenantId);
    customer.setStatus(CustomerStatus.DELETED);
    customer.setFirstName("Priya");
    customer.setLastName("Sharma");
    customer.setDisplayName("Priya S.");
    customer.setAttributes("{\"source\":\"CRM\"}");

    CustomerContact contact = new CustomerContact();
    contact.setId(UUID.randomUUID());
    contact.setCustomerId(customerId);
    contact.setTenantId(tenantId);
    contact.setType(ContactType.PHONE);
    contact.setValue("+919999999999");
    contact.setValueHash("original_hash");

    CustomerIdentifier identifier = new CustomerIdentifier();
    identifier.setId(UUID.randomUUID());
    identifier.setCustomerId(customerId);
    identifier.setTenantId(tenantId);

    when(customerRepository.findByStatus(CustomerStatus.DELETED))
        .thenReturn(Collections.singletonList(customer));
    when(contactRepository.findByCustomerId(customerId))
        .thenReturn(Collections.singletonList(contact));
    when(identifierRepository.findByCustomerId(customerId))
        .thenReturn(Collections.singletonList(identifier));

    task.runAnonymization();

    // Check customer anonymized fields
    assertEquals("ANONYMIZED", customer.getFirstName());
    assertEquals("ANONYMIZED", customer.getLastName());
    assertEquals("Anonymized Customer", customer.getDisplayName());
    assertNull(customer.getExternalId());
    assertTrue(customer.getAttributes().contains("\"anonymized\":true"));

    // Check contacts anonymized fields
    assertTrue(contact.getValue().startsWith("ANONYMIZED_"));
    assertNotEquals("original_hash", contact.getValueHash());
    assertFalse(contact.isVerified());

    // Verify identifier deleted
    verify(identifierRepository).deleteAllInBatch(anyList());
    verify(customerRepository).save(customer);
    verify(contactRepository).save(contact);

    assertNull(TenantContext.getCurrentTenantId());
  }

  @Test
  void testAnonymizationProcess_WithAlreadyAnonymizedCustomer_Skips() {
    Customer customer = new Customer();
    customer.setId(UUID.randomUUID());
    customer.setDisplayName("Anonymized Customer");
    customer.setStatus(CustomerStatus.DELETED);

    when(customerRepository.findByStatus(CustomerStatus.DELETED))
        .thenReturn(Collections.singletonList(customer));

    task.runAnonymization();

    verifyNoInteractions(contactRepository);
    verifyNoInteractions(identifierRepository);
    verify(customerRepository, never()).save(any());
  }
}
