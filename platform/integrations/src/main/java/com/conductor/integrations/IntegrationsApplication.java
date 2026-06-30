package com.conductor.integrations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.conductor.integrations", "com.conductor.shared"})
public class IntegrationsApplication {
  public static void main(String[] args) {
    SpringApplication.run(IntegrationsApplication.class, args);
  }
}
