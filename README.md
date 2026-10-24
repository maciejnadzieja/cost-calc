# Cost Calc AI

Work planning: the private **Cost Calc delivery** GitHub Project, [GitHub Issues](https://github.com/maciejnadzieja/cost-calc/issues), and [request workflow](docs/work/README.md). See [CLI connection instructions](docs/work/workflow.md#agent-access) to access the Project.

Shared instructions for development agents: [AGENTS.md](AGENTS.md).

Cost Calc AI is a powerful cost calculation and estimation application designed for small businesses and independent professionals. It leverages Artificial Intelligence to help users manage services, materials, and generate accurate quotes for clients.

The application is built with a modern technology stack, featuring a robust Kotlin Spring Boot backend and a responsive Vue.js frontend, all containerized with Docker for seamless deployment.

## ✨ Features

-   **AI-Powered Estimation**: Intelligent cost calculation algorithms.
-   **Project Management**: Manage services, materials, and client details.
-   **Quote Generation**: Create and export professional PDF quotes.
-   **Secure Authentication**: Integrated with Keycloak for robust security.
-   **Modern UI**: Responsive interface built with Vue.js and Bootstrap.
-   **RESTful API**: Fully documented API for backend interactions.

## 🛠 Technology Stack

### Backend
-   **Language**: Kotlin
-   **Framework**: Spring Boot (Web, Security, Data MongoDB)
-   **Database**: MongoDB
-   **Testing**: JUnit, MockK, Testcontainers

### Frontend
-   **Framework**: Vue.js 3
-   **Build Tool**: Vite
-   **State Management**: Pinia
-   **Styling**: Bootstrap
-   **Testing**: Vitest

### Infrastructure
-   **Containerization**: Docker & Docker Compose
-   **Authentication**: Keycloak

## 📋 Prerequisites

Ensure you have the following installed on your local machine:
-   **Java 17+**
-   **Maven**
-   **Node.js 20+**
-   **Docker** & **Docker Compose**

## 🚀 Getting Started

### Running with Docker (Recommended)

The easiest way to run the entire application (Backend, Frontend, Database, Auth) is using Docker Compose.

```bash
docker-compose up --build
```

-   **Frontend**: [http://localhost:5173](http://localhost:5173)
-   **Backend API**: [http://localhost:8888](http://localhost:8888)

#### Build images and restart locally (PowerShell)

From the repository root:

```powershell
./scripts/rebuild-local.ps1
```

The script checks Docker Compose v2, the Docker daemon and the Compose configuration, then builds the `api` and `frontend` images. Compilation happens inside Docker: the API Dockerfile runs Maven packaging (with tests skipped), and the frontend Dockerfile runs `npm run build`, including the existing Vue/TypeScript check. Java, Maven and Node do not need to be installed on the host. The frontend continues to run the local Vite development server with the existing source mounts; this does not switch local hosting to a production static server.

Only after both images build successfully does the script run `docker compose up` with `--no-build --force-recreate` for the API/frontend services and a bounded running/healthy wait. Compose also starts or reconciles their MongoDB/Keycloak dependencies as needed. Existing `data/` bind mounts are retained; the script never runs `down`, deletes data/volumes or prunes Docker resources. A failed preflight or build exits before container recreation. A startup failure exits with an error and may leave partially started containers; it does not automatically roll back.

```powershell
./scripts/rebuild-local.ps1 -BuildOnly                  # Prepare images without changing containers
./scripts/rebuild-local.ps1 -WhatIf                     # Describe the operation without invoking Docker
./scripts/rebuild-local.ps1 -WaitTimeoutSeconds 300     # Allow longer for local startup (default: 180)
```

Use `pwsh -File ./scripts/rebuild-local.ps1` if you prefer an explicit PowerShell invocation. The script requires PowerShell 5.1 or later and Docker Compose v2 with `--wait`/`--wait-timeout` support. It resolves the Compose file/project directory from its own location, so an absolute script path works from another working directory. It uses the current Docker context and Compose environment/project-name settings; select your local Docker Desktop context before running it. It does not stop independently running host Java/Vite processes: if they occupy ports 8888/5173, resolve that conflict before choosing container startup.

The final wait reports Compose running/healthy state. Only services with configured health checks are checked for health; this is not an authenticated API or browser smoke test. See the regression guide below for those checks. No services need to be restarted merely to develop or validate this script.

Run the command-boundary tests without Docker or service changes:

```powershell
pwsh -NoProfile -File ./scripts/tests/rebuild-local.tests.ps1
```

For browser checks against the running local stack, see [local regression tests](docs/local-regression-tests.md). The default `npm run test:e2e` command (from `frontend/`) is read-only; pagination fixtures require a separate opt-in command.

Local container state is stored in the ignored project `data/` directory:

| Service | Host storage | Contents |
| --- | --- | --- |
| MongoDB | `data/mongodb/db`, `data/mongodb/configdb` | Database files |
| Keycloak | `data/keycloak` | Local development realm database |
| API | `data/api/logs` | Rolling application logs |
| Frontend | `data/frontend/vite`, `data/frontend/npm` | Vite and npm caches |

Docker creates the directories on first use. Database state survives container recreation and `docker compose down`. Application binaries stay in images; frontend sources remain mounted from the repository. Runtime data is excluded from Git and the API image build context. For local administrator and directory-client configuration, see [the admin directory guide](docs/admin-user-directory.md).

On a fresh MongoDB installation (an empty `data/mongodb/db`), `docker/mongodb/init-mongo.js` seeds materials, services, settings, and three synthetic calculations for `tester@cost-calc.eu`:

| Calculation | Contents | Total (USD) |
| --- | --- | --- |
| Weekly lawn care | Services only | 175.00 |
| Spring planting supplies | Materials only | 185.00 |
| Garden renovation | Two stages with services and materials | 509.50 |

These calculations use the tester's fixed Keycloak ID and appear after signing in as the tester. Other accounts, including the application administrator, do not gain access to the tester's calculations. Items are embedded snapshots of the example services and materials; totals are calculated by the application.

Initialization runs only for a new MongoDB data directory. Rebuilding images or recreating containers with existing data does not add or restore fixtures, overwrite edits, or duplicate calculations. An existing installation keeps its current data; there is no migration or manual seeding command for these samples.

Fresh local installations also include the ordinary Polish tester `testowy@cost-calc.eu` (same local password as the English tester), with fixed UUID `00000000-0000-0000-0000-000000000001`. Its separate fixtures are Polish counterparts of the English tester's 20 materials, 20 services and three calculations, using PLN with the same nominal totals (175.00, 185.00 and 509.50) and a Polish print footer. Select Polish manually using the existing language switch. The account and data are imported only into fresh Keycloak/MongoDB stores; existing stores are unchanged.

For an existing installation using `mongodb_data/`, stop MongoDB before copying that directory's contents into an empty `data/mongodb/db/`; retain the original as a backup. Do not copy live database files or overwrite an already populated destination. The new path otherwise starts an empty database. Existing Keycloak data inside an old container must likewise be preserved before replacing that container; see the migration note in the guide. These paths contain local application data and credentials, so keep them out of source control.

### Running Locally

If you prefer to run services individually for development:

#### 1. Backend

```bash
# From the root directory
mvn spring-boot:run
```
The backend server will start at `http://localhost:8888`.

#### 2. Frontend

```bash
# From the root directory
cd frontend
npm install
npm run dev
```
The frontend development server will start at `http://localhost:5173`.

## ⚙️ Configuration

-   **Backend**: Main configuration is located in `src/main/resources/application.properties`.
-   **Database**: MongoDB initialization scripts are in `docker/mongodb/init-mongo.js`.
-   **Environment**: You can customize environment variables in `docker-compose.yml`.
-   **Admin user directory**: See [Keycloak roles and backend directory credentials](docs/admin-user-directory.md) before using the admin-only user list.

## 🧪 Testing

### Backend Tests
```bash
mvn test
```

### Frontend Tests
```bash
cd frontend
npm run test
```
