-- ClickHouse: Core events table for all Conductor domain events
-- Partitioned by tenant_id and month for isolation and query performance
-- TTL: 90 days per DATA_GOVERNANCE.md retention for anonymous metrics (Tier 3)

CREATE TABLE IF NOT EXISTS conductor_events (
    event_id UUID,
    event_type String,
    tenant_id String,
    domain String,
    entity String,
    action String,
    correlation_id String,
    source String,
    payload String,
    created_at DateTime64(3)
) ENGINE = MergeTree()
PARTITION BY (tenant_id, toYYYYMM(created_at))
ORDER BY (tenant_id, domain, entity, action, created_at)
TTL toDateTime(created_at) + INTERVAL 90 DAY;
