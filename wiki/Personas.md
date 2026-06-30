# Personas — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** Inferred from Customer Segments, Business Needs  
**Last Updated:** June 2026

---

## Purpose
Defines the buyer, user, and administrator personas for Conductor. Personas inform product decisions, UX design, and go-to-market messaging.

---

## Persona 1: The Overwhelmed Clinic Owner (Buyer + Admin)

**Name:** Dr. Sunita Reddy  
**Role:** Owner, 10-bed multi-specialty clinic  
**Location:** Hyderabad, Telangana  
**Business size:** 15 employees  
**Plan:** Growth (₹4,999/month)

### Background
Sunita runs a busy clinic with 40–60 appointments per day. She spends 2 hours daily on WhatsApp — confirming appointments, answering "Is the doctor available today?", and sending lab reports manually. She has tried a basic CRM (didn't stick) and uses Google Sheets to track patients.

### Goals
- Reduce no-shows from 25% to under 10%
- Stop spending personal time on appointment confirmations
- Deliver lab reports faster (currently takes 1 day, should take 1 hour)
- Know which patients need follow-up without manually tracking

### Frustrations
- "My staff spends all morning confirming appointments that are already booked"
- "Patients forget appointments even after confirmation"
- "I can't afford a developer and existing tools are too complex"
- "I just want something that works — I don't want to understand 'webhooks'"

### Technology Comfort
- Uses WhatsApp Business daily
- Uses Google Workspace (Sheets, Drive)
- Has tried basic automation tools but abandoned them
- No developer on team

### Decision Trigger
A colleague's clinic reduced no-shows by 30% using a WhatsApp automation tool. Sunita wants the same.

### Success Criteria
- Setup in <30 minutes
- First reminder sent within 1 hour of setup
- Measurable no-show reduction within 30 days

---

## Persona 2: The E-Commerce Founder (Buyer + Power User)

**Name:** Rohan Sharma  
**Role:** Founder, D2C apparel brand (Shopify)  
**Location:** Mumbai, Maharashtra  
**Business size:** 8 employees  
**Plan:** Business (₹12,999/month)

### Background
Rohan runs a growing D2C brand with 500–800 orders/month. He has a Shopify store and uses Razorpay for payments. His cart abandonment rate is 68%. He knows about WhatsApp automation but has only used bulk broadcast tools (AiSensy), which don't connect to Shopify properly.

### Goals
- Recover 15%+ of abandoned carts
- Automate order and shipping notifications (currently done manually by 1 staff member)
- Send personalized re-engagement campaigns to lapsed customers
- Reduce COD returns by verifying COD orders before dispatch

### Frustrations
- "AiSensy lets me broadcast but can't read my Shopify data"
- "My staff spends 3 hours/day copying order details into WhatsApp messages"
- "I lose customers because my follow-up is inconsistent"

### Technology Comfort
- Comfortable with SaaS tools (Shopify, Klaviyo, Google Analytics)
- Understands basic automation concepts
- Willing to invest time in setup if ROI is clear

### Decision Trigger
Sees a competitor's cart recovery WhatsApp message and wants the same capability. Finds Conductor via Google search.

### Success Criteria
- Shopify integration working in <1 hour
- First abandoned cart message sent within same day
- 10%+ cart recovery rate within 30 days

---

## Persona 3: The Operations Manager (Primary User, not Buyer)

**Name:** Priya Nair  
**Role:** Operations Manager, 200-person staffing agency  
**Location:** Bengaluru, Karnataka  
**Business size:** 200 employees  
**Plan:** Business (₹12,999/month)

### Background
Priya manages client communications and candidate coordination for a staffing agency. She's not the buyer (the MD bought the platform) but is the primary user. She needs to build and maintain workflows without waiting for IT.

### Goals
- Build and modify workflows independently
- Set up automated candidate status updates
- Create reminder sequences for client meetings
- Generate weekly workflow performance reports

### Frustrations
- "IT is always busy — I need to do this myself"
- "Existing tools have too many options, I get lost"
- "When I change a template, I don't want to break the whole workflow"

### Technology Comfort
- Power user of Excel, Google Sheets
- Has used Zapier briefly
- Understands the concept of automation but not technical details
- Very comfortable with mobile apps

### Decision Criteria for Success
- Can build a new workflow in <15 minutes without help
- Workflow performance dashboard is self-explanatory
- Can fix/update a template without involving IT

---

## Persona 4: The Agency Reseller (Partner Persona)

**Name:** Kiran Patel  
**Role:** Founder, Digital Marketing Agency  
**Location:** Ahmedabad, Gujarat  
**Business size:** 12 employees, 40 SMB clients

### Background
Kiran's agency manages WhatsApp marketing for 40 SMB clients. He's looking for a white-label or reseller platform he can offer to clients. Currently using a mix of WATI, AiSensy, and manual work.

### Goals
- Offer a unified automation platform to all clients
- White-label or reseller pricing to maintain margins
- Manage multiple client accounts from a single dashboard
- Earn recurring revenue from the platform

### Decision Criteria
- Multi-tenant management dashboard
- Reseller/white-label pricing model
- Enough features to justify ₹5,000–₹15,000/month client billing

---

## Persona 5: The SMB Owner (Digital Traditionalist) ⚡

**Name:** Ramesh Gupta  
**Role:** Owner, furniture retail store chain (3 locations)  
**Location:** Delhi NCR  
**Business size:** 25 employees  
**Plan:** Starter → Growth

### Background
Ramesh runs a traditional retail business that is increasingly online. He personally manages a WhatsApp group with 500+ customers and sends promotions manually. His son set up a basic Shopify store. He hears about WhatsApp automation from a business group and is curious.

### Goals
- Stop manually sending promotions
- Get reminders sent to customers about EMI payments
- Know which customers haven't purchased in 3 months

### Frustrations
- "I don't understand software. If it's complicated, I won't use it."
- "My last software cost ₹40,000 and I never used it"

### Technology Comfort
- WhatsApp only. Basic smartphone user.
- Needs guided onboarding (video or in-person)
- High churn risk if not activated within first week

---

## Persona Summary Table

| Persona | Role | Pain Intensity | Tech Comfort | Revenue Potential | Acquisition Channel |
|---|---|---|---|---|---|
| Dr. Sunita (Clinic Owner) | Buyer + Admin | Very High | Low | Medium | SEO, healthcare communities |
| Rohan (E-Commerce) | Buyer + Power User | High | Medium-High | High | Google, Shopify app store |
| Priya (Ops Manager) | Primary User | High | Medium | Medium (via employer) | Partner, enterprise sales |
| Kiran (Agency) | Partner/Reseller | Medium | High | Very High (×40 clients) | Direct outreach, events |
| Ramesh (Traditional) | Buyer | Medium | Very Low | Low-Medium | Word of mouth, field sales |

---

## Cross-References
- `02-Business/Customer-Segments.md` — Segment definitions
- `02-Business/Customer-Journeys.md` — Journey maps per persona
- `03-Product/User-Stories.md` — User stories derived from persona needs
- `02-Business/Go-To-Market.md` — Channel strategy per persona

## Maintenance Guidance
Validate personas with 5+ customer interviews per persona. Update based on actual customer data quarterly. Add persona for healthcare regulatory decision maker (CMO/Medical Director) as healthcare vertical scales.
