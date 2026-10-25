# Work management

We use [GitHub Issues](https://github.com/maciejnadzieja/cost-calc/issues) for requests, features, tasks, decisions, and evidence, and the private **Cost Calc delivery** Project owned by `maciejnadzieja` for live status, priority, and scheduling. Activation [#1](https://github.com/maciejnadzieja/cost-calc/issues/1) is complete. Guides and issue templates are published on the default branch; verification is recorded in [#2](https://github.com/maciejnadzieja/cost-calc/issues/2).

Start with **Cost Calc delivery** through the [CLI connection instructions](workflow.md#agent-access), or browse [issues](https://github.com/maciejnadzieja/cost-calc/issues). Read the [workflow](workflow.md) before refining or implementing work.

The [coordinator role](coordinator.md) defines request refinement, developer/reviewer handoffs, verification records and owner acceptance. Project-scoped role configurations live under `.codex/agents/`.

The [Cost Calc engineering layer](engineering-guidance.md) combines pinned Matt Pocock guidance with this delivery process. Use `$cost-calc-workflow` with an issue and stage, or the existing entry points below. It documents local adaptations, direct skill invocation, attribution and deliberate upstream updates.

Final acceptance of delivered changes triggers the coordinator's [local merge into main](workflow.md#acceptance-and-local-merge), without another merge confirmation. Scope approval is not final acceptance. Pushing and service operations remain separate; explicit owner merge holds are respected.

Use the [coordinate-delivery skill](../../.agents/skills/coordinate-delivery/SKILL.md) to activate coordination in a new repository chat, for example `$coordinate-delivery Refine issue #N`.

| Skill | Entry point | Example assignment |
| --- | --- | --- |
| [coordinate-delivery](../../.agents/skills/coordinate-delivery/SKILL.md) | Coordinate the selected delivery stage | `$coordinate-delivery Refine issue #N` |
| [develop-task](../../.agents/skills/develop-task/SKILL.md) | Implement an assigned task from its persisted plan | `$develop-task Implement issue #N` |
| [review-task](../../.agents/skills/review-task/SKILL.md) | Review requirements or a specified implementation diff, read-only | `$review-task Review requirements and testability for issue #N` |

For implementation review, supply the issue and exact comparison scope, such as `BASE..HEAD`. Use a separate reviewer session for an independent assessment. Skills load workflow instructions into the current conversation; invocation does not automatically spawn agents or change runtime permissions. The coordinate-delivery workflow requires the coordinator to dispatch reviewer subagents under the standing read-only authorization in root AGENTS.md unless the owner explicitly opts out for the selected task. Existing role TOMLs define named custom agents for authorized delegation.

Repository Markdown holds workflow rules, agent guidance, and the software factory plan. Work records, decision history, the board, and roadmap live in GitHub; do not create parallel Markdown trackers.

| Work | GitHub representation | Purpose |
| --- | --- | --- |
| Request | Issue | Original need and refinement decisions |
| Feature | Parent issue | User outcome and acceptance criteria |
| Task | Linked issue | Bounded implementation or investigation |
| Milestone | Repository milestone, when agreed | Group of outcomes with an exit condition |

Use GitHub issue numbers as work IDs. Use parent/sub-issue relationships when supported, and explicit issue links otherwise. Do not create Markdown backlog records.

## Maintenance and feature ideas

Keep both in the same Project and separate them using the existing Labels field:

- maintenance: upkeep of existing behavior, including fixes, dependency updates, refactoring, CI, and documentation.
- enhancement: ideas and new or expanded user capabilities; reuse GitHub's existing label.

Choose one primary category per issue. Add bug or documentation when appropriate as more specific labels. Feature implementation tasks inherit enhancement from their parent; being a task does not make work maintenance. Split mixed work if it represents independent outcomes.

Ideas and unrefined maintenance requests start in Backlog. Move either category to Ready only after explicit owner acceptance of the current refinement and all other [readiness criteria](workflow.md#make-tasks-ready) are met. Prepared proposals stay in Backlog awaiting acceptance. Labels describe the kind of work, not its priority or authorization.

Filter the Project with `label:maintenance` or `label:enhancement`, or save those filters as Maintenance and Feature ideas views. Use the existing Labels field; no custom field or second Project is needed.

Use the existing Project fields: Status (Backlog, Ready, In progress, In review, Done), Priority (P0, P1, P2), Size, Estimate, Start date, and Target date. Estimates and dates are optional; the owner sets priorities. No additional roadmap field is required.

GitHub CLI is installed and authenticated locally; Project fields and issue membership have been verified. Use CLI for Project operations and the GitHub connector for issue/repository operations. Other agent environments must establish their own access. See the [workflow](workflow.md). Migration decisions are recorded in [issue #1](https://github.com/maciejnadzieja/cost-calc/issues/1).

GitHub issue templates in .github/ISSUE_TEMPLATE are published and recognized by GitHub: Product request, Maintenance request, and Implementation or investigation task. Record decision dates as YYYY-MM-DD in Europe/Warsaw time. Do not claim commits, approval, or verification without evidence.

Agent entry point: capture the request, refine its outcome, then create only the tasks necessary to satisfy it. Follow existing user authorization; do not add routine confirmation steps.
