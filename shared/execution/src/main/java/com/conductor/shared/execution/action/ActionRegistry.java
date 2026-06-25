package com.conductor.shared.execution.action;

import java.util.Collection;
import java.util.Optional;

/** Interface for registering and resolving action execution handlers. */
public interface ActionRegistry {

  /** Resolves the ActionHandler for a given action type name. */
  Optional<ActionHandler> getHandler(String actionType);

  /** Returns all registered action handlers. */
  Collection<ActionHandler> getHandlers();
}
