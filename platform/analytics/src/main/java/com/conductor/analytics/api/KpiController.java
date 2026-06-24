package com.conductor.analytics.api;

import com.conductor.analytics.domain.KpiDefinition;
import com.conductor.analytics.domain.KpiValue;
import com.conductor.analytics.kpi.KpiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API for KPI definitions and current values.
 */
@RestController
@RequestMapping("/api/v1/analytics/kpis")
public class KpiController {

    private final KpiService kpiService;

    public KpiController(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    @GetMapping
    public ResponseEntity<List<KpiDefinition>> list() {
        return ResponseEntity.ok(kpiService.getActiveKpis());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KpiDefinition> get(@PathVariable UUID id) {
        return kpiService.getKpi(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<KpiDefinition> create(@RequestBody KpiDefinition kpi) {
        return ResponseEntity.ok(kpiService.createKpi(kpi));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KpiDefinition> update(@PathVariable UUID id, @RequestBody KpiDefinition kpi) {
        return ResponseEntity.ok(kpiService.updateKpi(id, kpi));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        kpiService.deleteKpi(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/current")
    public ResponseEntity<List<KpiValue>> getCurrentValues() {
        return ResponseEntity.ok(kpiService.evaluateCurrentKpis());
    }
}
