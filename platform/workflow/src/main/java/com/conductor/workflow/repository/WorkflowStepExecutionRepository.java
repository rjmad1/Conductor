package com.conductor.workflow.repository;

import com.conductor.workflow.domain.WorkflowStepExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowStepExecutionRepository extends JpaRepository<WorkflowStepExecution, UUID> {

    List<WorkflowStepExecution> findByExecutionIdOrderByStartedAtAsc(UUID executionId);
}
