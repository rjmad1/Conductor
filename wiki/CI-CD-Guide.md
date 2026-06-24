# CI/CD Guide

## A. Purpose
This page documents the GitHub Actions workflows, test compliance checks, and deployment automation pipelines that govern the Conductor repository.

---

## B. Continuous Integration (CI) Workflows

The repository configures two primary validation pipelines under `.github/workflows`:

### 1. PR Checks (`pr-checks.yml`)
Runs automatically on every pull request targeting the `main` branch.

- **Environment**: Ubuntu runner configured with **JDK 17** (Temurin distribution, Gradle caching enabled).
- **Execution Steps**:
  1. **Build and Compile**: Compiles all production and test Java classes:
     ```bash
     ./gradlew compileJava compileTestJava --no-daemon
     ```
  2. **Run Tests**: Runs the unit and integration test pyramid suite:
     ```bash
     ./gradlew test --no-daemon
     ```
  3. **Documentation Check**: Executes the custom script [docs-check.sh](file:///c:/Users/rajaj/Projects/Conductor/scripts/docs-check.sh) to ensure no broken relative/absolute workspace file links exist and verifies that required compliance artifacts (`THREAT_MODEL.md`, `TRUST_BOUNDARIES.md`, `ARCHITECTURE_GUARDRAILS.md`) are present.

### 2. CI Push Check (`ci.yml`)
Triggered automatically on commits pushed to `main`.
- Performs compile tasks and runs the full test suite to guarantee that merged code remains stable.

---

## C. Continuous Delivery (CD) Pipeline (`cd.yml`)
The CD pipeline is configured under `.github/workflows/cd.yml` to trigger when a GitHub release is **published**.

### Current Implementation State (Gap):
- The deployment step is currently a **stub**:
  ```yaml
  steps:
    - uses: actions/checkout@v3
    - name: Run Deploy
      run: echo "Deploy stub"
  ```
- **Remediation**: The DevOps roadmap prioritizes updating this workflow to build Docker images, push them to AWS ECR, and execute a serverless ECS Fargate service update task.

---

## D. Related Pages
- [Repository Structure](Repository-Structure)
- [Coding Standards](Coding-Standards)
