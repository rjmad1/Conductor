package com.conductor.tenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.conductor.tenant", "com.conductor.shared"})
public class TenantApplication {
  public static void main(String[] args) {
    SpringApplication.run(TenantApplication.class, args);
  }
}
