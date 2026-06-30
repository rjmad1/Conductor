package com.conductor.customer.service;

import com.conductor.customer.domain.Customer;
import com.conductor.customer.domain.CustomerContact;
import com.conductor.customer.domain.CustomerIdentifier;
import com.conductor.customer.repository.CustomerContactRepository;
import com.conductor.customer.repository.CustomerIdentifierRepository;
import com.conductor.customer.repository.CustomerRepository;
import com.conductor.shared.customer.CustomerStatus;
import com.conductor.shared.middleware.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ScheduledAnonymizationTask — DPDP Section 12 Right to Erasure compliance task (T-204). Scans for
 * customers with status = DELETED and overwrites names, emails, and phones with random values to
 * erase PII while preserving workflow and campaign statistics.
 */
@Service
@SuppressWarnings("null")
public class ScheduledAnonymizationTask {

  private static final Logger log = LoggerFactory.getLogger(ScheduledAnonymizationTask.class);

  private final CustomerRepository customerRepository;
  private final CustomerContactRepository contactRepository;
  private final CustomerIdentifierRepository identifierRepository;
  private final ObjectMapper objectMapper;

  public ScheduledAnonymizationTask(
      CustomerRepository customerRepository,
      CustomerContactRepository contactRepository,
      CustomerIdentifierRepository identifierRepository) {
    this.customerRepository = customerRepository;
    this.contactRepository = contactRepository;
    this.identifierRepository = identifierRepository;
    this.objectMapper = new ObjectMapper();
  }

  /** Run the customer anonymization scheduler daily. Default: 2 AM daily. */
  @Scheduled(cron = "${conductor.anonymization.cron:0 0 2 * * ?}")
  @Transactional
  public void runAnonymization() {
    log.info("Starting scheduled customer anonymization sweep...");

    // Ensure no active tenant context for the background runner query (fetches all tenants)
    TenantContext.clear();

    List<Customer> deletedCustomers = customerRepository.findByStatus(CustomerStatus.DELETED);
    int count = 0;

    for (Customer customer : deletedCustomers) {
      if (isAlreadyAnonymized(customer)) {
        continue;
      }

      try {
        anonymizeCustomerRecord(customer);
        count++;
      } catch (Exception e) {
        log.error("Failed to anonymize customer record ID: {}", customer.getId(), e);
      }
    }

    if (count > 0) {
      log.info("Completed anonymization sweep. Anonymized {} customer records.", count);
    }
  }

  private boolean isAlreadyAnonymized(Customer customer) {
    String attrs = customer.getAttributes();
    if (attrs != null && attrs.contains("\"anonymized\":true")) {
      return true;
    }
    return "Anonymized Customer".equals(customer.getDisplayName());
  }

  private void anonymizeCustomerRecord(Customer customer) {
    UUID tenantId = customer.getTenantId();
    UUID customerId = customer.getId();
    log.info("Anonymizing customer PII for ID: {} [Tenant: {}]", customerId, tenantId);

    try {
      // Set TenantContext boundary for updates
      TenantContext.setCurrentTenantId(tenantId);

      // 1. Anonymize Contacts (phone/email values)
      List<CustomerContact> contacts = contactRepository.findByCustomerId(customerId);
      for (CustomerContact contact : contacts) {
        String dummyVal = "ANONYMIZED_" + UUID.randomUUID();
        contact.setValue(dummyVal);
        contact.setValueHash(hashValue(dummyVal));
        contact.setLabel("ANONYMIZED");
        contact.setVerifiedAt(null);
        contact.setVerified(false);
        contact.setUpdatedAt(Instant.now());
        contactRepository.save(contact);
      }

      // 2. Delete lookup identifiers to prevent collision on duplicate checks in future
      List<CustomerIdentifier> identifiers = identifierRepository.findByCustomerId(customerId);
      identifierRepository.deleteAllInBatch(identifiers);

      // 3. Anonymize Customer root aggregate
      customer.setFirstName("ANONYMIZED");
      customer.setLastName("ANONYMIZED");
      customer.setDisplayName("Anonymized Customer");
      customer.setExternalId(null);
      customer.setSourceSystem("ANONYMIZED");
      customer.setUpdatedAt(Instant.now());

      // Add anonymized flag to JSON attributes bag
      String currentAttrs = customer.getAttributes();
      ObjectNode rootNode;
      if (currentAttrs == null || currentAttrs.isBlank()) {
        rootNode = objectMapper.createObjectNode();
      } else {
        try {
          rootNode = (ObjectNode) objectMapper.readTree(currentAttrs);
        } catch (Exception e) {
          rootNode = objectMapper.createObjectNode();
        }
      }
      rootNode.put("anonymized", true);
      customer.setAttributes(objectMapper.writeValueAsString(rootNode));

      customerRepository.save(customer);
      log.info("PII successfully erased for customer ID: {}", customerId);

    } catch (Exception e) {
      throw new IllegalStateException("Anonymization failed for customer " + customerId, e);
    } finally {
      TenantContext.clear();
    }
  }

  private String hashValue(String value) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
