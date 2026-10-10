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

1. Select a Ready task in owner-approved priority order. Confirm dependencies are satisfied.
2. Set Project Status to In progress, record who is working, and add a dated issue comment.
3. Implement within scope. Link new discoveries as follow-ups rather than silently expanding scope.
4. Record changed files and exact verification results, including failures and limitations.
5. Set In review when ready for review or owner acceptance. Request a separate review agent only when delegation is authorized; otherwise record the review method and its limitations.
6. Address findings. Return to In progress for repairs and repeat affected checks.
7. Close the issue as completed and set Project Status to Done only when acceptance criteria are satisfied, verification is recorded, and the agreed review/acceptance requirement is met. Verify both issue state and Project status. Never infer owner acceptance.

No commits are currently authorized under the root AGENTS.md. Record evidence in GitHub issues, identifying local changes that are not yet published; add commit or PR links only after authorization exists. Do not restart the existing backend or frontend as part of routine task handling.

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
