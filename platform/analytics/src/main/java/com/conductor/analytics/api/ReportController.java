package com.conductor.analytics.api;

import com.conductor.analytics.domain.Report;
import com.conductor.analytics.domain.ReportExecution;
import com.conductor.analytics.reporting.ReportService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** REST API for analytics report management and execution. */
@RestController
@RequestMapping("/api/v1/analytics/reports")
public class ReportController {

  private final ReportService reportService;

  public ReportController(ReportService reportService) {
    this.reportService = reportService;
  }

  @GetMapping
  public ResponseEntity<List<Report>> list() {
    return ResponseEntity.ok(reportService.getReports());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Report> get(@PathVariable UUID id) {
    return reportService
        .getReport(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Report> create(@RequestBody Report report) {
    return ResponseEntity.ok(reportService.createReport(report));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    reportService.deleteReport(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/execute")
  public ResponseEntity<ReportExecution> execute(@PathVariable UUID id) {
    return ResponseEntity.ok(reportService.executeReport(id));
  }

  @GetMapping("/{id}/executions")
  public ResponseEntity<List<ReportExecution>> getExecutions(@PathVariable UUID id) {
    return ResponseEntity.ok(reportService.getExecutions(id));
  }

  @GetMapping("/{id}/download")
  public ResponseEntity<byte[]> download(@PathVariable UUID id) {
    Report report =
        reportService
            .getReport(id)
            .orElseThrow(() -> new IllegalArgumentException("Report not found: " + id));

    byte[] data = reportService.executeAndExport(id);

    String contentType =
        switch (report.getOutputFormat().toUpperCase()) {
          case "CSV" -> "text/csv";
          case "EXCEL" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
          case "PDF" -> "application/pdf";
          default -> "application/json";
        };

    String extension =
        switch (report.getOutputFormat().toUpperCase()) {
          case "CSV" -> ".csv";
          case "EXCEL" -> ".xlsx";
          case "PDF" -> ".pdf";
          default -> ".json";
        };

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-" + id + extension)
        .body(data);
  }
}
