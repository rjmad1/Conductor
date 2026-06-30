package com.conductor.shared.security;

/** Interface detailing methods for evaluating RBAC permissions and resource ownership. */
public interface AuthorizationService {

  /**
   * Evaluates if the current principal context possesses the requested permission scope.
   *
   * @param requiredPermission permission string (e.g. 'workflows:read')
   * @return true if access is permitted, false otherwise
   */
  boolean hasPermission(String requiredPermission);

  /**
   * Checks if the active caller context owns the resource.
   *
   * @param resourceTenantId tenant ID of the resource
   * @param activeTenantId active tenant ID resolved in the context
   * @return true if tenant IDs match, false otherwise
   */
  boolean isResourceOwner(String resourceTenantId, String activeTenantId);
}
