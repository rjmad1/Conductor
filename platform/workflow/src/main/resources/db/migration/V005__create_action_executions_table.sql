CREATE TABLE action_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    workflow_execution_id UUID REFERENCES workflow_executions(id),
    correlation_id VARCHAR(255),
    inputs JSONB,
    outputs JSONB,
    status VARCHAR(20) NOT NULL,
    failure_reason TEXT,
    execution_duration_ms BIGINT,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);

CREATE INDEX idx_action_exec_tenant ON action_executions(tenant_id);
CREATE INDEX idx_action_exec_wf ON action_executions(workflow_execution_id);
CREATE INDEX idx_action_exec_correlation ON action_executions(correlation_id);
