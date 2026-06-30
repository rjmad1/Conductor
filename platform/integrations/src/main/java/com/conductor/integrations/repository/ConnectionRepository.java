package com.conductor.integrations.repository;

import com.conductor.integrations.domain.Connection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionRepository extends JpaRepository<Connection, UUID> {
  Optional<Connection> findByIntegrationId(UUID integrationId);

  Optional<Connection> findByIntegrationIdAndTenantId(UUID integrationId, UUID tenantId);
}
