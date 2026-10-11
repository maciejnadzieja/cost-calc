---
name: coordinate-delivery
description: Coordinate Cost Calc GitHub delivery across request refinement, task preparation, authorized developer/reviewer handoffs, and owner acceptance. Use when asked to act as software factory coordinator or coordinate/resume delivery; not for an isolated developer or read-only reviewer assignment.
---

# Coordinate delivery

Use this skill to make the current conversation the coordinator for the requested work. It does not spawn an agent or change runtime permissions by itself.

Read the repository's root and applicable scoped AGENTS.md. Load the coordinator behavior from [coordinator.toml](../../../.codex/agents/coordinator.toml) and the [role guide](../../../docs/work/coordinator.md); apply the role's developer_instructions as workflow guidance, without treating its configuration fields as changes to this chat's runtime. Read [the delivery workflow](../../../docs/work/workflow.md) for readiness, tracking and CLI access.

## Establish the assignment

Read the selected issue, its decisions and evidence, the Cost Calc delivery Project item, and the current branch/diff. Reconcile existing work and authorization before choosing the next action. If no issue or bounded outcome is specified, establish that target without selecting product priorities or starting unrelated implementation. Keep coordination active for follow-up messages concerning this delivery; incorporate owner corrections and summarize durable context in the issue for the next session.

## Carry the work forward

- Refine the outcome, scope, acceptance criteria, dependencies and appropriate checks when needed. Distinguish proposals from confirmed decisions and actual verification.
- Before authorized implementation, ensure the task branch and a plan persisted/read back using [persist-implementation-plan](../persist-implementation-plan/SKILL.md). Implementation authorization does not require another routine approval gate.
- Delegate only with explicit owner authorization for agent work on this task. Give developer the issue/plan, baseline, ownership and checks; give reviewer the requested stage and exact comparison scope. Use the existing role guide's handoff contracts. Invoking this skill alone does not authorize delegation, messaging other chats, implementation, pushing, merging or deploying.
- Check returned diffs and evidence, coordinate authorized repairs, and maintain GitHub decisions/status with readback. Do not report self-inspection as independent review or a prior test run as proof of later changed behavior.
- Present the delivery outcome and limits for owner acceptance. Close only under the role guide's evidence/acceptance rules; verify issue and Project state. Retain the distinction between local commits, merged/pushed work and release.

Keep detailed role rules in the linked role/guide rather than duplicating them here. For a new chat, the owner can explicitly invoke `$coordinate-delivery` with the issue and desired stage. A request to refine remains refinement; a request to coordinate implementation must include or carry existing implementation authorization.
