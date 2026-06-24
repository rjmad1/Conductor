# Agent Framework — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** Agent Architecture  
**Last Updated:** June 2026

---

## Purpose
Implementation guide for Conductor's AI agent framework — the infrastructure that manages multi-turn AI conversations within WhatsApp.

---

## Framework Components

```
┌──────────────────────────────────────────────────────────┐
│                    AGENT FRAMEWORK                        │
│                                                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │  Agent      │  │  Tool       │  │  State          │  │
│  │  Registry   │  │  Executor   │  │  Manager        │  │
│  └──────┬──────┘  └──────┬──────┘  └────────┬────────┘  │
│         │                │                   │           │
│  ┌──────▼──────────────────────────────────▼────────┐   │
│  │                 Agent Controller                  │   │
│  │  Manages conversation flow, model calls, tools   │   │
│  └──────────────────────┬────────────────────────────┘   │
│                         │                                 │
│  ┌──────────────────────▼────────────────────────────┐   │
│  │                  Dify Client                       │   │
│  │  HTTP client for Dify API (LLM + RAG + tools)     │   │
│  └───────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
```

---

## Agent Registry

Maps intent types and triggers to specific agent configurations:

```java
@Component
public class AgentRegistry {
    
    private final Map<String, AgentDefinition> agents = Map.of(
        "appointment_booking",  AgentDefinition.of("appointment_agent_system_v1", List.of("get_availability", "book_appointment")),
        "appointment_cancel",   AgentDefinition.of("appointment_agent_system_v1", List.of("get_appointment", "cancel_appointment")),
        "lead_qualification",   AgentDefinition.of("lead_qualification_v1", List.of("lookup_customer", "create_lead")),
        "faq",                  AgentDefinition.of("faq_bot_system_v2", List.of("search_knowledge_base"))
    );
    
    public Optional<AgentDefinition> findAgent(String intent) {
        return Optional.ofNullable(agents.get(intent));
    }
}
```

---

## Agent Controller

The core orchestration logic for a single agent conversation:

```java
@Service
public class AgentController {
    
    public AgentResponse processMessage(
            AgentSession session, 
            String customerMessage,
            AgentDefinition agentDef) {
        
        // 1. Check turn limit
        if (session.getTurnCount() >= agentDef.getMaxTurns()) {
            return AgentResponse.escalate("Agent reached max turns");
        }
        
        // 2. Build message history for context
        List<DifyMessage> history = buildMessageHistory(session);
        
        // 3. Call Dify (which calls LLM with tools)
        DifyResponse difyResponse = difyClient.chat(
            DifyChatRequest.builder()
                .appId(agentDef.getDifyAppId())
                .query(customerMessage)
                .conversationId(session.getDifyConversationId())
                .inputs(buildInputs(session))
                .build()
        );
        
        // 4. Parse tool calls from response (if any)
        if (difyResponse.hasToolCall()) {
            ToolResult toolResult = toolExecutor.execute(
                difyResponse.getToolCall(), 
                session
            );
            // Tool results fed back to Dify in next turn
            difyResponse = difyClient.continueWithToolResult(
                session.getDifyConversationId(), toolResult
            );
        }
        
        // 5. Check for escalation signal
        if (difyResponse.requiresEscalation()) {
            return AgentResponse.escalate(difyResponse.getEscalationReason());
        }
        
        // 6. Update session state
        session.incrementTurnCount();
        session.setDifyConversationId(difyResponse.getConversationId());
        stateManager.save(session);
        
        // 7. Return response message
        return AgentResponse.message(difyResponse.getAnswer());
    }
}
```

---

## Tool Executor

Executes tools requested by the LLM:

```java
@Component
public class ToolExecutor {
    
    public ToolResult execute(ToolCall toolCall, AgentSession session) {
        return switch (toolCall.getName()) {
            case "get_availability"      -> getAvailability(toolCall.getArgs(), session);
            case "book_appointment"      -> bookAppointment(toolCall.getArgs(), session);
            case "cancel_appointment"    -> cancelAppointment(toolCall.getArgs(), session);
            case "lookup_customer"       -> lookupCustomer(session.getCustomerId());
            case "create_lead"          -> createLead(toolCall.getArgs(), session);
            case "search_knowledge_base" -> searchKnowledgeBase(toolCall.getArgs(), session);
            case "generate_payment_link" -> generatePaymentLink(toolCall.getArgs(), session);
            default -> ToolResult.error("Unknown tool: " + toolCall.getName());
        };
    }
    
    private ToolResult getAvailability(Map<String, Object> args, AgentSession session) {
        // Fetch from Google Calendar connector for this tenant
        ConnectorConfig calendarConfig = connectorService.getConfig(
            session.getTenantId(), "google_calendar"
        );
        
        List<TimeSlot> slots = calendarConnector.getAvailableSlots(
            calendarConfig,
            (String) args.get("date_range"),
            (int) args.get("duration_minutes")
        );
        
        return ToolResult.success(Map.of("available_slots", slots));
    }
}
```

---

## State Manager

Manages agent session state in Redis:

```java
@Component
public class AgentStateManager {
    
    private final RedisTemplate<String, AgentSession> redis;
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);
    
    private String sessionKey(String tenantId, String customerId) {
        return "agent_session:%s:%s".formatted(tenantId, customerId);
    }
    
    public Optional<AgentSession> getSession(String tenantId, String customerId) {
        return Optional.ofNullable(redis.opsForValue().get(sessionKey(tenantId, customerId)));
    }
    
    public void save(AgentSession session) {
        redis.opsForValue().set(
            sessionKey(session.getTenantId(), session.getCustomerId()),
            session,
            SESSION_TTL
        );
    }
    
    public void clearSession(String tenantId, String customerId) {
        redis.delete(sessionKey(tenantId, customerId));
    }
}
```

---

## Agent Session Schema (Redis)

```json
{
  "session_id": "sess-uuid",
  "tenant_id": "t-uuid",
  "customer_id": "c-uuid",
  "agent_type": "appointment_booking",
  "agent_definition_id": "appointment_agent_system_v1",
  "dify_conversation_id": "dify-conv-uuid",
  "turn_count": 3,
  "max_turns": 8,
  "collected_data": {
    "appointment_type": "consultation",
    "preferred_date": "2026-06-20",
    "duration_minutes": 30
  },
  "tool_results": {
    "get_availability": {
      "slots": [
        { "start": "2026-06-20T10:00:00", "end": "2026-06-20T10:30:00" },
        { "start": "2026-06-20T15:00:00", "end": "2026-06-20T15:30:00" }
      ]
    }
  },
  "started_at": "2026-06-15T10:00:00Z",
  "last_activity_at": "2026-06-15T10:08:00Z",
  "status": "active"
}
```

---

## Agent Activation Flow (in conversation-service)

```
1. Inbound customer message received
2. Intent classifier runs → detects "appointment_booking" with confidence 0.94
3. Check: Is there an active agent session for this customer?
   YES → resume existing session with AgentController
   NO → check: Does a workflow exist for this intent? Does it invoke an agent?
        YES → start new AgentSession, invoke AgentController
        NO → return to standard workflow/conversation engine
4. AgentController processes message, calls tools, generates response
5. AgentController checks: is task complete? is escalation needed?
   COMPLETE → clear session, send completion message, trigger outcome events
   ESCALATE → clear session, route to Chatwoot agent inbox
   CONTINUE → update session, send agent response to customer
```

---

## Escalation Handling

When an agent cannot complete the task:

```java
private void handleEscalation(AgentResponse escalation, AgentSession session) {
    // 1. Clear agent session
    stateManager.clearSession(session.getTenantId(), session.getCustomerId());
    
    // 2. Send escalation message to customer
    String escalationMessage = 
        "Let me connect you with one of our team members who can help better. Please hold on.";
    whatsappAdapter.sendTextMessage(session.getCustomerPhone(), escalationMessage);
    
    // 3. Route to Chatwoot
    chatwootClient.createConversation(
        ChatwootConversation.builder()
            .tenantId(session.getTenantId())
            .customerId(session.getCustomerId())
            .escalationReason(escalation.getReason())
            .conversationHistory(session.getHistory())
            .priority("normal")
            .build()
    );
    
    // 4. Publish escalation event
    eventBus.publish(Event.of("agent.escalated", session.getTenantId(), Map.of(
        "customer_id", session.getCustomerId(),
        "agent_type", session.getAgentType(),
        "escalation_reason", escalation.getReason(),
        "turns_completed", session.getTurnCount()
    )));
}
```

---

## Dify App Setup (Per Agent Type)

Each agent type requires a Dify app configured with:
1. System prompt (from Prompt Library)
2. Knowledge base connection (for FAQ bot)
3. Tool definitions (JSON schema for each tool)
4. Model configuration (which LLM, temperature, max tokens)

**Dify App IDs per tenant:** Each tenant gets their own Dify app instance (or shared with tenant-scoped knowledge bases).

---

## Cross-References
- `04-Architecture/Agent-Architecture.md` — Agent design principles
- `08-AI/Prompt-Library.md` — System prompts used by agents
- `08-AI/Evaluation-Framework.md` — Agent quality metrics
