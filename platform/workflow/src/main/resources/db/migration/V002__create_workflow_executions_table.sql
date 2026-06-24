CREATE TABLE workflow_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    definition_id UUID NOT NULL REFERENCES workflow_definitions(id),
    definition_version INT NOT NULL,
    temporal_workflow_id VARCHAR(255),
    temporal_run_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    input JSONB,
    output JSONB,
    variables JSONB,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    failure_reason TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    compensated BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wf_exec_tenant ON workflow_executions(tenant_id);
CREATE INDEX idx_wf_exec_definition ON workflow_executions(tenant_id, definition_id);
CREATE INDEX idx_wf_exec_status ON workflow_executions(tenant_id, status);
CREATE INDEX idx_wf_exec_temporal ON workflow_executions(temporal_workflow_id);
