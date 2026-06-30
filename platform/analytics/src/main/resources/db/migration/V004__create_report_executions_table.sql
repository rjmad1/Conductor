-- Analytics report execution history (tenant-scoped)
CREATE TABLE IF NOT EXISTS analytics_report_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    report_id UUID NOT NULL REFERENCES analytics_reports(id) ON DELETE CASCADE,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    completed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'RUNNING',
    output_path TEXT,
    row_count BIGINT DEFAULT 0,
    error_message TEXT
);

CREATE INDEX idx_report_executions_report ON analytics_report_executions(report_id);
CREATE INDEX idx_report_executions_tenant ON analytics_report_executions(tenant_id);
