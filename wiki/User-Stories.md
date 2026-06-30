# User Stories — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** Personas, Customer Journeys, Product Requirements  
**Last Updated:** June 2026

---

## Story Format
```
As a [persona role], I want to [action] so that [business outcome].
Acceptance Criteria:
- GIVEN [context] WHEN [action] THEN [expected result]
```

---

## Epic 1: Business Onboarding

### US-001: Self-Registration
**As a** business owner,  
**I want to** register my business on Conductor with my email and password,  
**so that** I can start automating my customer communications.

**Acceptance Criteria:**
- GIVEN I am on the signup page WHEN I enter name, email, password, and business type AND submit THEN my account is created and I am taken to the onboarding wizard
- GIVEN my email is already registered WHEN I attempt signup THEN I see "Email already in use" error
- GIVEN I complete signup THEN I receive a welcome email with verification link

**Priority:** P0 | **Sprint:** 1

---

### US-002: Industry-Specific Onboarding
**As a** new business owner,  
**I want to** select my industry vertical during onboarding,  
**so that** I see relevant pre-built templates and automations for my business type.

**Acceptance Criteria:**
- GIVEN I am in the onboarding wizard WHEN I select "Healthcare" THEN I see healthcare-specific capability packs and templates pre-loaded
- GIVEN I select "Retail/E-Commerce" THEN abandoned cart recovery and order status templates are highlighted
- GIVEN I can see the suggested packs THEN I can activate one with a single click

**Priority:** P0 | **Sprint:** 1

---

### US-003: WhatsApp Number Connection
**As a** business owner,  
**I want to** connect my WhatsApp Business number to Conductor,  
**so that** automated messages are sent from my business's WhatsApp number.

**Acceptance Criteria:**
- GIVEN I am in setup WHEN I click "Connect WhatsApp" THEN I am guided through a 5-step WABA connection flow
- GIVEN the WABA is being verified by Meta WHEN I am in the waiting period THEN I can use a sandbox number for testing
- GIVEN my WABA is connected THEN I see "Connected: +91XXXXXXXXXX" with a green status indicator

**Priority:** P0 | **Sprint:** 1

---

## Epic 2: Workflow Automation

### US-010: Create Appointment Reminder Workflow (Healthcare)
**As a** clinic owner,  
**I want to** set up an automated appointment reminder on WhatsApp,  
**so that** patient no-shows decrease and I don't need staff manually calling patients.

**Acceptance Criteria:**
- GIVEN I am in the workflow designer WHEN I select "Appointment Reminder" from the template library THEN a pre-configured workflow is loaded
- GIVEN I activate the workflow THEN all appointments created in Google Calendar automatically trigger a WhatsApp reminder 24 hours prior
- GIVEN a patient replies "RESCHEDULE" THEN the workflow detects this and either starts a rescheduling sub-flow or routes to a staff agent

**Priority:** P0 | **Sprint:** 2

---

### US-011: Create Abandoned Cart Recovery Workflow (E-Commerce)
**As an** e-commerce store owner,  
**I want to** automatically message customers who abandon their cart on Shopify,  
**so that** I recover lost sales without any manual effort.

**Acceptance Criteria:**
- GIVEN I have connected Shopify WHEN a customer abandons a cart with value > ₹500 THEN after 30 minutes a WhatsApp message is sent automatically
- GIVEN the message is sent WHEN the customer clicks the checkout link THEN I can see this in my analytics dashboard
- GIVEN I have not connected Shopify THEN the Shopify trigger in the workflow designer shows a "Connect Shopify first" prompt

**Priority:** P0 | **Sprint:** 2

---

### US-012: Create Custom Workflow (Advanced)
**As an** operations manager,  
**I want to** create a custom automation workflow from scratch,  
**so that** I can automate a business process specific to my operations that no template covers.

**Acceptance Criteria:**
- GIVEN I open the workflow designer WHEN I click "Start from scratch" THEN I see a blank canvas with trigger, condition, and action blocks
- GIVEN I add a "Razorpay Payment Failed" trigger THEN I can add conditions and actions to build a payment retry reminder
- GIVEN I save the workflow THEN it goes into "inactive" state and requires manual activation
- GIVEN I activate the workflow THEN it begins executing for new events

**Priority:** P0 | **Sprint:** 3

---

### US-013: Test Workflow Before Activation
**As a** business owner,  
**I want to** test my workflow with a specific customer record before activating it,  
**so that** I catch configuration errors before they impact real customers.

**Acceptance Criteria:**
- GIVEN I have built a workflow WHEN I click "Test Workflow" THEN I can select a customer record to test with
- GIVEN the test runs THEN I see each step executed with real data (conditions evaluated, actions simulated)
- GIVEN a test action would send a WhatsApp message THEN it sends only to my designated test number, not the actual customer

**Priority:** P1 | **Sprint:** 3

---

### US-014: View Workflow Execution History
**As a** manager,  
**I want to** see the execution history of each workflow,  
**so that** I can audit what automations ran for which customers and when.

**Acceptance Criteria:**
- GIVEN I open a workflow WHEN I click "Execution History" THEN I see a list of executions with: customer, trigger time, outcome (success/failed), actions taken
- GIVEN an execution failed THEN I can see the error reason (e.g., "WhatsApp delivery failed", "Template rejected")
- GIVEN I click a specific execution THEN I see the step-by-step trace

**Priority:** P0 | **Sprint:** 3

---

## Epic 3: Customer Management

### US-020: Import Customer Contacts
**As a** business owner,  
**I want to** import my existing customer contact list into Conductor,  
**so that** I can start running campaigns and workflows immediately without re-entering data.

**Acceptance Criteria:**
- GIVEN I download the CSV template WHEN I fill it with my customer data THEN I can upload it via the "Import Contacts" button
- GIVEN the CSV has 5,000 rows WHEN I upload it THEN import completes within 2 minutes and shows a success summary (imported, skipped duplicates, errors)
- GIVEN a row has an invalid phone number THEN that row is skipped and listed in an error report

**Priority:** P0 | **Sprint:** 1

---

### US-021: View Customer Activity
**As an** agent,  
**I want to** view a customer's full conversation and automation history,  
**so that** I have full context when I receive an escalated conversation.

**Acceptance Criteria:**
- GIVEN I search for a customer by phone number THEN I see their profile, consent status, tags, and full conversation history
- GIVEN the customer was part of a campaign THEN I can see which messages they received and whether they replied
- GIVEN the customer has custom attributes (e.g., doctor name, patient ID) THEN these are visible on their profile

**Priority:** P0 | **Sprint:** 2

---

### US-022: Manage Customer Consent
**As a** business owner,  
**I want to** see and manage consent status for each customer,  
**so that** I comply with DPDP India and WhatsApp Business Policy.

**Acceptance Criteria:**
- GIVEN a customer's profile WHEN I view it THEN I can see their current WhatsApp consent status (opted-in, opted-out, not set) and the date it was set
- GIVEN a customer replies STOP THEN their consent status changes to opted-out within 5 seconds
- GIVEN a customer is opted-out THEN any workflow attempting to send them a marketing message is blocked at execution time, not at configuration time

**Priority:** P0 | **Sprint:** 2

---

## Epic 4: Campaign Management

### US-030: Send Broadcast Campaign
**As a** marketing manager,  
**I want to** send a promotional WhatsApp message to all opted-in customers in a segment,  
**so that** I can announce offers, events, or product launches.

**Acceptance Criteria:**
- GIVEN I create a campaign WHEN I select a segment and a template THEN I see an estimated audience count
- GIVEN I schedule the campaign for a future date/time THEN it is queued and sends automatically at that time
- GIVEN the campaign has sent THEN I see a real-time dashboard: messages sent, delivered, read, replies

**Priority:** P0 | **Sprint:** 4

---

### US-031: Frequency Cap Enforcement
**As a** business owner,  
**I want to** ensure customers don't receive more than 1 marketing message per day,  
**so that** I protect my WhatsApp Business reputation and avoid customer complaints.

**Acceptance Criteria:**
- GIVEN a customer has already received a marketing WhatsApp today WHEN a campaign or workflow attempts to send another THEN that customer is skipped for today
- GIVEN a customer is skipped THEN they are NOT counted in the "sent" total for the campaign
- GIVEN I view campaign results THEN I can see a "frequency capped" count

**Priority:** P0 | **Sprint:** 4

---

## Epic 5: Team Management

### US-040: Invite Team Member
**As a** business owner (OWNER role),  
**I want to** invite team members to my Conductor account,  
**so that** my support agents and marketing managers can use the platform with appropriate access.

**Acceptance Criteria:**
- GIVEN I enter a team member's email and select their role WHEN I click "Send Invite" THEN they receive an email invitation
- GIVEN the invitee clicks the link THEN they can set their password and access Conductor with the assigned role
- GIVEN I invite an AGENT role THEN they can only access the conversation inbox, not workflow configuration

**Priority:** P0 | **Sprint:** 1

---

### US-041: Role-Based Access Enforcement
**As a** system,  
**I want to** enforce RBAC so that users can only perform actions permitted by their role,  
**so that** agents cannot accidentally modify workflows and admins cannot change billing.

**Acceptance Criteria:**
- GIVEN a user with AGENT role logs in THEN the Workflow Designer, Integrations, and Billing sections are not visible
- GIVEN an ANALYST logs in THEN all write operations return 403 Forbidden
- GIVEN an OWNER logs in THEN they have access to all sections including Billing and User Management

**Priority:** P0 | **Sprint:** 1

---

## Epic 6: Analytics & Reporting

### US-050: View Operations Dashboard
**As a** business owner,  
**I want to** see a real-time dashboard of my platform activity,  
**so that** I can monitor my automation performance and customer engagement.

**Acceptance Criteria:**
- GIVEN I log in WHEN I land on the dashboard THEN I see: messages sent today, active workflows, last campaign performance, and customer growth
- GIVEN I click "This month" filter THEN all metrics update to show month-to-date data
- GIVEN a workflow has failed executions THEN there is a visible alert on the dashboard

**Priority:** P0 | **Sprint:** 2

---

## Epic 7: Billing & Subscription

### US-060: Upgrade Plan
**As a** business owner,  
**I want to** upgrade my subscription plan from the platform,  
**so that** I can unlock higher message limits and additional features without contacting support.

**Acceptance Criteria:**
- GIVEN I am approaching my message limit THEN I see an in-product upgrade prompt
- GIVEN I click "Upgrade" WHEN I select a plan and complete payment via Razorpay THEN my new plan activates immediately
- GIVEN my plan upgrades THEN I receive a GST-compliant invoice by email

**Priority:** P0 | **Sprint:** 5

---

### US-061: View Usage and Billing
**As a** business owner,  
**I want to** see my current month's usage and upcoming invoice,  
**so that** I can forecast my costs and avoid surprise bills.

**Acceptance Criteria:**
- GIVEN I visit the Billing page THEN I see: messages used this month, messages remaining, next billing date, next invoice amount
- GIVEN I have used >80% of my message limit THEN I receive an email alert
- GIVEN I export my invoice THEN it contains GST number, GSTIN, and line items for base plan + overages

**Priority:** P0 | **Sprint:** 5

---

## Epic 8: Compliance & Safety

### US-070: STOP Opt-Out Processing
**As a** customer (end-user of a Conductor-powered business),  
**I want to** stop receiving messages by replying STOP,  
**so that** I have control over what messages I receive and my opt-out is honored immediately.

**Acceptance Criteria:**
- GIVEN I reply "STOP" to a WhatsApp message from a Conductor-powered business THEN my opt-out is recorded within 5 seconds
- GIVEN I am opted-out THEN no marketing messages are sent to me from that business for any reason
- GIVEN I later reply "START" THEN my opt-in is restored and I can receive messages again

**Priority:** P0 | **Sprint:** 1 (must be in first release)

---

### US-071: Data Deletion Request
**As a** customer (end-user),  
**I want to** request deletion of my data from a business's Conductor account,  
**so that** I can exercise my right to erasure under DPDP India.

**Acceptance Criteria:**
- GIVEN a business receives a data deletion request WHEN they use the Delete Customer function in the UI THEN the customer's PII is anonymized/deleted within 30 days
- GIVEN the deletion is processed THEN an audit log is created confirming deletion
- GIVEN the customer had active conversations THEN those conversation contents are also purged

**Priority:** P1 | **Sprint:** 3

---

## Story Map Summary

| Epic | Total Stories | P0 | P1 | P2 |
|---|---|---|---|---|
| Business Onboarding | 3 | 3 | 0 | 0 |
| Workflow Automation | 5 | 4 | 1 | 0 |
| Customer Management | 3 | 3 | 0 | 0 |
| Campaign Management | 2 | 2 | 0 | 0 |
| Team Management | 2 | 2 | 0 | 0 |
| Analytics & Reporting | 1 | 1 | 0 | 0 |
| Billing & Subscription | 2 | 2 | 0 | 0 |
| Compliance & Safety | 2 | 1 | 1 | 0 |
| **Total** | **20** | **18** | **2** | **0** |

---

## Cross-References
- `03-Product/Personas.md` — Personas referenced in "As a" statements
- `03-Product/Product-Requirements.md` — Functional requirements this stories implement
- `03-Product/Roadmap.md` — Sprint and phase mapping
- `02-Business/Customer-Journeys.md` — Journeys that these stories address
