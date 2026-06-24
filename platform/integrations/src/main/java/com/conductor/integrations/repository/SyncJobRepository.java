package com.conductor.integrations.repository;

import com.conductor.integrations.domain.SyncJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SyncJobRepository extends JpaRepository<SyncJob, UUID> {
    List<SyncJob> findByIntegrationId(UUID integrationId);
    List<SyncJob> findByIntegrationIdAndTenantId(UUID integrationId, UUID tenantId);
}
