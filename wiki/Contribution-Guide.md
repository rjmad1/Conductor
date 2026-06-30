# Contribution Guide

## A. Purpose
This contribution guide defines the workflow for adding features, fixing issues, and modifying code in the Conductor repository.

---

## B. Core Contribution Steps

Developers must complete this sequence before submitting changes to the `main` branch:

1. **Create Feature Branch**:
   Check out a new branch off `main`:
   ```bash
   git checkout -b feature/issue-title
   ```
2. **Implement & Validate Code**:
   Modify code files. Ensure that new APIs have matching tests and database changes have corresponding Flyway migration scripts.
3. **Execute Local Build & Test Checks**:
   Verify compilation and test coverage:
   ```bash
   ./gradlew clean compileJava test --no-daemon
   ```
4. **Validate Documentation & Links**:
   Run the PR documentation validator:
   ```bash
   ./scripts/docs-check.sh
   ```
5. **Commit Code changes**:
   Commit changes using Conventional Commits guidelines (e.g. `feat: add clickhouse ingestion batch metrics`).

---

## C. Automation Hook (Git Post-Commit)
The local repository sandboxes include a git hook helper to automate workflows:

- **Hook Behavior**: A git hook configured in `.git/hooks/post-commit` triggers automatically whenever you create a local commit.
- **Action**: It runs `git push` to push the active commit to the GitHub remote `rjmad1/Conductor` on the `main` branch.
- **Dev Sync**: Alternatively, developers can use the sync utilities:
  - PowerShell: `./sync.ps1`
  - Bash: `./sync.sh`

---

## D. Related Pages
- [CI/CD Guide](CI-CD-Guide)
- [Coding Standards](Coding-Standards)
