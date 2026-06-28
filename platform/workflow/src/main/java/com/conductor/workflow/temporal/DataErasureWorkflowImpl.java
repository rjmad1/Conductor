package com.conductor.workflow.temporal;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;

public class DataErasureWorkflowImpl implements DataErasureWorkflow {

  private static final Logger log = Workflow.getLogger(DataErasureWorkflowImpl.class);

  private final DataErasureActivities activities =
      Workflow.newActivityStub(
          DataErasureActivities.class,
          ActivityOptions.newBuilder()
              .setStartToCloseTimeout(Duration.ofMinutes(5))
              .setRetryOptions(
                  io.temporal.common.RetryOptions.newBuilder().setMaximumAttempts(5).build())
              .build());

  @Override
  public void eraseCustomerData(UUID tenantId, String customerId) {
    log.info("Starting data erasure workflow for tenant {}, customer {}", tenantId, customerId);

    // 1. Delete relational records (Postgres)
    activities.deletePostgresData(tenantId, customerId);

    // 2. Delete vector points (Qdrant)
    activities.deleteQdrantVectors(tenantId, customerId);

    log.info("Completed data erasure workflow for tenant {}, customer {}", tenantId, customerId);
  }
}
