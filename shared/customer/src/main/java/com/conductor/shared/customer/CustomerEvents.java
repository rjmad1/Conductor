package com.conductor.shared.customer;

/**
 * Canonical NATS subject constants for all customer domain events.
 * Subject pattern: conductor.{tenantId}.customer.{entity}.{action}
 * These constants define the {entity}.{action} suffix portion.
 */
public final class CustomerEvents {

    private CustomerEvents() {}

    // Domain prefix used by EventPublisher
    public static final String DOMAIN = "customer";

    // Entity: profile
    public static final String ENTITY_PROFILE = "profile";
    public static final String ACTION_CREATED  = "created";
    public static final String ACTION_UPDATED  = "updated";
    public static final String ACTION_MERGED   = "merged";
    public static final String ACTION_DELETED  = "deleted";
    public static final String ACTION_ARCHIVED = "archived";

    // Entity: tag
    public static final String ENTITY_TAG       = "tag";
    public static final String ACTION_ASSIGNED  = "assigned";
    public static final String ACTION_REMOVED   = "removed";

    // Entity: segment
    public static final String ENTITY_SEGMENT   = "segment";
    // reuses ACTION_ASSIGNED / ACTION_REMOVED

    // Entity: consent
    public static final String ENTITY_CONSENT   = "consent";
    public static final String ACTION_GRANTED   = "granted";
    public static final String ACTION_REVOKED   = "revoked";

    // Entity: preference
    public static final String ENTITY_PREFERENCE = "preference";
    // reuses ACTION_UPDATED
}
