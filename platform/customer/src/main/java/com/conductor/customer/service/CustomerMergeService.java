package com.conductor.customer.service;

import com.conductor.customer.domain.*;
import com.conductor.customer.repository.*;
import com.conductor.shared.customer.CustomerEvents;
import com.conductor.shared.customer.CustomerStatus;
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
 * CustomerMergeService — merge two customer records into one.
 *
 * Merge is a significant and auditable operation. It:
 *   1. Validates both customers exist and are in mergeable states
 *   2. Migrates all contacts, tags, segments, consents, attributes, preferences
 *      from source → target (duplicates skipped)
 *   3. Records a timeline entry on both source and target customers
 *   4. Marks source customer as MERGED with mergedIntoId pointing to target
 *   5. Publishes customer.profile.merged event
 *   6. Logs audit record
 *
 * Recovery: the merge can be audited via the timeline. The source customer record
 * is soft-deleted (MERGED status), not physically removed.
 *
 * Cross-tenant merges are impossible — TenantAwareEntity Hibernate filter scopes
 * all repository queries to the active tenant.
 */
@Service
public class CustomerMergeService {

    private static final Logger log = LoggerFactory.getLogger(CustomerMergeService.class);

    private final CustomerRepository         customerRepository;
    private final CustomerContactRepository  contactRepository;
    private final CustomerTagRepository      tagRepository;
    private final CustomerSegmentRepository  segmentRepository;
    private final ConsentRecordRepository    consentRepository;
    private final CustomerAttributeRepository attributeRepository;
    private final CustomerTimelineService    timelineService;
    private final NatsEventPublisher         eventPublisher;
    private final AuditLogger                auditLogger;

    public CustomerMergeService(
            CustomerRepository customerRepository,
            CustomerContactRepository contactRepository,
            CustomerTagRepository tagRepository,
            CustomerSegmentRepository segmentRepository,
            ConsentRecordRepository consentRepository,
            CustomerAttributeRepository attributeRepository,
            CustomerTimelineService timelineService,
            NatsEventPublisher eventPublisher,
            AuditLogger auditLogger) {
        this.customerRepository  = customerRepository;
        this.contactRepository   = contactRepository;
        this.tagRepository       = tagRepository;
        this.segmentRepository   = segmentRepository;
        this.consentRepository   = consentRepository;
        this.attributeRepository = attributeRepository;
        this.timelineService     = timelineService;
        this.eventPublisher      = eventPublisher;
        this.auditLogger         = auditLogger;
    }

    @Transactional
    public Customer merge(UUID sourceId, UUID targetId) {
        if (sourceId.equals(targetId)) {
            throw new IllegalArgumentException("Cannot merge a customer into itself");
        }

        Customer source = requireMergeable(sourceId);
        Customer target = requireMergeable(targetId);

        log.info("Merging customer {} into {}", sourceId, targetId);

        // 1. Migrate contacts (skip duplicates by value_hash)
        migrateContacts(sourceId, targetId);

        // 2. Migrate tags (skip if already assigned)
        migrateTags(sourceId, targetId);

        // 3. Migrate segment memberships (skip duplicates)
        migrateSegments(sourceId, targetId);

        // 4. Migrate attributes (source wins on conflict — target values preserved)
        migrateAttributes(sourceId, targetId);

        // 5. Consent records stay on source (auditable history); no migration needed.
        //    Target consent is independently managed.

        // 6. Record timeline on target
        timelineService.record(targetId, TimelineEventType.CUSTOMER_MERGED,
                "customer-service",
                "Customer merged from " + sourceId,
                String.format("{\"sourceId\":\"%s\",\"targetId\":\"%s\"}", sourceId, targetId));

        // 7. Record timeline on source
        timelineService.record(sourceId, TimelineEventType.CUSTOMER_MERGED,
                "customer-service",
                "Customer merged into " + targetId,
                String.format("{\"sourceId\":\"%s\",\"targetId\":\"%s\"}", sourceId, targetId));

        // 8. Mark source as MERGED
        source.setStatus(CustomerStatus.MERGED);
        source.setMergedIntoId(targetId);
        source.setUpdatedAt(Instant.now());
        customerRepository.save(source);

        // 9. Publish event (IDs only — no PII in events)
        eventPublisher.publishEvent(CustomerEvents.DOMAIN, CustomerEvents.ENTITY_PROFILE,
                CustomerEvents.ACTION_MERGED,
                String.format("{\"sourceId\":\"%s\",\"targetId\":\"%s\",\"mergedAt\":\"%s\"}",
                        sourceId, targetId, Instant.now()));

        auditLogger.logEvent("MERGE", "CUSTOMER:" + sourceId + "->CUSTOMER:" + targetId,
                "SUCCESS", "Customer merged");

        return customerRepository.findById(targetId).orElseThrow();
    }

    private Customer requireMergeable(UUID id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new com.conductor.customer.exception.CustomerNotFoundException(id));
        if (c.getStatus() == CustomerStatus.MERGED || c.getStatus() == CustomerStatus.DELETED) {
            throw new IllegalArgumentException(
                "Customer " + id + " is in state " + c.getStatus() + " and cannot be merged");
        }
        return c;
    }

    private void migrateContacts(UUID sourceId, UUID targetId) {
        List<CustomerContact> sourceContacts = contactRepository.findByCustomerId(sourceId);
        List<CustomerContact> targetContacts = contactRepository.findByCustomerId(targetId);

        sourceContacts.forEach(sc -> {
            boolean duplicate = targetContacts.stream()
                    .anyMatch(tc -> tc.getType() == sc.getType() && tc.getValueHash().equals(sc.getValueHash()));
            if (!duplicate) {
                sc.setCustomerId(targetId);
                sc.setPrimary(false); // Don't override target primary
                sc.setUpdatedAt(Instant.now());
                contactRepository.save(sc);
            }
        });
    }

    private void migrateTags(UUID sourceId, UUID targetId) {
        List<CustomerTag> sourceTags = tagRepository.findByCustomerId(sourceId);
        sourceTags.forEach(st -> {
            if (!tagRepository.existsByCustomerIdAndTagId(targetId, st.getTagId())) {
                st.setCustomerId(targetId);
                tagRepository.save(st);
            }
        });
    }

    private void migrateSegments(UUID sourceId, UUID targetId) {
        List<CustomerSegment> sourceSegments = segmentRepository.findByCustomerId(sourceId);
        sourceSegments.forEach(ss -> {
            if (!segmentRepository.existsByCustomerIdAndSegmentId(targetId, ss.getSegmentId())) {
                ss.setCustomerId(targetId);
                segmentRepository.save(ss);
            }
        });
    }

    private void migrateAttributes(UUID sourceId, UUID targetId) {
        List<CustomerAttribute> sourceAttrs = attributeRepository.findByCustomerId(sourceId);
        sourceAttrs.forEach(sa -> {
            // Only migrate if target does not already have this key
            attributeRepository.findByCustomerIdAndKey(targetId, sa.getKey()).ifPresentOrElse(
                existing -> { /* target value preserved */ },
                () -> {
                    sa.setCustomerId(targetId);
                    attributeRepository.save(sa);
                }
            );
        });
    }
}
