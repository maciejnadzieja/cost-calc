# From request to delivery

GitHub Issues hold scope, decisions, dependencies, history, and evidence. The Cost Calc delivery Project holds live status, priority, and scheduling. Use issue numbers as IDs; do not maintain local backlog records or duplicate Status fields in issue descriptions.

## Capture

Create a GitHub request issue. Preserve the user's original words with attribution; keep summaries separate. Record the problem, desired outcome, constraints, and unresolved questions. Search existing issues before creating duplicates; link related work.

Apply maintenance for upkeep of existing behavior or enhancement for new capabilities and feature ideas. Use one primary category, plus bug/documentation labels when relevant. Child tasks inherit their parent's category. Both categories use the same Project statuses and readiness criteria; a Backlog idea is not authorization to implement it.

## Refine

Inspect relevant code and existing behavior. Propose observable acceptance criteria, exclusions, and dependencies. Distinguish observations from assumptions. Ask only about missing product decisions that materially affect the result; continue independent investigation while waiting.

For a small fix or documentation change, create a task directly under the request. For a larger user outcome, create a feature and split it into tasks. Use investigation tasks when implementation cannot yet be specified; give them a concrete expected finding or decision.

Do not invent product priorities, acceptance, or approval. An explicit user instruction can already authorize implementation; record that evidence rather than asking again. The owner sets roadmap priorities. Agents may suggest changes with reasons.

## Make tasks ready

A Ready task must contain:

- A linked request or feature and a clear outcome.
- Scope and exclusions small enough for one coherent change.
- Observable acceptance criteria.
- Relevant code context and agreed contracts, if needed.
- Dependencies with their required completion conditions.
- Verification appropriate to the change.
- No unresolved question that prevents implementation.

Do not split every file into a task. Prefer complete behavior changes; split along genuine dependencies or independent deliverables. A task may cover both backend and frontend.

## Execute and review

The [coordinator](coordinator.md) maintains workflow records and authorized developer/reviewer handoffs. The owner retains priorities, product decisions, final acceptance and release authorization.

### Task branches

Use one branch per GitHub task. Name it `codex/<issue-number>-<short-description>`:

- Use the GitHub issue number without `#`.
- Use a concise description in lowercase, with words separated by hyphens.
- Example for issue #4: `codex/4-admin-user-list`.

Before implementation, check the current branch and working tree. Start new task branches from up-to-date `main` unless the task explicitly depends on another branch. Create and switch to the branch with `git switch -c codex/<issue-number>-<short-description>`; if it already exists, inspect and reuse it with `git switch <branch-name>`. Preserve unrelated local changes and do not reset the checkout to prepare a branch.

Agents may commit task-related changes on the task branch under the root AGENTS.md's commit policy. Before every commit, verify the current branch is the selected task branch and inspect the staged diff; stage only changes belonging to that task. Never commit to `main`. Read-only reviewers must not commit. Branch creation and permission to commit do not authorize pushing, merging, publishing, deploying, or implementing work outside the selected task.

Commit often so reviewers can follow the work in small, readable chunks. Make each commit a coherent change with one purpose, include relevant tests alongside the behavior they verify, and use a clear commit message. Run checks appropriate to the change before committing and record any verification limitations in the issue. Commit after meaningful completed steps rather than waiting for the entire task; avoid unrelated changes in one commit or arbitrary file-by-file splits.

Format every commit subject as `#<task-id> | <description of changes>`. Use the task's GitHub issue number, retain the leading `#` and spaces around `|`, and describe the changes in that commit. Example: `#4 | Add admin user list pagination`.

### Delivery steps

1. Select a Ready task in owner-approved priority order. Confirm dependencies are satisfied.
2. Set Project Status to In progress, record who is working, and add a dated issue comment.
3. Before changing application code, use [persist-implementation-plan](../../.agents/skills/persist-implementation-plan/SKILL.md) to inspect the baseline and save a concrete plan in the issue's `Implementation plan` section. Read it back to verify persistence. Include scope, implementation steps, acceptance checks, dependencies, and commit boundaries. On resumption, read the existing plan; update and verify it before materially changing the approach. If persistence fails or a blocking decision remains, resolve that before implementation. A saved plan does not require another approval when implementation is already authorized.
4. Implement within scope and maintain plan progress in the issue. Link new discoveries as follow-ups rather than silently expanding scope.
5. Record changed files and exact verification results, including failures and limitations.
6. Set In review when ready for review. Coordinator-managed work uses standing read-only reviewer authorization in root AGENTS.md unless the owner explicitly opts out for the task. Review/assessment assignments dispatch substantive inspection at work start; implementation must obtain independent review before owner acceptance. Give the reviewer review-task, criteria, stage and exact repository/document version, then wait and record the result. If delegation is unavailable, leave review pending and report the limitation; do not silently waive it.
7. Address findings. Return to In progress for repairs and repeat affected checks/review. Request focused rereview of material corrections and reconcile the final reviewed version. Before owner-acceptance handoff verify dispatch, received result, finding disposition and final version, or record the explicit owner opt-out and its limits.
8. Close the issue as completed and set Project Status to Done only when acceptance criteria are satisfied, verification is recorded, and the agreed review/acceptance requirement is met. Verify both issue state and Project status. Never infer owner acceptance.

Record evidence in GitHub issues, including task-branch commit IDs when available and identifying local changes or commits that are not yet published. Add remote commit or PR links only when they exist. Commits on task branches are permitted; commits to `main` are forbidden. Pushing and merging require separate authorization. Do not restart the existing backend or frontend as part of routine task handling.

## Status rules

| Project Status | Meaning |
| --- | --- |
| Backlog | Captured, being refined, deferred, or not selected yet |
| Ready | Meets readiness criteria and can be implemented when selected |
| In progress | Active implementation or investigation |
| In review | Awaiting agreed review or acceptance |
| Done | Completed outcome with verification evidence |

For blocked work, record the blocker and action needed in the issue; retain the appropriate existing Project status. Deferred work returns to Backlog with a reason. Cancelled work is closed as not planned, with a reason, and may be archived from active views; do not describe it as delivered. Reopened issues record new evidence and return to an active status. A feature is Done only when its outcome is verified; completed child tasks alone do not prove it. Milestones require evidence for their exit criteria.

## History and roadmap

Append dated history entries for creation, refinement decisions, scope changes, status changes, review findings, and acceptance. Include the actor, reason, and supporting links. Never fabricate user quotes, test results, approvals, or past events. Correct inaccurate entries with a subsequent correction rather than silently deleting them.

Use Priority (P0, P1, P2) for owner-approved ordering and Start date / Target date for agreed scheduling. Size and Estimate are optional. Use Project views and milestones for the roadmap. Do not create additional mandatory fields or invent dates.

Before handing off, check that links resolve, issue state and Project status match the outcome, and dependencies are recorded. Report what changed and what decision or action remains.

## Agent access

The private Project is named **Cost Calc delivery** and belongs to `maciejnadzieja`. Access it through authenticated GitHub CLI; a public Project URL is not required.

Install GitHub CLI in each agent environment. The examples below use PowerShell and `gh` on PATH. In the current Windows environment, if `gh` is not on PATH, replace it with `& 'C:\Program Files\GitHub CLI\gh.exe'`.

Check authentication first:

```powershell
gh auth status
```

If not authenticated, the operator signs in with `gh auth login --hostname github.com --web --scopes project`. For an existing login missing Project access, use `gh auth refresh --hostname github.com --scopes project`. Reading alone can use read:project; agents updating status need project access. Keep credentials outside the repository and issue comments. The GitHub plugin's login is separate from CLI authentication.

Discover the Project by its exact name and owner, then read its current fields and items. Do not choose a different Project if discovery fails or matches are ambiguous:

```powershell
$factoryOwner = 'maciejnadzieja'
$factoryTitle = 'Cost Calc delivery'
$factoryProjectsJson = gh project list --owner $factoryOwner --limit 1000 --format json
if ($LASTEXITCODE -ne 0) { throw 'Could not list GitHub Projects.' }
$factoryProjects = $factoryProjectsJson | ConvertFrom-Json
$factoryMatches = @($factoryProjects.projects | Where-Object { $_.title -ceq $factoryTitle })
if ($factoryMatches.Count -ne 1) { throw 'Expected exactly one Cost Calc delivery Project.' }
$factoryProjectNumber = $factoryMatches[0].number

gh project view $factoryProjectNumber --owner $factoryOwner --format json
gh project field-list $factoryProjectNumber --owner $factoryOwner --limit 100 --format json
gh project item-list $factoryProjectNumber --owner $factoryOwner --limit 1000 --format json
```

Check the returned counts if the Project grows beyond these limits and fetch all required items before selecting work. Use project item-edit with the discovered Project, item, field, and option IDs, then read the item back to verify updates. Other agent environments require their own authentication and network access. Respect execution permissions and request escalation when required; a failed sandboxed authentication check alone does not prove the stored login is invalid.
