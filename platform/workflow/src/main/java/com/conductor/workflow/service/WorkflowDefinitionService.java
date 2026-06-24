package com.conductor.workflow.service;

import com.conductor.shared.messaging.EventPublisher;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.TenantContext;
import com.conductor.shared.workflow.WorkflowEvents;
import com.conductor.shared.workflow.WorkflowVersionStatus;
import com.conductor.workflow.domain.WorkflowDefinition;
import com.conductor.workflow.repository.WorkflowDefinitionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages workflow definition lifecycle: CRUD, versioning, and publishing.
 * All mutations are audited and publish domain events.
 */
@Service
public class WorkflowDefinitionService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowDefinitionService.class);

    private final WorkflowDefinitionRepository repository;
    private final EventPublisher eventPublisher;
    private final AuditLogger auditLogger;
    private final WorkflowMetrics metrics;
    private final ObjectMapper objectMapper;

    public WorkflowDefinitionService(WorkflowDefinitionRepository repository,
                                      EventPublisher eventPublisher,
                                      AuditLogger auditLogger,
                                      WorkflowMetrics metrics) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.auditLogger = auditLogger;
        this.metrics = metrics;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public WorkflowDefinition create(WorkflowDefinition definition) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        definition.setTenantId(tenantId);
        definition.setVersionStatus(WorkflowVersionStatus.DRAFT);
        definition.setVersion(1);
        definition.setCreatedAt(Instant.now());
        definition.setUpdatedAt(Instant.now());

        WorkflowDefinition saved = repository.save(definition);

        auditLogger.logEvent("WORKFLOW_DEFINITION_CREATED", "workflow:" + saved.getId(),
                "SUCCESS", "name=" + saved.getName());
        publishDefinitionEvent(WorkflowEvents.ACTION_CREATED, saved);
        metrics.recordDefinitionCreated();

        log.info("Created workflow definition {} for tenant {}", saved.getId(), tenantId);
        return saved;
    }

    @Transactional
    public WorkflowDefinition update(UUID id, WorkflowDefinition updates) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        WorkflowDefinition existing = findByIdAndTenant(id, tenantId);

        if (existing.getVersionStatus() != WorkflowVersionStatus.DRAFT) {
            throw new IllegalStateException(
                    "Cannot update workflow definition in status: " + existing.getVersionStatus());
        }

        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getTriggerType() != null) existing.setTriggerType(updates.getTriggerType());
        if (updates.getTriggerConfig() != null) existing.setTriggerConfig(updates.getTriggerConfig());
        if (updates.getSteps() != null) existing.setSteps(updates.getSteps());
        if (updates.getVariables() != null) existing.setVariables(updates.getVariables());
        existing.setUpdatedAt(Instant.now());

        WorkflowDefinition saved = repository.save(existing);

        auditLogger.logEvent("WORKFLOW_DEFINITION_UPDATED", "workflow:" + saved.getId(),
                "SUCCESS", "version=" + saved.getVersion());
        publishDefinitionEvent(WorkflowEvents.ACTION_UPDATED, saved);

        return saved;
    }

    @Transactional
    public WorkflowDefinition publish(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        WorkflowDefinition definition = findByIdAndTenant(id, tenantId);

        if (definition.getVersionStatus() != WorkflowVersionStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT definitions can be published. Current status: " + definition.getVersionStatus());
        }

        definition.setVersionStatus(WorkflowVersionStatus.PUBLISHED);
        definition.setUpdatedAt(Instant.now());
        WorkflowDefinition saved = repository.save(definition);

        auditLogger.logEvent("WORKFLOW_DEFINITION_PUBLISHED", "workflow:" + saved.getId(),
                "SUCCESS", "version=" + saved.getVersion());
        publishDefinitionEvent(WorkflowEvents.ACTION_PUBLISHED, saved);

        log.info("Published workflow definition {} v{}", saved.getId(), saved.getVersion());
        return saved;
    }

    @Transactional
    public WorkflowDefinition deprecate(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        WorkflowDefinition definition = findByIdAndTenant(id, tenantId);

        if (definition.getVersionStatus() != WorkflowVersionStatus.PUBLISHED) {
            throw new IllegalStateException(
                    "Only PUBLISHED definitions can be deprecated. Current status: " + definition.getVersionStatus());
        }

        definition.setVersionStatus(WorkflowVersionStatus.DEPRECATED);
        definition.setUpdatedAt(Instant.now());
        return repository.save(definition);
    }

    @Transactional
    public WorkflowDefinition archive(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        WorkflowDefinition definition = findByIdAndTenant(id, tenantId);

        if (definition.getVersionStatus() != WorkflowVersionStatus.DEPRECATED) {
            throw new IllegalStateException(
                    "Only DEPRECATED definitions can be archived. Current status: " + definition.getVersionStatus());
        }

        definition.setVersionStatus(WorkflowVersionStatus.ARCHIVED);
        definition.setUpdatedAt(Instant.now());
        return repository.save(definition);
    }

    /**
     * Clones a definition as a new draft version, incrementing the version number.
     */
    @Transactional
    public WorkflowDefinition clone(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        WorkflowDefinition source = findByIdAndTenant(id, tenantId);

        WorkflowDefinition cloned = WorkflowDefinition.builder()
                .name(source.getName())
                .description(source.getDescription())
                .triggerType(source.getTriggerType())
                .triggerConfig(source.getTriggerConfig())
                .steps(source.getSteps())
                .variables(source.getVariables())
                .versionStatus(WorkflowVersionStatus.DRAFT)
                .version(source.getVersion() + 1)
                .parentDefinitionId(source.getId())
                .createdBy(TenantContext.getCurrentUserId())
                .build();
        cloned.setTenantId(tenantId);

        WorkflowDefinition saved = repository.save(cloned);

        auditLogger.logEvent("WORKFLOW_DEFINITION_CLONED", "workflow:" + saved.getId(),
                "SUCCESS", "source=" + source.getId() + " newVersion=" + saved.getVersion());

        log.info("Cloned workflow definition {} -> {} (v{})", source.getId(), saved.getId(), saved.getVersion());
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        WorkflowDefinition definition = findByIdAndTenant(id, tenantId);

        if (definition.getVersionStatus() != WorkflowVersionStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT definitions can be deleted.");
        }

        repository.delete(definition);
        auditLogger.logEvent("WORKFLOW_DEFINITION_DELETED", "workflow:" + id, "SUCCESS", "");
    }

    public WorkflowDefinition findByIdAndTenant(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow definition not found: " + id));
    }

    public List<WorkflowDefinition> list(UUID tenantId, WorkflowVersionStatus statusFilter, int limit) {
        if (statusFilter != null) {
            return repository.findByTenantIdAndVersionStatusOrderByCreatedAtDesc(
                    tenantId, statusFilter, PageRequest.of(0, limit));
        }
        return repository.findByTenantIdOrderByCreatedAtDesc(tenantId, PageRequest.of(0, limit));
    }

    private void publishDefinitionEvent(String action, WorkflowDefinition definition) {
        try {
            Map<String, Object> payload = Map.of(
                    "definitionId", definition.getId().toString(),
                    "name", definition.getName(),
                    "version", definition.getVersion(),
                    "versionStatus", definition.getVersionStatus().name()
            );
            String payloadJson = objectMapper.writeValueAsString(payload);
            eventPublisher.publish(WorkflowEvents.DOMAIN, WorkflowEvents.ENTITY_DEFINITION,
                    action, WorkflowEvents.SCHEMA_VERSION, payloadJson);
        } catch (Exception e) {
            log.warn("Failed to publish workflow definition event: {}", e.getMessage());
        }
    }
}
