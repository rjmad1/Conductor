package com.conductor.analytics.dashboard;

import com.conductor.analytics.domain.Dashboard;
import com.conductor.shared.middleware.tenant.TenantContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Dashboard CRUD service. All operations are tenant-scoped. */
@Service
public class DashboardService {

  private final DashboardRepository repository;

  public DashboardService(DashboardRepository repository) {
    this.repository = repository;
  }

  public List<Dashboard> getDashboards() {
    UUID tenantId = TenantContext.getCurrentTenantId();
    return repository.findByTenantIdAndStatus(tenantId, "ACTIVE");
  }

  public Optional<Dashboard> getDashboard(UUID id) {
    return repository.findById(id);
  }

  @Transactional
  public Dashboard createDashboard(Dashboard dashboard) {
    return repository.save(dashboard);
  }

  @Transactional
  public Dashboard updateDashboard(UUID id, Dashboard updated) {
    Dashboard existing =
        repository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Dashboard not found: " + id));
    existing.setName(updated.getName());
    existing.setDescription(updated.getDescription());
    existing.setLayoutJson(updated.getLayoutJson());
    existing.setStatus(updated.getStatus());
    return repository.save(existing);
  }

  @Transactional
  public void deleteDashboard(UUID id) {
    repository.deleteById(id);
  }
}
