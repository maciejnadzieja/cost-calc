# Local regression tests

The Playwright suite checks the running frontend, API, and real Keycloak login. It does not launch or restart services. Existing backend and Vitest tests remain the faster checks for component behavior, failure cases, and data isolation.

## Setup

From `frontend/`, install dependencies and Chromium once:

```powershell
npm install
npx playwright install chromium
```

The local stack must already be available. Its realm needs the separate `admin@cost-calc.eu` / `admin-password` and `tester@cost-calc.eu` / `tester-password` fixtures described in [the admin directory guide](admin-user-directory.md). Persistent realms skip updated imports, so missing or incorrect fixtures must be corrected by the operator. Tests fail rather than silently skip unavailable services or failed logins.

## Read-only smoke suite

```powershell
npm run test:e2e
```

Three Chromium tests verify:

- Admin logs in through the real Keycloak page, sees Users, and loads the real directory and approved columns.
- Tester has no Users link, is redirected away from the direct URL without a directory request, and receives API HTTP 403.
- The API returns HTTP 200 for admin and HTTP 401 for missing or invalid bearer tokens.

Each test gets a fresh browser context. Tokens are captured from actual application requests and kept in memory; no password-grant bypass for human app users, mocked authentication, or saved browser state is used. The suite does not create accounts, change roles, or reset databases.

## Pagination suite (creates temporary accounts)

Run this only on a local realm containing exactly the two human fixtures above plus the Polish tester `testowy@cost-calc.eu` imported on fresh installations (see the [fixture guide](../README.md#running-with-docker)). The explicit opt-in prevents account provisioning during the default smoke command:

```powershell
$env:E2E_ALLOW_FIXTURES = '1'
try { npm run test:e2e:pagination }
finally { Remove-Item Env:E2E_ALLOW_FIXTURES -ErrorAction SilentlyContinue }
```

This test authenticates the local master administrator, verifies the three-human baseline, and creates 22 uniquely named synthetic humans to reach 25. It verifies API and browser pages of 20 and 5, Next/Previous, complete unique coverage, no service accounts, and inclusion of a disabled human with missing email. It deletes only IDs created by that run after checking their usernames, then confirms cleanup. Existing accounts, passwords, roles, and databases are not modified. If the realm has additional or missing humans, the test fails before creating fixtures.

Cleanup runs on normal success and test failure. Force-killing the process or losing Keycloak connectivity can prevent cleanup; the test-owned usernames have a `regression-<run UUID>-` prefix. Review those accounts in the console before removing leftovers; the tests never delete unrelated accounts to make the baseline fit. Run one pagination process at a time.

## Configuration and reports

| Environment variable | Default |
| --- | --- |
| `E2E_BASE_URL` | `http://localhost:5173` |
| `E2E_API_URL` | `http://localhost:8888/api` |
| `E2E_KEYCLOAK_URL` | `http://localhost:9090` |
| `E2E_ADMIN_USER`, `E2E_ADMIN_PASSWORD` | Local administrator fixture |
| `E2E_TESTER_USER`, `E2E_TESTER_PASSWORD` | Local tester fixture |
| `E2E_KEYCLOAK_ADMIN_USER`, `E2E_KEYCLOAK_ADMIN_PASSWORD` | Local master `admin` / `admin` (pagination only) |

Endpoints must be local; pagination targets `cost-calc-realm`. Supply private overrides through the process environment, not committed files. Login pages are checked for a local address before credentials are entered.

The suite runs serially without automatic retries. HTML reports are in `frontend/playwright-report/`; failure screenshots are in `frontend/test-results/`. Both paths are ignored by Git and can contain private local account details. Trace recording, videos, and authentication-state files are disabled so credentials and bearer tokens are not recorded in those artifacts. `npx playwright show-report` opens the most recent report.

Run `npm run test:e2e:types` to type-check the regression code separately. The normal `npm run test:unit -- --run` and `npm run build` remain unchanged; Vitest excludes `e2e/`.

A dedicated Compose test stack with separate ports and `data/regression/` is the next step when adding tests that change application data or when the everyday realm contains more than these three fixtures. The initial suite uses the existing stack and does not create that separate environment.
