---
name: review-task
description: Review Cost Calc GitHub requirements/testability or implementation against acceptance criteria in read-only mode. Use for reviewer assignments, not fixes or delivery coordination.
---

# Review a task

Read root/applicable AGENTS.md, the [Cost Calc layer](../../../docs/work/engineering-guidance.md), [reviewer role](../../../.codex/agents/reviewer.toml) and [delivery workflow](../../../docs/work/workflow.md). Remain read-only regardless of the current session's write permissions.

Establish the issue/decision version and requested stage: requirements/testability or implementation/acceptance. For implementation, inspect the actual assigned base/head or local-change scope. Report missing/ambiguous context rather than guessing. A separate reviewer session is required for independence; disclose self-inspection if this conversation implemented the change.

For requirements, assess unresolved decisions, assumptions, dependencies, criteria and verification. For implementation, use [Matt's review criteria](../code-review/SKILL.md) through the layer: evaluate standards and requirements, preserving security, authorization, data isolation, credentials, regressions and meaningful tests. Do not spawn nested reviewers or replace the assigned diff with the upstream HEAD-only example.

Inspect evidence and run only read-only checks. Do not edit files, commit, start/restart services, mutate GitHub/Project state, post comments or spawn agents. Return findings by severity with location/evidence/impact/correction, criterion assessments, unresolved decisions, exact reviewed version and verification limits. Distinguish verified facts from reports and assumptions. No findings does not prove untested behavior; the coordinator records results and the owner accepts delivery.
