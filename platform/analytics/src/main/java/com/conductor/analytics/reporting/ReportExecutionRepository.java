package com.conductor.analytics.reporting;

import com.conductor.analytics.domain.ReportExecution;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportExecutionRepository extends JpaRepository<ReportExecution, UUID> {

  List<ReportExecution> findByReportIdOrderByStartedAtDesc(UUID reportId);
}
