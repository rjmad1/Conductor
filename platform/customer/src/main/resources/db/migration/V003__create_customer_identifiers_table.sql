CREATE TABLE customer_identifiers (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    identifier_type VARCHAR(50) NOT NULL,
    identifier_hash VARCHAR(64) NOT NULL,
    source_system VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT idx_identifiers_lookup UNIQUE (tenant_id, identifier_type, identifier_hash)
);

CREATE INDEX idx_identifiers_customer_id ON customer_identifiers(tenant_id, customer_id);
