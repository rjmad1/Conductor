package com.conductor.customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.conductor.customer.domain.Customer;
import com.conductor.customer.domain.CustomerSegment;
import com.conductor.customer.domain.Segment;
import com.conductor.customer.repository.CustomerSegmentRepository;
import com.conductor.customer.repository.CustomerTagRepository;
import com.conductor.customer.repository.SegmentRepository;
import com.conductor.customer.service.CustomerService;
import com.conductor.customer.service.CustomerTimelineService;
import com.conductor.customer.service.SegmentService;
import com.conductor.shared.customer.SegmentType;
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
class SegmentServiceTest {

  @Mock private SegmentRepository segmentRepository;

  @Mock private CustomerSegmentRepository customerSegmentRepository;

  @Mock private CustomerTagRepository customerTagRepository;

  @Mock private CustomerService customerService;

  @Mock private CustomerTimelineService timelineService;

  @Mock private NatsEventPublisher eventPublisher;

  @Mock private AuditLogger auditLogger;

  private SegmentService segmentService;

  @BeforeEach
  void setUp() {
    segmentService =
        new SegmentService(
            segmentRepository,
            customerSegmentRepository,
            customerTagRepository,
            customerService,
            timelineService,
            eventPublisher,
            auditLogger);
  }

  @Test
  void testCreateSegment() {
    when(segmentRepository.existsBySlug("vip-customers")).thenReturn(false);
    when(segmentRepository.save(any(Segment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Segment segment =
        segmentService.createSegment("VIP Customers", SegmentType.STATIC, null, "desc");

    assertNotNull(segment);
    assertEquals("VIP Customers", segment.getName());
    assertEquals("vip-customers", segment.getSlug());
    assertEquals(SegmentType.STATIC, segment.getType());
    assertEquals("desc", segment.getDescription());
  }

  @Test
  void testCreateSegment_SlugExists_ThrowsException() {
    when(segmentRepository.existsBySlug("vip-customers")).thenReturn(true);
    assertThrows(
        IllegalArgumentException.class,
        () -> segmentService.createSegment("VIP Customers", SegmentType.STATIC, null, "desc"));
  }

  @Test
  void testAddCustomerToSegment() {
    UUID customerId = UUID.randomUUID();
    UUID segmentId = UUID.randomUUID();

    Customer customer = new Customer();
    customer.setId(customerId);

    Segment segment = new Segment();
    segment.setId(segmentId);

    when(customerService.requireCustomer(customerId)).thenReturn(customer);
    when(segmentRepository.findById(segmentId)).thenReturn(Optional.of(segment));
    when(customerSegmentRepository.existsByCustomerIdAndSegmentId(customerId, segmentId))
        .thenReturn(false);

    segmentService.addCustomerToSegment(customerId, segmentId, "admin");

    verify(customerSegmentRepository).save(any(CustomerSegment.class));
    verify(timelineService).record(eq(customerId), any(), anyString(), anyString(), anyString());
    verify(eventPublisher).publishEvent(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testRemoveCustomerFromSegment() {
    UUID customerId = UUID.randomUUID();
    UUID segmentId = UUID.randomUUID();

    Customer customer = new Customer();
    customer.setId(customerId);

    Segment segment = new Segment();
    segment.setId(segmentId);

    when(customerService.requireCustomer(customerId)).thenReturn(customer);
    when(customerSegmentRepository.existsByCustomerIdAndSegmentId(customerId, segmentId))
        .thenReturn(true);
    when(segmentRepository.findById(segmentId)).thenReturn(Optional.of(segment));

    segmentService.removeCustomerFromSegment(customerId, segmentId);

    verify(customerSegmentRepository).deleteByCustomerIdAndSegmentId(customerId, segmentId);
    verify(timelineService).record(eq(customerId), any(), anyString(), anyString(), anyString());
    verify(eventPublisher).publishEvent(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testRecomputeTagBasedSegment() {
    UUID segmentId = UUID.randomUUID();
    UUID tagId = UUID.randomUUID();
    UUID customer1 = UUID.randomUUID();
    UUID customer2 = UUID.randomUUID();

    Segment segment = new Segment();
    segment.setId(segmentId);
    segment.setType(SegmentType.TAG_BASED);

    when(segmentRepository.findById(segmentId)).thenReturn(Optional.of(segment));
    when(customerTagRepository.findCustomerIdsByTagId(tagId))
        .thenReturn(List.of(customer1, customer2));
    when(customerSegmentRepository.findCustomerIdsBySegmentId(segmentId))
        .thenReturn(List.of(customer1)); // customer1 already in segment, customer2 is not.

    segmentService.recomputeTagBasedSegment(segmentId, tagId);

    verify(customerSegmentRepository).save(any(CustomerSegment.class)); // customer2 should be added
    verify(customerSegmentRepository, never())
        .deleteByCustomerIdAndSegmentId(any(UUID.class), any(UUID.class));
  }
}
