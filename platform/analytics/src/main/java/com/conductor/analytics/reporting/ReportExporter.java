package com.conductor.analytics.reporting;

import com.opencsv.CSVWriter;
import java.io.*;
import java.util.List;
import java.util.Map;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Report export strategy implementations for CSV, Excel, PDF, and JSON formats. */
@Component
public class ReportExporter {

  private static final Logger log = LoggerFactory.getLogger(ReportExporter.class);

  public byte[] exportCsv(List<String> headers, List<Map<String, Object>> rows) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {

      writer.writeNext(headers.toArray(new String[0]));
      for (Map<String, Object> row : rows) {
        String[] values =
            headers.stream()
                .map(h -> String.valueOf(row.getOrDefault(h, "")))
                .toArray(String[]::new);
        writer.writeNext(values);
      }
      writer.flush();
      return out.toByteArray();
    } catch (IOException e) {
      log.error("CSV export failed", e);
      throw new RuntimeException("CSV export failed", e);
    }
  }

  public byte[] exportExcel(List<String> headers, List<Map<String, Object>> rows) {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      XSSFSheet sheet = workbook.createSheet("Report");
      XSSFRow headerRow = sheet.createRow(0);
      for (int i = 0; i < headers.size(); i++) {
        headerRow.createCell(i).setCellValue(headers.get(i));
      }

      for (int r = 0; r < rows.size(); r++) {
        XSSFRow dataRow = sheet.createRow(r + 1);
        Map<String, Object> row = rows.get(r);
        for (int c = 0; c < headers.size(); c++) {
          Object val = row.getOrDefault(headers.get(c), "");
          if (val instanceof Number) {
            dataRow.createCell(c).setCellValue(((Number) val).doubleValue());
          } else {
            dataRow.createCell(c).setCellValue(String.valueOf(val));
          }
        }
      }

      workbook.write(out);
      return out.toByteArray();
    } catch (IOException e) {
      log.error("Excel export failed", e);
      throw new RuntimeException("Excel export failed", e);
    }
  }

  public byte[] exportJson(List<Map<String, Object>> rows) {
    try {
      com.fasterxml.jackson.databind.ObjectMapper mapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(rows);
    } catch (Exception e) {
      log.error("JSON export failed", e);
      throw new RuntimeException("JSON export failed", e);
    }
  }

  public byte[] exportPdf(List<String> headers, List<Map<String, Object>> rows) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      com.lowagie.text.Document document = new com.lowagie.text.Document();
      com.lowagie.text.pdf.PdfWriter.getInstance(document, out);
      document.open();

      document.add(new com.lowagie.text.Paragraph("Conductor Analytics Report"));
      document.add(new com.lowagie.text.Paragraph(" "));

      com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(headers.size());
      for (String header : headers) {
        table.addCell(new com.lowagie.text.Phrase(header));
      }
      for (Map<String, Object> row : rows) {
        for (String header : headers) {
          table.addCell(new com.lowagie.text.Phrase(String.valueOf(row.getOrDefault(header, ""))));
        }
      }

      document.add(table);
      document.close();
      return out.toByteArray();
    } catch (Exception e) {
      log.error("PDF export failed", e);
      throw new RuntimeException("PDF export failed", e);
    }
  }
}
