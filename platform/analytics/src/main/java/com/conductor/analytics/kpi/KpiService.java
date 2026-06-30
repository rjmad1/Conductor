package com.conductor.analytics.kpi;

import com.conductor.analytics.domain.KpiDefinition;
import com.conductor.analytics.domain.KpiValue;
import com.conductor.shared.middleware.tenant.TenantContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD operations for KPI definitions and evaluation access. */
@Service
public class KpiService {

  private final KpiDefinitionRepository repository;
  private final KpiEngine engine;

  public KpiService(KpiDefinitionRepository repository, KpiEngine engine) {
    this.repository = repository;
    this.engine = engine;
  }

  public List<KpiDefinition> getActiveKpis() {
    UUID tenantId = TenantContext.getCurrentTenantId();
    if (tenantId != null) {
      return repository.findByTenantIdAndStatus(tenantId, "ACTIVE");
    }
    return repository.findByStatus("ACTIVE");
  }

  public Optional<KpiDefinition> getKpi(UUID id) {
    return repository.findById(id);
  }

  @Transactional
  public KpiDefinition createKpi(KpiDefinition definition) {
    return repository.save(definition);
  }

  @Transactional
  public KpiDefinition updateKpi(UUID id, KpiDefinition updated) {
    KpiDefinition existing =
        repository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("KPI not found: " + id));
    existing.setName(updated.getName());
    existing.setDescription(updated.getDescription());
    existing.setMetricName(updated.getMetricName());
    existing.setAggregation(updated.getAggregation());
    existing.setThresholdWarning(updated.getThresholdWarning());
    existing.setThresholdCritical(updated.getThresholdCritical());
    existing.setOwner(updated.getOwner());
    existing.setStatus(updated.getStatus());
    return repository.save(existing);
  }

  @Transactional
  public void deleteKpi(UUID id) {
    repository.deleteById(id);
  }

  public List<KpiValue> evaluateCurrentKpis() {
    return engine.evaluateAll();
  }
}
