package com.conductor.shared.rules;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A condition node in the expression tree. Leaf nodes have field/operator/value; branch nodes
 * (AND/OR/NOT) have children.
 */
@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class Condition implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The variable path to evaluate (e.g., "contact.email", "trigger.amount"). Null for boolean
   * combinators.
   */
  private final String field;

  /** The operator to apply. */
  private final Operator operator;

  /** The comparison value. Null for EXISTS/NOT_EXISTS and boolean combinators. */
  private final Object value;

  /** Child conditions for AND/OR/NOT operators. */
  private final List<Condition> children;
}
