package com.conductor.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.conductor.identity", "com.conductor.shared"})
public class IdentityApplication {
  public static void main(String[] args) {
    SpringApplication.run(IdentityApplication.class, args);
  }
}
