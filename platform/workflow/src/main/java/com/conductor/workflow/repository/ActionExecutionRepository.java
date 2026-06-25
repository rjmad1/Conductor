package com.conductor.workflow.repository;

import com.conductor.workflow.domain.ActionExecution;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA Repository for managing action executions persistence. */
@Repository
public interface ActionExecutionRepository extends JpaRepository<ActionExecution, UUID> {

  List<ActionExecution> findByWorkflowExecutionId(UUID workflowExecutionId);

  List<ActionExecution> findByCorrelationId(String correlationId);
}
