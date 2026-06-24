CREATE TABLE segments (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    segment_type VARCHAR(50) NOT NULL,
    rules JSONB,
    description TEXT,
    customer_count BIGINT NOT NULL DEFAULT 0,
    last_computed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT idx_segments_tenant_slug UNIQUE (tenant_id, slug)
);

CREATE INDEX idx_segments_type ON segments(tenant_id, segment_type);

CREATE TABLE customer_segments (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    segment_id UUID NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL,
    added_by VARCHAR(255),
    source VARCHAR(50) NOT NULL,
    CONSTRAINT idx_customer_segments_unique UNIQUE (tenant_id, customer_id, segment_id)
);

CREATE INDEX idx_customer_segments_customer ON customer_segments(tenant_id, customer_id);
CREATE INDEX idx_customer_segments_segment ON customer_segments(tenant_id, segment_id);
