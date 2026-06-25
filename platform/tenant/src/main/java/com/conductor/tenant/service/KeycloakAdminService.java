package com.conductor.tenant.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.conductor.shared.auth.KeycloakUserManager;
import java.util.Collections;
import java.util.List;
import jakarta.ws.rs.core.Response;

@Service
public class KeycloakAdminService implements KeycloakUserManager {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminService.class);

    private final String serverUrl;
    private final String adminUsername;
    private final String adminPassword;

    public KeycloakAdminService(
            @Value("${keycloak.server-url:http://localhost:8080}") String serverUrl,
            @Value("${keycloak.admin.username:admin}") String adminUsername,
            @Value("${keycloak.admin.password:admin_password}") String adminPassword) {
        this.serverUrl = serverUrl;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    private Keycloak getAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    public void createTenantRealm(String realmName) {
        log.info("Provisioning Keycloak Realm: {}", realmName);
        Keycloak keycloak = getAdminClient();

        RealmRepresentation realm = new RealmRepresentation();
        realm.setId(realmName);
        realm.setRealm(realmName);
        realm.setEnabled(true);

        try {
            keycloak.realms().create(realm);
            log.info("Realm '{}' created successfully", realmName);
        } catch (Exception e) {
            log.error("Failed to create Keycloak Realm '{}'", realmName, e);
            throw new RuntimeException("Keycloak realm creation failed: " + e.getMessage(), e);
        }
    }

    public void createClient(String realmName, String clientId, boolean publicClient, String secret) {
        log.info("Provisioning Client '{}' in Realm '{}'", clientId, realmName);
        Keycloak keycloak = getAdminClient();

        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(clientId);
        client.setEnabled(true);
        if (publicClient) {
            client.setPublicClient(true);
            client.setRedirectUris(Collections.singletonList("*"));
        } else {
            client.setPublicClient(false);
            client.setSecret(secret);
            client.setServiceAccountsEnabled(true);
        }

        try {
            Response response = keycloak.realm(realmName).clients().create(client);
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new RuntimeException("Keycloak returned status " + response.getStatus());
            }
            log.info("Client '{}' provisioned successfully", clientId);
        } catch (Exception e) {
            log.error("Failed to provision client '{}' in realm '{}'", clientId, realmName, e);
            throw new RuntimeException("Client creation failed: " + e.getMessage(), e);
        }
    }

    public void createRealmRole(String realmName, String roleName) {
        log.info("Creating Realm Role '{}' in Realm '{}'", roleName, realmName);
        Keycloak keycloak = getAdminClient();

        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);

        try {
            keycloak.realm(realmName).roles().create(role);
            log.info("Role '{}' created successfully", roleName);
        } catch (Exception e) {
            log.error("Failed to create role '{}' in realm '{}'", roleName, realmName, e);
            throw new RuntimeException("Role creation failed: " + e.getMessage(), e);
        }
    }

    public String createUser(String realmName, String username, String email, String password) {
        log.info("Creating User '{}' in Realm '{}'", username, realmName);
        Keycloak keycloak = getAdminClient();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(password);
        cred.setTemporary(false);
        user.setCredentials(Collections.singletonList(cred));

        try (Response response = keycloak.realm(realmName).users().create(user)) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new RuntimeException("Keycloak returned status " + response.getStatus());
            }
            // Retrieve created user ID from header
            String path = response.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf('/') + 1);
            log.info("User '{}' created with Keycloak ID '{}'", username, userId);
            return userId;
        } catch (Exception e) {
            log.error("Failed to create user '{}' in realm '{}'", username, realmName, e);
            throw new RuntimeException("User creation failed: " + e.getMessage(), e);
        }
    }

    public void assignRoleToUser(String realmName, String keycloakUserId, String roleName) {
        log.info("Assigning role '{}' to user '{}' in realm '{}'", roleName, keycloakUserId, realmName);
        Keycloak keycloak = getAdminClient();

        try {
            RoleRepresentation role = keycloak.realm(realmName).roles().get(roleName).toRepresentation();
            keycloak.realm(realmName).users().get(keycloakUserId).roles().realmLevel().add(Collections.singletonList(role));
            log.info("Role '{}' assigned successfully", roleName);
        } catch (Exception e) {
            log.error("Failed to assign role '{}' to user '{}' in realm '{}'", roleName, keycloakUserId, realmName, e);
            throw new RuntimeException("Role assignment failed: " + e.getMessage(), e);
        }
    }

    public void provisionDefaultRoles(String realmName) {
        log.info("Provisioning standard RBAC roles for realm: {}", realmName);
        List<String> defaultRoles = List.of(
            "Platform Admin",
            "Tenant Admin",
            "Workflow Admin",
            "Operations Manager",
            "Support Agent",
            "Read Only User",
            "API Client",
            "Service Account"
        );
        for (String role : defaultRoles) {
            try {
                createRealmRole(realmName, role);
            } catch (Exception e) {
                log.warn("Role '{}' already exists or failed to create in realm '{}': {}", role, realmName, e.getMessage());
            }
        }
    }
}
