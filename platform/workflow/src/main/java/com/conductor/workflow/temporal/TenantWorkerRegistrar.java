package com.conductor.workflow.temporal;

import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Dynamically registers Temporal workers for per-tenant task queues. Task queue naming:
 * workflow-{tenantId} Workers are registered lazily on first execution for a given tenant.
 */
@Component
public class TenantWorkerRegistrar {

  private static final Logger log = LoggerFactory.getLogger(TenantWorkerRegistrar.class);

  private final WorkerFactory workerFactory;
  private final ConductorActivitiesImpl activitiesImpl;

  /** Tracks which tenant task queues already have registered workers. */
  private final Set<String> registeredQueues = ConcurrentHashMap.newKeySet();

  public TenantWorkerRegistrar(
      WorkerFactory workerFactory, ConductorActivitiesImpl activitiesImpl) {
    this.workerFactory = workerFactory;
    this.activitiesImpl = activitiesImpl;
  }

  /**
   * Ensures a worker is registered for the given tenant's task queue. Idempotent — safe to call
   * repeatedly for the same tenant.
   *
   * @param tenantId the tenant UUID string
   * @return the task queue name for this tenant
   */
  public String ensureWorkerForTenant(String tenantId) {
    String taskQueue = "workflow-" + tenantId;

    if (registeredQueues.add(taskQueue)) {
      Worker worker = workerFactory.newWorker(taskQueue);
      worker.registerWorkflowImplementationTypes(ConductorWorkflowImpl.class);
      worker.registerActivitiesImplementations(activitiesImpl);

      // Factory must be started if not already running; factory.start() is idempotent
      workerFactory.start();

      log.info("Registered Temporal worker for tenant task queue: {}", taskQueue);
    }

    return taskQueue;
  }
}
