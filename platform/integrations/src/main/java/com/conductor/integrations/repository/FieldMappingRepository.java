package com.conductor.integrations.repository;

import com.conductor.integrations.domain.FieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FieldMappingRepository extends JpaRepository<FieldMapping, UUID> {
    List<FieldMapping> findByIntegrationId(UUID integrationId);
    List<FieldMapping> findByIntegrationIdAndTenantId(UUID integrationId, UUID tenantId);
}
