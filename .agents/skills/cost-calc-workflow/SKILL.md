---
name: cost-calc-workflow
description: Apply Cost Calc's GitHub delivery rules with selected Matt Pocock engineering guidance when refining, implementing, debugging or reviewing repository work.
---

# Cost Calc workflow

Read root/applicable AGENTS.md and the [engineering layer](../../../docs/work/engineering-guidance.md), then the [delivery workflow](../../../docs/work/workflow.md). The layer resolves imported-skill conflicts even when a Matt skill is invoked directly. Read only relevant engineering guidance; do not launch the upstream orchestration workflow.

Keep the assigned role and authorization. Route to the matching entry point when needed:

- Coordination: [coordinate-delivery](../coordinate-delivery/SKILL.md), the coordinator role and handoff guide.
- Implementation or repair: [develop-task](../develop-task/SKILL.md), the developer role and persisted plan.
- Requirements or implementation review: [review-task](../review-task/SKILL.md), the read-only reviewer role and exact comparison scope.
- Plan-only work: [persist-implementation-plan](../persist-implementation-plan/SKILL.md); do not begin implementation.

Entry points consume this same layer; loading it again is unnecessary. For an already established role, continue its assignment rather than recursively invoking entry points. A direct implementation request authorizes its bounded work, not developer delegation or release. Preserve existing decisions and plan progress on resumption. Report the actual result, verification limits, issue/branch/commits and remaining acceptance or review.

Final acceptance triggers the coordinator's [local merge procedure](../../../docs/work/workflow.md#acceptance-and-local-merge) without another merge confirmation. Preserve its exact-version/review prerequisites and explicit owner holds; do not treat scope approval as final acceptance.
