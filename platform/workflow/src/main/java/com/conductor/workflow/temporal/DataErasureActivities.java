package com.conductor.workflow.temporal;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import java.util.UUID;

@ActivityInterface
public interface DataErasureActivities {

  @ActivityMethod
  void deletePostgresData(UUID tenantId, String customerId);

  @ActivityMethod
  void deleteQdrantVectors(UUID tenantId, String customerId);
}
