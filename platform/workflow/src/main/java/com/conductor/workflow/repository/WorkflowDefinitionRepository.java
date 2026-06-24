package com.conductor.workflow.repository;

import com.conductor.workflow.domain.WorkflowDefinition;
import com.conductor.shared.workflow.WorkflowVersionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, UUID> {

    List<WorkflowDefinition> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);

    List<WorkflowDefinition> findByTenantIdAndVersionStatusOrderByCreatedAtDesc(
            UUID tenantId, WorkflowVersionStatus versionStatus, Pageable pageable);

    Optional<WorkflowDefinition> findByIdAndTenantId(UUID id, UUID tenantId);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndVersionStatus(UUID tenantId, WorkflowVersionStatus versionStatus);
}
