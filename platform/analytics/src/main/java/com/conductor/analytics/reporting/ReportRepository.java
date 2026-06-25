package com.conductor.analytics.reporting;

import com.conductor.analytics.domain.Report;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

  List<Report> findByTenantIdAndStatus(UUID tenantId, String status);

  List<Report> findByReportTypeAndStatus(String reportType, String status);
}
