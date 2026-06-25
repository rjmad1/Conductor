package com.conductor.integrations.repository;

import com.conductor.integrations.domain.Credential;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialRepository extends JpaRepository<Credential, UUID> {
  Optional<Credential> findByIntegrationId(UUID integrationId);

  Optional<Credential> findByIntegrationIdAndTenantId(UUID integrationId, UUID tenantId);
}
