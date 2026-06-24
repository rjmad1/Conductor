package com.conductor.customer.service;

import com.conductor.customer.domain.Customer;
import com.conductor.customer.domain.CustomerSegment;
import com.conductor.customer.domain.Segment;
import com.conductor.customer.exception.CustomerNotFoundException;
import com.conductor.customer.repository.CustomerRepository;
import com.conductor.customer.repository.CustomerSegmentRepository;
import com.conductor.customer.repository.CustomerTagRepository;
import com.conductor.customer.repository.SegmentRepository;
import com.conductor.shared.customer.CustomerEvents;
import com.conductor.shared.customer.CustomerStatus;
import com.conductor.shared.customer.SegmentType;
import com.conductor.shared.customer.TimelineEventType;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * SegmentService — manages customer segments and segment membership.
 *
 * Segment types:
 *   - STATIC: manual membership managed via API
 *   - DYNAMIC / RULE_BASED: membership computed from customer attributes
 *     (uses ConditionEvaluator from :shared:rules — scheduled or event-triggered recompute)
 *   - TAG_BASED: membership derived from tag assignments
 *   - BEHAVIOR_BASED: membership based on timeline event history (future phase)
 *
 * Dynamic segment recompute is intentionally deferred (not real-time) to avoid
 * write amplification on every customer update. Recompute is triggered by:
 *   1. Scheduled job (future: @Scheduled annotation)
 *   2. Explicit API call: POST /api/v1/segments/{id}/recompute
 *
 * The customerCount field on Segment is a cached value refreshed on recompute.
 */
@Service
public class SegmentService {

    private static final Logger log = LoggerFactory.getLogger(SegmentService.class);

    private final SegmentRepository         segmentRepository;
    private final CustomerSegmentRepository customerSegmentRepository;
    private final CustomerRepository        customerRepository;
    private final CustomerTagRepository     customerTagRepository;
    private final CustomerService           customerService;
    private final CustomerTimelineService   timelineService;
    private final NatsEventPublisher        eventPublisher;
    private final AuditLogger               auditLogger;

    public SegmentService(
            SegmentRepository segmentRepository,
            CustomerSegmentRepository customerSegmentRepository,
            CustomerRepository customerRepository,
            CustomerTagRepository customerTagRepository,
            CustomerService customerService,
            CustomerTimelineService timelineService,
            NatsEventPublisher eventPublisher,
            AuditLogger auditLogger) {
        this.segmentRepository         = segmentRepository;
        this.customerSegmentRepository = customerSegmentRepository;
        this.customerRepository        = customerRepository;
        this.customerTagRepository     = customerTagRepository;
        this.customerService           = customerService;
        this.timelineService           = timelineService;
        this.eventPublisher            = eventPublisher;
        this.auditLogger               = auditLogger;
    }

    // ── Segment CRUD ──────────────────────────────────────────────────────────

    @Transactional
    public Segment createSegment(String name, SegmentType type, String rulesJson, String description) {
        String slug = slugify(name);
        if (segmentRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Segment slug already exists: " + slug);
        }
        Segment segment = new Segment();
        segment.setName(name);
        segment.setSlug(slug);
        segment.setType(type);
        segment.setRules(rulesJson);
        segment.setDescription(description);
        Segment saved = segmentRepository.save(segment);
        auditLogger.logEvent("CREATE_SEGMENT", "SEGMENT:" + saved.getId(), "SUCCESS", "Segment created: " + slug);
        return saved;
    }

    @Transactional
    public Segment updateSegment(UUID segmentId, String name, String rulesJson, String description) {
        Segment segment = requireSegment(segmentId);
        segment.setName(name);
        segment.setRules(rulesJson);
        segment.setDescription(description);
        segment.setUpdatedAt(Instant.now());
        return segmentRepository.save(segment);
    }

    @Transactional
    public void deleteSegment(UUID segmentId) {
        Segment segment = requireSegment(segmentId);
        customerSegmentRepository.findBySegmentId(segmentId)
                .forEach(customerSegmentRepository::delete);
        segmentRepository.delete(segment);
        auditLogger.logEvent("DELETE_SEGMENT", "SEGMENT:" + segmentId, "SUCCESS", "Segment deleted");
    }

    public List<Segment> listSegments() {
        return segmentRepository.findAll();
    }

    // ── Segment Membership ────────────────────────────────────────────────────

    @Transactional
    public void addCustomerToSegment(UUID customerId, UUID segmentId, String addedBy) {
        customerService.requireCustomer(customerId);
        requireSegment(segmentId);

        if (customerSegmentRepository.existsByCustomerIdAndSegmentId(customerId, segmentId)) {
            return; // Idempotent
        }

        CustomerSegment cs = new CustomerSegment();
        cs.setCustomerId(customerId);
        cs.setSegmentId(segmentId);
        cs.setAddedBy(addedBy);
        cs.setSource("MANUAL");
        customerSegmentRepository.save(cs);

        refreshCount(segmentId);

        timelineService.record(customerId, TimelineEventType.SEGMENT_ASSIGNED,
                "customer-service", "Added to segment: " + segmentId,
                String.format("{\"segmentId\":\"%s\"}", segmentId));

        eventPublisher.publishEvent(CustomerEvents.DOMAIN, CustomerEvents.ENTITY_SEGMENT,
                CustomerEvents.ACTION_ASSIGNED,
                String.format("{\"customerId\":\"%s\",\"segmentId\":\"%s\"}", customerId, segmentId));

        auditLogger.logEvent("ADD_TO_SEGMENT", "CUSTOMER:" + customerId + ":SEGMENT:" + segmentId,
                "SUCCESS", "Customer added to segment");
    }

    @Transactional
    public void removeCustomerFromSegment(UUID customerId, UUID segmentId) {
        customerService.requireCustomer(customerId);

        if (!customerSegmentRepository.existsByCustomerIdAndSegmentId(customerId, segmentId)) {
            throw new CustomerNotFoundException("Customer " + customerId + " not in segment " + segmentId);
        }

        customerSegmentRepository.deleteByCustomerIdAndSegmentId(customerId, segmentId);
        refreshCount(segmentId);

        timelineService.record(customerId, TimelineEventType.SEGMENT_REMOVED,
                "customer-service", "Removed from segment: " + segmentId,
                String.format("{\"segmentId\":\"%s\"}", segmentId));

        eventPublisher.publishEvent(CustomerEvents.DOMAIN, CustomerEvents.ENTITY_SEGMENT,
                CustomerEvents.ACTION_REMOVED,
                String.format("{\"customerId\":\"%s\",\"segmentId\":\"%s\"}", customerId, segmentId));

        auditLogger.logEvent("REMOVE_FROM_SEGMENT", "CUSTOMER:" + customerId + ":SEGMENT:" + segmentId,
                "SUCCESS", "Customer removed from segment");
    }

    public List<UUID> getSegmentCustomerIds(UUID segmentId) {
        requireSegment(segmentId);
        return customerSegmentRepository.findCustomerIdsBySegmentId(segmentId);
    }

    public List<CustomerSegment> getCustomerSegments(UUID customerId) {
        customerService.requireCustomer(customerId);
        return customerSegmentRepository.findByCustomerId(customerId);
    }

    /**
     * Recompute TAG_BASED segment membership.
     * For the given tag-based segment, the rules JSON contains {"tagId":"<uuid>"}.
     * All customers with that tag are added; customers without are removed.
     */
    @Transactional
    public void recomputeTagBasedSegment(UUID segmentId, UUID tagId) {
        Segment segment = requireSegment(segmentId);
        if (segment.getType() != SegmentType.TAG_BASED) {
            throw new IllegalArgumentException("Segment " + segmentId + " is not TAG_BASED");
        }

        List<UUID> taggedCustomers = customerTagRepository.findCustomerIdsByTagId(tagId);
        List<UUID> currentMembers  = customerSegmentRepository.findCustomerIdsBySegmentId(segmentId);

        // Add customers who have the tag but aren't in segment
        taggedCustomers.stream()
                .filter(id -> !currentMembers.contains(id))
                .forEach(id -> {
                    CustomerSegment cs = new CustomerSegment();
                    cs.setCustomerId(id);
                    cs.setSegmentId(segmentId);
                    cs.setSource("RULE_ENGINE");
                    customerSegmentRepository.save(cs);
                });

        // Remove customers who no longer have the tag
        currentMembers.stream()
                .filter(id -> !taggedCustomers.contains(id))
                .forEach(id -> customerSegmentRepository.deleteByCustomerIdAndSegmentId(id, segmentId));

        refreshCount(segmentId);
        log.info("Recomputed TAG_BASED segment {} — {} members", segmentId, taggedCustomers.size());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Segment requireSegment(UUID segmentId) {
        return segmentRepository.findById(segmentId)
                .orElseThrow(() -> new CustomerNotFoundException("Segment not found: " + segmentId));
    }

    private void refreshCount(UUID segmentId) {
        long count = customerSegmentRepository.countBySegmentId(segmentId);
        segmentRepository.findById(segmentId).ifPresent(s -> {
            s.setCustomerCount(count);
            s.setLastComputedAt(Instant.now());
            segmentRepository.save(s);
        });
    }

    private String slugify(String name) {
        return name.trim().toLowerCase()
                   .replaceAll("[^a-z0-9]+", "-")
                   .replaceAll("^-|-$", "");
    }
}
