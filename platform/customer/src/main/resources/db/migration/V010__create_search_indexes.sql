CREATE INDEX idx_customers_fts ON customers USING GIN (to_tsvector('english', COALESCE(display_name, '')));
