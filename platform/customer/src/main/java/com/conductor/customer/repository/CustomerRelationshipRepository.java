package com.conductor.customer.repository;

import com.conductor.customer.domain.CustomerRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CustomerRelationshipRepository extends JpaRepository<CustomerRelationship, UUID> {

    List<CustomerRelationship> findByFromCustomerId(UUID fromCustomerId);

    List<CustomerRelationship> findByToCustomerId(UUID toCustomerId);

    /** Fetch all relationships (both directions) for a given customer. */
    @Query("""
        SELECT r FROM CustomerRelationship r
        WHERE r.fromCustomerId = :customerId OR r.toCustomerId = :customerId
        """)
    List<CustomerRelationship> findAllByCustomerId(@Param("customerId") UUID customerId);

    void deleteAllByFromCustomerIdOrToCustomerId(UUID fromCustomerId, UUID toCustomerId);
}
