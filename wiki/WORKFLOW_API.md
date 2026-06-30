# Workflow API

## Base URL
`/api/v1/workflows`

## Authentication
All endpoints require OIDC JWT bearer token. Tenant context is extracted from the token automatically.

## Workflow Definitions

| Method | Path | Description |
|:---|:---|:---|
| POST | `/api/v1/workflows` | Create a new DRAFT definition |
| GET | `/api/v1/workflows` | List definitions (cursor paginated) |
| GET | `/api/v1/workflows/{id}` | Get a definition |
| PUT | `/api/v1/workflows/{id}` | Update a DRAFT definition |
| POST | `/api/v1/workflows/{id}/publish` | Publish a DRAFT definition |
| POST | `/api/v1/workflows/{id}/deprecate` | Deprecate a PUBLISHED definition |
| POST | `/api/v1/workflows/{id}/archive` | Archive a DEPRECATED definition |
| POST | `/api/v1/workflows/{id}/clone` | Clone as a new DRAFT version |
| DELETE | `/api/v1/workflows/{id}` | Delete a DRAFT definition |

## Workflow Executions

| Method | Path | Description |
|:---|:---|:---|
| POST | `/api/v1/workflows/{id}/execute` | Start an execution |
| GET | `/api/v1/workflows/executions` | List executions |
| GET | `/api/v1/workflows/executions/{executionId}` | Get execution details |
| POST | `/api/v1/workflows/executions/{executionId}/cancel` | Cancel an execution |
| POST | `/api/v1/workflows/executions/{executionId}/replay` | Replay an execution |
| GET | `/api/v1/workflows/executions/{executionId}/history` | Get execution history |

## Templates

| Method | Path | Description |
|:---|:---|:---|
| GET | `/api/v1/workflows/templates` | List available templates |
| GET | `/api/v1/workflows/templates/{templateId}` | Get a template |
| POST | `/api/v1/workflows/templates/{templateId}/instantiate` | Create workflow from template |

## Triggers

| Method | Path | Description |
|:---|:---|:---|
| POST | `/api/v1/workflows/triggers/webhook/{definitionId}` | Fire a webhook trigger |
| POST | `/api/v1/workflows/triggers/manual/{definitionId}` | Fire a manual trigger |

## Error Responses

All errors follow RFC 7807 Problem Details:

```json
{
  "type": "https://conductor.io/errors/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Workflow definition not found: ...",
  "instance": "/api/v1/workflows/...",
  "timestamp": "2026-01-01T00:00:00Z"
}
```

## JSON DSL

A workflow definition's `steps` field is a JSON array of step objects:

```json
[
  {
    "name": "send-welcome",
    "type": "GENERATE_NOTIFICATION",
    "config": {
      "channel": "whatsapp",
      "templateName": "welcome",
      "recipient": "{{trigger.phone}}"
    },
    "condition": {
      "field": "trigger.phone",
      "operator": "EXISTS"
    },
    "onFailure": {
      "action": "COMPENSATE"
    }
  }
]
```

### Supported Step Types

`SEND_EVENT`, `INVOKE_INTEGRATION`, `CREATE_RECORD`, `UPDATE_RECORD`, `ASSIGN_USER`, `GENERATE_NOTIFICATION`, `INVOKE_WORKFLOW`, `DELAY`, `WAIT`, `TERMINATE`

### Supported Condition Operators

`EQUALS`, `NOT_EQUALS`, `CONTAINS`, `GREATER_THAN`, `LESS_THAN`, `EXISTS`, `NOT_EXISTS`, `AND`, `OR`, `NOT`
