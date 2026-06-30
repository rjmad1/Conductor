# Loop Engineering — Agent Operating Instructions
> For: Google Antigravity Intelligent IDE  
> Protocol version: 1.0  
> Last updated: 2026-06-24

This document governs how this IDE's AI agents plan, execute, evaluate, and learn across all workflows. Every agent session must conform to these directives. Deviation requires explicit human approval and must be logged.

---

## 1. Pre-Execution: Define Before You Generate

**Before producing any output, the agent must establish:**

- **Success criteria** — Measurable, specific, and agreed upon with the human owner. No vague targets (e.g., "improve performance" → "reduce p99 latency below 200ms on the checkout endpoint").
- **Stop conditions** — Explicit conditions that terminate the loop, whether by success, failure threshold, time budget, or escalation trigger. Document them in the task header.
- **Evaluation rubric** — The rubric is designed *before* agents are designed. Agents may not modify evaluation criteria once execution begins.

```
## Task Header (required for every agent session)
- Goal: [one-line measurable outcome]
- Success condition: [specific, verifiable]
- Stop conditions: [list; at least one failure threshold]
- Rubric: [link or inline criteria — locked at start]
- Human owner: [name or role]
```

---

## 2. Execution Architecture: Three Separate Stages

Agents must never collapse generation, evaluation, and decision-making into a single step.

| Stage | Agent Role | Human Checkpoint? |
|---|---|---|
| **Generate** | Produce candidate outputs | No |
| **Evaluate** | Score against rubric; collect evidence | Required if score is borderline |
| **Decide** | Promote, reject, revise, or escalate | Required for promotion to memory |

- Outputs that cannot be audited (no provenance, no intermediate state) are automatically rejected.
- Outputs that cannot be reproduced from the same inputs are flagged for review before promotion.

---

## 3. Memory: First-Class System Component

Memory is not a side effect — it is an engineered artifact.

### 3.1 Decision Recording
Every stored decision must include:
- **What** was decided
- **Why** (rationale, not just outcome)
- **Evidence** that justified the decision
- **Timestamp** and **author** (agent or human)

Recording outcomes without rationale is prohibited.

### 3.2 Memory Tiers
| Tier | Purpose | Persistence |
|---|---|---|
| **Working memory** | Current task context only | Ephemeral — cleared after task |
| **Organizational memory** | Validated patterns, decisions, standards | Persistent — requires evidence gate |
| **Experiment store** | Hypothesis testing, trials | Isolated — never bleeds into production memory |

Agents minimize context to only what the current decision requires. Retrieval is ranked by **relevance**, not completeness.

### 3.3 Memory Hygiene
- Prune stale, duplicated, and low-quality artifacts continuously.
- Failures are stored with the same structure and rigor as successes.
- Unverified assumptions must not be reinforced by feedback loops.

---

## 4. Versioning

Version independently:
- **Prompts** — every prompt template has a version identifier
- **Workflows** — workflow definitions are versioned and diffable
- **Tools** — tool configurations and schemas are versioned
- **Memory** — organizational memory entries carry version + deprecation state

A change to one does not automatically update the others.

---

## 5. Evaluation & Quality

### 5.1 Metrics
- Measure loop quality by **outcome metrics** (decision accuracy, error rate, learning delta).
- Do not use **activity metrics** (tokens generated, iterations run, files touched) as proxies for quality.

### 5.2 Observability
Every workflow must be instrumented with:
- At least one **leading indicator** (early signal of success/failure)
- At least one **lagging indicator** (confirmed outcome)
- A **feedback signal** that is readable by the next loop iteration

Workflows without observable feedback signals are not eligible for automation.

### 5.3 Evidence Gate
Outputs are promoted to organizational memory only after:
1. Passing the evaluation rubric
2. Providing reproducible evidence
3. Receiving explicit human approval

---

## 6. Automation & Scaling

**The scaling sequence is non-negotiable:**

```
Measure → Improve → Learn → Scale
```

Never:
```
Scale → Measure → Learn  ✗
```

- Manual loops must succeed **consistently** before any automation is applied.
- A loop is eligible for automation only when its feedback signals are stable and its failure modes are documented.
- Patterns become standards only after **repeated successful validation** — not after a single run.
- Scale loops that improve **decision quality**, not those that merely increase throughput.

---

## 7. Structured Outputs

Wherever a downstream action depends on AI-generated output, the output must be structured (JSON, YAML, or a defined schema). Free-text output is not acceptable as input to automated pipelines.

---

## 8. Uncertainty & Ownership

- **Escalate uncertainty** — agents do not force autonomous decisions when confidence is below threshold. They surface the decision to the human owner with context and options.
- **Ownership boundaries** — each task declares which decisions are agent-owned and which are human-owned. Agents do not cross those boundaries without logging and approval.
- **Experimentation is isolated** — experiment environments have no write access to production memory stores.

---

## 9. Continuous Learning

- Evaluate systems on **long-term learning efficiency**, not single-run performance.
- Every workflow is designed to produce **reusable organizational knowledge** as a by-product.
- Maintain three separate loops with distinct metrics:

| Loop | Focus | Key Metric |
|---|---|---|
| **Quality loop** | Output correctness | Error rate, rubric score |
| **Operational loop** | Reliability & throughput | Uptime, latency, cost |
| **Learning loop** | Knowledge accumulation | Memory utility, reuse rate |

- Optimize for **learning velocity** before scaling automation.

---

## 10. Quick-Reference Checklist

Use this before every agent session:

- [ ] Success criteria are written and measurable
- [ ] Stop conditions are defined (including at least one failure threshold)
- [ ] Evaluation rubric is locked before execution begins
- [ ] Memory tier for this task is declared (working / organizational / experiment)
- [ ] Output format is structured if downstream actions depend on it
- [ ] Observability signals are instrumented
- [ ] Human owner and escalation path are identified
- [ ] This task has passed manual loops before any automation is applied (if automated)

---

## 11. Prohibited Behaviors

Agents operating in this IDE must never:

1. Modify evaluation criteria during an active execution loop
2. Promote outputs to organizational memory without evidence and human approval
3. Store decisions without rationale
4. Use activity metrics to justify loop continuation
5. Write to production memory from an experiment environment
6. Force an autonomous decision when uncertainty is above the declared threshold
7. Skip the Generate → Evaluate → Decide separation
8. Scale a loop before it has been measured and validated manually

---

## 12. AI-Assisted Development (EWOS Guidelines)

All AI sessions and agents in this workspace must adhere to the following context engineering, workflow, and verification rules:

### 12.1 Mandatory Workflow
1. **Spec & Plan First:** Start with `spec.md` and `plan.md`.
2. **Task Checklist:** Break work into a checklist at the component or file level.
3. **Narrow Context:** Keep prompts narrow, task-scoped, and feed only direct dependencies/target files.
4. **Validation Setup:** Define verification commands, repro steps, and criteria before code generation.
5. **Immediate Validation:** Test, lint, and diff code immediately. Review against architecture rules before merge.

### 12.2 Context Engineering & Execution Controls
- **Layered Context:** Maintain stable project conventions separate from volatile session goals and task-level details.
- **Just-in-Time Retrieval:** Do not preload or eagerly dump large files/logs. Fetch references only when needed.
- **Aggressive Compaction:** When resetting context, preserve changed files, decisions, remaining tasks, and validation status. Drop raw tool noise.
- **Planner vs Executor Modes:** Formulate design and identify files (Planner mode) before editing files and executing checks (Executor mode).
- **One Goal per Session:** Do not mix unrelated implementation targets in one thread. Avoid parallel updates on the same file cluster across threads.
- **Verification-First Prompting:** Structure prompts with objective, scope boundaries, relevant files, validation commands, and acceptance criteria.
- **Course-Correction:** Intervene and review early (after plan, after first edits, after verification) to prevent compounding drift.

### 12.3 Loop Engineering & Repository Sync
- **Check Remote Loops:** At the beginning of development or workspace initialization, always verify that the project's loops under `docs/loops` and `RajaJeevanLoopEngineering` are in sync with the remote repository at `https://github.com/rjmad1/RajaJeevanLoopEngineering`.
- **Port/Update Procedure:** If remote updates exist or files are missing, run the porting script (such as `port-loops.ps1`) to sync the latest loop definitions and implementation libraries to the local workspace.

---

*This document is the source of truth for agent behavior in this project. Amendments require a version bump and human sign-off.*

