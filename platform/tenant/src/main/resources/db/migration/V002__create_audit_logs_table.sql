CREATE TABLE audit_logs (
    event_id UUID NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    pre_image JSONB,
    post_image JSONB,
    ip_address VARCHAR(45) NOT NULL,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (tenant_id, event_id, created_at)
) PARTITION BY RANGE (created_at);

-- Create a default partition to handle all inserts seamlessly
CREATE TABLE audit_logs_default PARTITION OF audit_logs DEFAULT;

-- Enforce immutability trigger: reject UPDATE/DELETE on audit_logs
CREATE OR REPLACE FUNCTION block_audit_log_alteration()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Alteration of audit logs is strictly prohibited. Violates compliance constraints.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER enforce_immutable_audit
BEFORE UPDATE OR DELETE ON audit_logs
FOR EACH ROW EXECUTE FUNCTION block_audit_log_alteration();
