package com.conductor.customer.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CustomerImportService {

  private static final Logger log = LoggerFactory.getLogger(CustomerImportService.class);

  private final CustomerService customerService;

  public CustomerImportService(CustomerService customerService) {
    this.customerService = customerService;
  }

  public ImportSummary importCsv(MultipartFile file) {
    int successCount = 0;
    int failureCount = 0;

    try (BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        CSVParser csvParser =
            new CSVParser(
                reader,
                CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build())) {

      for (CSVRecord csvRecord : csvParser) {
        try {
          String firstName = csvRecord.get("firstName");
          String lastName = csvRecord.get("lastName");
          String displayName = csvRecord.get("displayName");

          String externalId = null;
          if (csvRecord.isMapped("externalId")) {
            externalId = csvRecord.get("externalId");
          }

          if (displayName == null || displayName.isBlank()) {
            displayName = firstName + " " + lastName;
          }

          customerService.createCustomer(
              firstName, lastName, displayName, externalId, "CSV_IMPORT");
          successCount++;
        } catch (Exception e) {
          log.error("Failed to import customer at row {}", csvRecord.getRecordNumber(), e);
          failureCount++;
        }
      }
    } catch (Exception e) {
      log.error("Failed to parse CSV file", e);
      throw new RuntimeException("CSV Parsing failed: " + e.getMessage());
    }

    return new ImportSummary(successCount + failureCount, successCount, failureCount);
  }

  public record ImportSummary(int totalRows, int successCount, int failureCount) {}
}
