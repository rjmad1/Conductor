package com.conductor.identity.repository;

import com.conductor.identity.domain.Membership;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {
  List<Membership> findByUserId(UUID userId);

  Optional<Membership> findByUserIdAndRole(UUID userId, String role);
}
