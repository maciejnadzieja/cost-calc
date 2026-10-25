# From request to delivery

GitHub Issues hold scope, decisions, dependencies, history, and evidence. The Cost Calc delivery Project holds live status, priority, and scheduling. Use issue numbers as IDs; do not maintain local backlog records or duplicate Status fields in issue descriptions.

Use selected Matt Pocock engineering practices through [the Cost Calc layer](engineering-guidance.md). This workflow remains the delivery authority; upstream skills do not replace its plan, branch, authorization, review or acceptance requirements.

## Capture

Create a GitHub request issue. Preserve the user's original words with attribution; keep summaries separate. Record the problem, desired outcome, constraints, and unresolved questions. Search existing issues before creating duplicates; link related work.

Apply maintenance for upkeep of existing behavior or enhancement for new capabilities and feature ideas. Use one primary category, plus bug/documentation labels when relevant. Child tasks inherit their parent's category. Both categories use the same Project statuses and readiness criteria; a Backlog idea is not authorization to implement it.

## Refine

Inspect relevant code and existing behavior. Propose observable acceptance criteria, exclusions, and dependencies. Distinguish observations from assumptions. Ask only about missing product decisions that materially affect the result; continue independent investigation while waiting.

For a small fix or documentation change, create a task directly under the request. For a larger user outcome, create a feature and split it into tasks. Use investigation tasks when implementation cannot yet be specified; give them a concrete expected finding or decision.

Present the concrete refined proposal for owner review. Complete refinement only after explicit owner acceptance under [the readiness gate](#make-tasks-ready). An explicit instruction may accept the presented refinement and authorize implementation together; record both rather than asking again. The owner sets roadmap priorities. Agents may suggest changes with reasons.

## Make tasks ready

Refinement is complete, and an issue may move to Ready, only after the owner explicitly accepts its current refined proposal. The coordinator prepares scope, exclusions, acceptance criteria, dependencies and verification, presents that concrete version, then waits for acceptance. Keep issues being refined or awaiting this acceptance in Backlog; independent reviewer approval and agent assessment do not substitute for the owner.

Record the owner's acceptance in the issue with the Europe/Warsaw date, their words or an accurate attribution, and the accepted version (issue update timestamp, linked proposal, or identifiable sections). Clarification answers, silence and a generic "start work" before the proposal was presented do not establish acceptance of that proposal. On resumption, reuse recorded acceptance for unchanged scope; materially changed scope, criteria, exclusions, dependencies or verification returns to refinement and needs acceptance of the revised proposal before implementation proceeds. Preserve prior decisions and explain the change.

Refinement acceptance permits Ready once all criteria below are met. Implementation still requires owner authorization, and final delivery acceptance remains separate. One explicit owner message may supply refinement acceptance and implementation authorization together; do not add another routine confirmation in that case. A saved implementation plan alone establishes neither.

A Ready task must contain:

- A linked request or feature and a clear outcome.
- Scope and exclusions small enough for one coherent change.
- Observable acceptance criteria.
- Relevant code context and agreed contracts, if needed.
- Dependencies with their required completion conditions.
- Verification appropriate to the change.
- No unresolved question that prevents implementation.
- Recorded explicit owner acceptance of the current refined proposal.

Do not split every file into a task. Prefer complete behavior changes; split along genuine dependencies or independent deliverables. A task may cover both backend and frontend.

## Execute and review

The [coordinator](coordinator.md) maintains workflow records and authorized developer/reviewer handoffs. The owner retains priorities, product decisions, final acceptance and release authorization.

### Task branches

Use one branch per GitHub task. Name it `codex/<issue-number>-<short-description>`:

- Use the GitHub issue number without `#`.
- Use a concise description in lowercase, with words separated by hyphens.
- Example for issue #4: `codex/4-admin-user-list`.

Before implementation, check the current branch and working tree. Start new task branches from up-to-date `main` unless the task explicitly depends on another branch. Create and switch to the branch with `git switch -c codex/<issue-number>-<short-description>`; if it already exists, inspect and reuse it with `git switch <branch-name>`. Preserve unrelated local changes and do not reset the checkout to prepare a branch.

Agents may commit task-related changes on the task branch under the root AGENTS.md's commit policy. Before every commit, verify the current branch is the selected task branch and inspect the staged diff; stage only changes belonging to that task. Do not author implementation changes on `main`; coordinator merge commits under the acceptance procedure below are the sole exception. Read-only reviewers must not commit or merge. Branch creation and permission to commit do not authorize release or work outside the selected task; local merge authorization comes from final owner acceptance.

Commit often so reviewers can follow the work in small, readable chunks. Make each commit a coherent change with one purpose, include relevant tests alongside the behavior they verify, and use a clear commit message. Run checks appropriate to the change before committing and record any verification limitations in the issue. Commit after meaningful completed steps rather than waiting for the entire task; avoid unrelated changes in one commit or arbitrary file-by-file splits.

Format every commit subject as `#<task-id> | <description of changes>`. Use the task's GitHub issue number, retain the leading `#` and spaces around `|`, and describe the changes in that commit. Example: `#4 | Add admin user list pagination`.

### Persisted implementation plans

Use [persist-implementation-plan](../../.agents/skills/persist-implementation-plan/SKILL.md) before implementation. Read the issue and decision history, inspect relevant code/tests and establish the task branch/baseline. For resumed work, reconcile the existing plan with the current issue and checkout; preserve completed steps and prior decisions.

Save a concrete `## Implementation plan` section in the task issue body. Scale detail to the task and include:

- Task ID, Europe/Warsaw date, branch and inspected baseline commit.
- Outcome, confirmed scope/exclusions, relevant code/contracts and blocking prerequisites; distinguish observations from proposals.
- Ordered steps and dependencies mapped to acceptance criteria, with small coherent commit boundaries using the required subject format.
- Verification for each outcome, including meaningful negative/edge cases and manual/environment prerequisites. Planned checks are not results.

Fetch the latest issue before editing; preserve its original request, criteria, decisions and unrelated sections. Read it back and verify the saved plan and preserved content before editing application code. If persistence/readback fails or a blocking decision remains, continue independent investigation but do not start dependent implementation. A verified plan adds no renewed approval gate when work is already authorized.

Before materially changing the approach, revise and read back the plan with a dated reason, preserving decision history. Maintain progress, commit IDs, actual verification results and limitations in the issue; keep secrets and real user data out of it. Do not substitute a local plan/backlog file. A plan does not authorize additional scope, release or owner acceptance.

### Delivery steps

1. Select a Ready task in owner-approved priority order. Verify recorded owner acceptance of the current refinement, implementation authorization and satisfied dependencies. A stale Ready status cannot replace missing acceptance evidence; return unaccepted refinement to Backlog.
2. Set Project Status to In progress, record who is working, and add a dated issue comment.
3. Before changing application code, use [persist-implementation-plan](../../.agents/skills/persist-implementation-plan/SKILL.md) to inspect the baseline and save a concrete plan in the issue's `Implementation plan` section. Read it back to verify persistence. Include scope, implementation steps, acceptance checks, dependencies, and commit boundaries. On resumption, read the existing plan; update and verify it before materially changing the approach. If persistence fails or a blocking decision remains, resolve that before implementation. A saved plan does not require another approval when implementation is already authorized.
4. Implement within scope and maintain plan progress in the issue. Link new discoveries as follow-ups rather than silently expanding scope.
5. Record changed files and exact verification results, including failures and limitations.
6. Set In review when ready for review. Coordinator-managed work uses standing read-only reviewer authorization in root AGENTS.md unless the owner explicitly opts out for the task. Review/assessment assignments dispatch substantive inspection at work start; implementation must obtain independent review before owner acceptance. Give the reviewer review-task, criteria, stage and exact repository/document version, then wait and record the result. If delegation is unavailable, leave review pending and report the limitation; do not silently waive it.
7. Address findings. Return to In progress for repairs and repeat affected checks/review. Request focused rereview of material corrections and reconcile the final reviewed version. Before owner-acceptance handoff verify dispatch, received result, finding disposition and final version, or record the explicit owner opt-out and its limits.
8. On final owner acceptance, follow [acceptance and local merge](#acceptance-and-local-merge) without another routine merge confirmation. Close the issue as completed and set Done after successful integration, or the documented no-code/explicit-hold exception below. Verify both issue state and Project status. Never infer owner acceptance.

Record evidence in GitHub issues, including task-branch and merge commit IDs and identifying work that is not yet published. Add remote commit or PR links only when they exist. Author changes on task branches; only authorized integration creates merge commits on `main`. Pushing, publication and service operations require their own authorization. Do not restart the existing backend or frontend as part of routine task handling.

### Acceptance and local merge

Final acceptance of delivered changes (for example, "I accept the fix" or an explicit request to close the completed task) is standing authorization for the coordinator to merge the accepted task into local `main`. Do not ask for merge permission again. Approval of scope, a plan, or "start work" is not final acceptance. An explicit "do not merge" or merge hold overrides the default. Acceptance never authorizes pushing, remote PR merging, branch deletion, publication, deployment or service restarts.

The coordinator owns this follow-up. A direct developer chat may adopt the coordinator role for acceptance and integration; reviewers remain read-only. Use the accepted issue and exact reviewed commit, not an arbitrary current branch. Required review/evidence must be complete or an explicit owner review opt-out recorded before integration.

1. **Pin the accepted version.** Reconcile owner acceptance, criteria, verification and the final reviewed commit. Record the accepted source SHA and branch in the issue. If the branch has advanced, do not merge its newer tip: merge only the accepted SHA when that version remains unambiguous; otherwise resolve the missing acceptance/review context. No-code assessments need no merge. If the accepted SHA is already an ancestor of local `main`, record that fact and the containing main revision rather than creating a duplicate merge.
2. **Prepare safely.** Require a clean working tree/index, no merge/rebase in progress, and no other active work in the checkout. Do not reset, discard, auto-stash unrelated edits or switch an active developer's shared checkout. Use a suitable isolated checkout when needed, or report the blocker and keep integration pending. Refresh `origin/main` when a remote is configured. If refresh fails, do not claim main is current; retain pending integration until synchronization is possible or the owner explicitly authorizes proceeding offline. On `main`, fast-forward to `origin/main` when it is ahead; retain local-only accepted merges when origin is an ancestor. If they have diverged, reconcile on a task/integration branch rather than resetting or silently merging remote history on main.
3. **Integrate the accepted commit.** Inspect the diff against the current main and accepted dependencies; do not include unrelated or unaccepted ancestor work. Switch to `main` only after preparation succeeds, then use `git merge --no-ff <accepted-sha> -m "#<task-id> | Merge accepted task into main"`. Preserve task commits and leave branches intact. If conflicts arise, record affected paths and preserve any resolution work. Abort only this newly attempted merge after verifying it began from a clean checkout, then reproduce and resolve it on a task/integration branch. Run affected checks and independent review before completing integration. Never hide conflict-resolution edits in an unreviewed main commit. Ask about product conflicts or materially changed outcomes; routine authorized merge mechanics need no renewed permission.
4. **Verify and record.** Check the merge result, clean tree and `git merge-base --is-ancestor <accepted-sha> main`. Run integration checks appropriate to the combined diff; previous task results are not evidence for changed conflict resolutions. Record the acceptance, accepted SHA, resulting main/merge SHA, actual checks and any unpublished status in GitHub, with readback. Report merge failure or missing checks instead of claiming completion. Finish on local `main` after successful integration unless another authorized assignment needs its own branch.
5. **Complete tracking.** For source-changing tasks, close and mark Done after acceptance, successful local integration and required evidence. If integration is blocked, record acceptance but leave the task open/In review until resolved. An explicit merge hold may instead complete the accepted task with that hold recorded; never imply integration occurred, and wait for the owner to lift the hold before merging. No-code work may complete after acceptance without a merge. Verify issue state and Project status. Acceptance, local integration and remote publication remain distinct events.

Check these instruction paths when changing this procedure: final acceptance with a clean checkout; scope approval only; explicit merge hold; already-contained accepted SHA; an advanced unreviewed branch tip; dirty/shared checkout; failed fetch or divergent main; conflicts; and accepted no-code assessment. State the resulting action and evidence rather than assuming every case permits a merge.

## Status rules

| Project Status | Meaning |
| --- | --- |
| Backlog | Captured, being refined, deferred, or not selected yet |
| Ready | Owner explicitly accepted the current refinement; all readiness criteria met; implementation requires authorization |
| In progress | Active implementation or investigation |
| In review | Awaiting agreed review or acceptance |
| Done | Completed outcome with verification evidence |

For blocked work, record the blocker and action needed in the issue; retain the appropriate existing Project status. Deferred work returns to Backlog with a reason. Cancelled work is closed as not planned, with a reason, and may be archived from active views; do not describe it as delivered. Reopened issues record new evidence and return to an active status. A feature is Done only when its outcome is verified; completed child tasks alone do not prove it. Milestones require evidence for their exit criteria.

Validate the readiness gate with these scenarios: proposal awaiting acceptance or clarification answers only -> Backlog, refinement pending; accepted refinement without implementation authorization -> Ready, wait to implement; explicit acceptance plus start -> proceed without another routine confirmation after prerequisites; material proposal change -> Backlog and renewed acceptance; unchanged accepted resumption -> retain acceptance. Final delivery acceptance continues to follow the separate local-merge procedure.

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
