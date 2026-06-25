package com.conductor.integrations.repository;

import com.conductor.integrations.domain.Transformation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransformationRepository extends JpaRepository<Transformation, UUID> {
  List<Transformation> findByIntegrationId(UUID integrationId);

  List<Transformation> findByIntegrationIdAndTenantId(UUID integrationId, UUID tenantId);

  Optional<Transformation> findByIntegrationIdAndNameAndTenantId(
      UUID integrationId, String name, UUID tenantId);
}
