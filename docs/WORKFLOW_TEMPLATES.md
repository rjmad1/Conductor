# Workflow Templates

## Overview

Built-in workflow templates allow tenants to quickly create workflow definitions from curated starting points. Templates are loaded from classpath JSON files at startup and cached in memory.

## Available Templates

| Template ID | Name | Category | Trigger |
|:---|:---|:---|:---|
| `lead-capture` | Lead Capture | marketing | WEBHOOK |
| `appointment-reminder` | Appointment Reminder | engagement | SCHEDULE |
| `payment-reminder` | Payment Reminder | billing | EVENT |
| `customer-support-escalation` | Customer Support Escalation | support | EVENT |
| `order-status-update` | Order Status Update | ecommerce | EVENT |

## Instantiation

Templates are instantiated via the API:

```http
POST /api/v1/workflows/templates/{templateId}/instantiate
Content-Type: application/json

{
  "name": "My Custom Lead Capture",
  "variables": {
    "trigger.phone": "+1234567890"
  }
}
```

This creates a DRAFT workflow definition with the template's steps, which can then be customized and published.

## Template JSON Format

Templates follow the same JSON DSL format as workflow definitions. They are stored in:
`shared/templates/src/main/resources/templates/`

See [WORKFLOW_API.md](WORKFLOW_API.md) for the full DSL specification.

## Adding New Templates

1. Create a JSON file in `shared/templates/src/main/resources/templates/`
2. Include all required fields: `templateId`, `name`, `category`, `version`, `triggerType`, `steps`
3. The template will be loaded automatically on next service startup
