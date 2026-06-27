package com.conductor.shared.rules;

import com.conductor.shared.execution.ExecutionContext;
import java.util.List;
import java.util.Map;

/**
 * Evaluates condition expressions against an execution context. Supports comparison operators,
 * existence checks, and boolean combinators (AND/OR/NOT). Variables are resolved from the execution
 * context's variables map using dot-notation paths.
 */
public class ConditionEvaluator {

  /**
   * Evaluates a condition tree against the given execution context.
   *
   * @param condition the condition to evaluate
   * @param context the execution context providing variable values
   * @return true if the condition is satisfied
   */
  public boolean evaluate(Condition condition, ExecutionContext context) {
    if (condition == null) {
      return true;
    }
    return evaluateNode(condition, context.getVariables());
  }

  private boolean evaluateNode(Condition condition, Map<String, Object> variables) {
    Operator op = condition.getOperator();

    switch (op) {
      case AND:
        return evaluateAnd(condition.getChildren(), variables);
      case OR:
        return evaluateOr(condition.getChildren(), variables);
      case NOT:
        return evaluateNot(condition.getChildren(), variables);
      default:
        return evaluateComparison(condition, variables);
    }
  }

  private boolean evaluateAnd(List<Condition> children, Map<String, Object> variables) {
    if (children == null || children.isEmpty()) {
      return true;
    }
    for (Condition child : children) {
      if (!evaluateNode(child, variables)) {
        return false;
      }
    }
    return true;
  }

  private boolean evaluateOr(List<Condition> children, Map<String, Object> variables) {
    if (children == null || children.isEmpty()) {
      return false;
    }
    for (Condition child : children) {
      if (evaluateNode(child, variables)) {
        return true;
      }
    }
    return false;
  }

  private boolean evaluateNot(List<Condition> children, Map<String, Object> variables) {
    if (children == null || children.isEmpty()) {
      return true;
    }
    return !evaluateNode(children.get(0), variables);
  }

  private boolean evaluateComparison(Condition condition, Map<String, Object> variables) {
    String field = condition.getField();
    Object resolvedValue = resolveVariable(field, variables);
    Object expectedValue = condition.getValue();

    switch (condition.getOperator()) {
      case EXISTS:
        return resolvedValue != null;
      case NOT_EXISTS:
        return resolvedValue == null;
      case EQUALS:
        return resolvedValue != null && resolvedValue.toString().equals(toString(expectedValue));
      case NOT_EQUALS:
        return resolvedValue == null || !resolvedValue.toString().equals(toString(expectedValue));
      case CONTAINS:
        return resolvedValue != null && resolvedValue.toString().contains(toString(expectedValue));
      case GREATER_THAN:
        return compareNumeric(resolvedValue, expectedValue) > 0;
      case LESS_THAN:
        return compareNumeric(resolvedValue, expectedValue) < 0;
      default:
        return false;
    }
  }

  /**
   * Resolves a dot-notation variable path from the variables map. E.g., "contact.email" resolves
   * variables.get("contact") -> map.get("email").
   */
  @SuppressWarnings("unchecked")
  private Object resolveVariable(String path, Map<String, Object> variables) {
    if (path == null || variables == null) {
      return null;
    }
    String[] segments = path.split("\\.");
    Object current = variables;
    for (String segment : segments) {
      if (current instanceof Map) {
        current = ((Map<String, Object>) current).get(segment);
      } else {
        return null;
      }
    }
    return current;
  }

  private int compareNumeric(Object resolved, Object expected) {
    if (resolved == null || expected == null) {
      return 0;
    }
    try {
      double resolvedNum = Double.parseDouble(resolved.toString());
      double expectedNum = Double.parseDouble(expected.toString());
      return Double.compare(resolvedNum, expectedNum);
    } catch (NumberFormatException e) {
      return resolved.toString().compareTo(expected.toString());
    }
  }

  private String toString(Object value) {
    return value != null ? value.toString() : "";
  }
}
