package com.conductor.shared.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.conductor.shared.execution.ExecutionContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for ConditionEvaluator covering all operators and boolean combinators. */
class ConditionEvaluatorTest {

  private ConditionEvaluator evaluator;
  private ExecutionContext context;

  @BeforeEach
  void setUp() {
    evaluator = new ConditionEvaluator();
    context =
        ExecutionContext.builder()
            .workflowDefinitionId(UUID.randomUUID())
            .executionId(UUID.randomUUID())
            .tenantId("test-tenant")
            .variables(
                Map.of(
                    "status", "active",
                    "amount", "500",
                    "email", "user@example.com",
                    "nested", Map.of("field", "value"),
                    "flag", "true"))
            .build();
  }

  @Test
  @DisplayName("EQUALS returns true when field matches value")
  void equalsTrue() {
    Condition c =
        Condition.builder().field("status").operator(Operator.EQUALS).value("active").build();
    assertThat(evaluator.evaluate(c, context)).isTrue();
  }

  @Test
  @DisplayName("EQUALS returns false when field does not match")
  void equalsFalse() {
    Condition c =
        Condition.builder().field("status").operator(Operator.EQUALS).value("inactive").build();
    assertThat(evaluator.evaluate(c, context)).isFalse();
  }

  @Test
  @DisplayName("NOT_EQUALS returns true when value differs")
  void notEquals() {
    Condition c =
        Condition.builder().field("status").operator(Operator.NOT_EQUALS).value("inactive").build();
    assertThat(evaluator.evaluate(c, context)).isTrue();
  }

  @Test
  @DisplayName("CONTAINS returns true when field contains substring")
  void contains() {
    Condition c =
        Condition.builder().field("email").operator(Operator.CONTAINS).value("@example").build();
    assertThat(evaluator.evaluate(c, context)).isTrue();
  }

  @Test
  @DisplayName("GREATER_THAN returns true when numeric field exceeds value")
  void greaterThan() {
    Condition c =
        Condition.builder().field("amount").operator(Operator.GREATER_THAN).value("100").build();
    assertThat(evaluator.evaluate(c, context)).isTrue();
  }

  @Test
  @DisplayName("LESS_THAN returns true when numeric field is below value")
  void lessThan() {
    Condition c =
        Condition.builder().field("amount").operator(Operator.LESS_THAN).value("1000").build();
    assertThat(evaluator.evaluate(c, context)).isTrue();
  }

  @Test
  @DisplayName("EXISTS returns true for present field")
  void exists() {
    Condition c = Condition.builder().field("status").operator(Operator.EXISTS).build();
    assertThat(evaluator.evaluate(c, context)).isTrue();
  }

  @Test
  @DisplayName("EXISTS returns false for absent field")
  void existsAbsent() {
    Condition c = Condition.builder().field("missingField").operator(Operator.EXISTS).build();
    assertThat(evaluator.evaluate(c, context)).isFalse();
  }

  @Test
  @DisplayName("NOT_EXISTS returns true for absent field")
  void notExists() {
    Condition c = Condition.builder().field("ghost").operator(Operator.NOT_EXISTS).build();
    assertThat(evaluator.evaluate(c, context)).isTrue();
  }

  @Test
  @DisplayName("AND is true when all children are true")
  void andAllTrue() {
    Condition c =
        Condition.builder()
            .operator(Operator.AND)
            .children(
                List.of(
                    Condition.builder()
                        .field("status")
                        .operator(Operator.EQUALS)
                        .value("active")
                        .build(),
                    Condition.builder()
                        .field("amount")
                        .operator(Operator.GREATER_THAN)
                        .value("0")
                        .build()))
            .build();
    assertThat(evaluator.evaluate(c, context)).isTrue();
  }

  @Test
  @DisplayName("AND is false when any child is false")
  void andOneFalse() {
    Condition c =
        Condition.builder()
            .operator(Operator.AND)
            .children(
                List.of(
                    Condition.builder()
                        .field("status")
                        .operator(Operator.EQUALS)
                        .value("active")
                        .build(),
                    Condition.builder()
                        .field("amount")
                        .operator(Operator.GREATER_THAN)
                        .value("1000")
                        .build()))
            .build();
    assertThat(evaluator.evaluate(c, context)).isFalse();
  }

  @Test
  @DisplayName("OR is true when any child is true")
  void orOneTrue() {
    Condition c =
        Condition.builder()
            .operator(Operator.OR)
            .children(
                List.of(
                    Condition.builder()
                        .field("status")
                        .operator(Operator.EQUALS)
                        .value("inactive")
                        .build(),
                    Condition.builder()
                        .field("amount")
                        .operator(Operator.GREATER_THAN)
                        .value("100")
                        .build()))
            .build();
    assertThat(evaluator.evaluate(c, context)).isTrue();
  }

  @Test
  @DisplayName("NOT negates the child condition")
  void notNegates() {
    Condition c =
        Condition.builder()
            .operator(Operator.NOT)
            .children(
                List.of(
                    Condition.builder()
                        .field("status")
                        .operator(Operator.EQUALS)
                        .value("inactive")
                        .build()))
            .build();
    assertThat(evaluator.evaluate(c, context)).isTrue();
  }

  @Test
  @DisplayName("Dot-notation resolves nested variable path")
  void dotNotationResolution() {
    Condition c =
        Condition.builder().field("nested.field").operator(Operator.EQUALS).value("value").build();
    assertThat(evaluator.evaluate(c, context)).isTrue();
  }

  @Test
  @DisplayName("Null condition always returns true (step always executes)")
  void nullCondition() {
    assertThat(evaluator.evaluate(null, context)).isTrue();
  }
}
