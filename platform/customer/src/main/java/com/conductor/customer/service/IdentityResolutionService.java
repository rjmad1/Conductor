package com.conductor.customer.service;

import com.conductor.customer.domain.CustomerContact;
import com.conductor.customer.domain.CustomerIdentifier;
import com.conductor.customer.repository.CustomerContactRepository;
import com.conductor.customer.repository.CustomerIdentifierRepository;
import com.conductor.shared.customer.ContactType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * IdentityResolutionService — tenant-scoped duplicate customer detection.
 *
 * <p>All lookups are performed on SHA-256 hashes of normalized values. Raw PII is never exposed
 * during resolution — identity check is purely hash-based.
 *
 * <p>Normalization rules: - Email: trim + lowercase - Phone: strip non-digit chars, ensure leading
 * + for E.164 - External ID: trim
 *
 * <p>All resolution operations are tenant-scoped via Hibernate @Filter (TenantAwareEntity). Audit
 * trail is maintained by callers (CustomerService, CustomerMergeService).
 */
@Service
public class IdentityResolutionService {

  private final CustomerContactRepository contactRepository;
  private final CustomerIdentifierRepository identifierRepository;

  public IdentityResolutionService(
      CustomerContactRepository contactRepository,
      CustomerIdentifierRepository identifierRepository) {
    this.contactRepository = contactRepository;
    this.identifierRepository = identifierRepository;
  }

  /** Resolve a customer by email address. Returns customerId if found. */
  public Optional<UUID> resolveByEmail(String email) {
    String hash = hashNormalized(normalizeEmail(email));
    return contactRepository
        .findByTypeAndValueHash(ContactType.EMAIL, hash)
        .map(CustomerContact::getCustomerId);
  }

  /** Resolve a customer by phone number. Returns customerId if found. */
  public Optional<UUID> resolveByPhone(String phone) {
    String hash = hashNormalized(normalizePhone(phone));
    return contactRepository
        .findByTypeAndValueHash(ContactType.PHONE, hash)
        .map(CustomerContact::getCustomerId);
  }

  /** Resolve a customer by external CRM / source system identifier. */
  public Optional<UUID> resolveByExternalId(String externalId, String sourceSystem) {
    String identifierType =
        "EXTERNAL_ID:" + (sourceSystem != null ? sourceSystem.toUpperCase() : "UNKNOWN");
    String hash = hashNormalized(externalId.trim());
    return identifierRepository
        .findByIdentifierTypeAndIdentifierHash(identifierType, hash)
        .map(CustomerIdentifier::getCustomerId);
  }

  /**
   * Register an email identifier for a customer. Creates an entry in customer_contacts (with hash
   * for lookup) and customer_identifiers (for the identity index). Called by ContactService when a
   * new email contact is added.
   */
  public String hashEmail(String email) {
    return hashNormalized(normalizeEmail(email));
  }

  public String hashPhone(String phone) {
    return hashNormalized(normalizePhone(phone));
  }

  public String hashValue(String rawValue) {
    return hashNormalized(rawValue.trim());
  }

  // ── Normalization ─────────────────────────────────────────────────────────

  public String normalizeEmail(String email) {
    if (email == null) return "";
    return email.trim().toLowerCase();
  }

  public String normalizePhone(String phone) {
    if (phone == null) return "";
    // Strip all non-digit characters except leading +
    String stripped = phone.trim().replaceAll("[^+\\d]", "");
    return stripped;
  }

  // ── Hashing ──────────────────────────────────────────────────────────────

  private String hashNormalized(String value) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      // SHA-256 is always available in JVM — this is unreachable
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
