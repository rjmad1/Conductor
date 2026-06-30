# Conductor User Guide

## A. Purpose
This guide walks end users (business owners, staff agents) through the standard operational procedures of the Conductor platform, including account setup, connector authentication, campaign building, and analytics reporting.

## B. Intended Audience
- Tenant Business Owners
- Customer Support Agents / Operators
- Sales & Marketing Teams

## C. Scope
Covers dashboard interfaces, campaign designer workflow, integrations workspace, and metrics explanation.

## D. Prerequisites
- An active Conductor account registered under a tenant domain.
- Admin credentials for your Shopify or Zoho CRM systems if configuring integrations.

---

## E. Detailed Content

### 1. Account Onboarding & Team Invites
1. **Initial Login**: Check your email for the Conductor invitation link. Log in at `https://<your-domain>.conductor.app` via Keycloak SSO.
2. **Password Reset**: Update your temporary password in your Account Settings.
3. **Invite Staff**:
   - Go to **Settings > Team Management**.
   - Input your colleague’s email and choose a role: `Admin` (full integration config, campaign launches) or `Agent` (only access the conversation inbox).
   - Click **Invite User** to trigger Keycloak invitation flows.

---

### 2. Configuring Connectors
Conductor automates business workflows by listening to event logs from your external platforms.

#### Shopify Setup:
1. Go to **Integrations > Shopify** and click **Install App**.
2. Input your Shopify store URL (e.g. `my-store.myshopify.com`).
3. Authorize the OAuth permission request. Shopify webhooks for `orders/create` and `cart/abandoned` will register automatically.

#### Zoho CRM Setup:
1. Go to **Integrations > Zoho CRM** and click **Connect**.
2. Click **Authorize** to redirect to the Zoho OAuth screen. Log in to your Zoho CRM admin console.
3. Accept scopes to allow reading `Leads` and `Contacts`.

#### Razorpay Setup:
1. Go to **Integrations > Razorpay**.
2. Copy the Webhook Ingress URL: `https://api.conductor.app/api/v1/integrations/webhooks/ingress/razorpay/<tenantId>`.
3. Log in to your Razorpay Dashboard, create a new Webhook, paste the Ingress URL, and define a secret.
4. Input the webhook secret back into Conductor to validate signatures.

---

### 3. Creating & Scheduling Campaigns
Campaigns automate customer outreach via WhatsApp.

1. **Select template**: Go to **Campaigns > Templates**. Submit a WhatsApp template to Meta for approval (takes 10m to 2h).
2. **Define Segment**: In **Customers > Segments**, create target customer groups (e.g., customers who added to cart but didn't check out in 2 hours).
3. **Build Workflow**:
   - Create a workflow on the designer canvas.
   - Set a trigger: `Shopify Cart Abandoned`.
   - Set an action: `Wait 30 minutes`.
   - Set condition check: `Check if order completed == false`.
   - Action: `Send WhatsApp template: Cart Recovery Message`.
4. **Publish**: Toggle **Activate** to launch the campaign.

---

### 4. Managing Customer Conversations
If a customer replies to an automated WhatsApp campaign, the message is routed to the **Shared Inbox**.

- **Access Inbox**: Click **Inbox** on the sidebar to load the embedded Chatwoot dashboard.
- **Assign Chats**: Conversations are automatically assigned based on agent queues or can be manually claimed.
- **Tags & Attributes**: Add status tags (e.g., `Warm Lead`, `Billing Query`) to update customer registries in real-time.

---

### 5. Understanding Dashboard Metrics
The home dashboard tracks system-wide delivery performance:

- **Sent vs Delivered vs Read**: Shows exact Meta API status callbacks.
- **Click-Through-Rate (CTR)**: Tracks user interaction with links in WhatsApp campaigns.
- **Opt-Out Rate**: The percentage of recipients replying with `STOP`. High opt-out rates (>3%) trigger warning flags to avoid WhatsApp number suspensions.

---

## F. References
- [Product Guide](Product-Guide)

## G. Related Wiki Pages
- [Best Practices Guide](Best-Practices-Guide)
- [Developer & API Guide](Developer-and-API-Guide)
