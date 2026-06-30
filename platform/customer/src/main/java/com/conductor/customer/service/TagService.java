package com.conductor.customer.service;

import com.conductor.customer.domain.CustomerTag;
import com.conductor.customer.domain.Tag;
import com.conductor.customer.exception.CustomerNotFoundException;
import com.conductor.customer.repository.CustomerTagRepository;
import com.conductor.customer.repository.TagRepository;
import com.conductor.shared.customer.CustomerEvents;
import com.conductor.shared.customer.TimelineEventType;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class TagService {

  private final TagRepository tagRepository;
  private final CustomerTagRepository customerTagRepository;
  private final CustomerService customerService;
  private final CustomerTimelineService timelineService;
  private final NatsEventPublisher eventPublisher;
  private final AuditLogger auditLogger;

  public TagService(
      TagRepository tagRepository,
      CustomerTagRepository customerTagRepository,
      CustomerService customerService,
      CustomerTimelineService timelineService,
      NatsEventPublisher eventPublisher,
      AuditLogger auditLogger) {
    this.tagRepository = tagRepository;
    this.customerTagRepository = customerTagRepository;
    this.customerService = customerService;
    this.timelineService = timelineService;
    this.eventPublisher = eventPublisher;
    this.auditLogger = auditLogger;
  }

  // ── Tag CRUD ──────────────────────────────────────────────────────────────

  @Transactional
  public Tag createTag(String name, String category, String color, String description) {
    String slug = slugify(name);
    if (tagRepository.existsBySlug(slug)) {
      throw new IllegalArgumentException("Tag slug already exists: " + slug);
    }
    Tag tag = new Tag();
    tag.setName(name);
    tag.setSlug(slug);
    tag.setCategory(category);
    tag.setColor(color);
    tag.setDescription(description);
    Tag saved = tagRepository.save(tag);
    auditLogger.logEvent("CREATE_TAG", "TAG:" + saved.getId(), "SUCCESS", "Tag created: " + slug);
    return saved;
  }

  @Transactional
  public Tag updateTag(UUID tagId, String name, String category, String color, String description) {
    Tag tag = requireTag(tagId);
    tag.setName(name);
    tag.setCategory(category);
    tag.setColor(color);
    tag.setDescription(description);
    tag.setUpdatedAt(Instant.now());
    return tagRepository.save(tag);
  }

  @Transactional
  public void deleteTag(UUID tagId) {
    Tag tag = requireTag(tagId);
    // Remove all customer assignments first
    List<CustomerTag> assignments = customerTagRepository.findByTagId(tagId);
    customerTagRepository.deleteAll(assignments);
    tagRepository.delete(tag);
    auditLogger.logEvent("DELETE_TAG", "TAG:" + tagId, "SUCCESS", "Tag deleted");
  }

  public List<Tag> listTags() {
    return tagRepository.findAll();
  }

  public List<Tag> listTagsByCategory(String category) {
    return tagRepository.findByCategory(category);
  }

  // ── Customer Tag Assignment ───────────────────────────────────────────────

  @Transactional
  public void assignTag(UUID customerId, UUID tagId, String assignedBy) {
    customerService.requireCustomer(customerId);
    requireTag(tagId);

    if (customerTagRepository.existsByCustomerIdAndTagId(customerId, tagId)) {
      return; // Idempotent — already assigned
    }

    CustomerTag ct = new CustomerTag();
    ct.setCustomerId(customerId);
    ct.setTagId(tagId);
    ct.setAssignedBy(assignedBy);
    customerTagRepository.save(ct);

    timelineService.record(
        customerId,
        TimelineEventType.TAG_ASSIGNED,
        "customer-service",
        "Tag assigned: " + tagId,
        String.format("{\"tagId\":\"%s\"}", tagId));

    eventPublisher.publishEvent(
        CustomerEvents.DOMAIN,
        CustomerEvents.ENTITY_TAG,
        CustomerEvents.ACTION_ASSIGNED,
        String.format("{\"customerId\":\"%s\",\"tagId\":\"%s\"}", customerId, tagId));

    auditLogger.logEvent(
        "ASSIGN_TAG", "CUSTOMER:" + customerId + ":TAG:" + tagId, "SUCCESS", "Tag assigned");
  }

  @Transactional
  public void removeTag(UUID customerId, UUID tagId) {
    customerService.requireCustomer(customerId);

    if (!customerTagRepository.existsByCustomerIdAndTagId(customerId, tagId)) {
      throw new CustomerNotFoundException("Tag assignment not found for customer " + customerId);
    }

    customerTagRepository.deleteByCustomerIdAndTagId(customerId, tagId);

    timelineService.record(
        customerId,
        TimelineEventType.TAG_REMOVED,
        "customer-service",
        "Tag removed: " + tagId,
        String.format("{\"tagId\":\"%s\"}", tagId));

    eventPublisher.publishEvent(
        CustomerEvents.DOMAIN,
        CustomerEvents.ENTITY_TAG,
        CustomerEvents.ACTION_REMOVED,
        String.format("{\"customerId\":\"%s\",\"tagId\":\"%s\"}", customerId, tagId));

    auditLogger.logEvent(
        "REMOVE_TAG", "CUSTOMER:" + customerId + ":TAG:" + tagId, "SUCCESS", "Tag removed");
  }

  public List<CustomerTag> getCustomerTags(UUID customerId) {
    customerService.requireCustomer(customerId);
    return customerTagRepository.findByCustomerId(customerId);
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private Tag requireTag(UUID tagId) {
    return tagRepository
        .findById(tagId)
        .orElseThrow(() -> new CustomerNotFoundException("Tag not found: " + tagId));
  }

  private String slugify(String name) {
    return name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
  }
}
