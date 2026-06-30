package com.conductor.workflow.repository;

import com.conductor.workflow.domain.WorkflowStepExecution;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowStepExecutionRepository
    extends JpaRepository<WorkflowStepExecution, UUID> {

  List<WorkflowStepExecution> findByExecutionIdOrderByStartedAtAsc(UUID executionId);
}
