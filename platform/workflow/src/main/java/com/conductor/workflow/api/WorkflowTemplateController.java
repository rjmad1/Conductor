package com.conductor.workflow.api;

import com.conductor.shared.templates.WorkflowTemplateDefinition;
import com.conductor.workflow.api.dto.WorkflowDefinitionResponse;
import com.conductor.workflow.domain.WorkflowDefinition;
import com.conductor.workflow.service.WorkflowTemplateService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API for workflow template management. Base path: /api/v1/workflows/templates */
@RestController
@RequestMapping("/api/v1/workflows/templates")
@PreAuthorize(
    "hasAnyAuthority('ROLE_TENANT_OWNER', 'ROLE_TENANT_ADMIN', 'ROLE_TENANT_AGENT', 'ROLE_PLATFORM_ADMIN')")
public class WorkflowTemplateController {

  private final WorkflowTemplateService templateService;

  public WorkflowTemplateController(WorkflowTemplateService templateService) {
    this.templateService = templateService;
  }

  /** GET /api/v1/workflows/templates — List all available built-in templates. */
  @GetMapping
  public ResponseEntity<List<WorkflowTemplateDefinition>> listTemplates() {
    return ResponseEntity.ok(templateService.listTemplates());
  }

  /** GET /api/v1/workflows/templates/{templateId} — Get a single template. */
  @GetMapping("/{templateId}")
  public ResponseEntity<WorkflowTemplateDefinition> getTemplate(@PathVariable String templateId) {
    return ResponseEntity.ok(templateService.getTemplate(templateId));
  }

  /**
   * POST /api/v1/workflows/templates/{templateId}/instantiate Creates a DRAFT workflow definition
   * from the template. Body (optional): { "name": "...", "variables": { ... } }
   */
  @PostMapping("/{templateId}/instantiate")
  public ResponseEntity<WorkflowDefinitionResponse> instantiate(
      @PathVariable String templateId, @RequestBody(required = false) Map<String, Object> body) {

    String name = body != null ? (String) body.get("name") : null;
    @SuppressWarnings("unchecked")
    Map<String, Object> variables =
        body != null ? (Map<String, Object>) body.get("variables") : null;

    WorkflowDefinition definition = templateService.instantiate(templateId, name, variables);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(WorkflowDefinitionResponse.from(definition));
  }
}
