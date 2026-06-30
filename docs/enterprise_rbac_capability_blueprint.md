# Enterprise Role-Based Access Control (RBAC) Strategic Capability Blueprint
## A Core Identity Architecture & Governance Blueprint

---

## 1. Executive Summary

In the modern enterprise, security is no longer a perimeter-based construct but an identity-centric discipline. Access control is the primary mechanism through which organizational trust is expressed, enforced, and audited. This blueprint defines a strategic, enterprise-grade methodology for architecting, designing, implementing, governing, and continuously optimizing a **Role-Based Access Control (RBAC)** capability.

This blueprint does not treat RBAC as a mere software feature or a collection of system ACLs. Instead, it defines RBAC as a **Strategic Business Capability**—a synthesis of people, processes, governance policies, and technology interfaces that translates corporate business strategy and risk tolerance into automated, precise, and auditable access control policies.

```
       ┌────────────────────────────────────────────────────────┐
       │                  BUSINESS STRATEGY                     │
       └───────────────────────────┬────────────────────────────┘
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │                 BUSINESS ARCHITECTURE                  │
       │   (Capabilities, Services, Processes, Organizations)   │
       └───────────────────────────┬────────────────────────────┘
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │               ENTERPRISE GOVERNANCE & GRC              │
       │           (SoD, Regulatory, Security Policies)         │
       └───────────────────────────┬────────────────────────────┘
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │                 IDENTITY ARCHITECTURE                  │
       │     (Roles, Identity Lifecycle, Access Boundaries)     │
       └────────────────────────────────────────────────────────┘
```

### Strategic Objectives:
1. **Business Alignment:** Enforce authorization boundaries mapped directly to business functions, processes, and corporate hierarchies rather than technical system configurations.
2. **Zero Trust Enforcement:** Operationalize the principle of least privilege, ensuring that every access request is authenticated, authorized, and continuously validated against dynamic risk parameters.
3. **Regulatory Compliance:** Maintain continuous audit readiness for frameworks such as SOX, GDPR, HIPAA, PCI-DSS, and ISO 27001 by implementing strict separation of duties (SoD), clear role ownership, and automated access recertification.
4. **Operational Efficiency:** Drastically reduce the cost of access administration by replacing ad-hoc permission requests with birthright provisioning, automated role-based assignments, and self-service access requests.

---

## 2. RBAC Foundations

### 2.1 Formal Definition of RBAC
Role-Based Access Control is an access control paradigm where permissions are associated with specific roles (representing job functions or enterprise responsibilities) and users are assigned to those roles. 

According to the **ANSI/INCITS 359-2004 (NIST) RBAC Standard**, RBAC is defined by four core components and three relations:
- **Users (U):** Human operators, service identities, API clients, or machine accounts.
- **Roles (R):** Structured job functions or responsibilities within the organization.
- **Permissions (P):** Approvals to perform specific operations on designated resources.
- **Sessions (S):** Temporary mappings associating a user with one or more assigned roles.
- **User Assignment (UA):** A many-to-many relation mapping users to roles.
- **Permission Assignment (PA):** A many-to-many relation mapping permissions to roles.
- **Role Hierarchy (RH):** A partial order defining inheritance relations among roles (e.g., a junior role inherits a subset of a senior role's permissions, or vice versa).

```
   ┌─────────┐        UA        ┌─────────┐        PA        ┌─────────────┐
   │  Users  ├────────◄═►───────┤  Roles  ├────────◄═►───────┤ Permissions │
   └────┬────┘                  └────┬────┘                  └──────┬──────┘
        │                            │                              │
        │ User-to-Role               │ Role-to-Role                 │ Permission-to-Role
        ▼ Mapping                    ▼ Hierarchy                    ▼ Mapping
   ┌─────────┐                  ┌─────────┐                  ┌─────────────┐
   │ Session │                  │ Inherited │                  │  Resource/  │
   │ Context │                  │  Roles  │                  │  Operation  │
   └─────────┘                  └─────────┘                  └─────────────┘
```

### 2.2 What RBAC is NOT
- **RBAC is not AD/LDAP Group Management:** In many organizations, "RBAC" is mistakenly implemented by creating Active Directory groups (e.g., `AD-Group-Finance-Read`) and directly assigning them to users. Groups are flat transport mechanisms; they lack formal lifecycle governance, hierarchy inheritance rules, separation of duty constraints, and context validation.
- **RBAC is not Access Control Lists (ACLs):** ACLs map users directly to resources (e.g., User X has Write access to File Y). This creates an $O(U \times Res)$ management explosion, which is completely unscalable.
- **RBAC is not a static technology solution:** Implementing an IGA tool (like SailPoint, Saviynt, or Keycloak) does not mean you have implemented an RBAC capability. The tool is merely an execution platform for the underlying business policies, role lifecycle processes, and governance boundaries.

### 2.3 Strategic Capability Objectives

| Objective Category | Key Goals | Target State |
| :--- | :--- | :--- |
| **Business** | Minimize employee onboarding friction; align authorization with organizational design. | "Day-One Readiness" where new hires receive all required access automatically via job family assignments. |
| **Security** | Enforce Least Privilege; eliminate privilege creep; implement Segregation of Duties (SoD). | Zero standing administrative access; zero high-risk toxic combinations in active environments. |
| **Governance** | Define clear accountability for access; enforce continuous verification. | Every role and permission has a designated Business Owner; automated access reviews achieve 100% compliance. |
| **Operational** | Reduce manual IAM tickets; automate lifecycle state changes (Joiner-Mover-Leaver). | >90% of role assignments occur automatically via identity attribute triggers; access requests resolved via self-service. |
| **Architectural** | Standardize authorization across multi-cloud, SaaS, and legacy applications. | Single authoritative policy store feeding runtime Policy Decision Points (PDPs) using standard protocols (SCIM, OIDC). |

### 2.4 Common Misconceptions & Implementation Failures
1. **The "Role Explosion" Trap:** Designing roles to match individual preferences rather than normalized job functions. This leads to an organization having more roles than users (e.g., 5,000 employees but 7,500 distinct roles).
2. **The "Set-and-Forget" Anti-Pattern:** Creating roles and never reviewing, pruning, or decommissioning them. Over time, business changes render roles obsolete, leading to security debt.
3. **The "IT-Owned Security Project" Fallacy:** Running the RBAC project out of the IT Infrastructure department. Without active business ownership, role engineering is based on technical permissions rather than business processes, causing massive disruption.
4. **Ignoring the "Mover" Lifecycle:** Most organizations manage "Joiners" (provisioning) and "Leavers" (deprovisioning) reasonably well, but fail at "Movers" (transfers). When an employee changes departments, they retain their old roles while gaining new ones, violating Least Privilege.

### 2.5 Required Organizational Maturity
Before embarking on an enterprise RBAC implementation, the organization must assess its maturity across key foundational pillars. Attempting a comprehensive RBAC system without stable source directories will result in failure.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        IAM MATURITY SCALE                              │
│                                                                        │
│ Level 5: OPTIMIZED   ──► Continuous RBAC/ABAC optimization & AI mining│
│ Level 4: MANAGED     ──► Role Lifecycle and automated recertifications │
│ Level 3: DEFINED     ──► Enterprise RBAC Framework & Core Role Catalog │
│ Level 2: REPEATABLE  ──► Standardized AD groups, manual entitlement inventory│
│ Level 1: AD-HOC      ──► Direct resource-level permissions, no owner tracking│
└────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Business Discovery Framework

The foundation of a business-aligned RBAC model is the thorough discovery and cataloging of the enterprise's operating model. 

### 3.1 Business Capability Mapping
We map access requirements to **Business Capabilities** (what the business does) and **Business Services** (how it does it), rather than mapping directly to systems.

```
                           [Core Enterprise]
                                  │
         ┌────────────────────────┼────────────────────────┐
         ▼                        ▼                        ▼
  [Finance Domain]       [Operations Domain]      [Customer Domain]
         │                        │                        │
  ┌──────┴──────┐          ┌──────┴──────┐          ┌──────┴──────┐
  ▼             ▼          ▼             ▼          ▼             ▼
[Billing]  [Accounting]  [SCM]    [Production]  [Sales]       [Support]
```

To structure this, we define a recursive Business Capability Decomposition:
1. **Level 1 Domain:** Finance Management.
2. **Level 2 Capability:** Treasury Operations.
3. **Level 3 Business Service:** Cash Flow Reconciliation.
4. **Level 4 Business Process:** Daily Ledger Postings.

### 3.2 Operating Model Alignment
The organizational structure determines the distribution of authorization authority:
- **Centralized Model:** Ideal for highly standardized organizations. The IAM team defines and controls all roles. Highly auditable, but slow to adapt to local business needs.
- **Decentralized Model:** Individual business units define and assign roles. Highly agile, but prone to role duplication, compliance gaps, and inconsistent security postures.
- **Federated Model (Recommended):** The central IAM team defines governance guardrails, role metadata standards, and SoD frameworks, while local Business Unit Administrators own and manage the specific entitlements mapped to their business processes.

### 3.3 Business Discovery Questionnaire
The following discovery activities must be conducted with business sponsors:
- **Goal:** Identify the business processes, corresponding systems, and data owners.
- **Key Questions:**
  1. *Who is the accountable business owner for the financial ledger process?*
  2. *What distinct user actions are performed during ledger reconciliation?*
  3. *Which regulatory restrictions (e.g., SOX) govern the ledger postings?*
  4. *What are the current operational bottlenecks during employee transfers?*

---

## 4. Identity Discovery Framework

An authorization model is only as accurate as the identity source data feeding it. We must establish a strict hierarchy of identity classification and lifecycle boundaries.

### 4.1 Workforce Identities (Employees)
- **Source of Truth:** Human Resources Information System (HRIS) (e.g., Workday, SAP SuccessFactors).
- **Core Attributes Required:** Employee ID, Status (Active/Inactive), Job Code, Department, Cost Center, Manager ID, Physical Location, Legal Entity.
- **Governance Requirement:** Every workforce identity must be associated with a valid job profile and manager.

### 4.2 Non-Employee Identities (Contractors, Vendors, Partners)
- **Source of Truth:** Vendor Management System (VMS) (e.g., Fieldglass) or Sponsored Identity Registry.
- **Core Attributes Required:** Contractor ID, Sponsor (Employee Manager), Vendor Agency, Contract Start/End Date, Project Code.
- **Governance Requirement:** Non-employee identities must have an active internal sponsor and a hard-coded expiration date (max 180 days, requiring active re-approval).

### 4.3 Customer and Partner Identities (External Users)
- **Source of Truth:** Customer Identity and Access Management (CIAM) Directory.
- **Governance Requirement:** Strict logical isolation from internal directories. External users must never be assigned internal business roles.

### 4.4 Service and Machine Identities (Non-Human Accounts)
- **Source of Truth:** Service Account Registry, Configuration Management Database (CMDB), or Secrets Management platform (e.g., HashiCorp Vault).
- **Core Attributes Required:** Application ID, System Owner, Purpose, Environment (Dev/Stage/Prod), IP Restriction, Cryptographic Key Expiration.
- **Governance Requirement:** Service accounts must be bound to a single application domain, have zero interactive login capabilities, and must not inherit broad human administrative roles.

---

## 5. Requirements Engineering Framework

Requirements engineering for RBAC must capture both technical capabilities and functional boundaries.

### 5.1 Requirement Categorization

#### Functional Requirements:
- The system must dynamically calculate user role assignments based on HRIS job code attributes.
- The system must provide a self-service catalog for requesting temporary, out-of-band roles.
- The system must enforce static Separation of Duties (SoD) constraints preventing the assignment of both "Invoice Creator" and "Invoice Approver" roles to the same identity.

#### Non-Functional Requirements:
- **Latency:** Role resolution during runtime session initiation must be $< 100\text{ms}$ at the 99th percentile.
- **Throughput:** The authorization engine must support a peak of $50,000$ access validation queries per second.
- **Availability:** The authorization PDP must maintain $99.999\%$ uptime, utilizing distributed read-replicas.
- **Maintainability:** Adding a new permission to a role must propagate to all assigned users within $5$ seconds of approval.

#### Security & Compliance Requirements:
- All administrative role modifications must be logged to a write-once, tamper-evident log store.
- Cryptographic checks must validate that JWT access tokens are signed by a trusted identity provider (IdP).
- Personal Identifiable Information (PII) of identity records must be encrypted at rest and in transit, complying with GDPR.

### 5.2 Elicitation Methodologies

```
┌────────────────────────────────────────────────────────────────────────┐
│                        REQUIREMENTS METHODOLOGY                        │
│                                                                        │
│ 1. Document Analysis ──► Gather Security Policies, Org Charts          │
│ 2. Stakeholder Interviews ──► Align Business Goals with Unit Owners   │
│ 3. Access Mining       ──► Extract existing system entitlement logs   │
│ 4. Role Mining         ──► Perform cluster analysis (Apriori algs)    │
│ 5. GRC Risk Audit      ──► Establish regulatory constraints (SoD)     │
└────────────────────────────────────────────────────────────────────────┘
```

- **Access Mining:** Scripted extraction of existing platform-level access records (e.g., exporting Keycloak client scopes, database roles, Active Directory groups).
- **Role Mining (Bottom-Up Analysis):** Run data-mining algorithms on existing access lists to find common clusters of permissions. For instance, if $95\%$ of engineers in the Billing team have the permissions `db:read`, `api:billing:read`, and `git:clone`, these permissions are candidates to form a base `Billing Developer` role.

---

## 6. Role Engineering Methodology

Role engineering is the core of the capability design. It determines how entitlements are grouped, structured, and governed.

### 6.1 Role Typology (The Enterprise Role Taxonomy)

To avoid role explosion and maintain clear governance, the enterprise must enforce a strict hierarchy of roles:

```
                  ┌──────────────────────────────┐
                  │       ENTERPRISE ROLE        │  (e.g., Senior Accountant)
                  └──────────────┬───────────────┘
                                 │ Maps to
                                 ▼
                  ┌──────────────────────────────┐
                  │        BUSINESS ROLE         │  (e.g., Accounts Receivable)
                  └──────────────┬───────────────┘
                                 │ Maps to
                                 ▼
                  ┌──────────────────────────────┐
                  │       APPLICATION ROLE       │  (e.g., Ledger Editor in ERP)
                  └──────────────┬───────────────┘
                                 │ Maps to
                                 ▼
                  ┌──────────────────────────────┐
                  │         ENTITLEMENT          │  (e.g., ledger:write permission)
                  └──────────────────────────────┘
```

1. **Enterprise Roles (Job Families):** Broad roles corresponding to corporate designations (e.g., `Senior Accountant`, `Platform Operations Engineer`). These are assigned automatically based on HRIS job code attributes.
2. **Business Roles (Capabilities):** Logical collections of tasks representing a business capability (e.g., `Accounts Receivable Billing Manager`). These map to one or more Application Roles.
3. **Application Roles (Technical Roles):** System-specific access profiles (e.g., `NetSuite Ledger Writer`, `Salesforce Account Editor`). These are the direct links to permissions inside individual software assets.
4. **Temporary & Emergency Roles:** Dynamic, time-bound roles configured for specific projects or operational incidents (e.g., `Incident Responder - Severity 1`, granted for 4 hours).
5. **Privileged Roles:** High-value access profiles (e.g., `Domain Admin`, `Cloud root owner`). These must never be assigned permanently to normal users and must reside inside a Privileged Access Management (PAM) vault.

### 6.2 Top-Down vs. Bottom-Up vs. Hybrid Engineering

- **Top-Down Methodology:** Start with the business process. Analyze the tasks within a process, map tasks to roles, and then map roles to application entitlements.
  - *Pro:* Highly aligned with business intent; clean from the start; minimal technical debt.
  - *Con:* Extremely time-consuming; requires significant coordination with business leaders.
- **Bottom-Up Methodology:** Analyze existing system accounts and permissions, cluster them using mathematical grouping (Apriori algorithm, hierarchical clustering), and abstract them into roles.
  - *Pro:* Fast; captures the exact technical reality of what users are actually doing.
  - *Con:* Codifies existing bad habits; leads to roles containing excess, unused permissions.
- **Hybrid Methodology (Recommended):** Use bottom-up clustering to discover what access currently exists, clean up orphaned and unused entitlements, and then map those clean clusters onto top-down business definitions designed by process owners.

### 6.3 Role Optimization and Consolidation
To prevent role explosion, enforce the following constraints:
- **Maximum Role Density:** No application should support more than $15$ functional roles unless it is a primary ERP platform.
- **Core-to-Edge Ratio:** At least $80\%$ of permissions should be assigned via standard Enterprise Roles (birthright), leaving no more than $20\%$ to be assigned via custom requested roles.
- **Redundancy Auditing:** Identify and consolidate duplicate roles. If Role A has $98\%$ permission overlap with Role B, they must be merged.

### 6.4 Role Quality Metrics
- **Role Density:** $\text{Ratio} = \frac{\text{Total Users}}{\text{Total Roles}}$. A ratio $< 1.0$ indicates role explosion. The target ratio should be $> 5.0$ for operational efficiency.
- **Entitlement Utilization:** Percentage of permissions in a role used within a 90-day window. If the utilization is $< 20\%$, the role is over-privileged and must be decomposed.
- **Mover Orphan Rate:** The percentage of transferred employees who retain old permissions. The target must be $0.0\%$.

---

## 7. Authorization Model Design

Below is the logical authorization metadata model designed to support the enterprise.

### 7.1 Entity Relationship Model
The access control configuration is defined by the following schema structure:

```
  Users (U) ──── (many-to-many) ──── Roles (R) [Supports Inheritance: R1 ⊂ R2]
                                      │
                               (many-to-many)
                                      │
                              Permissions (P)
                                      │
                                 consists of
                                  ┌───┴───┐
                                  ▼       ▼
                             Resource (Re) Operation (Op)
```

To incorporate runtime reality, we inject dynamic **Constraints (C)** and **Environment Context (Cx)**:
$$\text{AccessGranted} = f(U, R, P, Re, Op, C, Cx)$$

### 7.2 Core Enforcement Architecture
Authorization enforcement uses the standard **XACML / Zero Trust Architectural Reference Model**:

```
                       ┌─────────────────────────┐
                       │  Policy Administration  │
                       │       Point (PAP)       │
                       └────────────┬────────────┘
                                    │ Publishes Policy
                                    ▼
┌───────────────┐      ┌─────────────────────────┐      ┌─────────────────────────┐
│ User/Client   ├─────►│     Policy Decision     │◄────►│  Policy Information     │
│ Request       │      │       Point (PDP)       │      │       Point (PIP)       │
└──────┬────────┘      └────────────▲────────────┘      │ (Workday, Active Dir)   │
       │                            │                   └─────────────────────────┘
       │ Access                     │ Evaluates
       ▼                            ▼
┌───────────────┐      ┌─────────────────────────┐
│ API Gateway/  │◄────►│     Policy Enforcement  │
│ Microservice  │      │       Point (PEP)       │
└───────────────┘      └─────────────────────────┘
```

1. **Policy Administration Point (PAP):** The central governance console (e.g., Keycloak console, Open Policy Agent repo) where roles and access policies are defined.
2. **Policy Enforcement Point (PEP):** The runtime gateway or filter (e.g., Spring Security filter, Kong API Gateway) that intercepts access requests and blocks unauthorized actions.
3. **Policy Decision Point (PDP):** The centralized authorization engine (e.g., Open Policy Agent (OPA), Keycloak Authorization Server) that evaluates the rules and returns an `Allow` or `Deny` decision.
4. **Policy Information Point (PIP):** The contextual data sources (e.g., HR database, Threat Intel feed, IP geolocator) that supply the attributes required to resolve runtime policies.

### 7.3 Transitioning Beyond RBAC
While RBAC is the foundation of identity classification, it is fundamentally static. When authorization decisions depend on runtime variables, the architecture must transition to dynamic models:

```
┌────────────────────────────────────────────────────────────────────────┐
│                      AUTHORIZATION SPECTRUM                            │
│                                                                        │
│ RBAC  ──► "Who are you?" (Role-based: finance-analyst)                 │
│ ABAC  ──► "What are the attributes?" (IP, Time, Department, Location)  │
│ PBAC  ──► "What is the policy?" (Policy rules matching ABAC + RBAC)     │
│ ReBAC ──► "What is your relationship to resource?" (Owner, Collaborator)│
└────────────────────────────────────────────────────────────────────────┘
```

#### Transition Triggers:
- **ABAC (Attribute-Based Access Control):** Use when access depends on attributes like location, IP subnet, time of day, or asset classification. (e.g., *Access is denied if the network request originates from outside the corporate VPN, even for platform admins*).
- **PBAC (Policy-Based Access Control):** Use to centralize complex, declarative security rules that merge RBAC roles and ABAC parameters into executable policies.
- **ReBAC (Relationship-Based Access Control):** Use when access is determined by object-to-object or user-to-object relationships, typical in document sharing or hierarchical management structures (e.g., *Manager X can view records of User Y because Y reports to X in the org-chart*).

---

## 8. Capability Architecture

Following TOGAF and Business Architecture principles, we map the enterprise IAM/RBAC system as a core corporate capability.

### 8.1 Capability Map (IAM - Identity Governance and Administration)

```
┌────────────────────────────────────────────────────────────────────────────────┐
│                             IAM CAPABILITY MAP                                 │
├───────────────────────┬────────────────────────┬───────────────────────────────┤
│ Identity Lifecycle    │ Role & Access Governance│ Enforcement & Audit          │
├───────────────────────┼────────────────────────┼───────────────────────────────┤
│ - Onboarding (Joiner) │ - Role Engineering     │ - PEP Enforcement Gateway     │
│ - Transfers (Mover)   │ - SoD Conflict Analysis│ - Attestation / Review Cycles │
│ - Offboarding (Leaver)│ - Access Certifications│ - Tamper-Proof Audit Logging  │
└───────────────────────┴────────────────────────┴───────────────────────────────┘
```

### 8.2 Business Services & Processes
- **Role Creation Process:** A business unit owner requests a new business role. The request triggers an automated workflow verifying name standards, metadata completeness, and checking for static SoD conflicts before provisioning.
- **Access Certification Service:** A quarterly automated process that compiles active role assignments and triggers review requests for system owners and managers. If a reviewer does not take action within $15$ days, the access is automatically revoked (fail-secure posture).

### 8.3 Architecture Dimensions

| Dimension | Standard Design Pattern |
| :--- | :--- |
| **Operating Model** | Federated ownership: Center of Excellence (CoE) governs policies, business units own role maps. |
| **Data Architecture** | Graph-based representation of identities, roles, and entitlements to quickly detect path inheritance and toxic relationships. |
| **Security Architecture** | Identity perimeter integrated with network micro-segmentation. Authentication enforces MFA via FIDO2 keys. |
| **Integration Architecture** | Direct integration using standards-based protocols: SCIM 2.0 for user provisioning, OAuth2/OIDC for session assertion, gRPC/REST for PDP queries. |
| **Automation Engine** | Dynamic Event-Driven Engine listening to HRIS events (Workday webhooks) to trigger instant joiner/mover/leaver provisioning scripts. |

---

## 9. Governance Framework

Governance is the operational machinery that keeps the authorization configuration clean, compliant, and secure.

### 9.1 The Ownership Matrix
To maintain accountability, every object in the access model must have designated corporate owners:
- **Role Owner:** A business leader (typically a Director or VP) who is accountable for the business definition of a role, authorizing new members, and conducting access reviews.
- **Data Owner:** The business steward of an information asset (e.g., VP of Finance owns the ledger tables). They set data classifications and approve which application roles are allowed to access their data.
- **Application Owner:** The engineering manager or product owner of the application. They are responsible for mapping technical permissions to application roles and integrating the PEP with the enterprise PDP.

### 9.2 Approval Workflows & Exceptional Access
1. **Standard Role Requests:** Initiated by the user or manager via a self-service portal. Automatically routes to the Role Owner for approval.
2. **Privileged Access Requests:** Requires dual-authorization (Role Owner + Security team) and must enforce automatic revocation after a designated time window (e.g., 8 hours).
3. **Emergency Access (Break-Glass):** Pre-authorized roles that bypass normal workflow approval queues during active operational incidents. Activating a break-glass role triggers high-severity alerts to the Security Operations Center (SOC) and locks down the session configuration to record all actions in detail.

### 9.3 Attestation and Certification Cycles

```
   ┌──────────────────────┐      Auto-Generates      ┌──────────────────────┐
   │ IGA Governance Engine├─────────────────────────►│  Manager Review      │
   └──────────▲───────────┘                          │  Dashboard          │
              │                                      └──────────┬───────────┘
              │                                                 │
              │ Receives Audit                                  │ Approves/
              │ Confirmation                                    │ Revokes
              │                                                 ▼
   ┌──────────┴───────────┐                          ┌──────────────────────┐
   │ Audit & Logs Store   │◄─────────────────────────┤ Entitlement State    │
   └──────────────────────┘                          │ Executed             │
                                                     └──────────────────────┘
```

- **High-Risk Roles (Admin, Finance, Cryptographic Keys):** Recertification must run **monthly**.
- **Standard Business Roles:** Recertification must run **quarterly**.
- **Low-Risk Roles:** Recertification must run **semi-annually**.
- **Dynamic Revocation:** If an manager denies an entitlement or fails to attest to it before the expiration window, the access provisioner triggers automated SCIM revocation.

---

## 10. Security Architecture

Our security model integrates RBAC directly with standard cybersecurity architectures.

### 10.1 Zero Trust Alignment (NIST SP 800-207)
- **Assumption of Breach:** We assume that credentials will be compromised. Therefore, access is never granted based solely on location or identity verification at login.
- **Dynamic Session Evaluation:** Every access request is evaluated by the PDP at the time of execution. The PDP checks the user's role, the security status of their device, their geographic location, and active threat indicators before generating a transaction token.

### 10.2 Need-to-Know & Least Privilege
- **Least Privilege:** Users are assigned only the minimum set of permissions necessary to execute their immediate tasks.
- **Birthright Boundaries:** Employees receive a minimal set of baseline roles at hire (e.g., standard email, payroll view). All higher-level business capabilities require specific, documented requests and business approval.

### 10.3 Segregation of Duties (SoD) Framework
We enforce rules to prevent toxic combinations of privileges that could facilitate fraud or systemic failure:

| Role A | Role B | Constraint Type | Rationale |
| :--- | :--- | :--- | :--- |
| Invoice Creation | Invoice Approval | Static SoD | Prevents unauthorized self-payment. |
| Code Developer | Deployment Manager | Static SoD | Enforces independent validation of code before production release. |
| DB Security Officer | Database Administrator | Dynamic SoD | Ensures DB config updates cannot be performed by the database administrator without independent audit oversight. |

- **Static SoD:** The authorization repository prevents the assignment of both roles to the same user account.
- **Dynamic SoD:** A user may hold both roles but is prohibited from activating or exercising both roles during a single operational transaction or session.

---

## 11. Implementation Roadmap

A successful enterprise RBAC transition must follow a structured, phased roadmap to minimize business disruption.

```
┌────────────────────────────────────────────────────────────────────────┐
│                          PROJECT ROADMAP                               │
│                                                                        │
│ Phase 1: Assess & Govern (Month 1-2)  ──► Setup CoE, baseline tools    │
│ Phase 2: Discover & Clean (Month 3-4) ──► Audit permissions, map HRIS  │
│ Phase 3: Role Eng (Month 5-7)         ──► Hybrid mining, SoD modeling  │
│ Phase 4: Pilot (Month 8-9)            ──► Target high-value business units│
│ Phase 5: Migration (Month 10-12)      ──► Full enterprise transition   │
│ Phase 6: Continuous Opt (Ongoing)     ──► AI optimization, review loops│
└────────────────────────────────────────────────────────────────────────┘
```

### 11.1 Phase Detailed Plans

```
   =========================================================================
   PHASE 1: Assessment & Governance Establishment (Month 1 - Month 2)
   =========================================================================
   Objective:       Establish IAM Center of Excellence (CoE) and define 
                    governance metrics, policy frameworks, and standard roles.
   Deliverables:    IAM Governance Charter, Role Taxonomy Specification, 
                    SoD Core Rule Matrix, Tool Chain evaluation report.
   Activities:      - Secure executive sponsorship.
                    - Form the Federated IAM CoE.
                    - Define roles, permissions, and metadata standards.
                    - Catalog all regulatory obligations (SOX, GDPR, ISO).
   Inputs:          - Corporate Org Chart.
                    - Audit records and corporate risk policies.
   Outputs:         - IAM Charter and Role Engineering playbook.
   Success Criteria:- 100% agreement on role taxonomy and metadata schemas.
   Risks:           - Lack of business unit engagement.
   Mitigation:      - Tie engagement metrics directly to Business Unit OKRs.
   Dependencies:    - Executive sponsor approval.

   =========================================================================
   PHASE 2: Discovery & Data Cleansing (Month 3 - Month 4)
   =========================================================================
   Objective:       Establish current access baseline and identify 
                    orphaned permissions, shadow accounts, and unused access.
   Deliverables:    Orphaned Entitlement Report, Identity Inventory, 
                    Access Analytics Baseline Report.
   Activities:      - Extract active permission lists from databases and directories.
                    - Map HRIS fields to target identity records.
                    - Run access mining tools to detect stale profiles.
                    - Revoke accounts belonging to terminated users.
   Inputs:          - Active Directory exports, SaaS user files.
   Outputs:         - Cleaned-up baseline data directory.
   Success Criteria:- Revocation of >98% of orphaned accounts.
   Risks:           - Deleting accounts that are actually in use.
   Mitigation:      - Run a "silent audit" phase where accounts are disabled
                      for 14 days before deletion (Scream Test).
   Dependencies:    - Clean HRIS integration.

   =========================================================================
   PHASE 3: Role Engineering & Policy Design (Month 5 - Month 7)
   =========================================================================
   Objective:       Design Business Roles, Technical Roles, and define SoD maps.
   Deliverables:    Enterprise Role Catalog, OPA Policy Definitions, 
                    Role Hierarchy Maps.
   Activities:      - Conduct hybrid role mining on target business lines.
                    - Host design workshops with system owners.
                    - Formulate Role Hierarchies.
                    - Document SoD matrices.
   Inputs:          - Clean identity data and business process maps.
   Outputs:         - Standard Role Catalog database.
   Success Criteria:- Mapping of 80% of core users to standard role templates.
   Risks:           - Role Explosion during definition workshops.
   Mitigation:      - Enforce role design rules (e.g., maximum nesting limits).
   Dependencies:    - Cleansed data from Phase 2.

   =========================================================================
   PHASE 4: Pilot Implementation (Month 8 - Month 9)
   =========================================================================
   Objective:       Validate the RBAC implementation in a subset of 
                    high-value, highly auditable business units.
   Deliverables:    Pilot Walkthrough, Performance Latency reports, 
                    User Experience Feedback Audit.
   Activities:      - Select pilot business units (e.g., Finance & HR).
                    - Deploy PEP and central PDP configurations.
                    - Automate birthright provisioning for pilot users.
                    - Monitor authentication latencies.
   Inputs:          - Pilot user profiles and target applications.
   Outputs:         - Initial production performance logs.
   Success Criteria:- Pilot execution with zero high-severity incidents.
   Risks:           - Integration latency degrades application performance.
   Mitigation:      - Deploy read-replicas of the PDP close to target gateway.
   Dependencies:    - Identity system infrastructure readiness.

   =========================================================================
   PHASE 5: Enterprise Migration & Migration Deploy (Month 10 - Month 12)
   =========================================================================
   Objective:       Complete migration of all business units to the RBAC capability.
   Deliverables:    Enterprise Migration Report, Final System Configuration, 
                    Decommissioning Report for old provisioning servers.
   Activities:      - Bulk migrate remaining business divisions.
                    - Standardize role certifications across all assets.
                    - Decommission legacy ad-hoc AD groups.
                    - Conduct compliance reviews.
   Inputs:          - Full user database and migrated application inventory.
   Outputs:         - Modernized identity directory.
   Success Criteria:- >95% of active systems integrated with central PEPs.
   Risks:           - Scale-related bottlenecks.
   Mitigation:      - Implement regional clusters and load balancing.
   Dependencies:    - Pilot completion and sign-off.

   =========================================================================
   PHASE 6: Operations & Continuous Improvement (Ongoing)
   =========================================================================
   Objective:       Maintain role cleanliness and adapt access controls.
   Deliverables:    Monthly Performance Metrics, Security Drift Analysis, 
                    Audit compliance logs.
   Activities:      - Conduct quarterly automated access reviews.
                    - Run machine learning models to detect drift.
                    - Review and retire redundant roles.
                    - Train security staff on incident response rules.
   Inputs:          - Continuous logs and operational data.
   Outputs:         - Optimized and refined role structures.
   Success Criteria:- Zero failed compliance audits; zero role drift.
   Risks:           - Organizational drift.
   Mitigation:      - Establish monthly role review cycles.
   Dependencies:    - Completed deployment.
```

---

## 12. Risk Register

Managing access control changes involves technical, operational, and organizational risks. The table below details key risks, their severities, and planned mitigations:

| Risk Code | Risk Description | Likelihood | Impact | Score (L x I) | Mitigation Strategy |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **R-01** | **Role Explosion:** The number of custom roles grows uncontrollably, returning the organization to ad-hoc permissions management. | High | High | **High (16)** | Establish strict metadata requirements for new roles, implement a Central Role Governance Board, and set a hard rule: *No role may have fewer than 5 active members.* |
| **R-02** | **Business Process Disruption:** Migration of access controls inadvertently blocks standard business operations, halting revenue. | Medium | Critical | **High (12)** | Run migration in "dry-run" mode first, where access issues are logged but not blocked, and verify access against actual historical logs. |
| **R-03** | **Identity Directory Drift:** The source HR system fails to capture changes (contract extensions, internal transfers), causing RBAC data to become stale. | High | Medium | **Medium (9)** | Configure automated daily synchronization reconciliations between HRIS and the IAM directory with strict alerting for inconsistencies. |
| **R-04** | **Privilege Creep:** Employees accumulate roles as they move across teams, resulting in over-privileged user states. | High | High | **High (12)** | Implement automated dynamic revocation of old roles when key attributes (e.g., Department, Manager) change in the HRIS. |
| **R-05** | **PDP Engine Failure:** A central Policy Decision Point goes offline, blocking all access to integrated company applications. | Low | Critical | **Medium (8)** | Architect PDP deployments using a decentralized sidecar pattern or distributed read-replicas with a local fail-open backup configuration. |

---

## 13. Decision Matrix (RBAC vs ABAC vs PBAC vs ReBAC)

To guide application developers and system architects, the following matrix compares access control models:

| Dimension | Role-Based (RBAC) | Attribute-Based (ABAC) | Policy-Based (PBAC) | Relationship-Based (ReBAC) |
| :--- | :--- | :--- | :--- | :--- |
| **Core Evaluation Criteria** | Job Title, Group Membership. | User, Resource, and Environment attributes. | Consolidated logical rules linking roles and attributes. | Graph path validation (e.g., Owner, Manager, Direct Report). |
| **Management Complexity** | Low to Medium. Easy to understand. | High. Policies can become complex to troubleshoot. | Medium. Centralized management simplifies verification. | Medium. Requires a graph database or specialized tree logic. |
| **Enforcement Latency** | Low ($< 10\text{ms}$). | Medium ($10\text{ms} - 50\text{ms}$). | Medium ($15\text{ms} - 50\text{ms}$). | High ($30\text{ms} - 100\text{ms}$ depends on graph depth). |
| **Audit Simplicity** | Very High. Easy to see who has what role. | Low. Hard to prove what access a user might have. | High. Policies are codified and testable. | Medium. Requires evaluating relationship paths. |
| **Best Scenario** | Static workforce access (e.g., "All Finance members can read invoices"). | Contextual rules (e.g., "Allow access only from IP 10.0.0.1 on weekdays"). | Complex corporate environments mapping regulations to access. | Resource sharing models (e.g., "Allow Editor to edit because they own Folder X"). |

---

## 14. Architecture Diagrams

### 14.1 Enterprise Logical Authorization Model

```
   ┌────────────────────────────────────────────────────────┐
   │                     Identity Source                    │
   │               (Workday HRIS Attributes)                │
   └───────────────────────────┬────────────────────────────┘
                               │ Syncs Identity Attributes
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │                  Target Enterprise Role                │
   │              (Mapped based on Job Codes)               │
   └───────────────────────────┬────────────────────────────┘
                               │ Inherits
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │                     Business Role                      │
   │              (Logical Capability Mapping)              │
   └───────────────────────────┬────────────────────────────┘
                               │ Grouped into
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │                    Application Role                    │
   │                  (System-Specific Role)                │
   └───────────────────────────┬────────────────────────────┘
                               │ Enforces
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │                      Permissions                       │
   │                 (Resource + Operation)                 │
   └────────────────────────────────────────────────────────┘
```

### 14.2 PEP/PDP/PIP Runtime Execution Flow

```
User App           API Gateway / PEP          Central PDP              PIP / Dir
   │                       │                       │                       │
   │─── 1. Access Req ────►│                       │                       │
   │    (with JWT Token)   │                       │                       │
   │                       │─── 2. Evaluate ──────►│                       │
   │                       │    (Token + Action)   │                       │
   │                       │                       │─── 3. Fetch Context ─►│
   │                       │                       │    (Org, Device)      │
   │                       │                       │                       │
   │                       │                       │◄── 4. Context Data ───│
   │                       │                       │                       │
   │                       │◄── 5. Decision ───────│                       │
   │                       │    (Allow / Deny)     │                       │
   │                       │                       │                       │
   │◄── 6. Response ───────│                       │                       │
   │    (Success/403)      │                       │                       │
```

---

## 15. RACI Matrix

This matrix assigns responsibility for key access control lifecycle activities:

| Activity | CISO | IAM Program Director | Business Unit Owner | Data Steward / Owner | HR Operations | Systems Developer | Internal Audit |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Establish IAM Charter & Policies** | **A** | **R** | **C** | **C** | **I** | **I** | **C** |
| **Define & Map Business Roles** | **I** | **R** | **A** | **C** | **C** | **I** | **C** |
| **Define SoD Core Rules Matrix** | **A** | **R** | **C** | **C** | **I** | **I** | **R** |
| **Cleanse Legacy Access Data** | **I** | **R** | **C** | **C** | **I** | **R** | **I** |
| **Provision Identity Lifecycle** | **I** | **A** | **I** | **I** | **R** | **I** | **I** |
| **Enforce PEP/PDP Integrations** | **I** | **A** | **I** | **I** | **I** | **R** | **I** |
| **Conduct Recertification Reviews**| **I** | **C** | **A** | **R** | **I** | **I** | **C** |
| **Audit Access Configurations** | **C** | **I** | **I** | **I** | **I** | **I** | **A** |

- **R:** Responsible (Does the work).
- **A:** Accountable (Approves the work; holds final ownership).
- **C:** Consulted (Inputs provided).
- **I:** Informed (Kept updated on progress).

---

## 16. Capability Maturity Model

To track progress, we define a customized 5-tier maturity model based on the CMMI framework:

```
┌────────────────────────────────────────────────────────────────────────────────┐
│                             MATURITY MATRIX                                    │
├───────────────┬────────────────────────────────────────────────────────────────┤
│ Level         │ Characteristics                                                │
├───────────────┼────────────────────────────────────────────────────────────────┤
│ 5. Optimizing │ - Continuous machine learning optimizations detect role drift.  │
│               │ - Risk-based dynamic conditional access checks executed.        │
├───────────────┼────────────────────────────────────────────────────────────────┤
│ 4. Managed    │ - Centralized IGA tool automates Joiner-Mover-Leaver flows.     │
│               │ - Role owners attest to role definitions on scheduled cycles.  │
├───────────────┼────────────────────────────────────────────────────────────────┤
│ 3. Defined    │ - Enterprise role taxonomy codified and approved.              │
│               │ - Separation of Duties (SoD) policies programmatically enforced.│
├───────────────┼────────────────────────────────────────────────────────────────┤
│ 2. Repeatable │ - Basic AD group taxonomy is documented.                       │
│               │ - Manual process handles role updates; inconsistent auditing.   │
├───────────────┼────────────────────────────────────────────────────────────────┤
│ 1. Initial    │ - Ad-hoc, direct permissions assigned to individual accounts.  │
│               │ - No centralized role directory, no designated role owners.    │
└───────────────┴────────────────────────────────────────────────────────────────┘
```

---

## 17. KPIs and KRIs

To measure the health and risk posture of the authorization capability, we track the following metrics:

### Key Performance Indicators (KPIs)
1. **Day-One Readiness Rate:** Percentage of new hires who receive all required access on their first day of work.
   - *Target:* $> 95\%$
2. **Access Provisioning Latency:** Average time taken to provision an approved access request.
   - *Target:* $< 10\text{ minutes}$ (Automated systems)
3. **Role Coverage Ratio:** Percentage of enterprise users whose access is governed entirely by standard birthright/enterprise roles.
   - *Target:* $> 80\%$
4. **Attestation Completion Rate:** Percentage of certification reviews completed on time.
   - *Target:* $100\%$

### Key Risk Indicators (KRIs)
1. **Privilege Creep Rate:** Percentage of users who transfer departments but retain old permissions for over $14$ days.
   - *Target:* $0.0\%$
2. **Toxic SoD Violations:** Number of active identities holding conflicting roles (e.g., Invoice Creator + Approver) without an approved risk exception.
   - *Target:* $0$
3. **Orphaned Account Count:** Number of active accounts in down-stream directories that cannot be mapped to an active identity.
   - *Target:* $0$
4. **Emergency Access Activations:** Number of times break-glass or privileged roles are activated.
   - *Target:* Minimise, with $100\%$ requiring post-incident audit validation.

---

## 18. Validation Checklist

This checklist is used by Internal Audit, Security Officers, and Compliance teams before validating the live RBAC capability:

- [ ] **Data Source Verification:** Is every active account mapped to a verified identity in the HRIS database?
- [ ] **Role Owner Verification:** Does every role in the enterprise directory have a designated, active Business Owner?
- [ ] **SoD Enforce Check:** Are the Static SoD rules active, and do they block toxic combination assignments?
- [ ] **Revocation Validation:** Does disabling a user in the HRIS trigger automated access revocation across all down-stream target systems within 15 minutes?
- [ ] **Least Privilege Audit:** Do role assignments contain any wildcards (`*:*`) or unnecessary administrative permissions?
- [ ] **Audit Trail completeness:** Are PEP access denials, administrative role changes, and certification decisions logged to the secure SIEM log store?
- [ ] **Mover Workflow Check:** Does an employee's role mapping automatically update when their Job Code changes in the HRIS?

---

## 19. Common Pitfalls & Prevention Strategies

- **Pitfall: Treating RBAC as a static, one-time project.**
  - *Symptom:* The project ends, the team disbands, and the roles drift out of sync with business needs over the next year.
  - *Prevention:* Form an IAM Center of Excellence (CoE) to continuously manage role modeling, review logs, and audit configuration health.
- **Pitfall: Creating roles that are too granular (Role Explosion).**
  - *Symptom:* Having roles like `Sales-Editor-US-West-Lead-Level2`.
  - *Prevention:* Restrict Role granularity. Use Enterprise Roles for high-level structure, and use ABAC attributes (Location = US-West, Tier = Level2) to filter access dynamically at runtime.
- **Pitfall: Over-reliance on "Clone User" configurations.**
  - *Symptom:* A new hire is provisioned by copying the access profile of a tenured colleague.
  - *Prevention:* Explicitly ban copy-user mechanisms in the provisioning tools. Access must only be granted via standard roles.

---

## 20. Best Practices

1. **Deny by Default:** Design PEP configurations to reject all requests unless a valid, active role explicitly grants the required permission.
2. **Standardize Role Naming:** Enforce structured naming conventions for roles (e.g., `bu:finance:procurement:invoice-approver`).
3. **Externalize Authorization Logic:** Applications should not hard-code role checks (`if user.hasRole('Admin')`). They should query an API PDP or validate OAuth scopes, decouple business rules from code.
4. **Adopt Cloud-Native Authorization:** Use containerized PDP sidecars (such as Open Policy Agent) and standards-based configuration (Rego policies) to secure microservice environments.
5. **Decouple Identity from Access:** Ensure that the Identity Provider (IdP) focuses on authentication (AuthN) and attribute assertions, while the specialized IGA/PDP handles authorization (AuthZ).

---

## 21. Final Recommendations

To successfully establish the Enterprise RBAC Strategic Capability, the organization should execute the following priorities:

1. **Form the IAM CoE Immediately:** Establish the cross-functional governance board containing Security, Business Process Owners, GRC, and IT Infrastructure leads.
2. **Establish the Source Directory as the Single Source of Truth:** Prioritize cleansing HRIS data. If the HR department, title, and job code attributes are not clean, the downstream automation will fail.
3. **Implement a Hybrid Role Engineering Approach:** Use bottom-up clustering to discover active patterns, map those patterns to clean business processes, and prune obsolete access profiles.
4. **Deploy a Standard Gateway PEP Strategy:** Intercept external and internal API traffic using standard PEP API Gateways. Centrally log all access decisions for audit analysis.
5. **Transition to Hybrid Authorization (RBAC + ABAC):** Keep roles high-level and standard. Enforce dynamic conditions (e.g., device health, IP, time of day) via ABAC attributes to limit the complexity of the role catalog.

---
*End of Blueprint Document. Approved by the Enterprise Security Architecture Center of Excellence.*
