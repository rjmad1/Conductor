-- Analytics reports table (tenant-scoped)
CREATE TABLE IF NOT EXISTS analytics_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    report_type VARCHAR(50) NOT NULL, -- SCHEDULED, ADHOC
    query_definition TEXT NOT NULL,
    output_format VARCHAR(50) NOT NULL, -- CSV, EXCEL, PDF, JSON
    schedule_cron VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    last_run_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_analytics_reports_tenant ON analytics_reports(tenant_id);
CREATE INDEX idx_analytics_reports_type ON analytics_reports(report_type, status);
