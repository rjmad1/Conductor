package com.conductor.workflow.temporal;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DataErasureActivitiesImpl implements DataErasureActivities {

  private static final Logger log = LoggerFactory.getLogger(DataErasureActivitiesImpl.class);

  private final JdbcTemplate jdbcTemplate;
  private final RestTemplate restTemplate;
  private final String qdrantUrl;

  public DataErasureActivitiesImpl(
      JdbcTemplate jdbcTemplate, @Value("${qdrant.url:http://localhost:6333}") String qdrantUrl) {
    this.jdbcTemplate = jdbcTemplate;
    this.restTemplate = new RestTemplate();
    this.qdrantUrl = qdrantUrl;
  }

  @Override
  public void deletePostgresData(UUID tenantId, String customerId) {
    log.info("Executing DPDP erasure for tenant {}, customer {}", tenantId, customerId);

    // Hard-delete records to meet DPDP compliance.
    // Order matters to avoid foreign key constraint violations if cascading is not fully set up.

    // Example physical deletion from core tables
    jdbcTemplate.update(
        "DELETE FROM customer_contacts WHERE tenant_id = ? AND customer_id = ?",
        tenantId,
        customerId);

    jdbcTemplate.update(
        "DELETE FROM customer_timeline WHERE tenant_id = ? AND customer_id = ?",
        tenantId,
        customerId);

    jdbcTemplate.update(
        "DELETE FROM customers WHERE tenant_id = ? AND id = ?", tenantId, customerId);

    log.info("Successfully deleted relational Postgres data for customer {}", customerId);
  }

  @Override
  public void deleteQdrantVectors(UUID tenantId, String customerId) {
    log.info("Deleting Qdrant vectors for tenant {}, customer {}", tenantId, customerId);

    // Delete from all relevant collections (e.g., 'customers')
    String collectionName = "customers";
    String url = qdrantUrl + "/collections/" + collectionName + "/points/delete";

    String payload =
        String.format(
            """
        {
          "filter": {
            "must": [
              { "key": "tenant_id", "match": { "value": "%s" } },
              { "key": "customer_id", "match": { "value": "%s" } }
            ]
          }
        }
        """,
            tenantId.toString(), customerId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    try {
      ResponseEntity<String> response =
          restTemplate.exchange(
              url, HttpMethod.POST, new HttpEntity<>(payload, headers), String.class);
      log.info("Qdrant delete response: {}", response.getStatusCode());
    } catch (Exception e) {
      // It's possible the collection doesn't exist yet, which is fine
      log.warn(
          "Failed to delete Qdrant vectors, possibly because collection is missing: {}",
          e.getMessage());
    }
  }
}
