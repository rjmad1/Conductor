-- ClickHouse: Workflow metrics materialized view
-- Aggregates workflow execution events by tenant, status, and hour

CREATE TABLE IF NOT EXISTS workflow_metrics_hourly (
    tenant_id String,
    hour DateTime,
    status String,
    executions_count UInt64,
    total_duration_ms Float64,
    min_duration_ms Float64,
    max_duration_ms Float64
) ENGINE = SummingMergeTree()
PARTITION BY (tenant_id, toYYYYMM(hour))
ORDER BY (tenant_id, hour, status)
TTL hour + INTERVAL 90 DAY;

CREATE MATERIALIZED VIEW IF NOT EXISTS workflow_metrics_hourly_mv
TO workflow_metrics_hourly AS
SELECT
    tenant_id,
    toStartOfHour(created_at) AS hour,
    action AS status,
    count() AS executions_count,
    sum(toFloat64OrZero(extractAll(payload, '"durationMs":(\d+)')[1])) AS total_duration_ms,
    min(toFloat64OrZero(extractAll(payload, '"durationMs":(\d+)')[1])) AS min_duration_ms,
    max(toFloat64OrZero(extractAll(payload, '"durationMs":(\d+)')[1])) AS max_duration_ms
FROM conductor_events
WHERE domain = 'workflow' AND entity = 'execution'
GROUP BY tenant_id, hour, status;
