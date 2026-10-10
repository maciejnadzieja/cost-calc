---
name: persist-implementation-plan
description: Create and persist a task implementation plan in its GitHub issue before changing application code in Cost Calc. Use when starting implementation, resuming a task, or revising its implementation approach.
---

# Persist an implementation plan

Before implementation, save a concrete plan in the selected GitHub issue and verify it can be read back. A chat response or transient planning tool alone does not satisfy this requirement. GitHub Issues remain authoritative; do not create duplicate Markdown task trackers or plan files in the repository.

## Establish the task

Read root and applicable AGENTS.md instructions, docs/work/workflow.md, the current issue and its decision history. Inspect relevant code and tests without modifying application code. Verify the task branch and working tree. Use the existing issue ID, confirmed scope, acceptance criteria, and owner authorization; an In progress status or a saved plan does not authorize additional scope.

For resumed work, read the persisted plan and compare it with the current issue, branch, commits, and working tree. Reuse it when still accurate; do not restart or overwrite completed work.

## Draft the plan

Scale detail to the task: a small change may need only a few steps. Include:

- Task ID, date in Europe/Warsaw, branch, and inspected baseline commit.
- Desired outcome, confirmed scope, and explicit exclusions.
- Relevant code and contracts; distinguish observed behavior from proposed changes.
- Ordered implementation steps, their expected outcomes, and dependencies. Map steps to acceptance criteria; group work by behavior rather than by file.
- Verification for each outcome: appropriate automated checks, important negative and edge cases, and any manual integration checks. Distinguish planned checks from results already obtained.
- Small, coherent commit boundaries using `#<task-id> | <description of changes>`.
- Required credentials or environment setup, unresolved decisions, and which prerequisites block development versus final integration acceptance. Never include secrets or real user data.

Resolve blocking product decisions with the owner. Continue independent investigation while waiting, but do not implement dependent behavior. Choose routine technical details within agreed scope without inventing another approval gate.

## Persist and verify

Fetch the latest issue immediately before updating it. Add or revise a dedicated `## Implementation plan` section in the issue body, preserving the original request, decisions, acceptance criteria, and unrelated sections. Keep a dated revision summary inside the plan section when changing an existing plan; preserve prior decisions and reasons for meaningful changes.

Use the available GitHub connector or authenticated CLI under the environment's permissions. Read the issue back and verify the intended plan and preserved content. Return the issue link and summarize any blockers. If persistence or readback fails, report the limitation and do not start implementation; do not substitute a local backlog file.

## Execute and maintain

Once the plan is persisted and verified, proceed when implementation is already authorized and no blocking decision remains. Saving a plan does not require renewed owner approval by itself and does not authorize pushing, merging, or deploying.

Before materially changing the approach, update and verify the persisted plan with the reason. During implementation, keep completed steps and verification evidence current in the issue, including commit IDs and limitations. Never mark a planned test as passed or infer owner acceptance. On handoff, link the plan and report completed work, remaining work, and deviations.
