# Credential Management

Securely stores API keys, webhook secrets, and OAuth metadata.

## Security Controls
- **AES-256-GCM Encryption:** Column values are encrypted using the dynamic environment key `INTEGRATION_ENCRYPTION_KEY`.
- **Validation Probes:** Validates presence and check connection validity.
- **Auditing:** Rotations or reads trigger standard log audits.
