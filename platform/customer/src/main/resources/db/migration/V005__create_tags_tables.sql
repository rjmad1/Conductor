CREATE TABLE tags (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    color VARCHAR(7),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT idx_tags_tenant_slug UNIQUE (tenant_id, slug)
);

CREATE INDEX idx_tags_category ON tags(tenant_id, category);

CREATE TABLE customer_tags (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL,
    assigned_by VARCHAR(255),
    CONSTRAINT idx_customer_tags_unique UNIQUE (tenant_id, customer_id, tag_id)
);

CREATE INDEX idx_customer_tags_customer ON customer_tags(tenant_id, customer_id);
CREATE INDEX idx_customer_tags_tag ON customer_tags(tenant_id, tag_id);
