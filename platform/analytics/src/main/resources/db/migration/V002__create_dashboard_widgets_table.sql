-- Analytics dashboard widgets table
CREATE TABLE IF NOT EXISTS analytics_dashboard_widgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dashboard_id UUID NOT NULL REFERENCES analytics_dashboards(id) ON DELETE CASCADE,
    widget_type VARCHAR(100) NOT NULL,
    metric_query TEXT NOT NULL,
    position_x INT DEFAULT 0,
    position_y INT DEFAULT 0,
    width INT DEFAULT 6,
    height INT DEFAULT 4,
    config_json TEXT
);

CREATE INDEX idx_dashboard_widgets_dashboard ON analytics_dashboard_widgets(dashboard_id);
