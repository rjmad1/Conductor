package com.conductor.analytics.ingestion;

import com.conductor.analytics.domain.AnalyticsEvent;
import com.conductor.analytics.observability.AnalyticsMetrics;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Batch writer for ClickHouse analytics events. Buffers events in memory and flushes when batch
 * size or time interval is reached. Idempotent: uses event_id to prevent duplicate insertion at
 * query level.
 */
@Component
public class ClickHouseWriter {

  private static final Logger log = LoggerFactory.getLogger(ClickHouseWriter.class);

  private static final String INSERT_SQL =
      "INSERT INTO conductor_events (event_id, event_type, tenant_id, domain, entity, action, "
          + "correlation_id, source, payload, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private final DataSource clickHouseDataSource;
  private final AnalyticsMetrics metrics;
  private final int batchSize;
  private final long flushIntervalMs;

  private final BlockingQueue<AnalyticsEvent> buffer = new LinkedBlockingQueue<>(100_000);
  private ScheduledExecutorService scheduler;

  public ClickHouseWriter(
      @Qualifier("clickHouseDataSource") DataSource clickHouseDataSource,
      AnalyticsMetrics metrics,
      @Value("${analytics.clickhouse.batch-size:1000}") int batchSize,
      @Value("${analytics.clickhouse.flush-interval-ms:5000}") long flushIntervalMs) {
    this.clickHouseDataSource = clickHouseDataSource;
    this.metrics = metrics;
    this.batchSize = batchSize;
    this.flushIntervalMs = flushIntervalMs;
  }

  @PostConstruct
  void startFlushScheduler() {
    scheduler =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread t = new Thread(r, "clickhouse-flush");
              t.setDaemon(true);
              return t;
            });
    scheduler.scheduleAtFixedRate(
        this::flushBuffer, flushIntervalMs, flushIntervalMs, TimeUnit.MILLISECONDS);
    log.info(
        "ClickHouseWriter started with batchSize={} flushInterval={}ms",
        batchSize,
        flushIntervalMs);
  }

  /** Enqueue an event for batch insertion. Non-blocking. */
  public boolean write(AnalyticsEvent event) {
    boolean offered = buffer.offer(event);
    if (!offered) {
      log.warn("ClickHouse buffer full, dropping event: {}", event.getEventId());
      metrics.recordEventsDropped(1);
    }
    if (buffer.size() >= batchSize) {
      scheduler.execute(this::flushBuffer);
    }
    return offered;
  }

  /** Drain buffered events and batch-insert into ClickHouse. */
  void flushBuffer() {
    List<AnalyticsEvent> batch = new ArrayList<>(batchSize);
    buffer.drainTo(batch, batchSize);

    if (batch.isEmpty()) {
      return;
    }

    long startTime = System.currentTimeMillis();
    try (Connection conn = clickHouseDataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

      for (AnalyticsEvent event : batch) {
        ps.setObject(1, event.getEventId());
        ps.setString(2, event.getEventType());
        ps.setString(3, event.getTenantId());
        ps.setString(4, event.getDomain());
        ps.setString(5, event.getEntity());
        ps.setString(6, event.getAction());
        ps.setString(7, event.getCorrelationId());
        ps.setString(8, event.getSource());
        ps.setString(9, event.getPayload());
        ps.setTimestamp(10, Timestamp.from(event.getCreatedAt()));
        ps.addBatch();
      }

      ps.executeBatch();
      long duration = System.currentTimeMillis() - startTime;
      metrics.recordEventsIngested(batch.size());
      metrics.recordBatchInsertLatency(duration);
      log.debug("Flushed {} events to ClickHouse in {}ms", batch.size(), duration);

    } catch (Exception e) {
      log.error("Failed to flush {} events to ClickHouse", batch.size(), e);
      metrics.recordEventsFailed(batch.size());
    }
  }

  /** Returns the current buffer size for observability. */
  public int getBufferSize() {
    return buffer.size();
  }
}
