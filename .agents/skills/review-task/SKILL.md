---
name: review-task
description: Review Cost Calc GitHub requirements/testability or implementation against acceptance criteria in read-only mode. Use for reviewer assignments or requested task review; not for implementing fixes or coordinating delivery.
---

# Review a task

Read root/applicable AGENTS.md, [reviewer.toml](../../../.codex/agents/reviewer.toml), and the [delivery workflow](../../../docs/work/workflow.md). Follow the role's developer_instructions; a skill does not enforce a runtime sandbox, so remain read-only even if the current session permits writes.

## Establish the review

Read the issue and decision/evidence history. Establish the requested stage: requirements/testability or implementation/acceptance. For implementation, identify the supplied base/head or local-change scope and inspect the actual diff. If the task, stage or comparison is ambiguous and cannot be established from the assignment, report the missing context instead of guessing.

Use a separate reviewer session for independent review. If this conversation implemented the reviewed changes, disclose that limitation and offer self-inspection only; do not spawn another agent or label the result independent without authorization.

## Inspect and report

For requirements, identify blocking decisions, assumptions, dependencies and unclear criteria; propose observable acceptance checks without treating them as owner decisions. For implementation, trace the changed behavior against each criterion and the meaningful negative/error cases in the existing reviewer role. Prioritize concrete correctness, authorization, data isolation, credentials and regression issues over style.

Inspect available tests and recorded results, distinguishing verified facts, owner reports, historical results and assumptions. Run only checks that remain read-only. Do not run suites that write build/report artifacts or create fixtures, and do not represent unexecuted checks as passed. Do not edit files, commit, mutate GitHub/Project state, post comments, restart services, or spawn agents.

Return actionable findings by severity with file/line or issue section, evidence, impact and a suggested correction/check. Assess each acceptance criterion as met, partial, unmet or unverified, citing the available evidence and its limits. Finish with unresolved decisions, unexecuted checks and an overall assessment. If no actionable findings exist, say so without asserting untested behavior is proven. Return the report to the coordinator or owner; the coordinator records it and the owner decides final acceptance.
