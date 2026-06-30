package com.conductor.analytics.reporting;

import com.conductor.analytics.domain.Report;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task that triggers execution of reports marked as SCHEDULED. Runs every minute and
 * checks for due reports based on their cron expression.
 *
 * <p>ponytail: Simplified cron matching — checks all SCHEDULED reports on each tick. For production
 * scale, implement a proper cron evaluator or use Spring's CronExpression.
 */
@Component
public class ReportScheduler {

  private static final Logger log = LoggerFactory.getLogger(ReportScheduler.class);
  private final ReportRepository reportRepository;
  private final ReportService reportService;

  public ReportScheduler(ReportRepository reportRepository, ReportService reportService) {
    this.reportRepository = reportRepository;
    this.reportService = reportService;
  }

  @Scheduled(fixedDelayString = "${analytics.report.scheduler-interval-ms:60000}")
  public void checkScheduledReports() {
    List<Report> scheduled = reportRepository.findByReportTypeAndStatus("SCHEDULED", "ACTIVE");
    for (Report report : scheduled) {
      if (report.getScheduleCron() != null && !report.getScheduleCron().isBlank()) {
        try {
          // ponytail: Simple interval-based triggering. Full cron evaluation deferred.
          reportService.executeReport(report.getId());
          log.info("Executed scheduled report: {}", report.getName());
        } catch (Exception e) {
          log.error("Failed to execute scheduled report: {}", report.getName(), e);
        }
      }
    }
  }
}
