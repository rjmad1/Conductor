package com.conductor.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
      "com.conductor.analytics",
      "com.conductor.shared.middleware.tenant",
      "com.conductor.shared.messaging",
      "com.conductor.shared.contracts"
    })
public class AnalyticsApplication {

  public static void main(String[] args) {
    SpringApplication.run(AnalyticsApplication.class, args);
  }
}
