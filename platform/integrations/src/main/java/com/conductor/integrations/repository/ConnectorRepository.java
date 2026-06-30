package com.conductor.integrations.repository;

import com.conductor.integrations.domain.Connector;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectorRepository extends JpaRepository<Connector, UUID> {
  Optional<Connector> findByType(String type);
}
