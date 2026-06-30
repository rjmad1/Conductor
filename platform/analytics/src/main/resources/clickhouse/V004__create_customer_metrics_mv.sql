-- ClickHouse: Customer metrics materialized view
-- Aggregates customer events by tenant, action, and day

CREATE TABLE IF NOT EXISTS customer_metrics_daily (
    tenant_id String,
    day Date,
    action String,
    customer_count UInt64
) ENGINE = SummingMergeTree()
PARTITION BY (tenant_id, toYYYYMM(day))
ORDER BY (tenant_id, day, action)
TTL day + INTERVAL 90 DAY;

CREATE MATERIALIZED VIEW IF NOT EXISTS customer_metrics_daily_mv
TO customer_metrics_daily AS
SELECT
    tenant_id,
    toDate(created_at) AS day,
    action,
    count() AS customer_count
FROM conductor_events
WHERE domain = 'customer'
GROUP BY tenant_id, day, action;
