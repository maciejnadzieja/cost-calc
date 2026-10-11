---
name: develop-task
description: Implement or repair an assigned Cost Calc GitHub task using its persisted plan, task branch, appropriate checks, and frequent coherent commits. Use for developer assignments or authorized task implementation; not for request refinement or independent read-only review.
---

# Develop a task

Apply the developer role to the assigned implementation. Read root/applicable AGENTS.md, [developer.toml](../../../.codex/agents/developer.toml), and the [delivery workflow](../../../docs/work/workflow.md). Follow the role's developer_instructions as workflow guidance; its configuration fields do not change this conversation's runtime permissions.

## Prepare or resume

Read the assigned issue, decisions, acceptance criteria and coordinator handoff. Inspect relevant code, branch, working tree and baseline. Confirm the requested scope is authorized, preserving existing authorization rather than asking again. Resolve a missing task, blocking decision or conflicting ownership before dependent edits; continue independent investigation when useful.

Use [persist-implementation-plan](../persist-implementation-plan/SKILL.md) to save the implementation plan and verify it by reading back before application code changes. Resume an accurate existing plan and update it before materially changing the approach. Use the issue's `codex/<issue-number>-<short-description>` branch; do not switch a shared checkout while another developer is active.

## Implement and hand off

Implement within the assigned scope and ownership, preserving other agents' edits. Follow the existing developer role for architecture, meaningful tests, environment limits, credential handling, and frequent coherent task-branch commits. Inspect the branch/staged diff and run appropriate checks before committing. Do not push, merge, publish, deploy or spawn agents without the required owner authorization.

Update the issue's plan and verification evidence under the assigned authorization and read mutations back. Return issue/plan links, branch/baseline, commits, changed behavior, exact checks/results, limitations and remaining work to the coordinator or owner. Leave Project status, acceptance records and closure to the coordinator; final acceptance belongs to the owner. Do not claim independent review of your own implementation.

An invocation such as `$develop-task Implement issue #N` is an implementation assignment within the stated scope. A request for a plan only remains planning. Do not extend either request into unrelated implementation or release actions.
