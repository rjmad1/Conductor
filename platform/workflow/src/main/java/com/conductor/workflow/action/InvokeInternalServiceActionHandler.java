package com.conductor.workflow.action;

import com.conductor.shared.execution.action.ActionContext;
import com.conductor.shared.execution.action.ActionHandler;
import com.conductor.shared.execution.action.ActionMetadata;
import com.conductor.shared.execution.action.ActionResult;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/** Built-in action to invoke Spring-managed internal services using Java reflection. */
@Component
public class InvokeInternalServiceActionHandler implements ActionHandler {

  private static final Logger log =
      LoggerFactory.getLogger(InvokeInternalServiceActionHandler.class);

  private final ApplicationContext applicationContext;

  public InvokeInternalServiceActionHandler(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public String getActionType() {
    return "INVOKE_INTERNAL_SERVICE";
  }

  @Override
  @SuppressWarnings("unchecked")
  public ActionResult execute(ActionContext context) {
    Map<String, Object> config = context.getConfiguration();
    String serviceBeanName = (String) config.get("serviceBeanName");
    String methodName = (String) config.get("methodName");
    List<?> args = (List<?>) config.get("arguments");

    try {
      Object bean = applicationContext.getBean(serviceBeanName);
      Method targetMethod = findMatchingMethod(bean.getClass(), methodName, args);
      if (targetMethod == null) {
        return ActionResult.failure(
            "METHOD_NOT_FOUND",
            String.format(
                "Method %s not found on bean %s with matching argument count",
                methodName, serviceBeanName));
      }

      Object[] params = (args != null) ? args.toArray() : new Object[0];
      Object result = targetMethod.invoke(bean, params);

      return ActionResult.success(Map.of("result", result != null ? result : "void_success"));

    } catch (Exception e) {
      log.error(
          "Failed to invoke internal service bean: {} method: {}", serviceBeanName, methodName, e);
      return ActionResult.failure("INVOCATION_ERROR", e.getMessage());
    }
  }

  private Method findMatchingMethod(Class<?> clazz, String methodName, List<?> args) {
    int argCount = (args != null) ? args.size() : 0;
    for (Method method : clazz.getMethods()) {
      if (method.getName().equals(methodName) && method.getParameterCount() == argCount) {
        return method;
      }
    }
    return null;
  }

  @Override
  public ActionMetadata getMetadata() {
    return ActionMetadata.builder()
        .actionType(getActionType())
        .name("Invoke Internal Service")
        .description("Invokes an internal Spring bean method dynamically using reflection.")
        .requiredConfigurationKeys(List.of("serviceBeanName", "methodName"))
        .supportedParameters(
            Map.of(
                "serviceBeanName", "Name of the Spring service bean",
                "methodName", "Name of the public method to execute",
                "arguments", "JSON list of arguments to pass to the method"))
        .build();
  }
}
