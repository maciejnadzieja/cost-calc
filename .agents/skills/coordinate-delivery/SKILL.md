---
name: coordinate-delivery
description: Coordinate Cost Calc GitHub delivery across request refinement, task preparation, authorized developer/reviewer handoffs, and owner acceptance. Use for coordination or resumption, not isolated implementation or read-only review.
---

# Coordinate delivery

Read root/applicable AGENTS.md, the [Cost Calc layer](../../../docs/work/engineering-guidance.md), [delivery workflow](../../../docs/work/workflow.md), [coordinator role](../../../.codex/agents/coordinator.toml) and [handoff guide](../../../docs/work/coordinator.md). Apply the role's instructions without treating configuration as runtime permission changes.

Reconcile the issue/decisions, Project item, branch/diff and existing authorization. Refine only missing scope, criteria, dependencies and checks. Before authorized implementation, ensure [the plan](../persist-implementation-plan/SKILL.md) is persisted and read back.

Dispatch required independent reviewer inspection under the standing authorization: at assessment start, or after implementation before owner acceptance, unless the owner explicitly opts out. Use [review-task](../review-task/SKILL.md) with stage, criteria and exact issue/diff version; wait, record findings, coordinate repairs and focused rereview, then reconcile the final version. Unavailable review stays pending.

Delegate developers only when authorized, with [develop-task](../develop-task/SKILL.md) and the guide's ownership/handoff contract. Maintain GitHub evidence and Project status with readback. Before acceptance handoff, verify dispatch/result/finding disposition/final version or recorded owner opt-out. Final acceptance belongs to the owner. Keep this coordination assignment active for related follow-ups; a refinement request does not authorize implementation or release.

On final acceptance, follow [acceptance and local merge](../../../docs/work/workflow.md#acceptance-and-local-merge) without asking for another merge confirmation. Merge the exact accepted version into local main, honor explicit holds and record actual integration before completion under that procedure. Scope approval is not final acceptance; pushing and service operations remain separate.
