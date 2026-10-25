# Engineering guidance for Cost Calc

Matt Pocock's selected skills supply engineering practice. The [delivery workflow](workflow.md) supplies Cost Calc's tracking and delivery rules. The [cost-calc-workflow skill](../../.agents/skills/cost-calc-workflow/SKILL.md) joins them; the existing coordinator, developer, reviewer and plan skills remain entry points.

Read this layer before using any imported skill, including a direct invocation such as `$tdd` or `$code-review`. Within repository guidance, this layer and the delivery/role rules take precedence over conflicting imported instructions. Explicit owner instructions and execution permissions still govern. Loading a skill neither changes your role nor authorizes additional work.

## Choose the practice

Read only the skills needed for the assignment, and their supporting files when relevant.

| Situation | Guidance | Cost Calc application |
| --- | --- | --- |
| Implement behavior or a regression fix | [tdd](../../.agents/skills/tdd/SKILL.md) | One behavior at a time, observed failure before the fix, independent expected results. |
| Diagnose a hard bug or regression | [diagnosing-bugs](../../.agents/skills/diagnosing-bugs/SKILL.md) | Reproduce the exact symptom, minimize, test hypotheses, remove instrumentation and verify the original scenario. |
| Clarify business terms | [domain-modeling](../../.agents/skills/domain-modeling/SKILL.md) | Define terms from real examples and code; create a glossary only as useful terms are resolved. |
| Design or refactor an interface | [codebase-design](../../.agents/skills/codebase-design/SKILL.md) | Hide complexity behind useful interfaces while preserving existing Kotlin/Spring and Vue conventions. |
| Independently review implementation | [code-review](../../.agents/skills/code-review/SKILL.md) | Inspect requirements and standards as separate dimensions within the read-only reviewer assignment. |
| Improve the agent environment after a session | [retro](../../.agents/skills/retro/SKILL.md) | Recommend changes based on observed friction; implementation of follow-ups needs its own scope. |
| Write agent-facing guidance | [writing-for-agents](../../.agents/skills/writing-for-agents/SKILL.md) | Keep routing concise and detailed rules in one discoverable place. |
| Prepare a continuation | [handoff](../../.agents/skills/handoff/SKILL.md) | Reference the GitHub plan/evidence, branch, commits and remaining work; redact sensitive information. |

## Local adaptations

- **Tracking and plans:** use GitHub Issues and the existing Cost Calc delivery Project, including its Backlog, Ready, In progress, In review and Done statuses. Use existing issue templates and maintenance/enhancement classification. Do not introduce upstream triage states, local ticket files, duplicate plan files or automatic issue closure. Persist and read back the [implementation plan](workflow.md#persisted-implementation-plans) before implementation; reconcile it on resumption. Follow the [owner acceptance gate](workflow.md#make-tasks-ready) before completing refinement or moving Ready; keep unaccepted proposals in Backlog. Skills and a Ready label/status are not implementation authorization.
- **Branches and commits:** follow [task branches](workflow.md#task-branches), including `codex/<issue-number>-<short-description>`, coherent commits and `#<task-id> | <description of changes>`. Check branch and staged diff first. Never interpret an imported instruction to commit as permission to commit to the current arbitrary branch. Do not reset another checkout, merge, push, publish or deploy without the applicable owner authorization.
- **Accepted integration:** final owner acceptance supplies local merge authorization through [acceptance and local merge](workflow.md#acceptance-and-local-merge); the coordinator follows that procedure without another confirmation. Scope approval does not supply it. Explicit holds, reviewed-version checks and role boundaries still apply; upstream orchestration never grants release or branch-deletion permission.
- **Delegation and acceptance:** follow the [coordinator handoff rules](coordinator.md#developer-and-reviewer-handoffs). Standing read-only reviewer authorization applies to coordinator-managed work unless the owner opts out. Developer delegation and messaging other user-owned chats retain separate authorization. Parallel design exploration is not automatically authorized. Missing reviewer tools leave review pending. Record results, findings and the final reviewed version; completion or passing tests does not establish owner acceptance.
- **Review scope:** `review-task` establishes the issue, stage and exact comparison. Use the assigned base/head or local-change scope, including staged, unstaged and untracked files when assigned; the upstream `...HEAD` example cannot replace that scope. A reviewer remains read-only and does not spawn agents, change files, run artifact-writing suites, commit or mutate GitHub. The assigned reviewer evaluates both requirements and standards; the coordinator may split them across authorized independent reviewers when useful, but two nested reviewers are not mandatory. Retain correctness, security, authorization, tenant/data isolation, credentials and regression coverage in the review. Report severity and criterion coverage while keeping the two dimensions visible. Self-inspection is not independent review.
- **Testing and questions:** choose appropriate public interfaces in the persisted plan. Existing agreed verification is sufficient; do not ask for renewed approval for each testing boundary or routine technical choice. Ask only when a missing product decision blocks work. Use red-green-refactor when useful, including safe refactoring during development. Do not require tests for reversible documentation-only changes, ban all internal mocks, delete tests by rule, or insist every integration loop completes in seconds. Preserve meaningful coverage; prefer real MongoDB tests when MongoDB semantics matter. Evidence must distinguish planned, executed, failed and unverified checks.
- **Debugging:** scale the diagnosis phases to the bug. Reading code and forming tentative hypotheses may help construct a reproducer. If the environment prevents reproduction, report the limitation and continue useful read-only investigation without claiming a proven cause or verified fix. Temporary instrumentation and harnesses stay within the assigned scope and must be cleaned up.
- **Documentation and terminology:** retain familiar Spring services, Vue components and API terminology where precise. Read existing glossary/ADR conventions first; do not restructure the application to match example folders. GitHub remains authoritative for decisions and history. A glossary or ADR may capture reusable domain/design context with a link to its decision issue, not a second work record. Reviewer assignments report proposed documentation changes without writing them. Retrospective suggestions do not authorize new hooks, dependencies, configuration changes or unrelated fixes.
- **Tools and environment:** upstream examples using a `Skill` tool mean load the relevant available skill through this environment's supported mechanism; do not assume a tool literally named `Skill` exists. Use PowerShell-compatible commands here. Bash templates are reference material unless a compatible runtime and the requested procedure justify their use. Keep secrets out of captured artifacts. Do not start, restart or suggest rerunning the existing backend/frontend. A handoff document does not authorize sending another chat a message.

For behavior changes, use the existing tools: Maven/JUnit and Testcontainers on the backend; `npm run test:unit -- --run`, `npm run type-check` and `npm run build` in `frontend` as appropriate. Use the existing Playwright projects for relevant browser verification after inspecting their fixture/environment requirements. Do not blindly run upstream example commands or start services. Read `pom.xml`, `frontend/package.json` and the affected tests before selecting checks.

## Usage

Examples in a repository chat:

```text
$cost-calc-workflow Implement issue #N using its saved plan.
$develop-task Implement issue #N.
$cost-calc-workflow Diagnose the regression described in issue #N.
$review-task Review issue #N against BASE..HEAD, read-only.
$coordinate-delivery Refine issue #N; do not implement yet.
```

Direct imported-skill invocations also use this layer and keep the current assignment's authorization. For an independent review, supply the exact issue/version and diff in a separate reviewer session. New skills may require a fresh repository chat to appear in the skill selector; their files can be read directly in the current chat. No service restart is needed.

## Source and updates

The eight selected skills are copied unchanged from [mattpocock/skills at 5beb35b](https://github.com/mattpocock/skills/tree/5beb35b040e3de97e54fa9e386d7e42d15c295c2), with their supporting files and selector metadata. [The manifest](matt-pocock-skills.lock.json) records upstream paths, the commit and Git blob hashes; [the MIT license](../../.agents/skills/matt-pocock.LICENSE) preserves attribution. Local adaptations live here rather than being patched into upstream copies.

Validate provenance and local links from the repository root:

```powershell
python scripts/verify-agent-skills.py
```

The check normalizes CRLF to LF for Windows checkouts. It is offline and checks the files against recorded hashes; it does not independently authenticate the remote commit or prove agent behavior.

Update deliberately in a GitHub task: inspect upstream differences at a chosen commit, reconcile the adaptations here, copy the selected skill folders with their supporting files, update the license and manifest from that commit, then validate and independently review the result. Do not install the entire marketplace plugin or auto-update these copies alongside the repository skills: that would add duplicate guidance and unadapted orchestration. Upstream `implement`, `implement-spec`, `triage`, `to-spec`, `to-tickets`, setup and wayfinder are intentionally not installed.
