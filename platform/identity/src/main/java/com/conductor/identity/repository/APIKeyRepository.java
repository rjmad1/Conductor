package com.conductor.identity.repository;

import com.conductor.identity.domain.APIKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface APIKeyRepository extends JpaRepository<APIKey, UUID> {
    
    // Native query is used to bypass Hibernate filters, as we do not yet have a tenant context when validating an API Key
    @Query(value = "SELECT * FROM api_keys k WHERE k.key_hash = :keyHash", nativeQuery = true)
    Optional<APIKey> findByKeyHashGlobal(@Param("keyHash") String keyHash);
}
