# Integration APIs

Exposes administration endpoints.

## Route Catalog
- `GET /api/v1/integrations/connectors` -> Lists connectors.
- `POST /api/v1/integrations/credentials` -> Saves API secrets.
- `POST /api/v1/integrations/oauth/authorize` -> Directs to OAuth login.
- `GET /api/v1/integrations/oauth/callback` -> Handles tokens callback.
- `POST /api/v1/integrations/webhooks` -> Registers subscriptions.
- `POST /api/v1/integrations/{id}/execute` -> Triggers synchronous actions.
- `GET /api/v1/integrations/{id}/history` -> Triggers history logs.
- `GET /api/v1/integrations/{id}/health` -> Triggers health checks.
