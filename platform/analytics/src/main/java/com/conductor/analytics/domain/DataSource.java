package com.conductor.analytics.domain;

/** Supported analytics data source backends. */
public enum DataSource {
  CLICKHOUSE,
  POSTGRES_REPLICA,
  PROMETHEUS
}
