package com.conductor.workflow.temporal;

import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the Temporal worker infrastructure. Strategy: single "conductor" namespace,
 * tenant-prefixed task queues (workflow-{tenantId}). The worker registered here handles the
 * shared/system task queue. Per-tenant task queues are started dynamically via
 * TenantWorkerRegistrar.
 */
@Configuration
@org.springframework.context.annotation.Profile("!test")
public class WorkflowWorkerConfig {

  private static final Logger log = LoggerFactory.getLogger(WorkflowWorkerConfig.class);

  public static final String SYSTEM_TASK_QUEUE = "workflow-system";

  @Value("${temporal.service-address:localhost:7233}")
  private String temporalServiceAddress;

  @Value("${temporal.namespace:default}")
  private String temporalNamespace;

  @Bean
  public WorkflowServiceStubs workflowServiceStubs() {
    log.info("Connecting to Temporal at {}", temporalServiceAddress);
    return WorkflowServiceStubs.newServiceStubs(
        WorkflowServiceStubsOptions.newBuilder().setTarget(temporalServiceAddress).build());
  }

  @Bean
  public WorkflowClient workflowClient(WorkflowServiceStubs stubs) {
    log.info("Creating WorkflowClient for namespace: {}", temporalNamespace);
    return WorkflowClient.newInstance(
        stubs,
        io.temporal.client.WorkflowClientOptions.newBuilder()
            .setNamespace(temporalNamespace)
            .build());
  }

  @Bean
  public WorkerFactory workerFactory(WorkflowClient workflowClient) {
    return WorkerFactory.newInstance(workflowClient);
  }

  @Bean
  public ActivityOptions defaultActivityOptions() {
    return ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(10))
        .setRetryOptions(
            io.temporal.common.RetryOptions.newBuilder()
                .setMaximumAttempts(3)
                .setInitialInterval(Duration.ofSeconds(2))
                .setBackoffCoefficient(2.0)
                .setMaximumInterval(Duration.ofSeconds(120))
                .build())
        .build();
  }

  /**
   * Registers the system worker on the system task queue. Per-tenant workers are registered
   * dynamically by TenantWorkerRegistrar.
   */
  @Bean(initMethod = "start")
  public WorkerFactory startWorkerFactory(
      WorkerFactory workerFactory,
      ConductorActivitiesImpl activitiesImpl,
      DataErasureActivitiesImpl dataErasureActivitiesImpl) {
    Worker systemWorker = workerFactory.newWorker(SYSTEM_TASK_QUEUE);
    systemWorker.registerWorkflowImplementationTypes(
        ConductorWorkflowImpl.class, DataErasureWorkflowImpl.class);
    systemWorker.registerActivitiesImplementations(activitiesImpl, dataErasureActivitiesImpl);
    log.info("Registered system worker on task queue: {}", SYSTEM_TASK_QUEUE);
    return workerFactory;
  }
}
