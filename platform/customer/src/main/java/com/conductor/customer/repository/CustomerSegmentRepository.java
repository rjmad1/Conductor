package com.conductor.customer.repository;

import com.conductor.customer.domain.CustomerSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerSegmentRepository extends JpaRepository<CustomerSegment, UUID> {

    List<CustomerSegment> findByCustomerId(UUID customerId);

    List<CustomerSegment> findBySegmentId(UUID segmentId);

    Optional<CustomerSegment> findByCustomerIdAndSegmentId(UUID customerId, UUID segmentId);

    boolean existsByCustomerIdAndSegmentId(UUID customerId, UUID segmentId);

    void deleteByCustomerIdAndSegmentId(UUID customerId, UUID segmentId);

    void deleteAllByCustomerId(UUID customerId);

    @Query("SELECT cs.customerId FROM CustomerSegment cs WHERE cs.segmentId = :segmentId")
    List<UUID> findCustomerIdsBySegmentId(@Param("segmentId") UUID segmentId);

    long countBySegmentId(UUID segmentId);
}
