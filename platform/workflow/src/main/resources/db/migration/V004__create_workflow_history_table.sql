CREATE TABLE workflow_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id UUID NOT NULL REFERENCES workflow_executions(id),
    event_type VARCHAR(100) NOT NULL,
    details JSONB,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actor_id VARCHAR(255)
);

CREATE INDEX idx_wf_history_execution ON workflow_history(execution_id);
CREATE INDEX idx_wf_history_timestamp ON workflow_history(execution_id, timestamp);
