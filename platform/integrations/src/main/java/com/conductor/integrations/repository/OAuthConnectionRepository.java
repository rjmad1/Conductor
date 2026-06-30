package com.conductor.integrations.repository;

import com.conductor.integrations.domain.OAuthConnection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthConnectionRepository extends JpaRepository<OAuthConnection, UUID> {
  Optional<OAuthConnection> findByIntegrationId(UUID integrationId);

  Optional<OAuthConnection> findByIntegrationIdAndTenantId(UUID integrationId, UUID tenantId);
}
