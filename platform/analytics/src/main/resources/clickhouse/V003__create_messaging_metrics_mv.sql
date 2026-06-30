-- ClickHouse: Messaging metrics materialized view
-- Aggregates message events by tenant, action (sent/delivered/read/failed), and hour

CREATE TABLE IF NOT EXISTS messaging_metrics_hourly (
    tenant_id String,
    hour DateTime,
    action String,
    channel String,
    message_count UInt64
) ENGINE = SummingMergeTree()
PARTITION BY (tenant_id, toYYYYMM(hour))
ORDER BY (tenant_id, hour, action, channel)
TTL hour + INTERVAL 90 DAY;

CREATE MATERIALIZED VIEW IF NOT EXISTS messaging_metrics_hourly_mv
TO messaging_metrics_hourly AS
SELECT
    tenant_id,
    toStartOfHour(created_at) AS hour,
    action,
    extractAll(payload, '"channel":"([^"]+)"')[1] AS channel,
    count() AS message_count
FROM conductor_events
WHERE domain = 'messaging'
GROUP BY tenant_id, hour, action, channel;
