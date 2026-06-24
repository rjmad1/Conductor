# Tag System

The tag system allows labelling customers with tenant-scoped keywords.

## Structure
Tags have:
- `name`: Human-readable label (e.g. "VIP").
- `slug`: URL-safe, tenant-unique string (e.g. "vip").
- `category`: Grouping for tag management (e.g. "behavioral").
- `color`: Hex color string (e.g. "#FF0000").

## Operations
- Tags are tenant-scoped and unique by slug per tenant.
- Assignments are idempotent. Assigning a tag that is already present has no side effects.
- Cascade deletion: deleting a tag automatically removes it from all assigned customers.
