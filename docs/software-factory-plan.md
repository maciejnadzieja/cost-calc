# AI software factory: starting plan

We use [GitHub Issues](https://github.com/maciejnadzieja/cost-calc/issues) and the private **Cost Calc delivery** Project owned by `maciejnadzieja`, with its existing fields. Follow the [CLI connection instructions](work/workflow.md#agent-access) to find and access it. CLI access, fields, and issue membership are verified; activation #1 is complete. Repository workflow rules remain in [docs/work](work/README.md); work records and decision history live in GitHub. Guides and templates are published; verification is recorded in [issue #2](https://github.com/maciejnadzieja/cost-calc/issues/2).

## Purpose

Build a repeatable process that turns a product request into a tested, reviewed change to Cost Calc. Start with human supervision and increase autonomy after the process produces reliable results.

This is a proposal, not permission to implement features, commit, publish, or deploy. Repository instructions in the root AGENTS.md currently prohibit commits; preserve that restriction unless the owner explicitly changes it.

## Starting point

The repository contains a Kotlin/Spring Boot API, Vue 3/TypeScript frontend, MongoDB, Keycloak, Docker Compose, backend integration tests, and one frontend test file. These observations come from source and configuration inspection; builds, tests, and runtime behavior have not been verified for this plan.

Resolve documentation drift early: pom.xml requires Java 21 although README.md says 17+, and frontend/package.json exposes test:unit although README.md says test. README feature claims also need checking against actual behavior.

## 1. Define the first product milestone

Proposed milestone: an authenticated user can manage services and materials, create and edit a calculation, save it, reopen it, and produce a usable quote through the existing print flow. Validate which parts already work before scheduling implementation. Decide whether a downloadable PDF is required or browser printing is sufficient.

Write a short product brief covering:

- Initial customer and the problem they need solved.
- One primary workflow and its acceptance criteria.
- Calculation rules: quantities, units, currencies, rounding, taxes, and discounts where applicable.
- Data ownership and what each authenticated user can access.
- Explicit exclusions for the milestone.

Keep AI estimation as a separate milestone until the underlying calculation and quote workflow is reliable.

## 2. Use a small team of roles

Roles are responsibilities, not permanently running agents. One agent may plan and implement sequentially; review should use a separate agent session.

| Role | Responsibility | Output |
| --- | --- | --- |
| You: product owner | Priorities, product decisions, acceptance, release authorization | Approved scope and feedback |
| Coordinator | Inspect the code, turn requests into bounded tasks, identify dependencies, integrate results | Task briefs and delivery report |
| Implementation agent | Complete one task, add appropriate tests, run checks, explain changes | Reviewable diff and validation evidence |
| Review agent | Examine the diff and requirements independently; inspect correctness, regression risks, and test gaps | Actionable findings or explicit review result |

Start with one implementation task at a time. Add a second implementation agent only when tasks have clear boundaries and isolated checkouts. Split backend and frontend work when an API contract is agreed first. Avoid several agents editing the same checkout concurrently.

## 3. Establish the repository contract

Prepare these artifacts during setup:

- AGENTS.md: architecture, supported commands, coding conventions, validation expectations, task boundaries, and actions requiring owner authorization.
- docs/product-brief.md: first milestone, user workflow, acceptance criteria, and exclusions.
- docs/architecture.md: backend/frontend boundaries, API contracts, authentication, data ownership, calculation rules, and important design decisions.
- .github/ISSUE_TEMPLATE: consistent GitHub request and task templates.
- GitHub Issues and **Cost Calc delivery**: active backlog with Backlog, Ready, In progress, In review, and Done states. Keep scope/evidence in issues, status in the Project, and decisions in comments.
- Repeatable validation commands and, after confirming the repository host, CI checks using the same commands.

Maintain shared repository guidance in the root AGENTS.md and scoped work-tracking guidance in docs/work/AGENTS.md. Preserve the instruction that agents must not restart the separately running backend and frontend as part of routine task handling.

## 4. Standard delivery loop

1. **Specify:** coordinator drafts a task with a user outcome, scope, acceptance criteria, and expected checks. Ask the owner only about decisions that materially change scope or behavior.
2. **Prepare:** inspect relevant code and establish the baseline. For parallel implementation, use separate worktrees or checkouts and one task per branch after the commit policy has been resolved.
3. **Implement:** make a bounded change and test the behavior that can regress. Record any discovered work outside scope as follow-up tasks.
4. **Verify:** run appropriate automated checks and inspect the user flow when UI changes are involved. Record command results and any environment limitations.
5. **Review:** a separate session reviews the actual diff against the task. For calculations and data access, require concrete edge cases and negative cases.
6. **Repair:** implementation agent addresses findings and repeats affected checks. Escalate unresolved product or design decisions to the owner.
7. **Accept:** owner receives the diff, outcome, evidence, and remaining risks. Commit/PR/release actions follow the owner's explicit repository policy.

Never declare a task done based only on an agent saying it is finished. The diff and verification results are the evidence.

## 5. Quality gates for this project

First run these commands to establish which checks currently pass. Existing failures must be recorded and distinguished from new regressions.

| Area | Starting check | Notes |
| --- | --- | --- |
| Backend | mvn test | Integration tests use Testcontainers; confirm Docker prerequisites |
| Frontend types and build | npm run build, in frontend | Existing script includes type checking |
| Frontend tests | npm run test:unit -- --run, in frontend | Run once rather than watch mode |
| Frontend lint | npm exec -- eslint ., in frontend | Existing npm run lint applies fixes; use a non-mutating command for a gate |
| Main user flow | Manual smoke check initially | Automate after agreeing a stable workflow and test environment |

Add checks proportionate to the change:

- Calculation changes: totals, rounding, quantities, invalid inputs, and currency behavior.
- Auth/data changes: one user cannot read or modify another user's records; test production authorization rules as well as application logic.
- API changes: request validation, error responses, and frontend/backend contract compatibility.
- UI changes: relevant interactions, validation messages, and print layout where affected.

Use synthetic test data. Keep agents away from production credentials and production databases. Initially reserve releases, production migrations, and destructive data operations for explicit owner authorization.

## 6. First five working sessions

These are sequencing suggestions, not delivery-time promises.

| Session | Work | Exit condition |
| --- | --- | --- |
| 1 | Agree product brief; inspect the main workflow; record build/test baseline | Known current behavior, gaps, and one bounded milestone |
| 2 | Prepare agent instructions, task template, architecture notes, and validation workflow | A new agent can understand and validate the project from repository documentation |
| 3 | Run one small pilot through implementation and independent review | One accepted change with reproducible evidence |
| 4 | Deliver the next bounded product change using the same loop | Workflow works for a behavior change, including meaningful tests |
| 5 | Evaluate results and try two independent implementation tasks if justified | Decide whether parallelism improves throughput without increasing rework |

Recommended pilot: correct the setup/test documentation and establish the baseline validation workflow. Next, choose a small real defect discovered while exercising the calculation lifecycle. Avoid inventing a feature solely to test the factory.

## 7. Task template

```text
Title:
User outcome:
Context and relevant files:
In scope:
Out of scope:
Acceptance criteria:
Dependencies and agreed API/data contracts:
Required verification:
Permitted actions and restricted actions:
Expected handoff: diff summary, checks and results, limitations, review findings
```

Keep each task small enough for one coherent diff. A vague request such as "improve calculations" must be refined before implementation.

## 8. Measure before adding automation

For the first five tasks, record elapsed time, owner intervention time, review findings, repair rounds, escaped defects, and agent usage cost where available. Track accepted changes, not generated code volume.

Advance to parallel implementation when tasks are clearly specified, checks are repeatable, and reviewed changes consistently pass acceptance. Add automated task dispatch or release preparation only after the manual loop is dependable. Do not start by building a custom orchestration platform.

## Decisions for the owner

1. Who is the first customer, and what is the smallest useful milestone?
2. Should agents remain local, or should hosted agents also be considered?
3. May agents create commits and pull requests, replacing the current no-commit rule?
4. What agent usage budget and review time are available?

Recommended initial posture: local development, one implementation agent, independent review, owner-controlled product scope and releases, and the existing commit restriction until explicitly changed.
