package com.conductor.tenant.repository;

import com.conductor.tenant.domain.Tenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
  Optional<Tenant> findByDomain(String domain);

  Optional<Tenant> findByTenantKey(String tenantKey);

  @Query(
      "SELECT t FROM Tenant t WHERE (:startingAfter IS NULL OR t.id > :startingAfter) AND t.status <> 'DELETED' ORDER BY t.id ASC")
  List<Tenant> findAllPaged(@Param("startingAfter") UUID startingAfter, Pageable pageable);
}
