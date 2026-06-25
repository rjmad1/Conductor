package com.conductor.identity.repository;

import com.conductor.identity.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByKeycloakId(String keycloakId);

  Optional<User> findByEmail(String email);
}
