package com.rajajeevan.loop.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * Agnostic Loop Engine HTTP Server. Exposes a language-neutral REST API for any agent stack
 * (Python, TypeScript, Bash, Autogen, etc.) to submit state transition requests.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>POST /api/v1/loops/transit — evaluate and execute a loop phase transition
 *   <li>GET /api/v1/loops/{loopId}/status — retrieve current loop state and circuit breaker status
 *   <li>GET /health — liveness probe
 * </ul>
 */
public class LoopEngineServer {

  private final LoopStateMachine stateMachine;
  private final ObjectMapper objectMapper;
  private final Javalin app;

  public LoopEngineServer() {
    this(0); // 0 = auto-pick port (used by tests); production uses main() with 8080
  }

  public LoopEngineServer(int port) {
    this.stateMachine = new LoopStateMachine();
    this.objectMapper = new ObjectMapper();
    this.app = createApp();
    if (port > 0) {
      this.app.start(port);
    }
  }

  /** Returns the Javalin instance (for test lifecycle management). */
  public Javalin getApp() {
    return app;
  }

  /** Returns the underlying state machine (for pre-registering loops in tests). */
  public LoopStateMachine getStateMachine() {
    return stateMachine;
  }

  private Javalin createApp() {
    Javalin javalin =
        Javalin.create(
            config -> {
              config.showJavalinBanner = false;
            });

    javalin.get("/health", ctx -> ctx.json(java.util.Map.of("status", "UP")));
    javalin.post("/api/v1/loops/transit", this::handleTransit);
    javalin.get("/api/v1/loops/{loopId}/status", this::handleStatus);

    return javalin;
  }

  private void handleTransit(Context ctx) {
    try {
      TransitRequest request = objectMapper.readValue(ctx.body(), TransitRequest.class);

      if (request.getLoopId() == null || request.getLoopId().isBlank()) {
        ctx.status(400).json(errorMap("loop_id is required"));
        return;
      }
      if (request.getSourcePhase() == null || request.getTargetPhase() == null) {
        ctx.status(400).json(errorMap("source_phase and target_phase are required"));
        return;
      }

      TransitResponse response = stateMachine.evaluateTransition(request);
      if ("UNKNOWN".equals(response.getCurrentState())) {
        ctx.status(400).json(response);
      } else {
        ctx.status(response.isTransitionAllowed() ? 200 : 409).json(response);
      }
    } catch (Exception e) {
      ctx.status(400).json(errorMap("Invalid request body: " + e.getMessage()));
    }
  }

  private void handleStatus(Context ctx) {
    String loopId = ctx.pathParam("loopId");
    LoopInstance loop = stateMachine.getLoop(loopId);

    if (loop == null) {
      ctx.status(404).json(errorMap("Loop not found: " + loopId));
      return;
    }

    TransitResponse response = stateMachine.buildStatusResponse(loop);
    ctx.json(response);
  }

  private java.util.Map<String, Object> errorMap(String message) {
    return java.util.Map.of("transition_allowed", false, "message", message);
  }

  /** Production entry point. Starts on port 8080. */
  public static void main(String[] args) {
    int port = 8080;
    if (args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException ignored) {
        // use default
      }
    }
    System.out.println("Starting Agnostic Loop Engine on port " + port + "...");
    new LoopEngineServer(port);
    System.out.println("Loop Engine is running. POST /api/v1/loops/transit to begin.");
  }
}
