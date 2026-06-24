package com.conductor.customer.repository;

import com.conductor.customer.domain.CustomerTimeline;
import com.conductor.shared.customer.TimelineEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CustomerTimelineRepository extends JpaRepository<CustomerTimeline, UUID> {

    Page<CustomerTimeline> findByCustomerIdOrderByOccurredAtDesc(UUID customerId, Pageable pageable);

    List<CustomerTimeline> findByCustomerIdAndEventTypeOrderByOccurredAtDesc(UUID customerId, TimelineEventType eventType);

    List<CustomerTimeline> findByCustomerIdAndOccurredAtBetweenOrderByOccurredAtDesc(
        UUID customerId, Instant from, Instant to);
}
