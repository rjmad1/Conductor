-- Analytics KPI definitions table (tenant-scoped)
CREATE TABLE IF NOT EXISTS analytics_kpi_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    metric_name VARCHAR(255) NOT NULL,
    aggregation VARCHAR(50) NOT NULL,
    threshold_warning DOUBLE PRECISION,
    threshold_critical DOUBLE PRECISION,
    owner VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_kpi_definitions_status ON analytics_kpi_definitions(status);
CREATE INDEX idx_kpi_definitions_tenant ON analytics_kpi_definitions(tenant_id);
