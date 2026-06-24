package com.conductor.analytics.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.conductor.analytics.domain")
@EnableJpaRepositories(basePackages = {
        "com.conductor.analytics.dashboard",
        "com.conductor.analytics.reporting",
        "com.conductor.analytics.kpi"
})
public class JpaConfig {
}
