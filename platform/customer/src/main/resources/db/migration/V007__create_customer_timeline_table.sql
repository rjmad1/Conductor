CREATE TABLE customer_timeline (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_source VARCHAR(255),
    summary TEXT,
    metadata JSONB,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_timeline_customer_occurred ON customer_timeline(tenant_id, customer_id, occurred_at);
CREATE INDEX idx_timeline_customer_event_type ON customer_timeline(tenant_id, customer_id, event_type);
