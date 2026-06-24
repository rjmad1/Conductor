package com.conductor.integrations.repository;

import com.conductor.integrations.domain.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, UUID> {
    List<WebhookSubscription> findByIntegrationId(UUID integrationId);
    List<WebhookSubscription> findByIntegrationIdAndTenantId(UUID integrationId, UUID tenantId);
    Optional<WebhookSubscription> findByIntegrationConnectorTypeAndEventNameAndTenantId(String connectorType, String eventName, UUID tenantId);
}
