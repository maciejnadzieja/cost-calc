# Maintaining work records

GitHub Issues in maciejnadzieja/cost-calc are authoritative for work records and history. Read README.md and workflow.md before refining requests or executing tasks. Preserve original requests, use issue numbers as IDs, and record decisions and evidence in issues. Do not create Markdown backlog records, boards, roadmaps, or migration logs that duplicate GitHub.

User instructions determine authorization and priorities. Do not fabricate acceptance, tests, or decisions. Proposed roadmap items do not authorize implementation. Follow the root AGENTS.md's commit policy: author task changes on the task branch; only coordinator merge commits under [acceptance and local merge](workflow.md#acceptance-and-local-merge) may be created on main. This file does not authorize spawning agents or parallel implementation.

Classify issues with maintenance (upkeep of existing behavior) or enhancement (new capabilities and ideas), using the existing Labels field. Child tasks inherit the parent's category. Add bug/documentation when relevant; labels do not authorize implementation or set priority.

Use the private **Cost Calc delivery** Project owned by `maciejnadzieja` for live status and priority. Connect with GitHub CLI and resolve its number by name as described in workflow.md under Agent access. Existing Status options are Backlog, Ready, In progress, In review, Done; use existing Priority, Size, Estimate, and date fields as useful. Do not add mandatory custom fields or duplicate status in issue descriptions. Read current fields before updating and verify mutations. Local CLI authentication does not imply access in other agent environments.
