CREATE TABLE customer_contacts (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    contact_type VARCHAR(50) NOT NULL,
    contact_value VARCHAR(255) NOT NULL,
    value_hash VARCHAR(64) NOT NULL,
    label VARCHAR(255),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_contacts_customer_id ON customer_contacts(tenant_id, customer_id);
CREATE INDEX idx_contacts_type_primary ON customer_contacts(tenant_id, contact_type, is_primary);
CREATE INDEX idx_contacts_value_hash ON customer_contacts(tenant_id, contact_type, value_hash);
