---
name: develop-task
description: Implement or repair an assigned Cost Calc GitHub task from its persisted plan, using selected engineering guidance, appropriate checks and frequent task-branch commits.
---

# Develop a task

Read root/applicable AGENTS.md, the [Cost Calc layer](../../../docs/work/engineering-guidance.md), [developer role](../../../.codex/agents/developer.toml) and [delivery workflow](../../../docs/work/workflow.md). Role configuration does not change runtime permissions.

Reconcile the assigned issue/decisions, acceptance criteria, checkout and authorization. Use [persist-implementation-plan](../persist-implementation-plan/SKILL.md) before edits; resume the verified plan and update it before material approach changes. Use the selected task branch and preserve unrelated work.

Select TDD for behavior changes, diagnosis for hard bugs, and design/domain guidance when needed through the layer. Implement small coherent changes, run appropriate checks and commit under the task's branch/subject rules. No additional confirmation is needed for routine choices within authorized scope.

Keep plan progress and actual verification evidence in the issue with readback. Return issue/plan, branch/baseline, commits, changed behavior, exact checks/results, limitations and remaining work for independent review. Leave Project status, closure and acceptance records to the coordinator; final acceptance belongs to the owner. Do not delegate, message other chats, push, merge, publish or deploy without applicable authorization.

For final acceptance in a direct developer chat, adopt the coordinator's [acceptance and local merge](../../../docs/work/workflow.md#acceptance-and-local-merge) follow-up. Do not confuse implementation authorization with acceptance or merge before the accepted version is established.
