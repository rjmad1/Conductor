-- Analytics dashboards metadata table (tenant-scoped)
CREATE TABLE IF NOT EXISTS analytics_dashboards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    layout_json TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_analytics_dashboards_tenant ON analytics_dashboards(tenant_id);
CREATE INDEX idx_analytics_dashboards_status ON analytics_dashboards(tenant_id, status);
