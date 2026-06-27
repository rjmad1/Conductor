package com.conductor.customer.service;

import com.conductor.customer.domain.*;
import com.conductor.customer.repository.*;
import com.conductor.shared.customer.CustomerEvents;
import com.conductor.shared.customer.CustomerStatus;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class CustomerService {

  private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

  private final CustomerRepository customerRepository;
  private final CustomerTimelineService timelineService;
  private final NatsEventPublisher eventPublisher;
  private final AuditLogger auditLogger;

  public CustomerService(
      CustomerRepository customerRepository,
      CustomerTimelineService timelineService,
      NatsEventPublisher eventPublisher,
      AuditLogger auditLogger) {
    this.customerRepository = customerRepository;
    this.timelineService = timelineService;
    this.eventPublisher = eventPublisher;
    this.auditLogger = auditLogger;
  }

  @Transactional
  public Customer createCustomer(
      String firstName,
      String lastName,
      String displayName,
      String externalId,
      String sourceSystem) {
    Customer customer = new Customer();
    customer.setFirstName(firstName);
    customer.setLastName(lastName);
    customer.setDisplayName(displayName);
    customer.setExternalId(externalId);
    customer.setSourceSystem(sourceSystem);
    customer.setStatus(CustomerStatus.ACTIVE);

    Customer saved = customerRepository.save(customer);

    timelineService.record(
        saved.getId(),
        com.conductor.shared.customer.TimelineEventType.CUSTOMER_CREATED,
        "customer-service",
        "Customer profile created",
        String.format("{\"source\":\"%s\"}", sourceSystem != null ? sourceSystem : "MANUAL"));

    eventPublisher.publishEvent(
        CustomerEvents.DOMAIN,
        CustomerEvents.ENTITY_PROFILE,
        CustomerEvents.ACTION_CREATED,
        String.format("{\"id\":\"%s\",\"displayName\":\"%s\"}", saved.getId(), displayName));

    auditLogger.logEvent("CREATE", "CUSTOMER:" + saved.getId(), "SUCCESS", "Customer created");
    log.info("Customer created id={} tenant-scoped", saved.getId());
    return saved;
  }

  @Transactional(readOnly = true)
  public Optional<Customer> findById(UUID id) {
    UUID tenantId = com.conductor.shared.middleware.tenant.TenantContext.getCurrentTenantId();
    if (tenantId != null) {
      return customerRepository.findByIdAndTenantId(id, tenantId);
    }
    return customerRepository.findById(id);
  }

  @Transactional(readOnly = true)
  public Page<Customer> findAll(Pageable pageable) {
    UUID tenantId = com.conductor.shared.middleware.tenant.TenantContext.getCurrentTenantId();
    if (tenantId != null) {
      return customerRepository.findAllActiveForTenant(tenantId, pageable);
    }
    return customerRepository.findAllActive(pageable);
  }

  @Transactional
  public Customer updateCustomer(UUID id, String firstName, String lastName, String displayName) {
    Customer customer = requireCustomer(id);

    customer.setFirstName(firstName);
    customer.setLastName(lastName);
    customer.setDisplayName(displayName);
    customer.setUpdatedAt(Instant.now());

    Customer updated = customerRepository.save(customer);

    timelineService.record(
        id,
        com.conductor.shared.customer.TimelineEventType.PROFILE_UPDATED,
        "customer-service",
        "Customer profile updated",
        null);

    eventPublisher.publishEvent(
        CustomerEvents.DOMAIN,
        CustomerEvents.ENTITY_PROFILE,
        CustomerEvents.ACTION_UPDATED,
        String.format("{\"id\":\"%s\"}", id));

    auditLogger.logEvent("UPDATE", "CUSTOMER:" + id, "SUCCESS", "Profile updated");
    return updated;
  }

  @Transactional
  public void deactivateCustomer(UUID id) {
    Customer customer = requireCustomer(id);
    customer.setStatus(CustomerStatus.INACTIVE);
    customer.setUpdatedAt(Instant.now());
    customerRepository.save(customer);

    timelineService.record(
        id,
        com.conductor.shared.customer.TimelineEventType.CUSTOMER_UPDATED,
        "customer-service",
        "Customer deactivated",
        null);

    eventPublisher.publishEvent(
        CustomerEvents.DOMAIN,
        CustomerEvents.ENTITY_PROFILE,
        CustomerEvents.ACTION_UPDATED,
        String.format("{\"id\":\"%s\",\"status\":\"INACTIVE\"}", id));

    auditLogger.logEvent("DEACTIVATE", "CUSTOMER:" + id, "SUCCESS", "Customer deactivated");
  }

  @Transactional
  public void softDeleteCustomer(UUID id) {
    Customer customer = requireCustomer(id);
    customer.setStatus(CustomerStatus.DELETED);
    customer.setDeletedAt(Instant.now());
    customer.setUpdatedAt(Instant.now());
    customerRepository.save(customer);

    eventPublisher.publishEvent(
        CustomerEvents.DOMAIN,
        CustomerEvents.ENTITY_PROFILE,
        CustomerEvents.ACTION_DELETED,
        String.format("{\"id\":\"%s\"}", id));

    auditLogger.logEvent("DELETE", "CUSTOMER:" + id, "SUCCESS", "Customer soft deleted");
  }

  @Transactional
  public void archiveCustomer(UUID id) {
    Customer customer = requireCustomer(id);
    customer.setStatus(CustomerStatus.ARCHIVED);
    customer.setUpdatedAt(Instant.now());
    customerRepository.save(customer);

    timelineService.record(
        id,
        com.conductor.shared.customer.TimelineEventType.CUSTOMER_ARCHIVED,
        "customer-service",
        "Customer archived",
        null);

    eventPublisher.publishEvent(
        CustomerEvents.DOMAIN,
        CustomerEvents.ENTITY_PROFILE,
        CustomerEvents.ACTION_ARCHIVED,
        String.format("{\"id\":\"%s\"}", id));

    auditLogger.logEvent("ARCHIVE", "CUSTOMER:" + id, "SUCCESS", "Customer archived");
  }

  /** Guard: resolves customer or throws. Returns 404-semantic exception for cross-tenant safety. */
  public Customer requireCustomer(UUID id) {
    return customerRepository
        .findById(id)
        .orElseThrow(() -> new com.conductor.customer.exception.CustomerNotFoundException(id));
  }
}
