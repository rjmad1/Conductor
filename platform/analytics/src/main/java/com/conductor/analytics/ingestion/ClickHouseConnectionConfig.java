package com.conductor.analytics.ingestion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import com.clickhouse.jdbc.ClickHouseDataSource;
import java.util.Properties;

/**
 * Configures the ClickHouse JDBC DataSource for analytics OLAP queries.
 * Separate from the Postgres DataSource used for JPA metadata.
 */
@Configuration
public class ClickHouseConnectionConfig {

    @Value("${analytics.clickhouse.url:jdbc:ch://localhost:8123/default}")
    private String clickhouseUrl;

    @Value("${analytics.clickhouse.user:default}")
    private String clickhouseUser;

    @Value("${analytics.clickhouse.password:}")
    private String clickhousePassword;

    @Bean(name = "clickHouseDataSource")
    public DataSource clickHouseDataSource() throws Exception {
        Properties properties = new Properties();
        if (clickhouseUser != null && !clickhouseUser.isBlank()) {
            properties.setProperty("user", clickhouseUser);
        }
        if (clickhousePassword != null && !clickhousePassword.isBlank()) {
            properties.setProperty("password", clickhousePassword);
        }
        properties.setProperty("socket_timeout", "30000");
        properties.setProperty("compress", "true");
        return new ClickHouseDataSource(clickhouseUrl, properties);
    }
}
