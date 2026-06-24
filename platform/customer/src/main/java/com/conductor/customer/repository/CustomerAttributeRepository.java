package com.conductor.customer.repository;

import com.conductor.customer.domain.CustomerAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerAttributeRepository extends JpaRepository<CustomerAttribute, UUID> {

    List<CustomerAttribute> findByCustomerId(UUID customerId);

    Optional<CustomerAttribute> findByCustomerIdAndKey(UUID customerId, String key);

    void deleteAllByCustomerId(UUID customerId);
}
