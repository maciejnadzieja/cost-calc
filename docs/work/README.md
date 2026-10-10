# Work management

We use [GitHub Issues](https://github.com/maciejnadzieja/cost-calc/issues) for requests, features, tasks, decisions, and evidence, and the private **Cost Calc delivery** Project owned by `maciejnadzieja` for live status, priority, and scheduling. Activation [#1](https://github.com/maciejnadzieja/cost-calc/issues/1) is complete. Guides and issue templates are published on the default branch; verification is recorded in [#2](https://github.com/maciejnadzieja/cost-calc/issues/2).

Start with **Cost Calc delivery** through the [CLI connection instructions](workflow.md#agent-access), or browse [issues](https://github.com/maciejnadzieja/cost-calc/issues). Read the [workflow](workflow.md) before refining or implementing work.

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

Ideas and unrefined maintenance requests start in Backlog. Move either category to Ready only when the readiness criteria are met. Labels describe the kind of work, not its priority or authorization.

Filter the Project with `label:maintenance` or `label:enhancement`, or save those filters as Maintenance and Feature ideas views. Use the existing Labels field; no custom field or second Project is needed.

Use the existing Project fields: Status (Backlog, Ready, In progress, In review, Done), Priority (P0, P1, P2), Size, Estimate, Start date, and Target date. Estimates and dates are optional; the owner sets priorities. No additional roadmap field is required.

GitHub CLI is installed and authenticated locally; Project fields and issue membership have been verified. Use CLI for Project operations and the GitHub connector for issue/repository operations. Other agent environments must establish their own access. See the [workflow](workflow.md). Migration decisions are recorded in [issue #1](https://github.com/maciejnadzieja/cost-calc/issues/1).

GitHub issue templates in .github/ISSUE_TEMPLATE are published and recognized by GitHub: Product request, Maintenance request, and Implementation or investigation task. Record decision dates as YYYY-MM-DD in Europe/Warsaw time. Do not claim commits, approval, or verification without evidence.

Agent entry point: capture the request, refine its outcome, then create only the tasks necessary to satisfy it. Follow existing user authorization; do not add routine confirmation steps.
