# RC1_PLAN.md — Conductor Release Candidate 1
## Implementation Stabilization & Production Readiness

**Prepared:** 2026-06-25  
**Branch:** main  
**Scope:** No new features. Fix confirmed defects in existing implementation.

---

## Inspection Summary

| Category | Count |
|---|---|
| Hardcoded credentials / config | 2 |
| Missing exception handlers | 1 |
| Stub / mock in production code | 2 |
| Architectural boundary violations | 1 |
| Inconsistent error response format | 1 |
| Incorrect healthCheck implementations | 3 |
| Flaky test patterns | 2 |
| CI stub (CD pipeline non-functional) | 1 |
| Docker-compose divergences | 6+ |
| Gradle naming inconsistency | 1 |

---

## Priority 1 — Release Blockers

Must fix before RC-1 tag.

---

### P1-A · Hardcoded database credentials in workflow service

**Problem:** `platform/workflow/src/main/resources/application.yml` contains
literal values:

```yaml
datasource:
  url: jdbc:postgresql://localhost:5432/conductor_db
  username: conductor
  password: conductor_password
```

Every other service (`customer`, `identity`, `integrations`, `tenant`) uses
`${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/conductor_db}`.
`temporal.service-address` is also hardcoded `localhost:7233`.

**Root cause:** Workflow service config was never aligned with the shared pattern.

**Impact:** Deployment is impossible without source code changes. Any container
built from this config will only connect to localhost.

**Risk:** HIGH

**Files affected:**
- `platform/workflow/src/main/resources/application.yml`

**Fix:** Replace all literal DB/temporal/NATS values with `${ENV_VAR:local-default}`
matching the convention in all other services.

**Effort:** S (< 30 min)

**Validation:** `./gradlew :platform:workflow:build`; config starts with env
variables overriding defaults.

---

### P1-B · `show-sql: true` in production service configs

**Problem:** Three services have `show-sql: true`:

- `platform/identity/src/main/resources/application.yml:15`
- `platform/integrations/src/main/resources/application.yml:15`
- `platform/tenant/src/main/resources/application.yml:15`

`customer` correctly uses `show-sql: false`. SQL logging in production emits
every query — including those with user PII — to the log aggregator.

**Root cause:** Dev config copied verbatim to production config.

**Risk:** HIGH — PII leakage; log volume explosion under load.

**Files affected:** Three `application.yml` files above.

**Fix:** Set `show-sql: false` in all three files.

**Effort:** XS (< 10 min)

**Validation:** Build passes; test output contains no SQL `Hibernate:` lines.

---

### P1-C · Mock OAuth tokens stored in production code path

**Problem:** `IntegrationController.oauthCallback()` at line 84:

```java
credentialService.saveOAuthConnection(integrationId,
    "mock-access-token-" + UUID.randomUUID(),
    "mock-refresh-token",
    Instant.now().plusSeconds(3600), "all");
```

The real authorization code received from the OAuth provider is discarded.
Fabricated tokens are persisted. Every downstream API call using stored OAuth
credentials will return 401 from the target system.

**Root cause:** OAuth code-for-token exchange was not implemented.
A placeholder was left in the production code path.

**Risk:** HIGH — integrations are non-functional end-to-end.

**Files affected:**
- `platform/integrations/src/main/java/com/conductor/integrations/api/IntegrationController.java`
- New: `platform/integrations/src/main/java/com/conductor/integrations/service/OAuthTokenExchangeService.java`

**Fix:** Extract a `OAuthTokenExchangeService` that accepts the authorization
code and the integration's stored OAuth config (client ID, client secret, token
URL from `Credential`), calls the provider token endpoint via `ProxyHttpClient`,
and returns real access/refresh/expiry values. The controller calls this service.
If token endpoint URL is not configured in credentials, throw `IllegalStateException`
with a descriptive message rather than silently storing fake tokens.

**Effort:** M (2–3 hours including unit test)

**Validation:** Unit test mocks `ProxyHttpClient`; confirms mock tokens no longer
written to credential store; confirms `IllegalStateException` thrown when config
absent.

---

### P1-D · No exception handler in integrations module — raw 500s in production

**Problem:** `IntegrationController` throws `IllegalArgumentException` (line 82:
"Integration not found") and connectors throw `UnsupportedOperationException`
(unknown action). There is no `@RestControllerAdvice` in the integrations module.
Spring's default handler returns HTTP 500 with a full stack trace in the response body.

By contrast:
- `customer` has `CustomerGlobalExceptionHandler` → `ProblemDetail`
- `workflow` has `WorkflowExceptionHandler` → `ErrorResponse`

**Root cause:** Exception handler was never added to the integrations module.

**Risk:** HIGH — stack traces in API responses; wrong HTTP status codes;
information disclosure in production.

**Files affected (new):**
- `platform/integrations/src/main/java/com/conductor/integrations/api/IntegrationExceptionHandler.java`

**Fix:** Add `@RestControllerAdvice(basePackages = "com.conductor.integrations.api")`
returning `ProblemDetail` (Spring 6 native RFC 7807) with mappings:

| Exception | HTTP |
|---|---|
| `IllegalArgumentException` | 404 Not Found |
| `IllegalStateException` | 422 Unprocessable Entity |
| `UnsupportedOperationException` | 400 Bad Request |
| `Exception` | 500 (sanitized: "An unexpected error occurred") |

**Effort:** S (1 hour including test)

**Validation:** Controller unit tests confirm 404/400/422 returned; no stack trace
in response body; regression test for each mapping.

---

### P1-E · `platform:identity` directly depends on `platform:tenant` service class

**Problem:** `platform/identity/build.gradle` declares:

```groovy
implementation project(':platform:tenant')
```

And `UserService.java:12` imports:

```java
import com.conductor.tenant.service.KeycloakAdminService;
```

This is a compile-time dependency between two sibling platform modules, violating
the architecture rule that platform modules must not depend on each other.

**Root cause:** `KeycloakAdminService` contains user-creation logic needed by
identity. Instead of extracting a shared contract, the identity module was coupled
directly to the concrete service.

**Risk:** MEDIUM-HIGH — blocks independent module deployment; prevents isolated
testing of identity without the tenant module; creates implicit circular-dependency
risk.

**Files affected:**
- `platform/identity/src/main/java/com/conductor/identity/service/UserService.java`
- `platform/identity/build.gradle`
- `shared/auth/build.gradle` (or new `shared:keycloak-contract` submodule)
- New interface file

**Fix (minimal):** Extract the two methods `UserService` actually calls from
`KeycloakAdminService` into an interface `KeycloakUserManager` in `shared:auth`.
`KeycloakAdminService` implements the interface. `UserService` depends on
`KeycloakUserManager`. Remove `implementation project(':platform:tenant')` from
`identity/build.gradle`.

**Effort:** M (2–3 hours)

**Validation:** `./gradlew :platform:identity:compileJava` succeeds without
`platform:tenant`; ArchUnit test added asserting no `platform:` → `platform:`
compile dependency.

---

## Priority 2 — Quality Defects

Fix before RC-1 if schedule allows; else RC-2.

---

### P2-A · Inconsistent error response format across modules

**Problem:** Two different error response shapes:

- `customer`: returns `ProblemDetail` (Spring 6 native)
- `workflow`: returns `ResponseEntity<ErrorResponse>` — a custom DTO with
  identical fields (`type`, `title`, `status`, `detail`, `instance`)

The `WorkflowExceptionHandler` also has no `basePackages` constraint: it will
catch exceptions from any controller in the Spring context if they share a
deployment unit.

**Files affected:**
- `platform/workflow/src/main/java/com/conductor/workflow/api/WorkflowExceptionHandler.java`
- `platform/workflow/src/main/java/com/conductor/workflow/api/dto/ErrorResponse.java`

**Fix:** Migrate `WorkflowExceptionHandler` to return `ProblemDetail`. Delete
`ErrorResponse`. Add `basePackages = "com.conductor.workflow.api"` scope.

**Effort:** S (1 hour)

**Validation:** Tests confirm `ProblemDetail` shape in workflow error responses;
`ErrorResponse.java` deleted; no compilation errors.

---

### P2-B · Connector `healthCheck()` always returns `true`

**Problem:** All three connectors (Shopify, Razorpay, Zoho) return `true`
unconditionally. `IntegrationController.checkHealth()` calls `adapter.healthCheck()`
and returns `{ "status": "UP" }` or `"DOWN"`. The endpoint is always `UP`.

**Files affected:**
- `platform/integrations/src/main/java/com/conductor/integrations/connectors/ShopifyConnector.java`
- `platform/integrations/src/main/java/com/conductor/integrations/connectors/RazorpayConnector.java`
- `platform/integrations/src/main/java/com/conductor/integrations/connectors/ZohoConnector.java`

**Fix:** Implement lightweight connectivity checks using `ProxyHttpClient`. Each
connector pings an unauthenticated status endpoint (Shopify: `/admin/api/2024-01/shop.json`
status; Razorpay: `api.razorpay.com` reachability; Zoho: `accounts.zoho.com`
reachability). Return `false` on exception or non-2xx. Apply 3-second timeout.

**Effort:** M (2 hours)

**Validation:** Unit tests mock `ProxyHttpClient`; confirm `false` on network
exception; `true` on 200.

---

### P2-C · `Thread.sleep` in `OAuthStateStoreTest` — timing-dependent flakiness

**Problem:** `consume_expiredToken_returnsEmpty` uses `Thread.sleep(1100)` to
expire a 1-second TTL token. This adds 1.1s to the test suite and will fail on
slow CI runners.

**Files affected:**
- `platform/integrations/src/test/java/com/conductor/integrations/OAuthStateStoreTest.java`
- `platform/integrations/src/main/java/com/conductor/integrations/framework/OAuthStateStore.java`

**Fix:** Inject `java.time.Clock` into `OAuthStateStore` (default `Clock.systemUTC()`).
In the test, substitute `Clock.fixed(Instant.now().plusSeconds(10), ZoneOffset.UTC)`
to simulate future time. No sleep required.

**Effort:** S (45 min)

**Validation:** Test passes without `Thread.sleep`; test duration under 50ms.

---

### P2-D · `System.out.println` in `EventPlatformIntegrationTest`

**Problem:** Two `System.out.println` calls in
`shared/messaging/src/test/java/com/conductor/shared/messaging/EventPlatformIntegrationTest.java`
(lines 91, 100).

**Fix:** Replace with `log.warn(...)` using the existing SLF4J logger.

**Effort:** XS (5 min)

---

## Priority 3 — Consistency (RC-2)

### P3-A · CD pipeline is a stub

`.github/workflows/cd.yml` executes only `echo "Deploy stub"`. No build, push,
deploy, or smoke test.

**Fix:** Implement: build Docker images, push to container registry, deploy to
staging, run smoke tests. Requires infrastructure decisions.

**Effort:** L (infra-dependent)

---

### P3-B · Docker-compose workspace/local divergences

Six confirmed differences between `docker-compose.local.yml` and
`workspace/infrastructure/docker-compose/docker-compose.yml`:

1. Missing services in workspace: Dify (LLM API), ActivePieces
2. All services missing `networks: [conductor-net]` in workspace
3. Kong volume path and missing `:ro` flag
4. Keycloak missing `realm-export.json` mount and `--import-realm` command
5. NATS missing management port `-m 8222`
6. Prometheus path and missing `:ro` flag

**Fix:** Establish one canonical compose; use `override` for local-only services.

**Effort:** M

---

### P3-C · Gradle settings.gradle project name remapping inconsistency

`shared:workflow` and `shared:customer` use explicit `project(':...').name = '...'`
remappings. Other shared modules do not. Inconsistent but low impact.

**Fix:** Remove the remappings (if no dependency references the aliased names) or
apply uniformly.

**Effort:** XS

---

### P3-D · `docs-check.sh` hardcodes machine-specific absolute path

`scripts/docs-check.sh` contains `C:/Users/rajaj/Projects/Conductor/`. The script
will always succeed silently on CI (no matching links found) and fail on any other
machine.

**Fix:** Derive repo root from `git rev-parse --show-toplevel` or
`${GITHUB_WORKSPACE:-$(git rev-parse --show-toplevel)}`.

**Effort:** XS

---

## Priority 4 — Low / Backlog

**P4-A:** `WorkflowExceptionHandler` missing `basePackages` scope — covered by P2-A.

**P4-B:** Add `SHOW_SQL: ${SHOW_SQL:false}` env-variable override for local
debugging while keeping production default `false`.

**P4-C:** Connector stubs (`refreshToken`, `subscribe`, `unsubscribe`) are
log-only. Document in `ConnectorAdapter` Javadoc which methods are intentionally
minimal until real SDKs are integrated.

---

## Risk Register

| ID | Risk | Likelihood | Mitigation |
|---|---|---|---|
| P1-C | Token exchange calls real OAuth provider | High | Mock `ProxyHttpClient` in unit test; configure mock server in integration env |
| P1-E | Interface extraction may break tenant module callers | Low | Only `UserService` uses it; extraction is additive |
| P1-D | New exception handler may conflict with Spring defaults | Low | Scope with `basePackages`; test each HTTP status mapping |

---

## Recommended Execution Order

```
Day 1
P1-B (show-sql, 10 min)
P1-A (workflow credentials, 30 min)
./gradlew clean build && ./gradlew test  ← gate

P1-D (exception handler, 1 hr)
./gradlew clean build && ./gradlew test  ← gate

Day 2
P1-C (OAuth token exchange, 3 hr)
./gradlew clean build && ./gradlew test  ← gate

P1-E (identity/tenant boundary, 3 hr)
./gradlew clean build && ./gradlew test  ← gate

Day 3 (if RC-1 schedule allows)
P2-D → P2-C → P2-A → P2-B
./gradlew clean build && ./gradlew test  ← final gate
```

---

## Definition of Done — RC-1

- [ ] `./gradlew clean build` — zero errors, zero credential-related warnings
- [ ] `./gradlew test` — all tests pass, no timing failures
- [ ] No SQL `Hibernate:` lines in test or production log output
- [ ] `IntegrationController` returns 404 on unknown resource, not 500
- [ ] OAuth callback does not store mock tokens
- [ ] `platform:identity` compiles without `platform:tenant` on classpath
- [ ] ArchUnit boundary test asserts no `platform:` → `platform:` dependency
- [ ] All `application.yml` files use `${...}` for all secrets and URLs
