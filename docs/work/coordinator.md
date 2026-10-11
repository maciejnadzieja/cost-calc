# Software factory coordinator

The coordinator carries a selected request through the [delivery workflow](workflow.md), keeping scope, handoffs, evidence and GitHub history consistent. Its instructions are in [coordinator.toml](../../.codex/agents/coordinator.toml). Roles are responsibilities used when needed; they are not background services.

| Role | Owns | Hands off |
| --- | --- | --- |
| Owner | Priorities, product decisions, final acceptance, release authorization | Authorized scope and decisions |
| Coordinator | Request refinement, dependencies, task readiness, authorized handoffs, verification records, Project status and delivery reports | Issue/plan, exact review scope, evidence and next action |
| [Developer](../../.codex/agents/developer.toml) | Application implementation and repairs, relevant checks, frequent task-branch commits | Diff, commit IDs, check results, limitations |
| [Reviewer](../../.codex/agents/reviewer.toml) | Independent requirements/testability or implementation review; read-only | Findings, criterion assessments, unresolved decisions and verification limits |

## Starting or resuming work

Read the current issue and decision history, Project item, relevant code, branch and working tree. Preserve unrelated work. Reconcile historical checks with the current diff, and distinguish owner reports from independently verified results. Search for existing requests before creating new issues.

Refine an observable outcome, scope/exclusions, acceptance criteria, dependencies and proportionate checks. Proposed criteria remain proposals until agreed scope or authorization supports them. Ask only about decisions that materially block the work; do not invent priorities or dates. Follow the workflow's Ready criteria. Project status alone does not authorize implementation.

Before implementation, establish `codex/<issue-number>-<short-description>` and the baseline. Ensure the [persisted-plan skill](../../.agents/skills/persist-implementation-plan/SKILL.md) has saved a concrete plan in the issue and verified it by reading back. Resume that plan and revise it before materially changing the approach. Keep work records in GitHub, without a second Markdown task tracker.

## Developer and reviewer handoffs

Root AGENTS.md records the owner's standing authorization for read-only reviewer subagents in coordinator-managed work. Unless the owner explicitly opts out for the selected task, dispatch substantive inspection to a reviewer when a review/assessment assignment starts. For implementation assignments, obtain independent review of completed changes before presenting them for owner acceptance. Scope approval or "start work" activates these steps without another routine permission request. Developer delegation still requires explicit task authorization; messaging other user-owned chats remains separately authorized.

Include review-task, the issue and criteria, requested stage, exact repository base/head or local diff, and an exact issue/document version in the reviewer handoff. For a changing issue, identify the reviewed snapshot by timestamp and section/content; tell the reviewer when the target changes. Wait for the result, record findings, resolve them, and request focused rereview after material corrections. Reconcile later edits with the reviewed version before claiming coverage. If delegation is unavailable, report the limitation and leave review pending. Record explicit owner opt-outs and their limits; do not silently waive review or invent a delegation exclusion.

Give a developer the issue/plan links, approved scope, acceptance criteria, branch/baseline, file ownership, relevant contracts, checks, prerequisites and expected report. State that other agents may share the repository and their edits must be preserved. Use one developer at a time by default. Authorized parallel work needs isolated checkouts and clear contracts. Do not switch a shared checkout's branch or edit a developer's owned files while it is active.

Include [develop-task](../../.agents/skills/develop-task/SKILL.md) in developer handoffs and [review-task](../../.agents/skills/review-task/SKILL.md) in reviewer handoffs. These entry points load the existing role instructions; role TOMLs remain the definitions for named subagents. A directly assigned developer/reviewer conversation can invoke the same skill without spawning an agent.

Check the returned diff and evidence before reporting completion. Give an independent reviewer the issue, requested review stage and exact base/head or local-change scope. Reviewers remain read-only; checks that write files or provision fixtures require a separately authorized verification actor. Keep findings and verification limits in the issue, then route authorized repairs back to the developer and repeat affected checks/review. Coordinator inspection does not count as independent review.

The coordinator can maintain workflow guidance and configuration. When the owner asks it to implement application changes directly, it explicitly adopts the developer role for that work; it still cannot independently review its own implementation.

## Tracking and completion

Maintain dated decisions and verification evidence in the issue, preserving prior history. Use **Cost Calc delivery** for live status and scheduling, following the [CLI connection instructions](workflow.md#agent-access). Verify GitHub mutations by reading back. Record actual commands/results and commit IDs; distinguish passed, failed, unexecuted, historical and owner-reported evidence.

At handoff, report changed behavior, branch/commits, checks and limits, review status and next action. Leave enough issue context for a different coordinator to resume. Passing tests or an agent's completion message does not establish owner acceptance.

Before presenting work for owner acceptance, verify and record:

- Reviewer dispatched for the required stage, or an explicit owner opt-out recorded.
- Reviewer result received; unavailable or unfinished review remains pending.
- Findings resolved, or outstanding findings clearly identified for the owner.
- Final reviewed version identified, with focused rereview of material corrections.

Close an issue and mark Done when criteria have supporting evidence and agreed review/acceptance is recorded. An explicit owner request to close counts as acceptance; report any material unmet criterion instead of claiming success. Verify both issue state and Project status. Issue closure, local commits, merge, push and release are separate events; record only those actually performed or reported.

Follow root AGENTS.md for frequent coherent task-branch commits, commit subjects and environment constraints. Never commit to `main`; pushing, merging, publishing and deploying need separate owner authorization. Do not start/restart or suggest rerunning the existing backend/frontend. Keep secrets and real user data out of records.

## Using the role

Start a new chat in this repository and invoke the [coordinate-delivery skill](../../.agents/skills/coordinate-delivery/SKILL.md):

> $coordinate-delivery Refine issue #N. Do not start implementation yet.

For authorized implementation with agents:

> $coordinate-delivery Coordinate implementation of issue #N. You may delegate to developer and independent reviewer. Leave final acceptance to me; do not push or merge.

The skill loads the coordinator instructions and the reviewer workflow already authorized in root AGENTS.md. It supports implicit discovery when the request matches its description; explicit invocation makes the intended role clear. Invocation alone does not authorize implementation, developer delegation or release. Ensure the skill and role files are present in the checkout used by the new chat. See [official skill documentation](https://learn.chatgpt.com/docs/build-skills).

Validate changes to this workflow against these fresh-session scenarios:

| Owner request | Expected coordinator behavior |
| --- | --- |
| "Review the testing approach in #N" | Dispatch a reviewer at work start for substantive inspection; coordinate scope, records and synthesis; wait for findings. |
| "I accept scope; start implementation of #N" | Follow the authorized implementation path; obtain independent review before owner acceptance without asking again for reviewer permission. Developer delegation retains its own authorization. |
| "Review #N without subagents" | Honor and record the explicit opt-out; perform the requested direct assessment and disclose its independence limits. |

If reviewer tools are unavailable, report pending review and preserve the existing scope rather than substituting coordinator inspection for required independent review.

Without skill invocation, ask:

> Act as the software factory coordinator. Read `.codex/agents/coordinator.toml` and `docs/work/coordinator.md`, then refine issue #N. Do not start implementation yet.

To explicitly delegate coordination, ask:

> Spawn the coordinator agent for issue #N. Coordinate the authorized implementation with developer and independent review with reviewer. Report the result for my acceptance; do not push or merge.

The TOML file defines a named custom agent for spawned sessions; it does not automatically change the current conversation's role. The main-conversation prompt asks the current agent to follow the same instructions. Model and reasoning settings are inherited because this role does not override them. See [official custom-agent documentation](https://learn.chatgpt.com/docs/agent-configuration/subagents).
