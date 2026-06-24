# Segmentation Engine

The segmentation engine categorizes customers into groups (Segments).

## Segment Types
- `STATIC`: Managed manually via membership assignments.
- `TAG_BASED`: Derived automatically from assigned tags.
- `RULE_BASED` / `DYNAMIC`: Evaluated using rule DSL conditions.

## Rules DSL
Rules are defined in a JSONB block:
```json
{
  "conditions": [
    {
      "field": "status",
      "operator": "EQ",
      "value": "ACTIVE"
    }
  ],
  "logic": "AND"
}
```

## Performance & Optimization
- Customer counts are cached on the `Segment` table to prevent heavy counting queries on every API call.
- Dynamic segment calculations are batch-processed or triggered on request via the `/recompute` endpoint.
