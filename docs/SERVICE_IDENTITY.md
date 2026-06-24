# Service Identity Specification — Conductor Platform

This specification describes system-to-system authentication, machine-to-machine integrations, and service account authorizations.

---

## 1. Actor Types & Scopes

Internal services (e.g. `workflow-service`, `whatsapp-adapter`) authenticate with Keycloak using Client Credentials flow to obtain short-lived machine tokens.

| Service Name | Keycloak Client ID | Mapped Authorization Scopes |
| :--- | :--- | :--- |
| **Workflow Engine** | `conductor-workflow-service` | `workflows:write`, `messages:send`, `contacts:read` |
| **WhatsApp Adapter**| `conductor-whatsapp-adapter` | `messages:send`, `messages:status` |
| **Analytics Engine**| `conductor-analytics-service`| `analytics:write`, `contacts:read` |

---

## 2. Token Exchange

1. An internal service requests a token using client credentials:
   - `POST /realms/{tenantId}/protocol/openid-connect/token`
   - Parameters: `grant_type=client_credentials`, `client_id`, `client_secret`
2. Keycloak validates parameters and returns a signed JWT containing client role scopes.
3. The calling service appends this token in the `Authorization: Bearer {token}` header to communicate with other services.

---

## 3. Infrastructure Security Mappings

- **Temporal to Temporal Server**: Communicates over gRPC secured with mutual TLS (mTLS 1.3) using client certificates.
- **NATS JetStream**: Client authentication uses token-based verification configurations managed at stream gateway endpoints.
