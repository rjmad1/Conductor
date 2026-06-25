package com.conductor.events.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.conductor.events.domain.DlqRecord;
import com.conductor.events.repository.DlqRecordRepository;
import com.conductor.shared.messaging.NatsConnectionManager;
import com.conductor.shared.middleware.tenant.AuditLogger;
import io.nats.client.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DlqServiceTest {

  private DlqRecordRepository repository;
  private NatsConnectionManager connectionManager;
  private AuditLogger auditLogger;
  private DlqService dlqService;

  @BeforeEach
  void setUp() {
    repository = mock(DlqRecordRepository.class);
    connectionManager = mock(NatsConnectionManager.class);
    auditLogger = mock(AuditLogger.class);
    dlqService = new DlqService(repository, connectionManager, auditLogger);
  }

  @Test
  void testGetPendingRecords() {
    DlqRecord record = new DlqRecord();
    record.setStatus("PENDING");
    when(repository.findByStatus("PENDING")).thenReturn(Collections.singletonList(record));

    List<DlqRecord> result = dlqService.getPendingRecords();
    assertEquals(1, result.size());
    assertEquals("PENDING", result.get(0).getStatus());
  }

  @Test
  void testReplayRecordNotPendingThrows() {
    UUID recordId = UUID.randomUUID();
    DlqRecord record = new DlqRecord();
    record.setStatus("REPLAYED");
    when(repository.findById(recordId)).thenReturn(Optional.of(record));

    assertThrows(IllegalStateException.class, () -> dlqService.replayRecord(recordId));
  }

  @Test
  void testReplayRecordSuccess() throws Exception {
    UUID recordId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    DlqRecord record = new DlqRecord();
    record.setId(recordId);
    record.setStatus("PENDING");
    record.setPayload(
        "{\"eventType\":\"conductor.tenant.profile.created\",\"tenantId\":\""
            + tenantId
            + "\",\"payload\":\"{}\"}");

    when(repository.findById(recordId)).thenReturn(Optional.of(record));

    Connection conn = mock(Connection.class);
    when(connectionManager.getConnection()).thenReturn(conn);

    boolean success = dlqService.replayRecord(recordId);

    assertTrue(success);
    assertEquals("REPLAYED", record.getStatus());
    verify(repository).save(record);
    verify(conn)
        .publish(eq("conductor." + tenantId + ".tenant.profile.created"), any(byte[].class));
    verify(auditLogger)
        .logEvent(eq("DLQ_REPLAY"), eq("DLQ_RECORD:" + recordId), eq("SUCCESS"), anyString());
  }

  @Test
  void testDiscardRecord() {
    UUID recordId = UUID.randomUUID();
    DlqRecord record = new DlqRecord();
    record.setId(recordId);
    record.setStatus("PENDING");
    when(repository.findById(recordId)).thenReturn(Optional.of(record));

    dlqService.discardRecord(recordId);

    assertEquals("DISCARDED", record.getStatus());
    verify(repository).save(record);
    verify(auditLogger)
        .logEvent(eq("DLQ_DISCARD"), eq("DLQ_RECORD:" + recordId), eq("SUCCESS"), anyString());
  }
}
