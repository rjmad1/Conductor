package com.conductor.customer.service;

import com.conductor.customer.domain.CustomerContact;
import com.conductor.customer.domain.CustomerIdentifier;
import com.conductor.customer.exception.CustomerNotFoundException;
import com.conductor.customer.repository.CustomerContactRepository;
import com.conductor.customer.repository.CustomerIdentifierRepository;
import com.conductor.shared.customer.ContactType;
import com.conductor.shared.customer.TimelineEventType;
import com.conductor.shared.middleware.tenant.AuditLogger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * ContactService — manages all contact channels for a customer.
 *
 * <p>Validation: - Email: RFC 5322 simple regex check - Phone: E.164 format check (leading +, 7-15
 * digits)
 *
 * <p>Identity resolution hook: when a new contact is added, its hash is registered in
 * customer_identifiers for future O(1) duplicate detection.
 *
 * <p>Primary contact rules: at most one contact per type may be isPrimary = true. Setting a new
 * primary automatically clears the previous one.
 */
@Service
@SuppressWarnings("null")
public class ContactService {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
  private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{6,14}$");

  private final CustomerContactRepository contactRepository;
  private final CustomerIdentifierRepository identifierRepository;
  private final IdentityResolutionService identityResolutionService;
  private final CustomerTimelineService timelineService;
  private final CustomerService customerService;
  private final AuditLogger auditLogger;

  public ContactService(
      CustomerContactRepository contactRepository,
      CustomerIdentifierRepository identifierRepository,
      IdentityResolutionService identityResolutionService,
      CustomerTimelineService timelineService,
      CustomerService customerService,
      AuditLogger auditLogger) {
    this.contactRepository = contactRepository;
    this.identifierRepository = identifierRepository;
    this.identityResolutionService = identityResolutionService;
    this.timelineService = timelineService;
    this.customerService = customerService;
    this.auditLogger = auditLogger;
  }

  public List<CustomerContact> getContacts(UUID customerId) {
    customerService.requireCustomer(customerId);
    return contactRepository.findByCustomerId(customerId);
  }

  @Transactional
  public CustomerContact addContact(
      UUID customerId, ContactType type, String value, String label, boolean setPrimary) {
    customerService.requireCustomer(customerId);
    validate(type, value);

    String normalized = normalize(type, value);
    String hash = computeHash(type, normalized);

    // Build contact record
    CustomerContact contact = new CustomerContact();
    contact.setCustomerId(customerId);
    contact.setType(type);
    contact.setValue(value); // stored encrypted via PiiEncryptedConverter
    contact.setValueHash(hash); // stored in plaintext for identity lookup
    contact.setLabel(label);
    contact.setPrimary(false);

    if (setPrimary) {
      clearPrimary(customerId, type);
      contact.setPrimary(true);
    }

    CustomerContact saved = contactRepository.save(contact);

    // Register identifier for O(1) future resolution
    registerIdentifier(customerId, type, hash);

    timelineService.record(
        customerId,
        TimelineEventType.CONTACT_ADDED,
        "customer-service",
        "Contact added: " + type.name(),
        String.format("{\"type\":\"%s\",\"label\":\"%s\"}", type, label));

    auditLogger.logEvent(
        "ADD_CONTACT", "CUSTOMER:" + customerId, "SUCCESS", "Contact type=" + type + " added");
    return saved;
  }

  @Transactional
  public CustomerContact updateContact(
      UUID customerId, UUID contactId, String label, boolean setPrimary) {
    CustomerContact contact =
        contactRepository
            .findById(contactId)
            .filter(c -> c.getCustomerId().equals(customerId))
            .orElseThrow(() -> new CustomerNotFoundException("Contact not found: " + contactId));

    contact.setLabel(label);
    contact.setUpdatedAt(Instant.now());

    if (setPrimary && !contact.isPrimary()) {
      clearPrimary(customerId, contact.getType());
      contact.setPrimary(true);
    }

    CustomerContact updated = contactRepository.save(contact);
    auditLogger.logEvent("UPDATE_CONTACT", "CUSTOMER:" + customerId, "SUCCESS", "Contact updated");
    return updated;
  }

  @Transactional
  public void setPrimaryContact(UUID customerId, UUID contactId) {
    CustomerContact contact =
        contactRepository
            .findById(contactId)
            .filter(c -> c.getCustomerId().equals(customerId))
            .orElseThrow(() -> new CustomerNotFoundException("Contact not found: " + contactId));

    clearPrimary(customerId, contact.getType());
    contact.setPrimary(true);
    contact.setUpdatedAt(Instant.now());
    contactRepository.save(contact);

    auditLogger.logEvent(
        "SET_PRIMARY_CONTACT",
        "CUSTOMER:" + customerId,
        "SUCCESS",
        "Primary contact set: " + contactId);
  }

  @Transactional
  public void removeContact(UUID customerId, UUID contactId) {
    CustomerContact contact =
        contactRepository
            .findById(contactId)
            .filter(c -> c.getCustomerId().equals(customerId))
            .orElseThrow(() -> new CustomerNotFoundException("Contact not found: " + contactId));

    contactRepository.delete(contact);

    timelineService.record(
        customerId,
        TimelineEventType.CONTACT_REMOVED,
        "customer-service",
        "Contact removed: " + contact.getType().name(),
        null);

    auditLogger.logEvent(
        "REMOVE_CONTACT", "CUSTOMER:" + customerId, "SUCCESS", "Contact removed: " + contactId);
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private void validate(ContactType type, String value) {
    if (!StringUtils.hasText(value)) {
      throw new IllegalArgumentException("Contact value cannot be blank");
    }
    switch (type) {
      case EMAIL -> {
        if (!EMAIL_PATTERN.matcher(value.trim()).matches()) {
          throw new IllegalArgumentException("Invalid email format: " + value);
        }
      }
      case PHONE, WHATSAPP -> {
        String normalized = identityResolutionService.normalizePhone(value);
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
          throw new IllegalArgumentException(
              "Phone must be in E.164 format (+<country><number>): " + value);
        }
      }
      default -> {
        /* ADDRESS, SOCIAL, CUSTOM — no structural validation */
      }
    }
  }

  private String normalize(ContactType type, String value) {
    return switch (type) {
      case EMAIL -> identityResolutionService.normalizeEmail(value);
      case PHONE, WHATSAPP -> identityResolutionService.normalizePhone(value);
      default -> value.trim();
    };
  }

  private String computeHash(ContactType type, String normalized) {
    return switch (type) {
      case EMAIL -> identityResolutionService.hashEmail(normalized);
      default -> identityResolutionService.hashValue(normalized);
    };
  }

  private void clearPrimary(UUID customerId, ContactType type) {
    contactRepository.findByCustomerIdAndIsPrimaryTrue(customerId).stream()
        .filter(c -> c.getType() == type)
        .forEach(
            c -> {
              c.setPrimary(false);
              c.setUpdatedAt(Instant.now());
              contactRepository.save(c);
            });
  }

  private void registerIdentifier(UUID customerId, ContactType type, String hash) {
    String identifierType =
        switch (type) {
          case EMAIL -> "EMAIL";
          case PHONE -> "PHONE";
          case WHATSAPP -> "WHATSAPP";
          default -> null;
        };
    if (identifierType == null) return;

    boolean exists =
        identifierRepository
            .findByIdentifierTypeAndIdentifierHash(identifierType, hash)
            .isPresent();
    if (!exists) {
      CustomerIdentifier identifier = new CustomerIdentifier();
      identifier.setCustomerId(customerId);
      identifier.setIdentifierType(identifierType);
      identifier.setIdentifierHash(hash);
      identifierRepository.save(identifier);
    }
  }
}
