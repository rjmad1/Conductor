CREATE TABLE replay_audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    username        VARCHAR(200) NOT NULL,
    stream          VARCHAR(100) NOT NULL,
    consumer        VARCHAR(100) NOT NULL,
    replay_type     VARCHAR(50) NOT NULL,
    start_value     VARCHAR(200) NOT NULL,
    requested_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE dlq_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    stream          VARCHAR(100) NOT NULL,
    consumer        VARCHAR(100) NOT NULL,
    reason          TEXT NOT NULL,
    original_event_id UUID,
    payload         TEXT NOT NULL,
    dead_lettered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX idx_replay_audit_tenant ON replay_audit_logs(tenant_id);
CREATE INDEX idx_dlq_records_tenant ON dlq_records(tenant_id, status);
