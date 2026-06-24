CREATE TABLE customer_relationships (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    from_customer_id UUID NOT NULL,
    to_customer_id UUID NOT NULL,
    relationship_type VARCHAR(100) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_relationships_from ON customer_relationships(tenant_id, from_customer_id);
CREATE INDEX idx_relationships_to ON customer_relationships(tenant_id, to_customer_id);
