package com.conductor.integrations.repository;

import com.conductor.integrations.domain.Integration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationRepository extends JpaRepository<Integration, UUID> {
  List<Integration> findByTenantId(UUID tenantId);

  Optional<Integration> findByIdAndTenantId(UUID id, UUID tenantId);

  Optional<Integration> findByConnectorTypeAndTenantId(String type, UUID tenantId);
}
