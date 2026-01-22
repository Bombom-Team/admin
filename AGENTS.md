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

- Execute **0️⃣ Guard Stage** before doing anything else
- Follow **all steps and stop rules** defined in `docs/WORKFLOW_CONVENTION.md`
- Stop immediately if required input is missing
- Never skip steps unless Fast Track is **explicitly declared by user**
- Never explore files, open code, or analyze logic before Dry-run approval

⚠️ Detailed workflow steps, Fast Track rules, and stop conditions  
are defined in `docs/WORKFLOW_CONVENTION.md` and MUST be followed verbatim.

---

## Code Access Rules

- During Dry-run stages:
    - Directory listing ❌
    - File open ❌
    - Code analysis ❌
- Code access is allowed **only after explicit user approval**

---

## Git & PR Rules

- Commit and PR rules MUST follow `docs/COMMIT_CONVENTION.md`
- PR template `.github/pull_request_template.md` is mandatory
- Never auto-commit or auto-push without user approval
- Never bypass PR checklist gates

---

## Failure Rule

- If rules conflict or required information is missing:
    - Stop immediately
    - Output a stop message
    - Do NOT guess
    - Do NOT continue
