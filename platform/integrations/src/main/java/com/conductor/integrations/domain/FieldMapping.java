package com.conductor.integrations.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "field_mappings")
@Getter
@Setter
public class FieldMapping extends TenantAwareEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "integration_id", nullable = false)
    private Integration integration;

    @Column(name = "source_field", nullable = false)
    private String sourceField;

    @Column(name = "target_field", nullable = false)
    private String targetField;

    @Column(name = "default_value")
    private String defaultValue;
}
