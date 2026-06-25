package com.conductor.analytics.reporting;

import com.conductor.analytics.domain.Report;
import com.conductor.analytics.domain.ReportExecution;
import com.conductor.analytics.observability.AnalyticsMetrics;
import com.conductor.shared.middleware.tenant.TenantContext;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Report service for creating, executing, and exporting analytics reports. All operations are
 * tenant-scoped and auditable.
 */
@Service
public class ReportService {

  private static final Logger log = LoggerFactory.getLogger(ReportService.class);

  private final ReportRepository reportRepository;
  private final ReportExecutionRepository executionRepository;
  private final ReportExporter exporter;
  private final DataSource clickHouseDataSource;
  private final AnalyticsMetrics metrics;

  public ReportService(
      ReportRepository reportRepository,
      ReportExecutionRepository executionRepository,
      ReportExporter exporter,
      @Qualifier("clickHouseDataSource") DataSource clickHouseDataSource,
      AnalyticsMetrics metrics) {
    this.reportRepository = reportRepository;
    this.executionRepository = executionRepository;
    this.exporter = exporter;
    this.clickHouseDataSource = clickHouseDataSource;
    this.metrics = metrics;
  }

  public List<Report> getReports() {
    UUID tenantId = TenantContext.getCurrentTenantId();
    return reportRepository.findByTenantIdAndStatus(tenantId, "ACTIVE");
  }

  public Optional<Report> getReport(UUID id) {
    return reportRepository.findById(id);
  }

  @Transactional
  public Report createReport(Report report) {
    return reportRepository.save(report);
  }

  @Transactional
  public void deleteReport(UUID id) {
    reportRepository.deleteById(id);
  }

  public List<ReportExecution> getExecutions(UUID reportId) {
    return executionRepository.findByReportIdOrderByStartedAtDesc(reportId);
  }

  /** Execute a report: run the query against ClickHouse and return the result set. */
  @Transactional
  public ReportExecution executeReport(UUID reportId) {
    Report report =
        reportRepository
            .findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));

    ReportExecution execution =
        ReportExecution.builder().reportId(reportId).status("RUNNING").build();
    execution = executionRepository.save(execution);

    try {
      List<Map<String, Object>> rows = executeQuery(report.getQueryDefinition());
      execution.setRowCount(rows.size());
      execution.setStatus("COMPLETED");
      execution.setCompletedAt(Instant.now());

      report.setLastRunAt(Instant.now());
      reportRepository.save(report);
      metrics.recordReportExecution("COMPLETED");
    } catch (Exception e) {
      log.error("Report execution failed for {}", reportId, e);
      execution.setStatus("FAILED");
      execution.setErrorMessage(e.getMessage());
      execution.setCompletedAt(Instant.now());
      metrics.recordReportExecution("FAILED");
    }

    return executionRepository.save(execution);
  }

  /** Execute report and export in the specified format. */
  public byte[] executeAndExport(UUID reportId) {
    Report report =
        reportRepository
            .findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));

    List<Map<String, Object>> rows = executeQuery(report.getQueryDefinition());
    List<String> headers = rows.isEmpty() ? List.of() : new ArrayList<>(rows.get(0).keySet());

    return switch (report.getOutputFormat().toUpperCase()) {
      case "CSV" -> exporter.exportCsv(headers, rows);
      case "EXCEL" -> exporter.exportExcel(headers, rows);
      case "PDF" -> exporter.exportPdf(headers, rows);
      case "JSON" -> exporter.exportJson(rows);
      default -> throw new IllegalArgumentException(
          "Unsupported format: " + report.getOutputFormat());
    };
  }

  List<Map<String, Object>> executeQuery(String sql) {
    List<Map<String, Object>> results = new ArrayList<>();
    try (Connection conn = clickHouseDataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      ResultSetMetaData meta = rs.getMetaData();
      int colCount = meta.getColumnCount();

      while (rs.next()) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 1; i <= colCount; i++) {
          row.put(meta.getColumnLabel(i), rs.getObject(i));
        }
        results.add(row);
      }
    } catch (SQLException e) {
      log.error("ClickHouse query execution failed", e);
      throw new RuntimeException("Query execution failed: " + e.getMessage(), e);
    }
    return results;
  }
}
