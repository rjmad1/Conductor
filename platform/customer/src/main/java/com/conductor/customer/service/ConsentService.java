package com.conductor.customer.service;

import com.conductor.customer.domain.ConsentRecord;
import com.conductor.customer.exception.ConsentException;
import com.conductor.customer.repository.ConsentRecordRepository;
import com.conductor.shared.customer.ConsentAction;
import com.conductor.shared.customer.ConsentType;
import com.conductor.shared.customer.CustomerEvents;
import com.conductor.shared.customer.TimelineEventType;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ConsentService — DPDP Act §6 compliant consent lifecycle management.
 *
 * <p>Key design constraints: 1. Consent records are APPEND-ONLY. No update or delete is ever
 * called. 2. Current consent status is derived from the most recent record. 3. Every consent change
 * is audited and publishes a NATS event. 4. Consent is versioned: each record carries a
 * consentVersion string. 5. Revoking a consent that was never granted throws ConsentException.
 *
 * <p>Compliance notes: - DPDP §12 Right to Erasure: customer deletion triggers consent record
 * anonymisation (handled by CustomerService, not here — consent history must be preserved for
 * compliance). - Meta WhatsApp Policy: opt-out keywords must be processed within 5 seconds. The
 * Messaging domain publishes opt-out events; this service reacts via event consumer.
 */
@Service
@SuppressWarnings("null")
public class ConsentService {

  private static final Logger log = LoggerFactory.getLogger(ConsentService.class);

  private final ConsentRecordRepository consentRepository;
  private final CustomerTimelineService timelineService;
  private final CustomerService customerService;
  private final NatsEventPublisher eventPublisher;
  private final AuditLogger auditLogger;

  public ConsentService(
      ConsentRecordRepository consentRepository,
      CustomerTimelineService timelineService,
      CustomerService customerService,
      NatsEventPublisher eventPublisher,
      AuditLogger auditLogger) {
    this.consentRepository = consentRepository;
    this.timelineService = timelineService;
    this.customerService = customerService;
    this.eventPublisher = eventPublisher;
    this.auditLogger = auditLogger;
  }

  @Transactional
  public ConsentRecord grantConsent(
      UUID customerId,
      ConsentType consentType,
      String channel,
      String legalBasis,
      String consentVersion,
      String ipAddress,
      String userAgent,
      String metadataJson) {
    customerService.requireCustomer(customerId);

    ConsentRecord record =
        buildRecord(
            customerId,
            consentType,
            ConsentAction.GRANTED,
            channel,
            legalBasis,
            consentVersion,
            ipAddress,
            userAgent,
            metadataJson);
    ConsentRecord saved = consentRepository.save(record);

    timelineService.record(
        customerId,
        TimelineEventType.CONSENT_GRANTED,
        "customer-service",
        "Consent granted: " + consentType.name(),
        String.format("{\"type\":\"%s\",\"version\":\"%s\"}", consentType, consentVersion));

    eventPublisher.publishEvent(
        CustomerEvents.DOMAIN,
        CustomerEvents.ENTITY_CONSENT,
        CustomerEvents.ACTION_GRANTED,
        String.format(
            "{\"customerId\":\"%s\",\"consentType\":\"%s\",\"version\":\"%s\"}",
            customerId, consentType, consentVersion));

    auditLogger.logEvent(
        "CONSENT_GRANTED",
        "CUSTOMER:" + customerId + ":CONSENT:" + consentType,
        "SUCCESS",
        "Consent granted version=" + consentVersion);

    log.info("Consent GRANTED customerId={} type={}", customerId, consentType);
    return saved;
  }

  @Transactional
  public ConsentRecord revokeConsent(
      UUID customerId,
      ConsentType consentType,
      String channel,
      String consentVersion,
      String ipAddress,
      String userAgent,
      String metadataJson) {
    customerService.requireCustomer(customerId);

    // Validate: can only revoke if currently granted
    Optional<ConsentRecord> latest =
        consentRepository.findLatestByCustomerIdAndConsentType(customerId, consentType);

    if (latest.isEmpty() || latest.get().getAction() != ConsentAction.GRANTED) {
      throw new ConsentException(
          "Cannot revoke consent for "
              + consentType
              + " — no active grant exists for customer "
              + customerId);
    }

    ConsentRecord record =
        buildRecord(
            customerId,
            consentType,
            ConsentAction.REVOKED,
            channel,
            null,
            consentVersion,
            ipAddress,
            userAgent,
            metadataJson);
    ConsentRecord saved = consentRepository.save(record);

    timelineService.record(
        customerId,
        TimelineEventType.CONSENT_REVOKED,
        "customer-service",
        "Consent revoked: " + consentType.name(),
        String.format("{\"type\":\"%s\"}", consentType));

    eventPublisher.publishEvent(
        CustomerEvents.DOMAIN,
        CustomerEvents.ENTITY_CONSENT,
        CustomerEvents.ACTION_REVOKED,
        String.format("{\"customerId\":\"%s\",\"consentType\":\"%s\"}", customerId, consentType));

    auditLogger.logEvent(
        "CONSENT_REVOKED",
        "CUSTOMER:" + customerId + ":CONSENT:" + consentType,
        "SUCCESS",
        "Consent revoked");

    log.info("Consent REVOKED customerId={} type={}", customerId, consentType);
    return saved;
  }

  /** Returns true only if the latest consent record action is GRANTED. */
  public boolean isConsentActive(UUID customerId, ConsentType consentType) {
    return consentRepository
        .findLatestByCustomerIdAndConsentType(customerId, consentType)
        .map(r -> r.getAction() == ConsentAction.GRANTED)
        .orElse(false);
  }

  /** Returns the latest consent record per type for a given customer. */
  public Optional<ConsentRecord> getCurrentConsent(UUID customerId, ConsentType consentType) {
    return consentRepository.findLatestByCustomerIdAndConsentType(customerId, consentType);
  }

  /** Full audit history for a customer — all consent events across all types. */
  public List<ConsentRecord> getConsentHistory(UUID customerId) {
    return consentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
  }

  /** Consent history for a specific type. */
  public List<ConsentRecord> getConsentHistoryByType(UUID customerId, ConsentType consentType) {
    return consentRepository.findByCustomerIdAndConsentTypeOrderByCreatedAtDesc(
        customerId, consentType);
  }

  // ── Builder ───────────────────────────────────────────────────────────────

  private ConsentRecord buildRecord(
      UUID customerId,
      ConsentType consentType,
      ConsentAction action,
      String channel,
      String legalBasis,
      String consentVersion,
      String ipAddress,
      String userAgent,
      String metadataJson) {
    ConsentRecord record = new ConsentRecord();
    record.setCustomerId(customerId);
    record.setConsentType(consentType);
    record.setAction(action);
    record.setChannel(channel);
    record.setLegalBasis(legalBasis);
    record.setConsentVersion(consentVersion);
    record.setIpAddress(ipAddress); // Masked in logs by Logback filter
    record.setUserAgent(userAgent);
    record.setMetadata(metadataJson);
    return record;
  }
}
