package com.conductor.customer.repository;

import com.conductor.customer.domain.ConsentRecord;
import com.conductor.shared.customer.ConsentType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {

  List<ConsentRecord> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

  List<ConsentRecord> findByCustomerIdAndConsentTypeOrderByCreatedAtDesc(
      UUID customerId, ConsentType consentType);

  /**
   * Derives current consent status: returns the most recent record for the given customer and
   * consent type (action = GRANTED means currently opted in).
   */
  @Query(
      """
        SELECT c FROM ConsentRecord c
        WHERE c.customerId = :customerId
          AND c.consentType = :consentType
        ORDER BY c.createdAt DESC
        LIMIT 1
        """)
  Optional<ConsentRecord> findLatestByCustomerIdAndConsentType(
      @Param("customerId") UUID customerId, @Param("consentType") ConsentType consentType);
}
