-- ClickHouse: Integration metrics materialized view
-- Aggregates integration events by tenant, connector type, action, and hour

CREATE TABLE IF NOT EXISTS integration_metrics_hourly (
    tenant_id String,
    hour DateTime,
    entity String,
    action String,
    event_count UInt64
) ENGINE = SummingMergeTree()
PARTITION BY (tenant_id, toYYYYMM(hour))
ORDER BY (tenant_id, hour, entity, action)
TTL hour + INTERVAL 90 DAY;

CREATE MATERIALIZED VIEW IF NOT EXISTS integration_metrics_hourly_mv
TO integration_metrics_hourly AS
SELECT
    tenant_id,
    toStartOfHour(created_at) AS hour,
    entity,
    action,
    count() AS event_count
FROM conductor_events
WHERE domain = 'integration'
GROUP BY tenant_id, hour, entity, action;
