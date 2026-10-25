---
name: persist-implementation-plan
description: Save and read back a concrete implementation plan in a Cost Calc GitHub issue before implementation, on resumption or before materially changing the approach.
---

# Persist an implementation plan

Read root/applicable AGENTS.md, the [Cost Calc layer](../../../docs/work/engineering-guidance.md) and [persisted-plan procedure](../../../docs/work/workflow.md#persisted-implementation-plans). That procedure is the source for plan contents, preservation, readback and revision rules.

Inspect the selected issue/decisions, code/tests, task branch and baseline without changing application code. Reuse an accurate existing plan on resumption. Save or revise the issue's Implementation plan section and verify the complete intended update by reading it back, preserving original requests, unrelated sections and decision history.

If persistence/readback fails or a blocking decision remains, report it and continue independent investigation only; no dependent implementation or substitute local plan. Return the issue link and blockers. A saved plan does not replace [owner acceptance of refinement](../../../docs/work/workflow.md#make-tasks-ready). Once current refinement is explicitly accepted, implementation authorized and no blockers remain, proceed without renewed routine approval. Keep progress, commit IDs and actual verification evidence current in GitHub.
