package com.conductor.analytics.reporting;

import com.conductor.analytics.domain.ReportExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportExecutionRepository extends JpaRepository<ReportExecution, UUID> {

    List<ReportExecution> findByReportIdOrderByStartedAtDesc(UUID reportId);
}
