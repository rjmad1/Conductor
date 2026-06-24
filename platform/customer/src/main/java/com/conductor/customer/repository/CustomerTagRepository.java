package com.conductor.customer.repository;

import com.conductor.customer.domain.CustomerTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerTagRepository extends JpaRepository<CustomerTag, UUID> {

    List<CustomerTag> findByCustomerId(UUID customerId);

    List<CustomerTag> findByTagId(UUID tagId);

    Optional<CustomerTag> findByCustomerIdAndTagId(UUID customerId, UUID tagId);

    boolean existsByCustomerIdAndTagId(UUID customerId, UUID tagId);

    void deleteByCustomerIdAndTagId(UUID customerId, UUID tagId);

    void deleteAllByCustomerId(UUID customerId);

    @Query("SELECT ct.customerId FROM CustomerTag ct WHERE ct.tagId = :tagId")
    List<UUID> findCustomerIdsByTagId(@Param("tagId") UUID tagId);
}
