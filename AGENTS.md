# AGENTS.md

## Scope

This AGENTS.md applies to the **backend workspace only**.

---

## Global Rules (Absolute)

- Follow `docs/DEVELOPMENT_CONVENTION.md` strictly
- Follow `docs/TEST_CONVENTION.md` strictly
- Follow `docs/WORKFLOW_CONVENTION.md` when WORKFLOW is active
- Never infer domain rules or business policies
- Never modify DB schema or domain entities without explicit user declaration
- Do NOT access or modify protected branches:
  - server
  - main
  - release/*

---

## Workflow Trigger Rule

- `[WORKFLOW]` prefix is the **only valid trigger** for workflow automation
- If `[WORKFLOW]` is NOT present:
  - Treat the request as a normal question
  - Do NOT apply workflow rules
  - Do NOT enforce guards or steps

---

## Workflow Enforcement (Backend)

When `[WORKFLOW]` prefix is present, you MUST:

- Execute **0️⃣ Guard Stage** automatically
- Enter **1️⃣ Dry-run Stage automatically** (no user approval required)
- Follow **all steps and stop rules** defined in `docs/WORKFLOW_CONVENTION.md`
- Never skip steps unless **Fast Track is explicitly declared by the user**
- Never guess or infer missing domain / policy decisions

⚠️ Detailed workflow steps, Fast Track rules, and stop conditions  
are defined in `docs/WORKFLOW_CONVENTION.md` and MUST be followed verbatim.

---

## Code Access Rules

### 0️⃣ Guard Stage (Limited Access Allowed)

Allowed:
- Open and read **rule documents only**:
  - docs/WORKFLOW_CONVENTION.md
  - docs/DEVELOPMENT_CONVENTION.md
  - docs/TEST_CONVENTION.md
  - docs/COMMIT_CONVENTION.md

Not allowed:
- Open application source code
- Analyze business logic
- Inspect implementation details
- Modify any file

---

### 1️⃣ Dry-run Stage (No Code Access)

Allowed:
- Text-based reasoning
- Planning
- Change scope declaration
- Recommendation output

Not allowed:
- Directory listing
- File open
- Code analysis
- Code generation or modification

Dry-run stage is entered **automatically** when `[WORKFLOW]` is present.  
No user approval is required for this stage.

---

### 7️⃣ Implementation Stage (Explicit Approval Required)

Allowed only **after user approval** to proceed beyond Dry-run:

- Directory listing
- File open
- Code analysis
- Code generation and modification
- Test creation

Without explicit approval:
- Any implementation action ❌
- Any file access ❌

---

## Git & PR Rules

- Commit and PR rules MUST follow `docs/COMMIT_CONVENTION.md`
- PR template `.github/pull_request_template.md` is mandatory
- Never auto-commit or auto-push without user approval
- Never bypass PR checklist gates
- Protected branches are always forbidden targets

---

## Failure / Stop Rule (Absolute)

If any of the following occurs, you MUST stop immediately:

- Required user input is missing
- Rules conflict or cannot be satisfied
- Domain or business logic must be inferred
- Scope exceeds declared change range

On stop:
- Output a stop message
- State the exact blocking reason
- Do NOT guess
- Do NOT continue automatically

Stopping is **correct behavior**, not a failure.
