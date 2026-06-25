package com.conductor.referenceapp.api;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reference")
public class ReferenceAppController {

  private static final Logger log = LoggerFactory.getLogger(ReferenceAppController.class);

  @Value("${keycloak.server-url:http://localhost:8080}")
  private String keycloakServerUrl;

  @Value("${keycloak.admin.username:admin}")
  private String adminUsername;

  @Value("${keycloak.admin.password:admin_password}")
  private String adminPassword;

  @PostMapping("/bootstrap-user")
  public ResponseEntity<Map<String, String>> bootstrapUser(
      @RequestBody Map<String, String> request) {
    String tenantIdStr = request.get("tenantId");
    String email = request.get("email");
    String password = request.get("password");

    if (tenantIdStr == null || email == null || password == null) {
      return ResponseEntity.badRequest()
          .body(Map.of("error", "tenantId, email, and password are required"));
    }

    String realmName = "conductor-" + tenantIdStr;
    log.info("Bootstrapping tenant admin user in realm: {}", realmName);

    try (Keycloak keycloak =
        KeycloakBuilder.builder()
            .serverUrl(keycloakServerUrl)
            .realm("master")
            .clientId("admin-cli")
            .username(adminUsername)
            .password(adminPassword)
            .build()) {

      // Create User representation
      UserRepresentation user = new UserRepresentation();
      user.setUsername(email);
      user.setEmail(email);
      user.setEnabled(true);

      CredentialRepresentation cred = new CredentialRepresentation();
      cred.setType(CredentialRepresentation.PASSWORD);
      cred.setValue(password);
      cred.setTemporary(false);
      user.setCredentials(Collections.singletonList(cred));

      // Create user
      try (Response response = keycloak.realm(realmName).users().create(user)) {
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
          if (response.getStatus() == 409) {
            log.info("User {} already exists in realm {}", email, realmName);
          } else {
            throw new RuntimeException(
                "Failed to create Keycloak user, status: " + response.getStatus());
          }
        }
      }

      // Find user ID
      List<UserRepresentation> foundUsers = keycloak.realm(realmName).users().search(email, true);
      if (foundUsers.isEmpty()) {
        throw new RuntimeException("Failed to find created user in Keycloak");
      }
      String keycloakUserId = foundUsers.get(0).getId();

      // Assign role "Tenant Admin"
      RoleRepresentation role =
          keycloak.realm(realmName).roles().get("Tenant Admin").toRepresentation();
      keycloak
          .realm(realmName)
          .users()
          .get(keycloakUserId)
          .roles()
          .realmLevel()
          .add(Collections.singletonList(role));

      log.info("Tenant admin user {} bootstrapped successfully in realm {}", email, realmName);
      return ResponseEntity.ok(Map.of("status", "SUCCESS", "userId", keycloakUserId));
    } catch (Exception e) {
      log.error("Bootstrap user failed for realm {}", realmName, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
    }
  }
}
