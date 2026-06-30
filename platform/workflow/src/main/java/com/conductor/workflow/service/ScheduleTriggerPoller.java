package com.conductor.workflow.service;

import com.conductor.shared.workflow.TriggerType;
import com.conductor.shared.workflow.WorkflowVersionStatus;
import com.conductor.workflow.domain.WorkflowDefinition;
import com.conductor.workflow.repository.WorkflowDefinitionRepository;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduleTriggerPoller {

  private static final Logger log = LoggerFactory.getLogger(ScheduleTriggerPoller.class);
  private final WorkflowDefinitionRepository definitionRepository;
  private final TriggerService triggerService;

  public ScheduleTriggerPoller(
      WorkflowDefinitionRepository definitionRepository, TriggerService triggerService) {
    this.definitionRepository = definitionRepository;
    this.triggerService = triggerService;
  }

  // Poll every minute
  @Scheduled(fixedRate = 60000)
  public void pollSchedules() {
    List<WorkflowDefinition> definitions =
        definitionRepository.findByTriggerTypeAndVersionStatus(
            TriggerType.SCHEDULE, WorkflowVersionStatus.PUBLISHED);

    for (WorkflowDefinition def : definitions) {
      log.debug("Evaluating schedule for workflow {}", def.getId());
      // For MVP, if it is scheduled, just fire it every minute (for demonstration).
      // In production, we'd parse cron expressions and shedlock distributed execution.
      try {
        triggerService.fireTrigger(
            def.getTenantId(),
            TriggerType.SCHEDULE,
            Map.of("scheduledTime", System.currentTimeMillis()));
      } catch (Exception e) {
        log.error("Failed to fire schedule trigger for workflow {}", def.getId(), e);
      }
    }
  }
}
