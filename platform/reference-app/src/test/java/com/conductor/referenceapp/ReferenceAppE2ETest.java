package com.conductor.referenceapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings({"rawtypes", "null"})
public class ReferenceAppE2ETest {

  private static final Logger log = LoggerFactory.getLogger(ReferenceAppE2ETest.class);
  private final RestTemplate restTemplate;

  public ReferenceAppE2ETest() {
    org.springframework.http.client.SimpleClientHttpRequestFactory factory =
        new org.springframework.http.client.SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(2000);
    factory.setReadTimeout(2000);
    this.restTemplate = new RestTemplate(factory);
  }

  @Autowired private MockMvc mockMvc;

  @Test
  void testStaticUiIsAccessible() throws Exception {
    mockMvc.perform(get("/index.html")).andExpect(status().isOk());
  }

  @Test
  void testBootstrapUserEndpointValidation() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/reference/bootstrap-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testLivePlatformEndToEndFlow() {
    log.info("Starting Live E2E Platform verification check...");

    // Check if local Keycloak is running. If not, skip the live test context gracefully.
    try {
      restTemplate.getForEntity("http://localhost:8080/realms/master", String.class);
    } catch (Exception e) {
      log.warn(
          "Local Keycloak is not running on port 8080. Skipping Live E2E platform checks: {}",
          e.getMessage());
      return;
    }

    try {
      // 1. Authenticate as Platform Admin (master realm)
      log.info("E2E Step 1: Authenticating as Platform Admin against master realm");
      String platformToken = getOidcToken("master", "admin", "admin_password");
      assertThat(platformToken).isNotNull();

      // 2. Create Tenant
      log.info("E2E Step 2: Creating Tenant via tenant-service API");
      String tenantKey = "e2e-tenant-" + UUID.randomUUID().toString().substring(0, 8);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(platformToken);

      Map<String, String> tenantReq =
          Map.of(
              "tenantKey",
              tenantKey,
              "displayName",
              "E2E Test Org",
              "domain",
              tenantKey + ".conductor.app");

      HttpEntity<Map<String, String>> tenantEntity = new HttpEntity<>(tenantReq, headers);
      ResponseEntity<Map> tenantRes =
          restTemplate.exchange(
              "http://localhost:8081/api/v1/tenants", HttpMethod.POST, tenantEntity, Map.class);

      assertThat(tenantRes.getStatusCode().is2xxSuccessful()).isTrue();
      String tenantId = (String) tenantRes.getBody().get("id");
      log.info("Created Tenant ID: {}", tenantId);

      // 3. Bootstrap Tenant User (custom realm)
      log.info("E2E Step 3: Bootstrapping Tenant Admin user in custom realm");
      Map<String, String> bootstrapReq =
          Map.of(
              "tenantId", tenantId,
              "email", "e2e_admin@conductor.app",
              "password", "AdminPassword123!");

      ResponseEntity<Map> bootstrapRes =
          restTemplate.postForEntity(
              "http://localhost:8083/api/v1/reference/bootstrap-user", bootstrapReq, Map.class);
      assertThat(bootstrapRes.getStatusCode().is2xxSuccessful()).isTrue();

      // 4. Authenticate as Tenant Admin (custom tenant realm)
      log.info("E2E Step 4: Authenticating as new Tenant Admin");
      String tenantAdminToken =
          getOidcToken("conductor-" + tenantId, "e2e_admin@conductor.app", "AdminPassword123!");
      assertThat(tenantAdminToken).isNotNull();

      // 5. Create Customer
      log.info("E2E Step 5: Creating Customer via customer-service API");
      HttpHeaders tenantHeaders = new HttpHeaders();
      tenantHeaders.setContentType(MediaType.APPLICATION_JSON);
      tenantHeaders.setBearerAuth(tenantAdminToken);
      tenantHeaders.set("X-Tenant-ID", tenantId);

      Map<String, String> customerReq =
          Map.of(
              "firstName", "John",
              "lastName", "Doe",
              "displayName", "John Doe",
              "sourceSystem", "REFERENCE_APP");

      HttpEntity<Map<String, String>> customerEntity = new HttpEntity<>(customerReq, tenantHeaders);
      ResponseEntity<Map> customerRes =
          restTemplate.exchange(
              "http://localhost:8084/api/v1/customers", HttpMethod.POST, customerEntity, Map.class);
      assertThat(customerRes.getStatusCode().is2xxSuccessful()).isTrue();
      String customerId = (String) customerRes.getBody().get("id");
      log.info("Created Customer ID: {}", customerId);

      // 6. View Customer Details
      log.info("E2E Step 6: Verifying Customer profile retrieval");
      HttpEntity<Void> getCustomerEntity = new HttpEntity<>(tenantHeaders);
      ResponseEntity<Map> getCustomerRes =
          restTemplate.exchange(
              "http://localhost:8084/api/v1/customers/" + customerId,
              HttpMethod.GET,
              getCustomerEntity,
              Map.class);
      assertThat(getCustomerRes.getStatusCode().is2xxSuccessful()).isTrue();
      assertThat(getCustomerRes.getBody().get("displayName")).isEqualTo("John Doe");

      // 7. Start Welcome Workflow (Create Lead)
      log.info("E2E Step 7: Submitting Lead trigger to start welcome workflow");
      Map<String, String> leadReq =
          Map.of(
              "firstName", "John",
              "lastName", "Doe",
              "email", "john.doe@example.com",
              "phone", "+15555554321");
      HttpEntity<Map<String, String>> leadEntity = new HttpEntity<>(leadReq, tenantHeaders);
      ResponseEntity<Map> leadRes =
          restTemplate.exchange(
              "http://localhost:8084/api/v1/leads", HttpMethod.POST, leadEntity, Map.class);
      assertThat(leadRes.getStatusCode().is2xxSuccessful()).isTrue();

      // 8. Observe Workflow Executions
      log.info("E2E Step 8: Observing Workflow executions");
      ResponseEntity<Map> executionsRes =
          restTemplate.exchange(
              "http://localhost:8090/api/v1/workflows/executions?limit=5",
              HttpMethod.GET,
              getCustomerEntity,
              Map.class);
      assertThat(executionsRes.getStatusCode().is2xxSuccessful()).isTrue();

      // 9. Fetch Customer Timeline
      log.info("E2E Step 9: Fetching Customer timeline events");
      ResponseEntity<Map> timelineRes =
          restTemplate.exchange(
              "http://localhost:8084/api/v1/customers/" + customerId + "/timeline",
              HttpMethod.GET,
              getCustomerEntity,
              Map.class);
      assertThat(timelineRes.getStatusCode().is2xxSuccessful()).isTrue();
      log.info("E2E E2E Live Platform verification check completed successfully!");

    } catch (Exception e) {
      log.error("Live E2E flow encountered an error", e);
      throw new RuntimeException("E2E verification failed: " + e.getMessage(), e);
    }
  }

  private String getOidcToken(String realm, String username, String password) {
    String url = "http://localhost:8080/realms/" + realm + "/protocol/openid-connect/token";
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "password");
    body.add("client_id", "admin-cli");
    body.add("username", username);
    body.add("password", password);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
    ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    return (String) response.getBody().get("access_token");
  }
}
