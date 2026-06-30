package com.conductor.analytics.api;

import com.conductor.analytics.dashboard.DashboardService;
import com.conductor.analytics.dashboard.MetabaseEmbedService;
import com.conductor.analytics.domain.Dashboard;
import com.conductor.analytics.observability.AnalyticsMetrics;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST API for analytics dashboard management and Metabase embedding. */
@RestController
@RequestMapping("/api/v1/analytics/dashboards")
public class DashboardController {

  private final DashboardService dashboardService;
  private final MetabaseEmbedService metabaseEmbedService;
  private final AnalyticsMetrics metrics;

  public DashboardController(
      DashboardService dashboardService,
      MetabaseEmbedService metabaseEmbedService,
      AnalyticsMetrics metrics) {
    this.dashboardService = dashboardService;
    this.metabaseEmbedService = metabaseEmbedService;
    this.metrics = metrics;
  }

  @GetMapping
  @PreAuthorize(
      "hasAnyAuthority('ROLE_TENANT_OWNER', 'ROLE_TENANT_ADMIN', 'ROLE_TENANT_AGENT', 'ROLE_PLATFORM_ADMIN')")
  public ResponseEntity<List<Dashboard>> list() {
    return ResponseEntity.ok(dashboardService.getDashboards());
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      "hasAnyAuthority('ROLE_TENANT_OWNER', 'ROLE_TENANT_ADMIN', 'ROLE_TENANT_AGENT', 'ROLE_PLATFORM_ADMIN')")
  public ResponseEntity<Dashboard> get(@PathVariable UUID id) {
    metrics.recordDashboardView();
    return dashboardService
        .getDashboard(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  @PreAuthorize("hasAnyAuthority('ROLE_TENANT_OWNER', 'ROLE_TENANT_ADMIN', 'ROLE_PLATFORM_ADMIN')")
  public ResponseEntity<Dashboard> create(@RequestBody Dashboard dashboard) {
    return ResponseEntity.ok(dashboardService.createDashboard(dashboard));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('ROLE_TENANT_OWNER', 'ROLE_TENANT_ADMIN', 'ROLE_PLATFORM_ADMIN')")
  public ResponseEntity<Dashboard> update(@PathVariable UUID id, @RequestBody Dashboard dashboard) {
    return ResponseEntity.ok(dashboardService.updateDashboard(id, dashboard));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('ROLE_TENANT_OWNER', 'ROLE_TENANT_ADMIN', 'ROLE_PLATFORM_ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    dashboardService.deleteDashboard(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/embed-url")
  @PreAuthorize(
      "hasAnyAuthority('ROLE_TENANT_OWNER', 'ROLE_TENANT_ADMIN', 'ROLE_TENANT_AGENT', 'ROLE_PLATFORM_ADMIN')")
  public ResponseEntity<Map<String, String>> getEmbedUrl(
      @PathVariable UUID id, @RequestParam int metabaseDashboardId) {
    String url = metabaseEmbedService.generateEmbedUrl(metabaseDashboardId);
    return ResponseEntity.ok(Map.of("embedUrl", url));
  }
}
