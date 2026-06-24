# Customer 360 Architecture

The Customer 360 view aggregates customer profiles, relationships, attributes, timeline history, and preferences.

## Data Structures

```mermaid
classDiagram
    class Customer {
        UUID id
        UUID tenantId
        String firstName
        String lastName
        String displayName
        CustomerStatus status
    }
    class CustomerContact {
        UUID id
        ContactType type
        String value
        Boolean isPrimary
    }
    class CustomerPreference {
        UUID id
        String channel
        String preference
    }
    class CustomerAttribute {
        UUID id
        String key
        JSONB value
    }
    class CustomerTimeline {
        UUID id
        TimelineEventType eventType
        Instant occurredAt
    }
    Customer --> CustomerContact : has many
    Customer --> CustomerPreference : has many
    Customer --> CustomerAttribute : has many
    Customer --> CustomerTimeline : has many
```

## Consolidated Profile Resolution
The Customer 360 view combines:
1. Contact cards with primary indicators.
2. Custom attributes (key-value bags).
3. Preferences (opt-in/opt-out per channel).
4. Direct relationships (Household, referred-by).
5. Append-only chronological timeline.
