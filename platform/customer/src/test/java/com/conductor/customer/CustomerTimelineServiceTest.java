package com.conductor.customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.conductor.customer.domain.CustomerTimeline;
import com.conductor.customer.repository.CustomerTimelineRepository;
import com.conductor.customer.service.CustomerTimelineService;
import com.conductor.shared.customer.TimelineEventType;
import java.time.Instant;
import java.util.List;
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
class CustomerTimelineServiceTest {

  @Mock private CustomerTimelineRepository timelineRepository;

  private CustomerTimelineService timelineService;

  @BeforeEach
  void setUp() {
    timelineService = new CustomerTimelineService(timelineRepository);
  }

  @Test
  void testRecord() {
    UUID customerId = UUID.randomUUID();
    when(timelineRepository.save(any(CustomerTimeline.class)))
        .thenAnswer(
            invocation -> {
              CustomerTimeline t = invocation.getArgument(0);
              t.setId(UUID.randomUUID());
              return t;
            });

    CustomerTimeline entry =
        timelineService.record(
            customerId, TimelineEventType.CUSTOMER_CREATED, "service", "Created customer", null);

    assertNotNull(entry);
    assertNotNull(entry.getId());
    assertEquals(customerId, entry.getCustomerId());
    assertEquals(TimelineEventType.CUSTOMER_CREATED, entry.getEventType());
    assertEquals("service", entry.getEventSource());
    assertEquals("Created customer", entry.getSummary());
    assertNull(entry.getMetadata());
    assertNotNull(entry.getOccurredAt());
  }

  @Test
  void testGetTimeline() {
    UUID customerId = UUID.randomUUID();
    PageRequest pageRequest = PageRequest.of(0, 10);
    CustomerTimeline t1 = new CustomerTimeline();
    CustomerTimeline t2 = new CustomerTimeline();

    when(timelineRepository.findByCustomerIdOrderByOccurredAtDesc(customerId, pageRequest))
        .thenReturn(new PageImpl<>(List.of(t1, t2)));

    Page<CustomerTimeline> page = timelineService.getTimeline(customerId, pageRequest);
    assertEquals(2, page.getTotalElements());
  }

  @Test
  void testGetTimelineByEventType() {
    UUID customerId = UUID.randomUUID();
    CustomerTimeline t1 = new CustomerTimeline();
    t1.setEventType(TimelineEventType.CONSENT_GRANTED);

    when(timelineRepository.findByCustomerIdAndEventTypeOrderByOccurredAtDesc(
            customerId, TimelineEventType.CONSENT_GRANTED))
        .thenReturn(List.of(t1));

    List<CustomerTimeline> list =
        timelineService.getTimelineByEventType(customerId, TimelineEventType.CONSENT_GRANTED);
    assertEquals(1, list.size());
    assertEquals(TimelineEventType.CONSENT_GRANTED, list.get(0).getEventType());
  }

  @Test
  void testGetTimelineInRange() {
    UUID customerId = UUID.randomUUID();
    Instant from = Instant.now().minusSeconds(3600);
    Instant to = Instant.now();
    CustomerTimeline t1 = new CustomerTimeline();

    when(timelineRepository.findByCustomerIdAndOccurredAtBetweenOrderByOccurredAtDesc(
            customerId, from, to))
        .thenReturn(List.of(t1));

    List<CustomerTimeline> list = timelineService.getTimelineInRange(customerId, from, to);
    assertEquals(1, list.size());
  }
}
