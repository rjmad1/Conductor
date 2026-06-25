package com.conductor.analytics.ingestion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.conductor.analytics.domain.AnalyticsEvent;
import com.conductor.analytics.observability.AnalyticsMetrics;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for ClickHouseWriter batch insert logic. */
class ClickHouseWriterTest {

  private DataSource mockDataSource;
  private AnalyticsMetrics mockMetrics;
  private ClickHouseWriter writer;

  @BeforeEach
  void setUp() {
    mockDataSource = mock(DataSource.class);
    mockMetrics = mock(AnalyticsMetrics.class);
    writer = new ClickHouseWriter(mockDataSource, mockMetrics, 10, 60000);
  }

  @Test
  void writeAddsEventToBuffer() {
    AnalyticsEvent event = buildEvent("test-tenant");
    boolean result = writer.write(event);
    assertTrue(result);
    assertEquals(1, writer.getBufferSize());
  }

  @Test
  void flushBufferExecutesBatchInsert() throws Exception {
    Connection mockConn = mock(Connection.class);
    PreparedStatement mockPs = mock(PreparedStatement.class);
    when(mockDataSource.getConnection()).thenReturn(mockConn);
    when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);

    writer.write(buildEvent("tenant-1"));
    writer.write(buildEvent("tenant-2"));
    writer.flushBuffer();

    verify(mockPs, times(2)).addBatch();
    verify(mockPs, times(1)).executeBatch();
    verify(mockMetrics).recordEventsIngested(2);
    assertEquals(0, writer.getBufferSize());
  }

  @Test
  void flushBufferHandlesException() throws Exception {
    when(mockDataSource.getConnection()).thenThrow(new RuntimeException("Connection failed"));

    writer.write(buildEvent("tenant-1"));
    writer.flushBuffer();

    verify(mockMetrics).recordEventsFailed(1);
  }

  @Test
  void emptyBufferFlushIsNoOp() {
    writer.flushBuffer();
    verifyNoInteractions(mockDataSource);
  }

  private AnalyticsEvent buildEvent(String tenantId) {
    return AnalyticsEvent.builder()
        .eventId(UUID.randomUUID())
        .eventType("conductor.workflow.execution.started")
        .tenantId(tenantId)
        .domain("workflow")
        .entity("execution")
        .action("started")
        .correlationId(UUID.randomUUID().toString())
        .source("/platform/test")
        .payload("{}")
        .createdAt(Instant.now())
        .build();
  }
}
