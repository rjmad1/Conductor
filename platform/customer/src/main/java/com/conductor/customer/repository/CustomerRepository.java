package com.conductor.customer.repository;

import com.conductor.customer.domain.Customer;
import com.conductor.shared.customer.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByExternalId(String externalId);

    List<Customer> findByStatus(CustomerStatus status);

    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);

    List<Customer> findByMergedIntoId(UUID mergedIntoId);

    /**
     * Full-text search on display_name using PostgreSQL tsvector.
     * The GIN index on search_vector (created in V010) makes this fast.
     */
    @Query(value = """
        SELECT * FROM customers
        WHERE tenant_id = CAST(:tenantId AS uuid)
          AND status != 'DELETED'
          AND to_tsvector('english', COALESCE(display_name, '')) @@ plainto_tsquery('english', :query)
        ORDER BY ts_rank(to_tsvector('english', COALESCE(display_name, '')), plainto_tsquery('english', :query)) DESC
        """, nativeQuery = true)
    List<Customer> searchByDisplayName(@Param("tenantId") String tenantId, @Param("query") String query);

    @Query("SELECT c FROM Customer c WHERE c.status != 'DELETED'")
    Page<Customer> findAllActive(Pageable pageable);
}
