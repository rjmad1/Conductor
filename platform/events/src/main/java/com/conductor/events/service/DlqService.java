package com.conductor.events.service;

import com.conductor.events.domain.DlqRecord;
import com.conductor.events.repository.DlqRecordRepository;
import com.conductor.shared.messaging.NatsConnectionManager;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class DlqService {

  private static final Logger log = LoggerFactory.getLogger(DlqService.class);

  private final DlqRecordRepository dlqRecordRepository;
  private final NatsConnectionManager connectionManager;
  private final AuditLogger auditLogger;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public DlqService(
      DlqRecordRepository dlqRecordRepository,
      NatsConnectionManager connectionManager,
      AuditLogger auditLogger) {
    this.dlqRecordRepository = dlqRecordRepository;
    this.connectionManager = connectionManager;
    this.auditLogger = auditLogger;
  }

  public List<DlqRecord> getPendingRecords() {
    return dlqRecordRepository.findByStatus("PENDING");
  }

  @Transactional
  public boolean replayRecord(UUID recordId) {
    DlqRecord record =
        dlqRecordRepository
            .findById(recordId)
            .orElseThrow(() -> new IllegalArgumentException("DLQ Record not found: " + recordId));

    if (!"PENDING".equals(record.getStatus())) {
      throw new IllegalStateException("DLQ Record is not in PENDING state");
    }

    Connection conn = connectionManager.getConnection();
    if (conn == null) {
      log.error("NATS connection is offline. Cannot replay DLQ message.");
      return false;
    }

    try {
      // Parse original event mapping to extract target NATS subject
      JsonNode rootNode = objectMapper.readTree(record.getPayload());
      String eventType =
          rootNode.get("eventType").asText(); // e.g. conductor.tenant.profile.created
      String tenantId = rootNode.get("tenantId").asText();

      // Reconstruct original NATS subject using the canonical metadata
      String[] segments = eventType.split("\\.");
      if (segments.length < 4) {
        throw new IllegalArgumentException("Invalid eventType format in DLQ record: " + eventType);
      }
      String domain = segments[1];
      String entity = segments[2];
      String action = segments[3];

      String subject = String.format("conductor.%s.%s.%s.%s", tenantId, domain, entity, action);

      // Re-publish to the original stream subject
      conn.publish(subject, record.getPayload().getBytes(StandardCharsets.UTF_8));
      log.info("Replayed DLQ record {} to subject: {}", recordId, subject);

      // Update record state
      record.setStatus("REPLAYED");
      dlqRecordRepository.save(record);

      auditLogger.logEvent(
          "DLQ_REPLAY",
          "DLQ_RECORD:" + recordId,
          "SUCCESS",
          "Message replayed to subject: " + subject);
      return true;
    } catch (Exception e) {
      log.error("Failed to replay DLQ record: {}", recordId, e);
      auditLogger.logEvent(
          "DLQ_REPLAY_FAILED", "DLQ_RECORD:" + recordId, "FAILURE", e.getMessage());
      return false;
    }
  }

  @Transactional
  public void discardRecord(UUID recordId) {
    DlqRecord record =
        dlqRecordRepository
            .findById(recordId)
            .orElseThrow(() -> new IllegalArgumentException("DLQ Record not found: " + recordId));

    record.setStatus("DISCARDED");
    dlqRecordRepository.save(record);

    auditLogger.logEvent(
        "DLQ_DISCARD", "DLQ_RECORD:" + recordId, "SUCCESS", "Message marked as discarded");
  }
}
