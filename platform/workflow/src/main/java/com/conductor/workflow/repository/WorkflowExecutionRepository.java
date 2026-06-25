package com.conductor.workflow.repository;

import com.conductor.shared.workflow.WorkflowStatus;
import com.conductor.workflow.domain.WorkflowExecution;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID> {

  List<WorkflowExecution> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);

  List<WorkflowExecution> findByTenantIdAndDefinitionIdOrderByCreatedAtDesc(
      UUID tenantId, UUID definitionId, Pageable pageable);

  List<WorkflowExecution> findByTenantIdAndStatusOrderByCreatedAtDesc(
      UUID tenantId, WorkflowStatus status, Pageable pageable);

  Optional<WorkflowExecution> findByIdAndTenantId(UUID id, UUID tenantId);

  Optional<WorkflowExecution> findByTemporalWorkflowId(String temporalWorkflowId);

  long countByTenantId(UUID tenantId);

  long countByTenantIdAndStatus(UUID tenantId, WorkflowStatus status);
}
