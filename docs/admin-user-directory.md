# Admin user directory

`/admin/users` is a read-only page for users with the Keycloak **realm** role `ADMIN`. It lists human accounts from the application realm, including accounts that have never used Cost Calc. Service accounts are excluded. The columns are username, email (a dash when absent), and enabled status; navigation uses pages of 20 human accounts.

## Keycloak prerequisites

Local Compose imports the required configuration for a fresh development realm. For an external or existing persistent realm, an operator must configure the following prerequisites; the application itself does not provision Keycloak accounts, roles or clients.

1. Create realm role `ADMIN` in the realm configured by the backend JWT issuer and frontend Keycloak settings. The import file includes the role for new realms; importing it does not update an already-running realm. Do not give it to all users or make it a default role.
2. Explicitly assign `ADMIN` to the intended human administrator. Ensure the application's access token contains it in `realm_access.roles`. A client role named `ADMIN` is insufficient. Obtain a new token after changing role assignments.
3. Create a dedicated OpenID Connect client, such as `cost-calc-directory`, in that same realm. Enable client authentication and service accounts. Disable browser/direct-access flows for this client. Store its generated secret in the backend environment or a secret manager.
4. Give this client's service account only the `realm-management` client role `view-users` needed to query and read user representations in the standard Keycloak role setup. Verify effective access on the deployed Keycloak version, especially when fine-grained admin policies are enabled. Do not grant `manage-users`, `realm-admin`, or unrelated management permissions. Do not grant directory-read permissions to the public browser client or to human app administrators.

The backend authenticates the application caller first, then obtains a client-credentials token. Human app administrators do not authenticate to the Keycloak Admin REST API directly.

The local realm import assigns `USER` and `ADMIN` to the bundled tester account and creates a separate confidential directory client whose service account has only `view-users`. The directory client has interactive login flows disabled. The tester's administrator assignment is a development fixture, not a default role for other accounts.

See the [Keycloak Admin REST API](https://www.keycloak.org/docs-api/latest/rest-api/index.html) and [server administration guide](https://www.keycloak.org/docs/latest/server_admin/) for client service accounts and effective administrative permissions. Match those references to the deployed version (Compose currently uses 26.4.0).

## Backend configuration

| Environment variable | Default | Purpose |
| --- | --- | --- |
| `KEYCLOAK_DIRECTORY_CLIENT_ID` | `cost-calc-directory` | Dedicated confidential client |
| `KEYCLOAK_DIRECTORY_CLIENT_SECRET` | empty | Secret supplied only to backend |
| `KEYCLOAK_DIRECTORY_SERVER_URL` | derived from JWT issuer | Optional backend-reachable Keycloak base URL, including any context path |

The realm is always derived from `spring.security.oauth2.resourceserver.jwt.issuer-uri` (`SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`), which must end in `/realms/<realm>`. The optional server URL changes connectivity, not the realm. Without an override, an issuer such as `https://login.example/auth/realms/company` uses `https://login.example/auth` for directory calls. For the Compose network, the override can be `http://keycloak:8080` while issuer validation retains the externally issued URL.

The Compose API service forwards these three environment variables. Local Compose supplies `cost-calc-directory` and an explicitly development-only fixture secret to both Keycloak and the API. The standalone application's default secret remains empty. Override the client ID and secret in an ignored `.env` file if needed; both containers receive the same values, and the realm import resolves them from environment placeholders. Never use `VITE_*` variables for credentials or commit real secrets. Use private credentials and trusted HTTPS endpoints outside local development. Missing credentials, invalid directory responses, timeouts and upstream failures produce HTTP 503 with a generic message; ordinary application routes do not need directory credentials.

## Local setup and troubleshooting

For a fresh local installation, `docker compose up --build` sets up the tester's roles, directory client, service-account read permission, and matching API credentials without console changes. The API waits until the imported realm's discovery endpoint responds successfully. Sign in with the bundled tester account to open Users.

Compose binds Keycloak's development database directory `/opt/keycloak/data/h2` to project `data/keycloak`. Realm changes, additional accounts, and client secrets survive container recreation. Startup import skips an existing realm; use the manual procedure below to update it without replacing its data. Changing a secret override also requires updating the existing directory client's secret.

When migrating from the earlier container-local setup, preserve its database before replacing that container. Stop Keycloak so its database is closed, then copy `/opt/keycloak/data/h2/.` from the stopped container into an empty `data/keycloak/` directory (Docker's `cp` supports stopped containers). Keep a backup and do not merge or overwrite existing database files. Starting with empty `data/keycloak/` instead imports the development fixture and does not retain the previous accounts or console changes. The local H2 database remains intended for development.

If Users appears but shows “Users could not be loaded”, assigning the human `ADMIN` role alone is insufficient. Check that the API has `KEYCLOAK_DIRECTORY_CLIENT_SECRET` configured and that the dedicated client exists with effective read permissions.

In the Keycloak Admin Console, select `cost-calc-realm` and create the OpenID Connect client `cost-calc-directory`. Enable **Client authentication** and **Service accounts roles**, and disable Standard flow, Direct access grants, and other interactive flows. In **Service accounts roles**, assign the `realm-management` client role `view-users`. In **Credentials**, obtain the client secret and supply it as `KEYCLOAK_DIRECTORY_CLIENT_SECRET` in the backend's Compose environment (for example, the ignored local `.env` file). Keep the public `cost-calc-client` unchanged.

An API container already running with an empty secret does not pick up a later environment change. Applying that change requires recreating the API container through the operator's normal Compose workflow. Agent verification must not restart the running application automatically. After the configuration is applied, retry Users. A continuing failure requires checking client credentials, effective `view-users` access, and backend connectivity to Keycloak; the browser deliberately receives a generic error.

Updating `realm-config.json` also does not change an existing realm. Keep the tester's live `ADMIN` assignment made through the console; the import change ensures future fresh development realms have it.

To verify the import independently on Windows, run `./docker/keycloak/test-local-realm.ps1`. It creates an isolated Keycloak 26.4.0 container with a random localhost port and a unique test directory under `data/`, and removes both afterward. It verifies directory authentication, human-only reads, tester roles, denial of user creation, and preservation of an added account after container recreation without changing the running stack. Its parameters allow checking a custom client ID and secret; use synthetic test values.

## API contract and limits

`GET /api/admin/users?page=0` returns:

```json
{
  "content": [{ "username": "example", "email": null, "enabled": true }],
  "number": 0,
  "size": 20,
  "hasNext": false
}
```

Page numbers are zero-based non-negative integers. Invalid pages return HTTP 400 without querying Keycloak; a page beyond the end is empty with `hasNext: false`. Page size is fixed. HTTP 401 rejects missing/invalid authentication; HTTP 403 rejects authenticated callers without realm `ADMIN`. Rejected callers make no directory reads. Only username, email and enabled are returned, without tokens, internal IDs, credentials or raw upstream errors.

The backend requests Keycloak users in batches of 100 and filters the service-account link defensively. It counts human rows and reads ahead one human to determine Next. It does not filter by application usage, username prefixes, email or enabled status. Earlier humans are scanned again when requesting later pages; it does not store a user directory in MongoDB. Connect timeout is 2 seconds, per-request read timeout is 3 seconds, and an overall scan deadline is checked at 10 seconds. An in-flight request may exceed that deadline until its own timeout completes; timeout returns failure, never silently truncated results. Redirects are not followed.

This is a live directory, not a snapshot. Account additions/deletions during navigation can move rows between pages. Deep pages require more upstream reads and may fail safely if the scan deadline is exceeded. Search, sorting, user modification and directory statistics are outside this feature.

## Integration acceptance

Automated tests use synthetic JWT claims and controlled HTTP responses. An operator still needs to verify the real realm, effective service-account permissions and access-token mapping in the deployed environment:

- A human realm `ADMIN` sees the navigation and page, while an ordinary account and a client-only `ADMIN` cannot access it; endpoint results are respectively 200 and 403, with 401 for missing/invalid bearer authentication.
- A human account that has never used the app appears. Disabled humans appear; missing email is a dash; service accounts do not appear. A realm with more than 20 humans supports Next and Previous correctly.
- The browser calls only the application API for directory reads. It never receives directory credentials/tokens or raw Keycloak errors. A directory outage produces the generic failure and Retry state.
- Ordinary users retain access to their own calculations, services, materials and settings, without gaining access to another user's data.

Do not restart the current backend/frontend or change the live realm as routine agent verification. Record real integration evidence in the GitHub issue once the operator has authorized and provided that setup; do not include secrets or real account details.
