package com.conductor.shared.rules;

/**
 * Comparison and logical operators for condition evaluation.
 */
public enum Operator {
    // Comparison
    EQUALS,
    NOT_EQUALS,
    CONTAINS,
    GREATER_THAN,
    LESS_THAN,
    EXISTS,
    NOT_EXISTS,

    // Boolean combinators
    AND,
    OR,
    NOT
}
