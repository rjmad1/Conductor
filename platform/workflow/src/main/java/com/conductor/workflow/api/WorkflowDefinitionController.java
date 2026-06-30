package com.conductor.workflow.api;

import com.conductor.shared.middleware.tenant.TenantContext;
import com.conductor.shared.workflow.WorkflowVersionStatus;
import com.conductor.workflow.api.dto.PageResponse;
import com.conductor.workflow.api.dto.WorkflowDefinitionRequest;
import com.conductor.workflow.api.dto.WorkflowDefinitionResponse;
import com.conductor.workflow.domain.WorkflowDefinition;
import com.conductor.workflow.service.WorkflowDefinitionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for workflow definition lifecycle management. Base path: /api/v1/workflows Auth:
 * OIDC-protected via shared security middleware.
 */
@RestController
@RequestMapping("/api/v1/workflows")
@PreAuthorize(
    "hasAnyAuthority('ROLE_TENANT_OWNER', 'ROLE_TENANT_ADMIN', 'ROLE_TENANT_AGENT', 'ROLE_PLATFORM_ADMIN')")
public class WorkflowDefinitionController {

  private final WorkflowDefinitionService service;
  private final ObjectMapper objectMapper;

  public WorkflowDefinitionController(WorkflowDefinitionService service) {
    this.service = service;
    this.objectMapper = new ObjectMapper();
  }

  /** POST /api/v1/workflows — Create a new workflow definition (DRAFT). */
  @PostMapping
  public ResponseEntity<WorkflowDefinitionResponse> create(
      @Valid @RequestBody WorkflowDefinitionRequest request) throws JsonProcessingException {

    WorkflowDefinition definition = mapRequestToDomain(request);
    WorkflowDefinition created = service.create(definition);
    return ResponseEntity.status(HttpStatus.CREATED).body(WorkflowDefinitionResponse.from(created));
  }

  /** GET /api/v1/workflows — List definitions for the current tenant. */
  @GetMapping
  public ResponseEntity<PageResponse<WorkflowDefinitionResponse>> list(
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(required = false) WorkflowVersionStatus status) {

    UUID tenantId = TenantContext.getCurrentTenantId();
    List<WorkflowDefinition> definitions = service.list(tenantId, status, limit);
    List<WorkflowDefinitionResponse> data =
        definitions.stream().map(WorkflowDefinitionResponse::from).collect(Collectors.toList());

    return ResponseEntity.ok(
        PageResponse.<WorkflowDefinitionResponse>builder()
            .data(data)
            .count(data.size())
            .hasMore(data.size() == limit)
            .build());
  }

  /** GET /api/v1/workflows/{id} — Get a single workflow definition. */
  @GetMapping("/{id}")
  public ResponseEntity<WorkflowDefinitionResponse> get(@PathVariable UUID id) {
    UUID tenantId = TenantContext.getCurrentTenantId();
    WorkflowDefinition definition = service.findByIdAndTenant(id, tenantId);
    return ResponseEntity.ok(WorkflowDefinitionResponse.from(definition));
  }

  /** PUT /api/v1/workflows/{id} — Update a DRAFT workflow definition. */
  @PutMapping("/{id}")
  public ResponseEntity<WorkflowDefinitionResponse> update(
      @PathVariable UUID id, @Valid @RequestBody WorkflowDefinitionRequest request)
      throws JsonProcessingException {

    WorkflowDefinition updates = mapRequestToDomain(request);
    WorkflowDefinition updated = service.update(id, updates);
    return ResponseEntity.ok(WorkflowDefinitionResponse.from(updated));
  }

  /** POST /api/v1/workflows/{id}/publish — Publish a DRAFT definition. */
  @PostMapping("/{id}/publish")
  public ResponseEntity<WorkflowDefinitionResponse> publish(@PathVariable UUID id) {
    return ResponseEntity.ok(WorkflowDefinitionResponse.from(service.publish(id)));
  }

  /** POST /api/v1/workflows/{id}/deprecate — Deprecate a PUBLISHED definition. */
  @PostMapping("/{id}/deprecate")
  public ResponseEntity<WorkflowDefinitionResponse> deprecate(@PathVariable UUID id) {
    return ResponseEntity.ok(WorkflowDefinitionResponse.from(service.deprecate(id)));
  }

  /** POST /api/v1/workflows/{id}/archive — Archive a DEPRECATED definition. */
  @PostMapping("/{id}/archive")
  public ResponseEntity<WorkflowDefinitionResponse> archive(@PathVariable UUID id) {
    return ResponseEntity.ok(WorkflowDefinitionResponse.from(service.archive(id)));
  }

  /** POST /api/v1/workflows/{id}/clone — Clone a definition as a new DRAFT version. */
  @PostMapping("/{id}/clone")
  public ResponseEntity<WorkflowDefinitionResponse> clone(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(WorkflowDefinitionResponse.from(service.clone(id)));
  }

  /** DELETE /api/v1/workflows/{id} — Delete a DRAFT definition. */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  private WorkflowDefinition mapRequestToDomain(WorkflowDefinitionRequest request)
      throws JsonProcessingException {
    return WorkflowDefinition.builder()
        .name(request.getName())
        .description(request.getDescription())
        .triggerType(request.getTriggerType())
        .triggerConfig(
            objectMapper.writeValueAsString(
                request.getTriggerConfig() != null
                    ? request.getTriggerConfig()
                    : java.util.Map.of()))
        .steps(
            objectMapper.writeValueAsString(
                request.getSteps() != null ? request.getSteps() : java.util.List.of()))
        .variables(
            objectMapper.writeValueAsString(
                request.getVariables() != null ? request.getVariables() : java.util.Map.of()))
        .createdBy(TenantContext.getCurrentUserId())
        .build();
  }
}
