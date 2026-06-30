# Authentication Standard — Conductor Platform

This standard defines the policies, protocol configurations, token lifecycles, and security controls for all user and machine identities accessing the Conductor Platform.

---

## 1. IAM Platform Evaluation

The platform utilizes **Keycloak** as its canonical Identity Provider (IDP) integrated with **Kong API Gateway** acting as the token validation layer. 

### Keycloak Evaluation
*   **SaaS Tenancy:** Supports native "Realms" allowing physical and logical isolation of user registers per tenant.
*   **Standards Compliance:** Complete OpenID Connect (OIDC) and OAuth 2.0 implementation.
*   **Session Management:** Centralized database tracking active sessions with back-channel logout capabilities.
*   **Security Feasibility:** Robust support for multi-factor authentication (TOTP/WebAuthn), password policy enforcement, and client credential mappings for service accounts.

---

## 2. Authentication Standards by Actor Type

### 2.1 User Authentication (Human-to-System)
*   **Protocol:** **OpenID Connect (OIDC) Standard**.
*   **Flow Type:** **Authorization Code Flow with PKCE (Proof Key for Code Exchange)** is mandated for all SPA React Client applications. Implicit flows are strictly banned.
*   **SSO Integration:** Keycloak federates with enterprise identity pools (SAML, Google Workspace, Azure AD) per tenant realm context.

### 2.2 Developer / API Access (System-to-API Ingress)
*   **Method:** **Custom API Keys**.
*   **Structure:** Keys prefixed with `cond_live_` or `cond_test_` containing cryptographically secure random bytes (min 32 characters).
*   **Verification:** Verified via SHA-256 signatures validated against hashed records at the Kong API Gateway layer.

### 2.3 Service Accounts (System-to-System Monolith/Adapters)
*   **Protocol:** **OAuth 2.0 Client Credentials Grant**.
*   **Flow Type:** Internal adapters (e.g. `whatsapp-adapter`) authenticate with Keycloak using Client IDs and Client Secrets to obtain short-lived machine tokens.

### 2.4 Machine-to-Machine (Internal Infrastructure)
*   **Protocol:** **mTLS (Mutual TLS 1.3) / Static Authentication Tokens**.
*   **Implementation:** 
    *   Temporal workers communicate with Temporal server via gRPC secured with mTLS.
    *   Spring Boot Monolith modules communicate with Redis and PostgreSQL using static connection credentials rotated regularly.
    *   NATS JetStream clients use token-based authentication verified at stream gateways.

---

## 3. Token Lifetimes & Refresh Policies

To minimize the impact of token hijacking, the platform enforces short-lived tokens and strict refresh policies:

| Token Type | Standard Lifetime | Refresh Strategy | Description |
| :--- | :--- | :--- | :--- |
| **Access Token (JWT)** | 15 Minutes | Non-renewable directly. Requires presenting Refresh Token. | Contains user roles, tenant identifier, and scopes. Cryptographically verified by Kong. |
| **Refresh Token** | 8 Hours | **Refresh Token Rotation (RTR)** enforced. | Used to request new Access Tokens. When consumed, the old Refresh Token is immediately invalidated. |
| **ID Token** | 15 Minutes | Matches Access Token lifecycle. | Contains user profile details (name, email) for client-side UI hydration. |
| **API Keys** | Infinite / Manual Expiry | Manual rotation required annually. | Developer access keys. User can invalidate them instantly from the workspace panel. |

### Refresh Token Rotation (RTR) Policy
When a client requests a new Access Token using a Refresh Token:
1.  Keycloak validates the Refresh Token signature and checks its active status in the database session list.
2.  Keycloak issues a *new* Access Token and a *new* Refresh Token to the client.
3.  The *old* Refresh Token is immediately deleted from the active list.
4.  **Replay Detection:** If a reused/invalidated Refresh Token is presented, Keycloak automatically flags the session as compromised, revokes all tokens associated with that token family tree, and triggers a security alert.

---

## 4. Password Policies

All local Keycloak tenant realms must enforce the following password complexity and behavior rules:

*   **Minimum Length:** 12 Characters.
*   **Complexity Rules:** Must contain at least:
    *   1 Uppercase Letter.
    *   1 Lowercase Letter.
    *   1 Numeric Digit.
    *   1 Special Character (e.g., `@`, `#`, `$`, `%`).
*   **Username Exclusions:** Password must not contain the username, first name, last name, or email string of the user.
*   **Password History:** Keycloak remembers the last 5 passwords, blocking reuse.
*   **Brute-Force Account Lockout:**
    *   **Maximum Failures:** 5 consecutive failed login attempts.
    *   **Lockout Duration:** 15 Minutes.
    *   **Failure Reset Window:** 30 Minutes.

---

## 5. Multi-Factor Authentication (MFA) Requirements

*   **Mandatory Scope:** MFA is **mandatory** for:
    *   All System Administrators (Global level).
    *   All Tenant Administrators (Workspace owners).
    *   Users altering Billing/Payment configurations.
    *   Users modifying SMS/WhatsApp API credentials or triggering batch campaigns.
*   **Supported Methods:**
    *   **TOTP (Time-Based One-Time Password):** Google Authenticator, Microsoft Authenticator, Duo (Algorithm: SHA-1, 6 digits, 30-second interval).
    *   **WebAuthn / FIDO2 (Recommended):** Hardware security keys (YubiKeys), biometric platform authenticators (FaceID, Windows Hello).
*   **Fallback Codes:** Keycloak generates 10 single-use recovery codes upon MFA setup, which must be stored securely by the user.

---

## 6. Session Management & Termination

*   **Inactive Session Timeout:** 30 Minutes. If no requests are made to the API Gateway within this window, the session is deactivated.
*   **Absolute Session Timeout:** 24 Hours. Irrespective of activity, the user must re-authenticate after 24 hours.
*   **Single Active Session (Configurable):** Tenant admins can enforce a single concurrent active session per user to block credential sharing.
*   **Logout Mechanism:** Calling `POST /api/v1/auth/logout` triggers:
    1.  Gateway forwards request to Keycloak's logout endpoint.
    2.  Keycloak deactivates the session database entry, invalidating the refresh token.
    3.  A back-channel logout signal is published to NATS JetStream, triggering local token cache evictions on active backend modules.

This standard is approved for all Conductor environments.
