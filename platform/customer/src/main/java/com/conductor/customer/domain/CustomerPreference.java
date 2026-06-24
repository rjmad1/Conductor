package com.conductor.customer.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Customer communication channel preference.
 *
 * Records whether a customer has opted in, opted out, or has no preference for
 * a given communication channel. Distinct from ConsentRecord — preferences are
 * mutable and represent customer settings; consent is an immutable audit ledger.
 */
@Entity
@Table(name = "customer_preferences", indexes = {
    @Index(name = "idx_preferences_customer_channel", columnList = "tenant_id, customer_id, channel", unique = true)
})
@Getter
@Setter
public class CustomerPreference extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    /** Communication channel: EMAIL, SMS, WHATSAPP, PUSH. */
    @Column(name = "channel", nullable = false)
    private String channel;

    /** OPTED_IN, OPTED_OUT, NOT_SET. */
    @Column(name = "preference", nullable = false)
    private String preference;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "updated_by")
    private String updatedBy;
}
