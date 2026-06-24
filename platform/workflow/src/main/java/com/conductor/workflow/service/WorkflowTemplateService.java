package com.conductor.workflow.service;

import com.conductor.shared.middleware.tenant.TenantContext;
import com.conductor.shared.templates.WorkflowTemplateDefinition;
import com.conductor.shared.workflow.WorkflowVersionStatus;
import com.conductor.workflow.domain.WorkflowDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads built-in workflow templates from classpath resources and allows tenants
 * to instantiate them as customized workflow definitions.
 * Templates are loaded once at startup and cached in memory.
 */
@Service
public class WorkflowTemplateService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTemplateService.class);
    private static final String TEMPLATE_LOCATION = "classpath:templates/*.json";

    private final WorkflowDefinitionService definitionService;
    private final ObjectMapper objectMapper;

    /** In-memory cache of loaded templates keyed by templateId. */
    private final Map<String, WorkflowTemplateDefinition> templates = new ConcurrentHashMap<>();

    public WorkflowTemplateService(WorkflowDefinitionService definitionService) {
        this.definitionService = definitionService;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void loadTemplates() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            org.springframework.core.io.Resource[] resources = resolver.getResources(TEMPLATE_LOCATION);

            for (org.springframework.core.io.Resource resource : resources) {
                try {
                    WorkflowTemplateDefinition template = objectMapper.readValue(
                            resource.getInputStream(), WorkflowTemplateDefinition.class);
                    templates.put(template.getTemplateId(), template);
                    log.info("Loaded workflow template: {} ({})", template.getTemplateId(), template.getName());
                } catch (Exception e) {
                    log.warn("Failed to load template from {}: {}", resource.getFilename(), e.getMessage());
                }
            }
            log.info("Loaded {} workflow templates", templates.size());
        } catch (Exception e) {
            log.error("Failed to scan workflow templates: {}", e.getMessage());
        }
    }

    /**
     * Returns all available built-in templates.
     */
    public List<WorkflowTemplateDefinition> listTemplates() {
        return new ArrayList<>(templates.values());
    }

    /**
     * Returns a specific template by ID.
     */
    public WorkflowTemplateDefinition getTemplate(String templateId) {
        WorkflowTemplateDefinition template = templates.get(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateId);
        }
        return template;
    }

    /**
     * Instantiates a workflow definition from a template for the current tenant.
     * Optionally overrides template variables with provided values.
     *
     * @param templateId       the template to instantiate
     * @param nameOverride     optional custom name for the definition
     * @param variableOverrides optional variable overrides
     * @return the created DRAFT workflow definition
     */
    public WorkflowDefinition instantiate(String templateId, String nameOverride,
                                           Map<String, Object> variableOverrides) {
        WorkflowTemplateDefinition template = getTemplate(templateId);

        // Merge template variables with overrides
        Map<String, Object> mergedVariables = new ConcurrentHashMap<>();
        if (template.getVariables() != null) {
            mergedVariables.putAll(template.getVariables());
        }
        if (variableOverrides != null) {
            mergedVariables.putAll(variableOverrides);
        }

        String name = nameOverride != null && !nameOverride.isBlank()
                ? nameOverride
                : template.getName();

        try {
            WorkflowDefinition definition = WorkflowDefinition.builder()
                    .name(name)
                    .description("Created from template: " + template.getName() + " v" + template.getVersion())
                    .triggerType(template.getTriggerType())
                    .triggerConfig(objectMapper.writeValueAsString(
                            template.getTriggerConfig() != null ? template.getTriggerConfig() : Map.of()))
                    .steps(objectMapper.writeValueAsString(
                            template.getSteps() != null ? template.getSteps() : List.of()))
                    .variables(objectMapper.writeValueAsString(mergedVariables))
                    .versionStatus(WorkflowVersionStatus.DRAFT)
                    .version(1)
                    .createdBy(TenantContext.getCurrentUserId())
                    .build();

            return definitionService.create(definition);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate template " + templateId + ": " + e.getMessage(), e);
        }
    }
}
