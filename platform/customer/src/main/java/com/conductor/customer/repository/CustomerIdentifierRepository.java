package com.conductor.customer.repository;

import com.conductor.customer.domain.CustomerIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerIdentifierRepository extends JpaRepository<CustomerIdentifier, UUID> {

    /** O(1) identity resolution via unique index on (tenant_id, identifier_type, identifier_hash). */
    Optional<CustomerIdentifier> findByIdentifierTypeAndIdentifierHash(String identifierType, String identifierHash);

    List<CustomerIdentifier> findByCustomerId(UUID customerId);

    void deleteAllByCustomerId(UUID customerId);
}
