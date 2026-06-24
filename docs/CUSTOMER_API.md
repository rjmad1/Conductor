# Customer REST APIs

Endpoints are secured with Spring Security annotations and require the roles `ROLE_TENANT_ADMIN` or `ROLE_PLATFORM_ADMIN`.

## Customer Endpoints
- `POST /api/v1/customers` - Create customer profile
- `GET /api/v1/customers/{id}` - Get customer profile
- `PUT /api/v1/customers/{id}` - Update customer profile
- `DELETE /api/v1/customers/{id}` - Soft-delete customer profile
- `POST /api/v1/customers/{id}/archive` - Archive profile
- `POST /api/v1/customers/{id}/merge?targetId={targetId}` - Merge source into target
- `GET /api/v1/customers/search` - Search customers (by phone, email, name, tag, segment)

## Contact Endpoints
- `GET /api/v1/customers/{customerId}/contacts` - List contact channels
- `POST /api/v1/customers/{customerId}/contacts` - Add contact channel
- `PUT /api/v1/customers/{customerId}/contacts/{contactId}` - Update contact label/primary
- `DELETE /api/v1/customers/{customerId}/contacts/{contactId}` - Remove contact channel
- `POST /api/v1/customers/{customerId}/contacts/{contactId}/set-primary` - Mark channel primary

## Consent Endpoints
- `POST /api/v1/customers/{customerId}/consent/grant` - Record consent grant
- `POST /api/v1/customers/{customerId}/consent/revoke` - Record consent revocation
- `GET /api/v1/customers/{customerId}/consent` - Fetch active consent statuses
- `GET /api/v1/customers/{customerId}/consent/history` - Fetch consent history

## Tag Endpoints
- `GET /api/v1/tags` - List tags
- `POST /api/v1/tags` - Create a tag
- `PUT /api/v1/tags/{id}` - Update tag
- `DELETE /api/v1/tags/{id}` - Delete tag
- `GET /api/v1/customers/{customerId}/tags` - Fetch customer tags
- `POST /api/v1/customers/{customerId}/tags?tagId={tagId}` - Assign tag to customer
- `DELETE /api/v1/customers/{customerId}/tags/{tagId}` - Remove tag assignment

## Segment Endpoints
- `GET /api/v1/segments` - List segments
- `POST /api/v1/segments` - Create segment
- `PUT /api/v1/segments/{id}` - Update segment
- `DELETE /api/v1/segments/{id}` - Delete segment
- `GET /api/v1/customers/{customerId}/segments` - Fetch customer segment memberships
- `GET /api/v1/segments/{id}/customers` - List customer IDs in segment
- `POST /api/v1/segments/{id}/customers?customerId={customerId}` - Assign customer to static segment
- `DELETE /api/v1/segments/{id}/customers/{customerId}` - Remove customer from segment
- `POST /api/v1/segments/{id}/recompute?tagId={tagId}` - Trigger tag-based recompute

## Timeline Endpoints
- `GET /api/v1/customers/{customerId}/timeline` - Get chronological timeline of events
