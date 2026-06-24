package com.conductor.events.service;

import com.conductor.events.domain.ReplayAuditLog;
import com.conductor.events.repository.ReplayAuditLogRepository;
import com.conductor.shared.messaging.NatsConnectionManager;
import com.conductor.shared.middleware.tenant.TenantContext;
import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReplayService {

    private static final Logger log = LoggerFactory.getLogger(ReplayService.class);
    
    private final NatsConnectionManager connectionManager;
    private final ReplayAuditLogRepository auditLogRepository;

    public ReplayService(
            NatsConnectionManager connectionManager,
            ReplayAuditLogRepository auditLogRepository) {
        this.connectionManager = connectionManager;
        this.auditLogRepository = auditLogRepository;
    }

    public List<String> executeReplay(
            String streamName,
            String consumerName,
            String replayType, // SEQUENCE, TIMESTAMP, ALL
            String startValue,
            String username) {

        UUID tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is required for replaying events");
        }

        log.info("Executing event replay: tenant={}, stream={}, type={}, start={}", 
                tenantId, streamName, replayType, startValue);

        // 1. Audit replay request (Phase 9)
        ReplayAuditLog auditLog = new ReplayAuditLog();
        auditLog.setTenantId(tenantId);
        auditLog.setUsername(username);
        auditLog.setStream(streamName);
        auditLog.setConsumer(consumerName);
        auditLog.setReplayType(replayType);
        auditLog.setStartValue(startValue);
        auditLogRepository.save(auditLog);

        List<String> replayedEvents = new ArrayList<>();
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            log.error("NATS connection is offline. Cannot execute replay.");
            return replayedEvents;
        }

        try {
            JetStream js = conn.jetStream();
            
            // Enforce tenant scoping:
            // Crucial tenant isolation gate: We filter by tenant specific prefix: conductor.{tenantId}.>
            // This prevents ANY cross-tenant event leakage at the NATS server routing tier.
            String filterSubject = String.format("conductor.%s.>", tenantId.toString());

            // Build consumer configuration based on starting offsets
            ConsumerConfiguration.Builder ccb = ConsumerConfiguration.builder()
                    .filterSubject(filterSubject);

            if ("SEQUENCE".equalsIgnoreCase(replayType)) {
                long seq = Long.parseLong(startValue);
                ccb.deliverPolicy(DeliverPolicy.ByStartSequence)
                   .startSequence(seq);
            } else if ("TIMESTAMP".equalsIgnoreCase(replayType)) {
                Instant startInstant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(startValue));
                ZonedDateTime startZoned = ZonedDateTime.ofInstant(startInstant, ZoneOffset.UTC);
                ccb.deliverPolicy(DeliverPolicy.ByStartTime)
                   .startTime(startZoned);
            } else {
                ccb.deliverPolicy(DeliverPolicy.All);
            }

            PullSubscribeOptions pso = PullSubscribeOptions.builder()
                    .configuration(ccb.build())
                    .stream(streamName)
                    .build();

            // Subscribe using a temporary transient pull subscription
            JetStreamSubscription sub = js.subscribe(filterSubject, pso);
            
            // Fetch messages (limit to 100 per replay request for safety)
            List<Message> messages = sub.fetch(100, Duration.ofSeconds(5));
            for (Message msg : messages) {
                String payload = new String(msg.getData(), StandardCharsets.UTF_8);
                replayedEvents.add(payload);
                msg.ack();
            }

        } catch (Exception e) {
            log.error("Error executing NATS historical event replay", e);
            throw new RuntimeException("Replay failed: " + e.getMessage(), e);
        }

        return replayedEvents;
    }
}
