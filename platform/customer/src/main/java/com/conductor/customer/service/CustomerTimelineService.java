package com.conductor.customer.service;

import com.conductor.customer.domain.CustomerTimeline;
import com.conductor.customer.repository.CustomerTimelineRepository;
import com.conductor.shared.customer.TimelineEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * CustomerTimelineService — append-only customer activity log.
 * This service is called by all other customer domain services to record events.
 * External services (Messaging, Workflow) should publish events that trigger
 * timeline entries via the event consumer.
 */
@Service
public class CustomerTimelineService {

    private final CustomerTimelineRepository timelineRepository;

    public CustomerTimelineService(CustomerTimelineRepository timelineRepository) {
        this.timelineRepository = timelineRepository;
    }

    @Transactional
    public CustomerTimeline record(UUID customerId, TimelineEventType eventType,
                                   String eventSource, String summary, String metadataJson) {
        CustomerTimeline entry = new CustomerTimeline();
        entry.setCustomerId(customerId);
        entry.setEventType(eventType);
        entry.setEventSource(eventSource);
        entry.setSummary(summary);
        entry.setMetadata(metadataJson);
        entry.setOccurredAt(Instant.now());
        return timelineRepository.save(entry);
    }

    public Page<CustomerTimeline> getTimeline(UUID customerId, Pageable pageable) {
        return timelineRepository.findByCustomerIdOrderByOccurredAtDesc(customerId, pageable);
    }

    public List<CustomerTimeline> getTimelineByEventType(UUID customerId, TimelineEventType eventType) {
        return timelineRepository.findByCustomerIdAndEventTypeOrderByOccurredAtDesc(customerId, eventType);
    }

    public List<CustomerTimeline> getTimelineInRange(UUID customerId, Instant from, Instant to) {
        return timelineRepository.findByCustomerIdAndOccurredAtBetweenOrderByOccurredAtDesc(customerId, from, to);
    }
}
