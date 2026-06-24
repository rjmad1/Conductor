package com.conductor.customer.repository;

import com.conductor.customer.domain.CustomerContact;
import com.conductor.shared.customer.ContactType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerContactRepository extends JpaRepository<CustomerContact, UUID> {

    List<CustomerContact> findByCustomerId(UUID customerId);

    List<CustomerContact> findByCustomerIdAndType(UUID customerId, ContactType type);

    Optional<CustomerContact> findByCustomerIdAndTypeAndIsPrimaryTrue(UUID customerId, ContactType type);

    /** Identity resolution: find a customer contact by hashed value. */
    Optional<CustomerContact> findByTypeAndValueHash(ContactType type, String valueHash);

    /** Used to enforce one primary per customer per type before setting a new one. */
    List<CustomerContact> findByCustomerIdAndIsPrimaryTrue(UUID customerId);

    void deleteAllByCustomerId(UUID customerId);

    @Query("SELECT c FROM CustomerContact c WHERE c.customerId IN :customerIds")
    List<CustomerContact> findByCustomerIdIn(@Param("customerIds") List<UUID> customerIds);
}
