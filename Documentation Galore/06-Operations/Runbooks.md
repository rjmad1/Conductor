# Operations Runbooks — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** None  
**Last Updated:** June 2026

---

## Purpose
Step-by-step runbooks for common operational procedures. Engineers and on-call responders MUST follow these during incidents.

---

## Runbook 1: Deploy to Production

**Trigger:** New release ready after staging approval

**Steps:**
```
1. Confirm CI/CD pipeline is green on staging
2. Review deployment checklist:
   □ All tests passing
   □ Database migrations reviewed and tested on staging
   □ No blocking issues in QA review
   □ On-call engineer notified

3. Tag release in Git:
   git tag -a v1.x.x -m "Release v1.x.x"
   git push origin v1.x.x

4. Trigger production deployment:
   GitHub Actions → "Deploy to Production" → Run workflow → Select tag

5. Monitor deployment progress:
   - Watch ECS service events for rolling deployment
   - Check health endpoints: GET /health for each service
   - Watch error rate in Grafana for 15 minutes post-deploy

6. Confirm deployment:
   □ All services healthy (ECS shows "running" desired count)
   □ Error rate within baseline (< 0.1%)
   □ Response time p95 within SLA

7. If deployment fails:
   → Trigger rollback (Runbook 3)
```

---

## Runbook 2: Database Migration

**Trigger:** Flyway migrations included in a release

**CRITICAL:** Always run migrations before deploying new application code.

**Steps:**
```
1. Take a manual RDS snapshot before migration:
   AWS Console → RDS → {production DB} → "Take Snapshot"
   Name: pre-migration-v1.x.x-{date}

2. Check current Flyway migration version:
   SELECT version, description, installed_at FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;

3. Validate migration scripts against staging first (should already be done)

4. Run migration on production:
   - Flyway runs automatically on service startup IF spring.flyway.enabled=true
   - OR run manually: flyway -url=jdbc:postgresql://... migrate

5. Verify migration succeeded:
   SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 3;
   -- success column must be TRUE for all rows

6. Monitor DB performance for 30 minutes:
   - Watch slow query log
   - Check pg_stat_activity for locks
   - Watch CPU and I/O on RDS

7. If migration fails:
   → DO NOT attempt to run the failed migration again without fixing it
   → Restore from pre-migration snapshot if service is down
   → Escalate to Engineering Lead
```

---

## Runbook 3: Rollback Deployment

**Trigger:** Deployment causes elevated error rate (> 1%) or P0/P1 incident

**Steps:**
```
1. Confirm rollback decision with Engineering Lead

2. Identify last stable image tag:
   AWS ECR → {service} → Sort by push date → Note previous image digest

3. Trigger rollback via ECS:
   AWS Console → ECS → Service → Update Service → Previous Task Definition revision
   OR:
   GitHub Actions → "Rollback Production" → Input: previous version tag

4. Monitor rollback:
   - Same monitoring as deployment (step 5 of Runbook 1)
   - Confirm error rate returns to baseline

5. If database migration was involved:
   → Application rollback alone is insufficient
   → Assess whether DB state is compatible with old app version
   → May require schema rollback (complex — escalate to CTO)

6. Post-rollback:
   □ Notify all engineers: "Production rolled back to v{X.X.X}"
   □ Open incident ticket
   □ Root cause analysis within 24 hours
```

---

## Runbook 4: Tenant Onboarding Issue

**Trigger:** Support ticket — tenant cannot complete signup or WhatsApp connection

**Common Issues:**

**Issue: Tenant cannot connect WhatsApp number**
```
Diagnosis:
1. Check Keycloak — did tenant account get created?
   Keycloak Admin → Realm: {tenant realm} → Users

2. Check wa_numbers table for the tenant:
   SELECT * FROM wa_numbers WHERE tenant_id = '{tenant_id}';

3. Check Meta WABA status:
   POST https://graph.facebook.com/v18.0/{waba_id}?fields=name,account_review_status
   (requires system access token)

Resolution:
- If WABA pending review: inform tenant (typically 1-3 business days)
- If WABA rejected: guide tenant to re-apply with correct business details
- If connector error: check connector_configs.error_message for details
```

**Issue: Tenant plan not activating after payment**
```
Diagnosis:
1. Check Razorpay payment status in billing dashboard
2. Check subscriptions table:
   SELECT * FROM subscriptions WHERE tenant_id = '{tenant_id}' ORDER BY created_at DESC;
3. Check audit_logs for billing events

Resolution:
- If payment captured but plan not updated: manually update subscription (Engineering)
- If payment failed: retry or update payment method
- Generate invoice manually if needed
```

---

## Runbook 5: High Message Failure Rate

**Trigger:** Alert — message delivery failure rate > 5% for any tenant in 15 minutes

**Steps:**
```
1. Check Grafana dashboard: "Message Delivery" → filter by tenant

2. Identify failure type:
   SELECT error_code, COUNT(*) FROM messages
   WHERE tenant_id = '{tenant_id}' AND status = 'failed' AND sent_at > NOW() - INTERVAL '1 hour'
   GROUP BY error_code ORDER BY COUNT(*) DESC;

Common error codes:
- 131026: Number not on WhatsApp
- 131047: Template not approved / message outside 24h window
- 131053: WhatsApp server unavailable (Meta issue)
- 131031: WABA suspended

3. Resolution by error type:
   131026 → Normal (opt-out the number, update customer record)
   131047 → Check template status, check if 24h window expired
   131053 → Check Meta status page (developers.facebook.com/status/)
   131031 → URGENT: Check Meta Business Manager for WABA suspension

4. If Meta platform is down:
   → Activate meta-incident runbook (below)
   → Notify affected tenants
   → Queue messages for retry when Meta recovers
```

---

## Runbook 6: WhatsApp / Meta Platform Incident

**Trigger:** Meta WhatsApp API returning 5xx errors or high latency for > 5 minutes

**Steps:**
```
1. Check Meta Status: https://developers.facebook.com/status/

2. Check our own metrics: Grafana → WhatsApp API calls → filter by status code

3. Activate incident response:
   → Post in #incidents Slack channel: "Meta WhatsApp API degraded — monitoring"
   → Notify on-call engineer

4. Queue management:
   - Temporal will retry failed workflows with exponential backoff
   - Do NOT manually retry — Temporal handles this
   - Monitor queue depth: Temporal UI → {workflow type} → pending

5. Notify tenants:
   - If degradation > 30 minutes: send in-app notification to active tenants
   - Template: "We're experiencing delays in WhatsApp message delivery due to a Meta platform issue. Messages will be delivered once Meta restores service."

6. Recovery:
   - Temporal retry will catch up once Meta recovers
   - Monitor message success rate returning to baseline
   - Close incident when success rate > 95% for 15 minutes
```

---

## Runbook 7: Database Emergency

**Trigger:** PostgreSQL RDS unavailable or performance severely degraded

**Steps:**
```
1. Check RDS status:
   AWS Console → RDS → {db instance} → Status

2. If Multi-AZ failover in progress:
   - Wait 60-90 seconds for failover to complete
   - Confirm new primary endpoint is healthy
   - Services auto-reconnect (connection pooling handles this)

3. If disk space critical (> 90% usage):
   Immediate: Disable analytics writes to reduce volume
   Short-term: Increase storage (RDS supports online storage increase)
   Long-term: Partition old data, archive to S3

4. If high CPU / slow queries:
   a. Check pg_stat_activity for blocking queries:
      SELECT pid, now() - pg_stat_activity.query_start AS duration, query
      FROM pg_stat_activity
      WHERE state = 'active' AND now() - query_start > interval '30 seconds';

   b. Kill blocking query if identified:
      SELECT pg_terminate_backend({pid});

   c. Check for missing indexes:
      SELECT * FROM pg_stat_user_tables WHERE seq_scan > idx_scan ORDER BY seq_scan DESC;

5. Escalate: If RDS unavailable > 10 minutes → P0 incident → Engineering Lead + CTO
```

---

## Runbook 8: Tenant Data Deletion (DPDP Compliance)

**Trigger:** Customer or regulator requests data deletion for a specific customer

**Steps:**
```
1. Verify the request:
   □ Confirm tenant has received legitimate erasure request
   □ Verify customer identity (phone number match)
   □ Log the request in audit_logs with timestamp

2. Execute deletion:
   -- Soft delete customer record
   UPDATE customers SET
     name = '[DELETED]',
     email = NULL,
     phone = '[DELETED-' || id || ']',
     custom_attributes = '{}',
     deleted_at = NOW()
   WHERE id = '{customer_id}' AND tenant_id = '{tenant_id}';

   -- Anonymize message content
   UPDATE messages SET
     content = '{"deleted": true}'
   WHERE customer_id = '{customer_id}';

   -- Consent records: retain the record but mark as withdrawn (for compliance audit)
   -- DO NOT delete consent records

3. Cancel any active workflow executions for this customer:
   -- Cancel via Temporal API for any in-flight executions

4. Verify deletion in all systems:
   □ Customer record anonymized
   □ Message content anonymized
   □ No active workflows for this customer
   □ Redis session/state cleared (if applicable)

5. Confirm to tenant in writing within 30 days of request
   Log confirmation in audit_logs

6. Never delete:
   □ Audit logs (required for compliance audit)
   □ Consent records (required as legal evidence)
   □ Invoice records (required for tax compliance)
```

---

## Cross-References
- `06-Operations/Incident-Management.md` — Incident classification and escalation
- `06-Operations/Monitoring.md` — Dashboards and alert definitions
- `04-Architecture/Security-Architecture.md` — Security incident procedures
