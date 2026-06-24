-- ClickHouse: Tenant metrics materialized view
-- Cross-domain daily aggregation per tenant for usage overview

CREATE TABLE IF NOT EXISTS tenant_metrics_daily (
    tenant_id String,
    day Date,
    domain String,
    event_count UInt64
) ENGINE = SummingMergeTree()
PARTITION BY (tenant_id, toYYYYMM(day))
ORDER BY (tenant_id, day, domain)
TTL day + INTERVAL 90 DAY;

CREATE MATERIALIZED VIEW IF NOT EXISTS tenant_metrics_daily_mv
TO tenant_metrics_daily AS
SELECT
    tenant_id,
    toDate(created_at) AS day,
    domain,
    count() AS event_count
FROM conductor_events
GROUP BY tenant_id, day, domain;
