ALTER TABLE tenants ADD COLUMN tenant_key VARCHAR(255);
ALTER TABLE tenants ADD COLUMN legal_name VARCHAR(255);
ALTER TABLE tenants ADD COLUMN status VARCHAR(50) DEFAULT 'ACTIVE' NOT NULL;
ALTER TABLE tenants ADD COLUMN timezone VARCHAR(100) DEFAULT 'UTC' NOT NULL;
ALTER TABLE tenants ADD COLUMN locale VARCHAR(50) DEFAULT 'en_US' NOT NULL;
ALTER TABLE tenants ADD COLUMN default_currency VARCHAR(10) DEFAULT 'USD' NOT NULL;
ALTER TABLE tenants ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE tenants ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

-- Populate tenant_key for existing rows (if any)
UPDATE tenants SET tenant_key = domain WHERE tenant_key IS NULL;

-- Make tenant_key NOT NULL and UNIQUE
ALTER TABLE tenants ALTER COLUMN tenant_key SET NOT NULL;
ALTER TABLE tenants ADD CONSTRAINT uq_tenants_tenant_key UNIQUE (tenant_key);
