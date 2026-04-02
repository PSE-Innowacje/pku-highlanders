# PKU Highlanders – System Rozliczeniowy

Web application for managing energy market settlement declarations (oświadczenia PKU). Contractors submit periodic declarations; administrators manage contractor types, declaration types, and user assignments.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 25, Spring Boot 4, Spring Security (OAuth2), Spring Data JPA |
| Frontend | React 19, TypeScript, Vite 8, MUI v7 |
| Database | PostgreSQL 17 |
| Auth | Keycloak 26 |
| Infrastructure | Docker Compose |

## Project Structure

```
.
├── backend/          # Spring Boot REST API
├── frontend/         # React SPA
├── keycloak/         # Realm config and DB init SQL
├── docs/             # ADRs and technical documentation
└── docker-compose.yml
```

## Prerequisites

- Java 25+
- Maven 3.9+
- Node.js 20+
- Docker & Docker Compose

## Running the Application

### 1. Start infrastructure (PostgreSQL + Keycloak)

```bash
docker compose up -d
```

Keycloak will import the `pku` realm automatically on first start. Wait ~30 seconds for it to be ready at http://localhost:8180.

### 2. Start the backend

```bash
cd backend
mvn spring-boot:run
```

The API will be available at http://localhost:8080.

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The app will be available at http://localhost:5173. API calls are proxied to the backend automatically.

## Test Users

| Username | Password | Role |
|---|---|---|
| `admin@pse.pl` | `admin123` | Administrator |
| `jan.kowalski@tauron.pl` | `jan123` | Kontrahent |
| `grzegorz.nowak@pge.pl` | `grzesiu123` | Kontrahent |
| `anna.kwiatkowska` | `ania123` | Kontrahent |

## Running Tests

### Backend — unit & integration tests

```bash
cd backend
mvn test
```

Runs unit tests (Spock), integration tests (Spring Boot Test + MockMvc + H2), and ArchUnit architecture tests.

### Backend — integration tests only

```bash
cd backend
mvn integration-test
```

### Frontend — E2E tests (Playwright)

Requires the full stack to be running (infrastructure + backend + frontend).

```bash
# Install Playwright browsers (first time only)
cd frontend
npx playwright install chromium

# Run all E2E tests
npx playwright test

# Run with UI (headed mode)
npx playwright test --headed

# Run a specific file
npx playwright test e2e/tests/auth.spec.ts
```

Test results are written to `frontend/test-results/`.

## Roles & Permissions

| Feature | Administrator | Kontrahent |
|---|---|---|
| View / manage declaration types | ✓ | — |
| View / manage contractor types | ✓ | — |
| Assign contractor types to users | ✓ | — |
| Generate own declarations | — | ✓ |
| Fill and submit declarations | — | ✓ |

## Key API Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/declarations` | List my declarations |
| `POST` | `/api/declarations/generate` | Generate declarations for my contractor type |
| `PUT` | `/api/declarations/{id}` | Save draft declaration |
| `POST` | `/api/declarations/{id}/submit` | Submit declaration |
| `GET` | `/api/admin/declaration-types` | List declaration types |
| `POST` | `/api/admin/declaration-types/{code}/generate` | Bulk generate for a schedule day |
| `GET` | `/api/admin/kontrahent-users` | List users with assignments |
| `PUT` | `/api/admin/kontrahent-users/{id}/contractor-type` | Update user's contractor type |
