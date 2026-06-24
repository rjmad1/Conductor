package com.conductor.integrations.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "webhook_subscriptions")
@Getter
@Setter
public class WebhookSubscription extends TenantAwareEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "integration_id", nullable = false)
    private Integration integration;

    @Column(name = "external_webhook_id")
    private String externalWebhookId;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "secret", columnDefinition = "text")
    private String secret;

    @Column(name = "target_url", nullable = false)
    private String targetUrl;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
