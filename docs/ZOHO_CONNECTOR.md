# Zoho CRM Connector

Synchronizes sales entities.

## Supported Operations
- **create-lead:** Submits lead profile details to Zoho CRM.
- **update-lead:** Modifies status or properties of existing Zoho leads.
- **sync-contacts:** Syncs customer contacts.
- **sync-opportunities:** Feeds opportunity milestones.
- **sync-activities:** Logs communication touchpoints.

## Webhooks
- `lead.create` -> publishes `zoho.lead.created`
- `lead.update` -> publishes `zoho.lead.updated`
- `contact.create` -> publishes `zoho.contact.created`
- `contact.update` -> publishes `zoho.contact.updated`
