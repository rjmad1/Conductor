package com.conductor.customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.conductor.customer.domain.ConsentRecord;
import com.conductor.customer.domain.Customer;
import com.conductor.customer.exception.ConsentException;
import com.conductor.customer.repository.ConsentRecordRepository;
import com.conductor.customer.service.ConsentService;
import com.conductor.customer.service.CustomerService;
import com.conductor.customer.service.CustomerTimelineService;
import com.conductor.shared.customer.ConsentAction;
import com.conductor.shared.customer.ConsentType;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ConsentServiceTest {

  @Mock private ConsentRecordRepository consentRepository;

  @Mock private CustomerTimelineService timelineService;

  @Mock private CustomerService customerService;

  @Mock private NatsEventPublisher eventPublisher;

  @Mock private AuditLogger auditLogger;

  private ConsentService consentService;

  @BeforeEach
  void setUp() {
    consentService =
        new ConsentService(
            consentRepository, timelineService, customerService, eventPublisher, auditLogger);
  }

  @Test
  void testGrantConsent() {
    UUID customerId = UUID.randomUUID();
    Customer customer = new Customer();
    customer.setId(customerId);

    when(customerService.requireCustomer(customerId)).thenReturn(customer);
    when(consentRepository.save(any(ConsentRecord.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ConsentRecord record =
        consentService.grantConsent(
            customerId,
            ConsentType.MARKETING,
            "EMAIL",
            "EXPLICIT",
            "v1",
            "127.0.0.1",
            "agent",
            null);

    assertNotNull(record);
    assertEquals(customerId, record.getCustomerId());
    assertEquals(ConsentType.MARKETING, record.getConsentType());
    assertEquals(ConsentAction.GRANTED, record.getAction());
    assertEquals("EMAIL", record.getChannel());
    assertEquals("EXPLICIT", record.getLegalBasis());
    assertEquals("v1", record.getConsentVersion());
    assertEquals("127.0.0.1", record.getIpAddress());
    assertEquals("agent", record.getUserAgent());

    verify(timelineService).record(eq(customerId), any(), anyString(), anyString(), anyString());
    verify(eventPublisher).publishEvent(anyString(), anyString(), anyString(), anyString());
    verify(auditLogger).logEvent(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testRevokeConsent_WithNoActiveGrant_ThrowsConsentException() {
    UUID customerId = UUID.randomUUID();
    Customer customer = new Customer();
    customer.setId(customerId);

    when(customerService.requireCustomer(customerId)).thenReturn(customer);
    when(consentRepository.findLatestByCustomerIdAndConsentType(customerId, ConsentType.MARKETING))
        .thenReturn(Optional.empty());

    assertThrows(
        ConsentException.class,
        () ->
            consentService.revokeConsent(
                customerId, ConsentType.MARKETING, "EMAIL", "v1", "127.0.0.1", "agent", null));
  }

  @Test
  void testRevokeConsent_WithActiveGrant_Succeeds() {
    UUID customerId = UUID.randomUUID();
    Customer customer = new Customer();
    customer.setId(customerId);

    ConsentRecord activeGrant = new ConsentRecord();
    activeGrant.setCustomerId(customerId);
    activeGrant.setConsentType(ConsentType.MARKETING);
    activeGrant.setAction(ConsentAction.GRANTED);

    when(customerService.requireCustomer(customerId)).thenReturn(customer);
    when(consentRepository.findLatestByCustomerIdAndConsentType(customerId, ConsentType.MARKETING))
        .thenReturn(Optional.of(activeGrant));
    when(consentRepository.save(any(ConsentRecord.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ConsentRecord record =
        consentService.revokeConsent(
            customerId, ConsentType.MARKETING, "EMAIL", "v1", "127.0.0.1", "agent", null);

    assertNotNull(record);
    assertEquals(customerId, record.getCustomerId());
    assertEquals(ConsentType.MARKETING, record.getConsentType());
    assertEquals(ConsentAction.REVOKED, record.getAction());
    assertEquals("EMAIL", record.getChannel());

    verify(timelineService).record(eq(customerId), any(), anyString(), anyString(), anyString());
    verify(eventPublisher).publishEvent(anyString(), anyString(), anyString(), anyString());
    verify(auditLogger).logEvent(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testIsConsentActive() {
    UUID customerId = UUID.randomUUID();

    // 1. No record exists
    when(consentRepository.findLatestByCustomerIdAndConsentType(customerId, ConsentType.MARKETING))
        .thenReturn(Optional.empty());
    assertFalse(consentService.isConsentActive(customerId, ConsentType.MARKETING));

    // 2. Revoked record is latest
    ConsentRecord revoked = new ConsentRecord();
    revoked.setAction(ConsentAction.REVOKED);
    when(consentRepository.findLatestByCustomerIdAndConsentType(customerId, ConsentType.MARKETING))
        .thenReturn(Optional.of(revoked));
    assertFalse(consentService.isConsentActive(customerId, ConsentType.MARKETING));

    // 3. Granted record is latest
    ConsentRecord granted = new ConsentRecord();
    granted.setAction(ConsentAction.GRANTED);
    when(consentRepository.findLatestByCustomerIdAndConsentType(customerId, ConsentType.MARKETING))
        .thenReturn(Optional.of(granted));
    assertTrue(consentService.isConsentActive(customerId, ConsentType.MARKETING));
  }
}
