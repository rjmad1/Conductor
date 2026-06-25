package com.conductor.shared.auth;

/**
 * Contract for Keycloak user provisioning operations used by the identity module.
 * Implemented by KeycloakAdminService in platform:tenant.
 * This interface exists to break the compile-time dependency between platform:identity
 * and platform:tenant.
 */
public interface KeycloakUserManager {

    /**
     * Creates a user in the given Keycloak realm and returns the Keycloak user ID.
     *
     * @param realmName   the target realm (e.g. "conductor-{tenantId}")
     * @param email       the user email address
     * @param username    the username (typically same as email)
     * @param password    the initial password
     * @return the Keycloak-assigned user ID
     */
    String createUser(String realmName, String email, String username, String password);

    /**
     * Assigns a realm role to a user.
     *
     * @param realmName   the target realm
     * @param keycloakId  the Keycloak user ID
     * @param roleName    the role name to assign
     */
    void assignRoleToUser(String realmName, String keycloakId, String roleName);
}
