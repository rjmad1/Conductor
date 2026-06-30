package com.conductor.customer.repository;

import com.conductor.customer.domain.CustomerPreference;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPreferenceRepository extends JpaRepository<CustomerPreference, UUID> {

  List<CustomerPreference> findByCustomerId(UUID customerId);

  Optional<CustomerPreference> findByCustomerIdAndChannel(UUID customerId, String channel);

  void deleteAllByCustomerId(UUID customerId);
}
