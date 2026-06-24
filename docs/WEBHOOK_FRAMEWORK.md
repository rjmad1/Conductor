# Webhook Ingress Framework

Ingests vendor webhook streams securely.

## Pipeline Lifecycle
1. **Header Resolution:** Ingress parses target tenant boundaries from request parameters.
2. **Signature Verification:** Cryptographic matches verify payloads (Shopify Base64, Razorpay Hex).
3. **Replay Protection:** Deduplication blocks duplicate deliveries.
4. **JetStream Dispatch:** Translates bodies into Conductor event envelopes and dispatches to NATS.
