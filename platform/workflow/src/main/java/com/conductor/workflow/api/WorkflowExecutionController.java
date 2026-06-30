package com.conductor.workflow.api;

import com.conductor.shared.workflow.WorkflowStatus;
import com.conductor.workflow.api.dto.ExecutionResponse;
import com.conductor.workflow.api.dto.PageResponse;
import com.conductor.workflow.domain.WorkflowExecution;
import com.conductor.workflow.domain.WorkflowHistory;
import com.conductor.workflow.repository.WorkflowHistoryRepository;
import com.conductor.workflow.service.WorkflowExecutionService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST API for workflow execution management. Base path: /api/v1/workflows */
@RestController
@RequestMapping("/api/v1/workflows")
@PreAuthorize(
    "hasAnyAuthority('ROLE_TENANT_OWNER', 'ROLE_TENANT_ADMIN', 'ROLE_TENANT_AGENT', 'ROLE_PLATFORM_ADMIN')")
public class WorkflowExecutionController {

  private final WorkflowExecutionService executionService;
  private final WorkflowHistoryRepository historyRepository;

  public WorkflowExecutionController(
      WorkflowExecutionService executionService, WorkflowHistoryRepository historyRepository) {
    this.executionService = executionService;
    this.historyRepository = historyRepository;
  }

  /** POST /api/v1/workflows/{id}/execute — Start a workflow execution. */
  @PostMapping("/{id}/execute")
  public ResponseEntity<ExecutionResponse> execute(
      @PathVariable UUID id, @RequestBody(required = false) Map<String, Object> input) {

    WorkflowExecution execution = executionService.startExecution(id, input);
    return ResponseEntity.status(HttpStatus.CREATED).body(ExecutionResponse.from(execution));
  }

  /** GET /api/v1/workflows/executions — List executions for the current tenant. */
  @GetMapping("/executions")
  public ResponseEntity<PageResponse<ExecutionResponse>> listExecutions(
      @RequestParam(required = false) UUID definitionId,
      @RequestParam(required = false) WorkflowStatus status,
      @RequestParam(defaultValue = "20") int limit) {

    List<WorkflowExecution> executions =
        executionService.listExecutions(definitionId, status, limit);
    List<ExecutionResponse> data =
        executions.stream().map(ExecutionResponse::from).collect(Collectors.toList());

    return ResponseEntity.ok(
        PageResponse.<ExecutionResponse>builder()
            .data(data)
            .count(data.size())
            .hasMore(data.size() == limit)
            .build());
  }

  /** GET /api/v1/workflows/executions/{executionId} — Get a single execution. */
  @GetMapping("/executions/{executionId}")
  public ResponseEntity<ExecutionResponse> getExecution(@PathVariable UUID executionId) {
    return ResponseEntity.ok(ExecutionResponse.from(executionService.getExecution(executionId)));
  }

  /** POST /api/v1/workflows/executions/{executionId}/cancel — Cancel an execution. */
  @PostMapping("/executions/{executionId}/cancel")
  public ResponseEntity<ExecutionResponse> cancel(@PathVariable UUID executionId) {
    return ResponseEntity.ok(ExecutionResponse.from(executionService.cancelExecution(executionId)));
  }

  /** POST /api/v1/workflows/executions/{executionId}/replay — Replay an execution. */
  @PostMapping("/executions/{executionId}/replay")
  public ResponseEntity<ExecutionResponse> replay(@PathVariable UUID executionId) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ExecutionResponse.from(executionService.replayExecution(executionId)));
  }

  /** GET /api/v1/workflows/executions/{executionId}/history — Get execution history. */
  @GetMapping("/executions/{executionId}/history")
  public ResponseEntity<List<WorkflowHistory>> getHistory(@PathVariable UUID executionId) {
    // Validate tenant access by loading execution first
    executionService.getExecution(executionId);
    List<WorkflowHistory> history =
        historyRepository.findByExecutionIdOrderByTimestampAsc(executionId);
    return ResponseEntity.ok(history);
  }
}
