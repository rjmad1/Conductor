# ADR 002: Use WhatsApp Cloud API

**Status:** Accepted
**Date:** 2026-06-28

## Context
Conductor targets SMBs, focusing first on WhatsApp as the primary communication channel. Previous documents suggested using unofficial libraries (like OpenWA) to bypass API costs or restrictions.

## Decision
We will use the official **WhatsApp Cloud API** for all outbound messaging and webhook integrations in production.

## Consequences
### Positive
- Zero risk of account bans due to Terms of Service violations.
- Official support, high reliability, and access to new interactive message templates.

### Negative
- Requires business verification and incurs per-conversation costs.
- Onboarding process is more complex for SMB tenants.

### Neutral
- Requires strict adherence to WhatsApp's Opt-in and commerce policies.

## Alternatives Considered
- **OpenWA / Baileys:** Rejected because the legal and operational risk of platform bans is unacceptable for an enterprise B2B SaaS product.

## Rationale
Building a SaaS product on an unofficial, reverse-engineered API introduces a fatal business risk. To offer reliable enterprise-grade communication, we must integrate with official Meta channels, accepting the cost as a necessary operating expense.

## Related Decisions
None.
