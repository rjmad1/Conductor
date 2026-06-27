# SKILL — Compliance

## Skill ID
SKILL-303

## Description
This skill governs the systematic assessment of the repository compliance posture against security, licensing, data protection, and engineering policies.

## Applicable Loops
LOOP-303

## Inputs
- Compliance requirements register (`docs/standards/Compliance.md`)
- License allowlist (`docs/standards/License-Allowlist.md`)
- Security validation report
- Verification report
- Dependency manifest files

## Outputs
- `docs/governance/compliance/compliance-status-report-{run-id}.md`
- `docs/governance/compliance/non-compliant-findings-{run-id}.md`

## Preconditions
- Requirements registers and reports are readable.
- Valid license allowlist exists.

## Steps
1. Parse the compliance register and dependency list.
2. Cross-reference dependency licenses against the allowlist.
3. Incorporate security findings.
4. Draft the compliance status report and submit it for Checker verification.
5. Record Gate-1 human approval and publish the final report.

## Postconditions
- Compliance report and findings are signed and published.

## Error Handling
- Pause and escalate if a high-severity licensing or security breach is identified without mitigation.

## Version History
- 1.0 Initial setup.
