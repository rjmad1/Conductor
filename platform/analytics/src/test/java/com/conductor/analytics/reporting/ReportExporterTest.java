package com.conductor.analytics.reporting;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for report export formats (CSV, Excel, JSON, PDF). */
class ReportExporterTest {

  private ReportExporter exporter;
  private List<String> headers;
  private List<Map<String, Object>> rows;

  @BeforeEach
  void setUp() {
    exporter = new ReportExporter();
    headers = List.of("tenant_id", "metric", "value");
    rows =
        List.of(
            Map.of("tenant_id", "t-1", "metric", "workflow_success", "value", 95.5),
            Map.of("tenant_id", "t-2", "metric", "delivery_rate", "value", 88.0));
  }

  @Test
  void exportCsvProducesValidOutput() {
    byte[] csv = exporter.exportCsv(headers, rows);
    assertNotNull(csv);
    String content = new String(csv);
    assertTrue(content.contains("tenant_id"));
    assertTrue(content.contains("t-1"));
    assertTrue(content.contains("95.5"));
  }

  @Test
  void exportExcelProducesNonEmptyOutput() {
    byte[] excel = exporter.exportExcel(headers, rows);
    assertNotNull(excel);
    assertTrue(excel.length > 0);
    // XLSX files start with PK (ZIP header)
    assertEquals(0x50, excel[0] & 0xFF);
    assertEquals(0x4B, excel[1] & 0xFF);
  }

  @Test
  void exportJsonProducesValidOutput() {
    byte[] json = exporter.exportJson(rows);
    assertNotNull(json);
    String content = new String(json);
    assertTrue(content.contains("workflow_success"));
    assertTrue(content.contains("t-2"));
  }

  @Test
  void exportPdfProducesNonEmptyOutput() {
    byte[] pdf = exporter.exportPdf(headers, rows);
    assertNotNull(pdf);
    assertTrue(pdf.length > 0);
    // PDF files start with %PDF
    String header = new String(pdf, 0, 4);
    assertEquals("%PDF", header);
  }

  @Test
  void exportCsvHandlesEmptyRows() {
    byte[] csv = exporter.exportCsv(headers, List.of());
    assertNotNull(csv);
    String content = new String(csv);
    assertTrue(content.contains("tenant_id"));
  }
}
