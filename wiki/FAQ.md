# Frequently Asked Questions (FAQ)

Here are answers to the most common architectural and technical questions regarding the Conductor platform.

---

### Q: Is Conductor a microservices architecture?
**A**: No. Conductor is structured as a **Modular Monolith** for its MVP (governed by `ADR-001`). All services run inside a single JVM process (Spring Boot 3.x), utilizing package boundaries to isolate domains. This eliminates the deployment complexity of microservices while preserving the ability to extract modules into independent services later when scale justifies it.

---

### Q: How is multi-tenancy data isolation enforced?
**A**: Data is isolated logically using a **Shared Database, Row-Level Isolation** strategy (`ADR-002`). Every tenant table contains a `tenant_id` column. A custom Spring interceptor and Hibernate aspect automatically inject a `WHERE tenant_id = ?` clause into JPA queries at runtime, preventing cross-tenant data leaks.

---

### Q: Why did you choose Temporal instead of Camunda or a custom scheduler?
**A**: Temporal was selected (`ADR-001` / `ADR-003`) because it provides robust, stateful, and durable execution out of the box. Multi-day workflows (such as waiting 3 days after cart abandonment to send a WhatsApp) run without keeping threads active or allocating local memory resources, and include automatic retry policies.

---

### Q: Why NATS JetStream instead of Apache Kafka?
**A**: NATS JetStream (`ADR-002` / `ADR-005`) provides the required at-least-once delivery guarantees and stream persistence while being extremely simple to operate (runs as a single binary, <50MB RAM footprint). This reduces infrastructure management costs for small engineering teams compared to Kafka, which requires ZooKeeper or KRaft.

---

### Q: How does the platform protect customer PII data?
**A**: Any database table column storing PII (such as emails, names, or phone numbers) is cryptographically encrypted at rest using AES-256 via the [PiiEncryptedConverter](file:///c:/Users/rajaj/Projects/Conductor/shared/customer/src/main/java/com/conductor/shared/customer/PiiEncryptedConverter.java) JPA converter wrapper.

---

### Q: How does Conductor process WhatsApp opt-out requests?
**A**: To comply with WhatsApp Business policies, any message reply containing opt-out keywords (like `STOP` or `UNSUBSCRIBE`) triggers an immediate asynchronous event. The [ConsentService](file:///c:/Users/rajaj/Projects/Conductor/platform/customer/src/main/java/com/conductor/customer/service/ConsentService.java) updates the customer's consent state database record within 5 seconds, stopping any further automated campaign dispatches.

---

### Related Pages
- [Architecture Overview](Architecture-Overview)
- [Implementation Guide](Implementation-Guide)
- [Security Guide](Security-Guide)
