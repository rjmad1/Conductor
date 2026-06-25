package com.conductor.integrations.repository;

import com.conductor.integrations.domain.ExecutionHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionHistoryRepository extends JpaRepository<ExecutionHistory, UUID> {
  List<ExecutionHistory> findByIntegrationId(UUID integrationId);

  List<ExecutionHistory> findByIntegrationIdAndTenantId(UUID integrationId, UUID tenantId);
}
