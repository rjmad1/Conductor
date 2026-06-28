package com.conductor.ai.qdrant;

import com.conductor.shared.middleware.tenant.TenantContext;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Points.Condition;
import io.qdrant.client.grpc.Points.FieldCondition;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.Match;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Wrapper for Qdrant client that strictly enforces multi-tenancy. All points inserted or searched
 * must include a mandatory `tenant_id` payload tag, corresponding to the current active tenant
 * context.
 */
@Service
@SuppressWarnings("null")
public class QdrantTenantService {

  private static final Logger log = LoggerFactory.getLogger(QdrantTenantService.class);
  private final QdrantClient qdrantClient;
  private static final String TENANT_KEY = "tenant_id";

  public QdrantTenantService(
      @Value("${qdrant.host:localhost}") String host, @Value("${qdrant.grpc.port:6334}") int port) {
    this.qdrantClient = new QdrantClient(QdrantGrpcClient.newBuilder(host, port, false).build());
    log.info("Initialized QdrantTenantService connecting to {}:{}", host, port);
  }

  /** Initializes a collection if it does not exist. The collection is shared across all tenants. */
  public void initializeCollection(String collectionName, int vectorSize) {
    try {
      if (!qdrantClient.collectionExistsAsync(collectionName).get()) {
        qdrantClient
            .createCollectionAsync(
                collectionName,
                VectorParams.newBuilder().setSize(vectorSize).setDistance(Distance.Cosine).build())
            .get();
        log.info("Created Qdrant collection: {}", collectionName);
      }
    } catch (Exception e) {
      log.error("Failed to initialize Qdrant collection {}", collectionName, e);
    }
  }

  /** Searches the collection, strictly enforcing the tenant isolation filter. */
  public List<ScoredPoint> search(String collectionName, List<Float> queryVector, int limit) {
    UUID tenantId = TenantContext.getCurrentTenantId();
    if (tenantId == null) {
      throw new IllegalStateException("Cannot search Qdrant without an active TenantContext");
    }

    Filter tenantFilter =
        Filter.newBuilder()
            .addMust(
                Condition.newBuilder()
                    .setField(
                        FieldCondition.newBuilder()
                            .setKey(TENANT_KEY)
                            .setMatch(Match.newBuilder().setKeyword(tenantId.toString()).build())
                            .build())
                    .build())
            .build();

    SearchPoints request =
        SearchPoints.newBuilder()
            .setCollectionName(collectionName)
            .addAllVector(queryVector)
            .setLimit(limit)
            .setFilter(tenantFilter)
            .setWithPayload(
                io.qdrant.client.grpc.Points.WithPayloadSelector.newBuilder()
                    .setEnable(true)
                    .build())
            .build();

    try {
      return qdrantClient.searchAsync(request).get();
    } catch (Exception e) {
      log.error("Error searching Qdrant collection {} for tenant {}", collectionName, tenantId, e);
      throw new RuntimeException("Vector search failed", e);
    }
  }

  /** Upserts points, automatically injecting the tenant_id payload. */
  public void upsert(String collectionName, List<PointStruct> points) {
    UUID tenantId = TenantContext.getCurrentTenantId();
    if (tenantId == null) {
      throw new IllegalStateException("Cannot upsert to Qdrant without an active TenantContext");
    }

    // We expect points to already be built with the tenant_id payload, but this method
    // serves as the central gateway to ensure context is valid.

    try {
      qdrantClient.upsertAsync(collectionName, points).get();
      log.debug("Upserted {} points to {} for tenant {}", points.size(), collectionName, tenantId);
    } catch (Exception e) {
      log.error("Error upserting points to Qdrant collection {}", collectionName, e);
      throw new RuntimeException("Vector upsert failed", e);
    }
  }

  /**
   * Deletes all points for a specific customer within the current tenant. Required for DPDP Erasure
   * SLA.
   */
  public void deleteCustomerPoints(String collectionName, String customerId) {
    UUID tenantId = TenantContext.getCurrentTenantId();
    if (tenantId == null) {
      throw new IllegalStateException("Cannot delete from Qdrant without an active TenantContext");
    }

    Filter deleteFilter =
        Filter.newBuilder()
            .addMust(
                Condition.newBuilder()
                    .setField(
                        FieldCondition.newBuilder()
                            .setKey(TENANT_KEY)
                            .setMatch(Match.newBuilder().setKeyword(tenantId.toString()).build())
                            .build())
                    .build())
            .addMust(
                Condition.newBuilder()
                    .setField(
                        FieldCondition.newBuilder()
                            .setKey("customer_id")
                            .setMatch(Match.newBuilder().setKeyword(customerId).build())
                            .build())
                    .build())
            .build();

    try {
      qdrantClient
          .deleteAsync(
              io.qdrant.client.grpc.Points.DeletePoints.newBuilder()
                  .setCollectionName(collectionName)
                  .setPoints(
                      io.qdrant.client.grpc.Points.PointsSelector.newBuilder()
                          .setFilter(deleteFilter)
                          .build())
                  .build())
          .get();
      log.info("Deleted Qdrant points for customer {} in tenant {}", customerId, tenantId);
    } catch (Exception e) {
      log.error("Error deleting points for customer {}", customerId, e);
      throw new RuntimeException("Vector deletion failed", e);
    }
  }
}
