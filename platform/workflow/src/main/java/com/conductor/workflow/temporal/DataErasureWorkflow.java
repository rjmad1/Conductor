package com.conductor.workflow.temporal;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.util.UUID;

/**
 * Orchestrates the physical deletion of customer data across all storage layers (PostgreSQL,
 * Qdrant). Satisfies DPDP Right to Erasure SLAs.
 */
@WorkflowInterface
public interface DataErasureWorkflow {

  @WorkflowMethod
  void eraseCustomerData(UUID tenantId, String customerId);
}
