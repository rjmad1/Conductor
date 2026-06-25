package com.conductor.identity.api;

import com.conductor.identity.domain.User;
import com.conductor.identity.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity")
public class IdentityController {

  private final UserService userService;

  public IdentityController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/users")
  @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    User user = userService.createUser(request.email(), request.password());
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
  }

  @PostMapping("/users/invite")
  @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
  public ResponseEntity<UserResponse> inviteUser(@Valid @RequestBody InviteUserRequest request) {
    User user = userService.inviteUser(request.email(), request.role());
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(toResponse(user));
  }

  @PostMapping("/users/{id}/roles")
  @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
  public ResponseEntity<Void> assignRole(
      @PathVariable UUID id, @Valid @RequestBody AssignRoleRequest request) {
    userService.assignRole(id, request.role());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/api-keys")
  @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_TENANT_OWNER')")
  public ResponseEntity<ApiKeyResponse> generateApiKey(
      @Valid @RequestBody GenerateApiKeyRequest request) {
    String plaintextKey =
        userService.generateApiKey(request.userId(), request.scopes(), request.expiresAt());
    return ResponseEntity.status(HttpStatus.CREATED).body(new ApiKeyResponse(plaintextKey));
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(user.getId(), user.getEmail(), user.getStatus());
  }

  public record CreateUserRequest(@NotBlank @Email String email, @NotBlank String password) {}

  public record InviteUserRequest(@NotBlank @Email String email, @NotBlank String role) {}

  public record AssignRoleRequest(@NotBlank String role) {}

  public record GenerateApiKeyRequest(
      @Valid UUID userId, @NotBlank String scopes, Instant expiresAt) {}

  public record ApiKeyResponse(String apiKey) {}

  public record UserResponse(UUID id, String email, String status) {}
}
