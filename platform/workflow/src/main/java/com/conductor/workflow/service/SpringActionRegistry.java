package com.conductor.workflow.service;

import com.conductor.shared.execution.action.ActionHandler;
import com.conductor.shared.execution.action.ActionRegistry;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Spring-backed implementation of ActionRegistry. Auto-wires all ActionHandler beans in the
 * application context and maps them by their action type.
 */
@Service
public class SpringActionRegistry implements ActionRegistry {

  private final Map<String, ActionHandler> handlers = new ConcurrentHashMap<>();

  public SpringActionRegistry(Collection<ActionHandler> actionHandlers) {
    if (actionHandlers != null) {
      for (ActionHandler handler : actionHandlers) {
        handlers.put(handler.getActionType().toUpperCase(), handler);
      }
    }
  }

  @Override
  public Optional<ActionHandler> getHandler(String actionType) {
    if (actionType == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(handlers.get(actionType.toUpperCase()));
  }

  @Override
  public Collection<ActionHandler> getHandlers() {
    return handlers.values();
  }
}
