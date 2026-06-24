package com.conductor.events.service;

import com.conductor.events.domain.ReplayAuditLog;
import com.conductor.events.repository.ReplayAuditLogRepository;
import com.conductor.shared.messaging.NatsConnectionManager;
import com.conductor.shared.middleware.tenant.TenantContext;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamSubscription;
import io.nats.client.PullSubscribeOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReplayServiceTest {

    private ReplayAuditLogRepository repository;
    private NatsConnectionManager connectionManager;
    private ReplayService replayService;

    @BeforeEach
    void setUp() {
        repository = mock(ReplayAuditLogRepository.class);
        connectionManager = mock(NatsConnectionManager.class);
        replayService = new ReplayService(connectionManager, repository);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testExecuteReplayNoTenantContextThrows() {
        assertThrows(IllegalStateException.class, () ->
                replayService.executeReplay("stream", "consumer", "ALL", "0", "user"));
    }

    @Test
    void testExecuteReplaySuccess() throws Exception {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setCurrentTenantId(tenantId);

        Connection conn = mock(Connection.class);
        JetStream js = mock(JetStream.class);
        JetStreamSubscription sub = mock(JetStreamSubscription.class);

        when(connectionManager.getConnection()).thenReturn(conn);
        when(conn.jetStream()).thenReturn(js);
        when(js.subscribe(anyString(), any(PullSubscribeOptions.class))).thenReturn(sub);
        when(sub.fetch(anyInt(), any(Duration.class))).thenReturn(Collections.emptyList());

        List<String> results = replayService.executeReplay(
                "TENANT_STREAM",
                "tenant_consumer",
                "SEQUENCE",
                "10",
                "admin"
        );

        assertNotNull(results);
        verify(repository).save(any(ReplayAuditLog.class));
        verify(js).subscribe(eq("conductor." + tenantId + ".>"), any(PullSubscribeOptions.class));
    }
}
