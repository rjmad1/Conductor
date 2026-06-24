# AI-EOS Manifest Specification

## Document Metadata
* **id:** EOS-22-MANIFEST-SPEC
* **title:** AI-EOS Manifest Specification
* **description:** Documents the structural schema, required fields, validation schemas, and configurations of the root manifest.
* **owner:** Chief Architect & Platform Engineering Lead
* **domain:** Enterprise Governance
* **tags:** [manifest, yaml, schema, configuration, system-of-record]
* **version:** 1.0.0
* **status:** Approved
* **created:** 2026-06-24T16:12:00Z
* **updated:** 2026-06-24T16:12:00Z
* **related_artifacts:** [01-constitution.md, 04-architecture-meta-model.md]
* **source_of_truth:** Git Repository
* **authority_level:** L2 - Governance
* **risk_tier:** Tier 4 — Critical
* **compliance_tags:** [ISO-27001-A.5, SOC2-CC8.1]
* **quality_score:** 1.00

---

## Purpose
This document defines the structural schema for `/eos-manifest.yaml`, the root system-interpretable manifest of the Conductor AI-EOS. It details the required sections, formats, and validation rules that CI/CD systems must enforce when parsing the manifest.

---

## YAML Root Structure Specification

The `/eos-manifest.yaml` must contain exactly the following root blocks:

### 1. `metadata`
Standard organizational details.
* **Fields:** `org_name`, `eos_version`, `profile`, `maturity_level`, `security_classification`, `regulatory_scope`.

### 2. `governance`
The authoritative registry of owners, approval matrices, and escalation rules.
* **Fields:** `ownership_mappings` (list of roles and email endpoints), `approval_matrix` (mapping of Risk Tiers to human approval requirements).

### 3. `risk_model`
Configuration of the five risk tiers.
* **Fields:** `tiers` (definitions of Tiers 0-4, associated code paths, agent permissions, and testing rules).

### 4. `compliance_mappings`
Maps compliance control IDs to international standards.
* **Fields:** `controls` (list of Control IDs, descriptions, standards like GDPR/DPDP, and evidence targets).

### 5. `architecture`
Lists active system domains, services, capabilities, and data assets.
* **Fields:** `domains` (registered business domains), `services` (registered microservices), `data_assets` (registered tables and storage buckets).

### 6. `agent_registry`
The central database of authorized agent identities, trust levels, and tool allocations.
* **Fields:** `agents` (agent definitions, trust levels, and approved schemas/tool signatures).

### 7. `traceability_rules`
Defines linking validation constraints.
* **Fields:** `chain` (ordered components list required for pull request check passes).

---

## Lifecycle Policy
* **Review Cycle:** Quarterly.
* **Revision Process:** Modifications must be approved by the Chief Architect.

## Validation Rules
* The manifest `/eos-manifest.yaml` must validate successfully against the official JSON Schema definition packaged in the repository.

## Audit Requirements
* Automated checks verify that the list of services and agents registered in `/eos-manifest.yaml` matches the list of running pods in the Kubernetes cluster dashboard.
