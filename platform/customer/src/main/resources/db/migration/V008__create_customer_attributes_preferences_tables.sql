CREATE TABLE customer_attributes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    attribute_key VARCHAR(255) NOT NULL,
    attribute_value JSONB,
    data_type VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT idx_attributes_customer_key UNIQUE (tenant_id, customer_id, attribute_key)
);

CREATE INDEX idx_attributes_key_all ON customer_attributes(tenant_id, attribute_key);

CREATE TABLE customer_preferences (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    channel VARCHAR(50) NOT NULL,
    preference VARCHAR(50) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(255),
    CONSTRAINT idx_preferences_customer_channel UNIQUE (tenant_id, customer_id, channel)
);
