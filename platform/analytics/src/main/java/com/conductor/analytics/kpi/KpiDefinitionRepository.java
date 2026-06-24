package com.conductor.analytics.kpi;

import com.conductor.analytics.domain.KpiDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KpiDefinitionRepository extends JpaRepository<KpiDefinition, UUID> {

    List<KpiDefinition> findByTenantIdAndStatus(UUID tenantId, String status);

    List<KpiDefinition> findByStatus(String status);
}
