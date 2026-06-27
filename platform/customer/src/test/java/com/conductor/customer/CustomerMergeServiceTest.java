package com.conductor.customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.conductor.customer.domain.*;
import com.conductor.customer.repository.*;
import com.conductor.customer.service.CustomerMergeService;
import com.conductor.customer.service.CustomerTimelineService;
import com.conductor.shared.customer.ContactType;
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

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class CustomerMergeServiceTest {

  @Mock private CustomerRepository customerRepository;

  @Mock private CustomerContactRepository contactRepository;

  @Mock private CustomerTagRepository tagRepository;

  @Mock private CustomerSegmentRepository segmentRepository;

  @Mock private CustomerAttributeRepository attributeRepository;

  @Mock private CustomerTimelineService timelineService;

  @Mock private NatsEventPublisher eventPublisher;

  @Mock private AuditLogger auditLogger;

  private CustomerMergeService customerMergeService;

  @BeforeEach
  void setUp() {
    customerMergeService =
        new CustomerMergeService(
            customerRepository,
            contactRepository,
            tagRepository,
            segmentRepository,
            attributeRepository,
            timelineService,
            eventPublisher,
            auditLogger);
  }

  @Test
  void testMerge_Succeeds() {
    UUID sourceId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();

    Customer source = new Customer();
    source.setId(sourceId);
    source.setStatus(CustomerStatus.ACTIVE);

    Customer target = new Customer();
    target.setId(targetId);
    target.setStatus(CustomerStatus.ACTIVE);

    when(customerRepository.findById(sourceId)).thenReturn(Optional.of(source));
    when(customerRepository.findById(targetId)).thenReturn(Optional.of(target));

    // 1. Contacts mock
    CustomerContact sc1 = new CustomerContact();
    sc1.setCustomerId(sourceId);
    sc1.setType(ContactType.EMAIL);
    sc1.setValueHash("hash1");

    CustomerContact tc1 = new CustomerContact();
    tc1.setCustomerId(targetId);
    tc1.setType(ContactType.EMAIL);
    tc1.setValueHash("hash2");

    when(contactRepository.findByCustomerId(sourceId)).thenReturn(List.of(sc1));
    when(contactRepository.findByCustomerId(targetId)).thenReturn(List.of(tc1));

    // 2. Tags mock
    CustomerTag st1 = new CustomerTag();
    st1.setCustomerId(sourceId);
    st1.setTagId(UUID.randomUUID());
    when(tagRepository.findByCustomerId(sourceId)).thenReturn(List.of(st1));
    when(tagRepository.existsByCustomerIdAndTagId(targetId, st1.getTagId())).thenReturn(false);

    // 3. Segments mock
    CustomerSegment ss1 = new CustomerSegment();
    ss1.setCustomerId(sourceId);
    ss1.setSegmentId(UUID.randomUUID());
    when(segmentRepository.findByCustomerId(sourceId)).thenReturn(List.of(ss1));
    when(segmentRepository.existsByCustomerIdAndSegmentId(targetId, ss1.getSegmentId()))
        .thenReturn(false);

    // 4. Attributes mock
    CustomerAttribute sa1 = new CustomerAttribute();
    sa1.setCustomerId(sourceId);
    sa1.setKey("attrKey");
    sa1.setValue("attrValue");
    when(attributeRepository.findByCustomerId(sourceId)).thenReturn(List.of(sa1));
    when(attributeRepository.findByCustomerIdAndKey(targetId, "attrKey"))
        .thenReturn(Optional.empty());

    // Perform merge
    Customer result = customerMergeService.merge(sourceId, targetId);

    assertNotNull(result);
    assertEquals(CustomerStatus.MERGED, source.getStatus());
    assertEquals(targetId, source.getMergedIntoId());

    verify(contactRepository).save(sc1);
    assertEquals(targetId, sc1.getCustomerId());

    verify(tagRepository).save(st1);
    assertEquals(targetId, st1.getCustomerId());

    verify(segmentRepository).save(ss1);
    assertEquals(targetId, ss1.getCustomerId());

    verify(attributeRepository).save(sa1);
    assertEquals(targetId, sa1.getCustomerId());

    verify(timelineService).record(eq(targetId), any(), anyString(), anyString(), anyString());
    verify(timelineService).record(eq(sourceId), any(), anyString(), anyString(), anyString());
    verify(eventPublisher).publishEvent(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testMerge_SameCustomer_ThrowsException() {
    UUID customerId = UUID.randomUUID();
    assertThrows(
        IllegalArgumentException.class, () -> customerMergeService.merge(customerId, customerId));
  }
}
