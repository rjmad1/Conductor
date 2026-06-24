# Coding Standards — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** None  
**Last Updated:** June 2026

---

## Scope
These standards apply to all code in the `conductor-platform` repository. Deviations require explicit team approval and must be documented.

---

## Java / Spring Boot Standards

### Java Version
- **Java 21** (LTS) — use virtual threads (Project Loom) for I/O-heavy services
- No Java 8 patterns (raw types, pre-lambda patterns)

### Package Structure (per service)
```java
io.conductor.{service-name}
    ├── api/          # REST controllers, request/response DTOs
    ├── domain/       # JPA entities, domain models, value objects
    ├── service/      # Business logic (no HTTP, no JPA in this layer)
    ├── repository/   # JPA repositories, custom queries
    ├── event/        # Event publishers and consumers
    ├── client/       # External API clients (Feign/RestTemplate)
    └── config/       # Spring configuration beans
```

### Naming Conventions
| Element | Convention | Example |
|---|---|---|
| Classes | PascalCase | `CustomerService`, `WorkflowRepository` |
| Methods | camelCase, verb-noun | `findByTenantId`, `processInboundMessage` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT = 3` |
| Packages | lowercase, no underscores | `io.conductor.workflow.engine` |
| Database tables | snake_case | `workflow_executions` |
| NATS subjects | dot.separated | `conductor.{tenantId}.order.created` |
| REST endpoints | kebab-case, plural nouns | `/api/v1/workflow-executions` |

### Entity Rules
```java
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;  // REQUIRED on every entity

    // No @ManyToOne across tenant boundaries
    // No lazy loading of tenant-scoped collections without explicit join
}
```

**Mandatory:** Every JPA entity MUST have `tenantId` field. Any query without `tenantId` in the WHERE clause MUST be explicitly justified in a code review comment.

### Service Layer Rules
- Services accept and return domain objects or DTOs (never raw JPA entities to controllers)
- Services MUST throw domain exceptions (`WorkflowNotFoundException`, `TenantLimitExceededException`), not HTTP exceptions
- Controllers translate domain exceptions to HTTP responses via `@ControllerAdvice`

### Error Handling
```java
// Domain exceptions (in service layer)
throw new CustomerNotFoundException("Customer %s not found in tenant %s".formatted(customerId, tenantId));

// Controller advice translates to HTTP
@ExceptionHandler(CustomerNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(CustomerNotFoundException e) {
    return ResponseEntity.status(404).body(new ErrorResponse("CUSTOMER_NOT_FOUND", e.getMessage()));
}

// Uniform error response format
{
    "error_code": "CUSTOMER_NOT_FOUND",
    "message": "Customer ...",
    "timestamp": "2026-06-15T10:00:00Z",
    "request_id": "req-uuid"
}
```

### Logging Standards
```java
// Use SLF4J with structured logging
private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

// CORRECT: structured parameters
log.info("Customer created. customerId={} tenantId={}", customer.getId(), tenantId);

// WRONG: string concatenation (performance + log injection risk)
log.info("Customer created: " + customer.toString());

// NEVER log PII
log.info("Message sent to customer");   // CORRECT
log.info("Message sent to +91{}", phone); // WRONG — PII in logs
```

**Log levels:**
- `ERROR` — Unexpected exceptions, system errors requiring immediate attention
- `WARN` — Business rule violations, recoverable errors, deprecation
- `INFO` — Business events, request/response (no payload), state transitions
- `DEBUG` — Implementation details (disabled in production)

---

## TypeScript / React Standards

### TypeScript Config
- `strict: true` in tsconfig.json — no exceptions
- No `any` types — use `unknown` if type is truly unknown, then narrow

### Component Structure
```typescript
// Functional components only — no class components
// Props always typed explicitly
interface CustomerCardProps {
  customer: Customer;
  onSelect: (id: string) => void;
}

export const CustomerCard: React.FC<CustomerCardProps> = ({ customer, onSelect }) => {
  // hooks at top
  const [isExpanded, setIsExpanded] = useState(false);

  // event handlers as named functions
  const handleSelect = () => onSelect(customer.id);

  // render
  return <div>...</div>;
};
```

### API Client
- All API calls go through the `services/` layer (never direct `fetch` in components)
- Use `react-query` for all server state (no manual loading/error state)
- API errors handled in error boundaries or react-query `onError`

### State Management
- Local UI state: `useState` / `useReducer`
- Server state: `react-query` (TanStack Query)
- Global client state: `zustand` (for auth, tenant config)
- NO Redux — too much boilerplate for this use case

---

## SQL / Database Standards

### Migration Naming
```
V001__create_tenants_table.sql
V002__create_customers_table.sql
V003__add_customer_segments.sql
```
- Sequential version numbers
- Description in snake_case
- One logical change per migration file

### Query Standards
```java
// CORRECT: Parameterized query with tenant filter
@Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.phone = :phone")
Optional<Customer> findByTenantIdAndPhone(@Param("tenantId") UUID tenantId, @Param("phone") String phone);

// WRONG: No tenant filter
@Query("SELECT c FROM Customer c WHERE c.phone = :phone") // Security violation
Optional<Customer> findByPhone(@Param("phone") String phone);
```

### Index Requirements
- Every foreign key column MUST have an index
- Every column used in WHERE clauses in production queries MUST have an index
- Composite indexes: (tenant_id, {filter_column}) on all main tables
- Indexes reviewed in code review before merge to main

---

## Testing Standards

### Coverage Requirements (Minimum)
- **Service layer:** 80% line coverage
- **Repository layer:** Integration tests against test PostgreSQL
- **API layer:** Integration tests with MockMvc
- **Overall:** 70% minimum (enforced in CI)

### Test Types
```
Unit Tests:         Service and utility logic (mock all dependencies)
Integration Tests:  Repository layer (test against real H2 or PostgreSQL)
API Tests:          Controller layer (MockMvc + Spring Test context)
E2E Tests:          Happy path flows (Phase 2 — Playwright for frontend)
```

### Test Naming
```java
@Test
void givenOptedOutCustomer_whenWorkflowTriesToSendMessage_thenMessageIsBlocked() {
    // Arrange
    // Act
    // Assert
}
```

---

## Code Review Requirements

Every PR must have:
- [ ] At least 1 reviewer approval
- [ ] CI pipeline passing (build, tests, lint)
- [ ] No new SonarQube issues at CRITICAL or BLOCKER severity
- [ ] `tenant_id` check present for any new data access
- [ ] No PII logged
- [ ] No hardcoded secrets or credentials
- [ ] New API endpoints have corresponding OpenAPI spec update

PRs that change the database schema additionally require:
- [ ] Flyway migration script included
- [ ] Migration is backward compatible (no column drops without deprecation period)
- [ ] Index additions are non-blocking (use `CREATE INDEX CONCURRENTLY`)

---

## Security Coding Rules

1. Never build SQL queries by string concatenation
2. Never store credentials in code or config files — use Secrets Manager
3. Never log customer phone numbers, emails, or message content
4. Never trust user input for tenant_id — always use JWT claim
5. Never make outbound HTTP calls to user-supplied URLs (SSRF risk)
6. Always validate HMAC signatures on inbound webhooks before processing
7. Always use `MessageDigest.isEqual()` for HMAC comparison (constant-time, prevents timing attacks)

---

## Cross-References
- `05-Engineering/Repositories.md` — Repository structure
- `05-Engineering/API-Contracts.md` — API design conventions
- `04-Architecture/Security-Architecture.md` — Security controls
