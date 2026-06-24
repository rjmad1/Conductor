CREATE TABLE consent_records (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    consent_type VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    channel VARCHAR(255),
    legal_basis VARCHAR(255),
    consent_version VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_consent_customer_type ON consent_records(tenant_id, customer_id, consent_type);
CREATE INDEX idx_consent_customer_created ON consent_records(tenant_id, customer_id, created_at);

-- Enforce immutability trigger: reject UPDATE/DELETE on consent_records
CREATE OR REPLACE FUNCTION block_consent_record_alteration()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Alteration of consent records is strictly prohibited. Violates compliance constraints.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER enforce_immutable_consent
BEFORE UPDATE OR DELETE ON consent_records
FOR EACH ROW EXECUTE FUNCTION block_consent_record_alteration();
