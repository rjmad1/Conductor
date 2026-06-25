package com.conductor.workflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.conductor.shared.workflow.WorkflowStatus;
import com.conductor.workflow.domain.WorkflowExecution;
import com.conductor.workflow.domain.WorkflowHistory;
import com.conductor.workflow.repository.WorkflowExecutionRepository;
import com.conductor.workflow.repository.WorkflowHistoryRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for WorkflowStateService — validates state machine transitions. */
@ExtendWith(MockitoExtension.class)
class WorkflowStateServiceTest {

  @Mock private WorkflowExecutionRepository executionRepository;

  @Mock private WorkflowHistoryRepository historyRepository;

  private WorkflowStateService stateService;

  @BeforeEach
  void setUp() {
    stateService = new WorkflowStateService(executionRepository, historyRepository);
  }

  private WorkflowExecution buildExecution(WorkflowStatus status) {
    WorkflowExecution e = new WorkflowExecution();
    e.setId(UUID.randomUUID());
    e.setStatus(status);
    return e;
  }

  @Test
  @DisplayName("PENDING -> RUNNING is a valid transition")
  void pendingToRunning() {
    WorkflowExecution execution = buildExecution(WorkflowStatus.PENDING);
    when(executionRepository.save(any())).thenReturn(execution);

    WorkflowExecution result =
        stateService.transition(execution, WorkflowStatus.RUNNING, null, "system");

    assertThat(result.getStatus()).isEqualTo(WorkflowStatus.RUNNING);
    assertThat(result.getStartedAt()).isNotNull();
  }

  @Test
  @DisplayName("RUNNING -> COMPLETED is a valid terminal transition")
  void runningToCompleted() {
    WorkflowExecution execution = buildExecution(WorkflowStatus.RUNNING);
    when(executionRepository.save(any())).thenReturn(execution);

    WorkflowExecution result =
        stateService.transition(execution, WorkflowStatus.COMPLETED, null, "temporal");

    assertThat(result.getStatus()).isEqualTo(WorkflowStatus.COMPLETED);
    assertThat(result.getCompletedAt()).isNotNull();
  }

  @Test
  @DisplayName("RUNNING -> FAILED sets failure reason")
  void runningToFailedWithReason() {
    WorkflowExecution execution = buildExecution(WorkflowStatus.RUNNING);
    when(executionRepository.save(any())).thenReturn(execution);

    stateService.transition(execution, WorkflowStatus.FAILED, "Step timeout", "temporal");

    assertThat(execution.getFailureReason()).isEqualTo("Step timeout");
  }

  @Test
  @DisplayName("FAILED -> COMPENSATED marks execution as compensated")
  void failedToCompensated() {
    WorkflowExecution execution = buildExecution(WorkflowStatus.FAILED);
    when(executionRepository.save(any())).thenReturn(execution);

    stateService.transition(execution, WorkflowStatus.COMPENSATED, null, "temporal");

    assertThat(execution.isCompensated()).isTrue();
  }

  @Test
  @DisplayName("COMPLETED -> RUNNING is invalid — throws IllegalStateException")
  void completedToRunningInvalid() {
    WorkflowExecution execution = buildExecution(WorkflowStatus.COMPLETED);

    assertThatThrownBy(
            () -> stateService.transition(execution, WorkflowStatus.RUNNING, null, "user"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Invalid state transition");
  }

  @Test
  @DisplayName("CANCELLED -> COMPLETED is invalid — terminal state")
  void cancelledToCompletedInvalid() {
    WorkflowExecution execution = buildExecution(WorkflowStatus.CANCELLED);

    assertThatThrownBy(
            () -> stateService.transition(execution, WorkflowStatus.COMPLETED, null, "user"))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("A history entry is recorded for every valid transition")
  void historyRecordedOnTransition() {
    WorkflowExecution execution = buildExecution(WorkflowStatus.PENDING);
    when(executionRepository.save(any())).thenReturn(execution);

    stateService.transition(execution, WorkflowStatus.RUNNING, null, "system");

    ArgumentCaptor<WorkflowHistory> historyCaptor = ArgumentCaptor.forClass(WorkflowHistory.class);
    verify(historyRepository).save(historyCaptor.capture());
    assertThat(historyCaptor.getValue().getEventType()).isEqualTo("STATE_TRANSITION");
  }

  @Test
  @DisplayName("isValidTransition correctly identifies valid paths")
  void isValidTransition() {
    assertThat(stateService.isValidTransition(WorkflowStatus.PENDING, WorkflowStatus.RUNNING))
        .isTrue();
    assertThat(stateService.isValidTransition(WorkflowStatus.RUNNING, WorkflowStatus.FAILED))
        .isTrue();
    assertThat(stateService.isValidTransition(WorkflowStatus.COMPLETED, WorkflowStatus.RUNNING))
        .isFalse();
    assertThat(stateService.isValidTransition(WorkflowStatus.COMPENSATED, WorkflowStatus.FAILED))
        .isFalse();
  }
}
