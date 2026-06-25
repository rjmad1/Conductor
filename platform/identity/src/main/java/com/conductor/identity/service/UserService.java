package com.conductor.identity.service;

import com.conductor.identity.domain.APIKey;
import com.conductor.identity.domain.Membership;
import com.conductor.identity.domain.User;
import com.conductor.identity.repository.APIKeyRepository;
import com.conductor.identity.repository.MembershipRepository;
import com.conductor.identity.repository.UserRepository;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import com.conductor.shared.middleware.tenant.TenantContext;
import com.conductor.tenant.service.KeycloakAdminService;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);
  private static final SecureRandom secureRandom = new SecureRandom();
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  private final UserRepository userRepository;
  private final MembershipRepository membershipRepository;
  private final APIKeyRepository apiKeyRepository;
  private final KeycloakAdminService keycloakAdminService;
  private final NatsEventPublisher eventPublisher;
  private final AuditLogger auditLogger;

  public UserService(
      UserRepository userRepository,
      MembershipRepository membershipRepository,
      APIKeyRepository apiKeyRepository,
      KeycloakAdminService keycloakAdminService,
      NatsEventPublisher eventPublisher,
      AuditLogger auditLogger) {
    this.userRepository = userRepository;
    this.membershipRepository = membershipRepository;
    this.apiKeyRepository = apiKeyRepository;
    this.keycloakAdminService = keycloakAdminService;
    this.eventPublisher = eventPublisher;
    this.auditLogger = auditLogger;
  }

  @Transactional
  public User createUser(String email, String password) {
    log.info("Creating user email={}", email);
    if (userRepository.findByEmail(email).isPresent()) {
      throw new IllegalArgumentException("User email already exists: " + email);
    }

    UUID tenantId = TenantContext.getCurrentTenantId();
    if (tenantId == null) {
      throw new IllegalStateException("Cannot create user without active tenant context");
    }

    String realmName = "conductor-" + tenantId;
    String keycloakId = keycloakAdminService.createUser(realmName, email, email, password);

    User user = new User();
    user.setEmail(email);
    user.setKeycloakId(keycloakId);
    user.setStatus("ACTIVE");
    user.setCreatedAt(Instant.now());
    user.setUpdatedAt(Instant.now());

    User savedUser = userRepository.save(user);

    // Assign default Membership role (e.g. READ_ONLY_USER)
    Membership membership = new Membership();
    membership.setUserId(savedUser.getId());
    membership.setTenantId(tenantId);
    membership.setRole("Read Only User");
    membershipRepository.save(membership);

    keycloakAdminService.assignRoleToUser(realmName, keycloakId, "Read Only User");

    eventPublisher.publishEvent(
        "identity",
        "user",
        "created",
        String.format(
            "{\"id\":\"%s\",\"email\":\"%s\",\"tenantId\":\"%s\"}",
            savedUser.getId(), email, tenantId));

    auditLogger.logEvent(
        "CREATE",
        "USER:" + savedUser.getId(),
        "SUCCESS",
        "User created and default membership assigned");

    return savedUser;
  }

  @Transactional
  public User inviteUser(String email, String role) {
    log.info("Inviting user email={} role={}", email, role);
    UUID tenantId = TenantContext.getCurrentTenantId();
    if (tenantId == null) {
      throw new IllegalStateException("Cannot invite user without active tenant context");
    }

    Optional<User> existingUserOpt = userRepository.findByEmail(email);
    User user;
    if (existingUserOpt.isPresent()) {
      user = existingUserOpt.get();
    } else {
      // Provision keycloak skeleton user with temporary random password
      String realmName = "conductor-" + tenantId;
      String tempPassword = UUID.randomUUID().toString();
      String keycloakId = keycloakAdminService.createUser(realmName, email, email, tempPassword);

      user = new User();
      user.setEmail(email);
      user.setKeycloakId(keycloakId);
      user.setStatus("INVITED");
      user.setCreatedAt(Instant.now());
      user.setUpdatedAt(Instant.now());
      user = userRepository.save(user);
    }

    Membership membership = new Membership();
    membership.setUserId(user.getId());
    membership.setTenantId(tenantId);
    membership.setRole(role);
    membershipRepository.save(membership);

    String realmName = "conductor-" + tenantId;
    keycloakAdminService.assignRoleToUser(realmName, user.getKeycloakId(), role);

    eventPublisher.publishEvent(
        "identity",
        "user",
        "invited",
        String.format(
            "{\"id\":\"%s\",\"email\":\"%s\",\"role\":\"%s\"}", user.getId(), email, role));

    auditLogger.logEvent(
        "INVITE", "USER:" + user.getId(), "SUCCESS", "User invited with role: " + role);

    return user;
  }

  @Transactional
  public void assignRole(UUID userId, String role) {
    UUID tenantId = TenantContext.getCurrentTenantId();
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    Optional<Membership> membershipOpt = membershipRepository.findByUserIdAndRole(userId, role);
    if (membershipOpt.isPresent()) {
      return; // Role already assigned
    }

    Membership membership = new Membership();
    membership.setUserId(userId);
    membership.setTenantId(tenantId);
    membership.setRole(role);
    membershipRepository.save(membership);

    String realmName = "conductor-" + tenantId;
    keycloakAdminService.assignRoleToUser(realmName, user.getKeycloakId(), role);

    eventPublisher.publishEvent(
        "identity",
        "role",
        "assigned",
        String.format("{\"userId\":\"%s\",\"role\":\"%s\"}", userId, role));

    auditLogger.logEvent("ASSIGN_ROLE", "USER:" + userId, "SUCCESS", "Role assigned: " + role);
  }

  @Transactional
  public String generateApiKey(UUID userId, String scopes, Instant expiresAt) {
    UUID tenantId = TenantContext.getCurrentTenantId();

    // Generate plaintext API key prefix + random secure bytes
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    String randomStr = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    String prefix = "cond_live_";
    String plaintextKey = prefix + randomStr;

    // Hash plaintext key
    String keyHash = passwordEncoder.encode(plaintextKey);

    APIKey apiKey = new APIKey();
    apiKey.setUserId(userId);
    apiKey.setTenantId(tenantId);
    apiKey.setPrefix(prefix);
    apiKey.setKeyHash(keyHash);
    apiKey.setScopes(scopes);
    apiKey.setExpiresAt(expiresAt);
    apiKey.setCreatedAt(Instant.now());

    apiKeyRepository.save(apiKey);

    auditLogger.logEvent("CREATE_API_KEY", "USER:" + userId, "SUCCESS", "API key generated");
    return plaintextKey;
  }

  public Optional<APIKey> validateApiKey(String plaintextKey) {
    // Exclude prefix and find in DB
    String hashedCompare = plaintextKey;
    // Verify key matching
    Optional<APIKey> apiKeyOpt =
        apiKeyRepository.findAll().stream()
            .filter(k -> passwordEncoder.matches(plaintextKey, k.getKeyHash()))
            .findFirst();

    if (apiKeyOpt.isPresent()) {
      APIKey key = apiKeyOpt.get();
      if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(Instant.now())) {
        log.warn("API key has expired: {}", key.getId());
        return Optional.empty();
      }
      return Optional.of(key);
    }
    return Optional.empty();
  }
}
