package com.conductor.analytics.dashboard;

import com.conductor.analytics.domain.Dashboard;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, UUID> {

  List<Dashboard> findByTenantIdAndStatus(UUID tenantId, String status);
}
