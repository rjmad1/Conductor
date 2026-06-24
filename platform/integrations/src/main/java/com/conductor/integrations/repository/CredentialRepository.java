package com.conductor.integrations.repository;

import com.conductor.integrations.domain.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CredentialRepository extends JpaRepository<Credential, UUID> {
    Optional<Credential> findByIntegrationId(UUID integrationId);
    Optional<Credential> findByIntegrationIdAndTenantId(UUID integrationId, UUID tenantId);
}
