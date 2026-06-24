package com.conductor.integrations.repository;

import com.conductor.integrations.domain.Connector;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ConnectorRepository extends JpaRepository<Connector, UUID> {
    Optional<Connector> findByType(String type);
}
