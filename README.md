# ACME Salary Management

Web-based salary management for a 10,000-employee, multi-country org. Built for the HR Manager
persona: manage employee/compensation records and see payroll analytics without touching a
spreadsheet.

- **Backend**: Java 21 + Spring Boot 4, PostgreSQL, Flyway migrations, Maven
- **Frontend**: React + Vite (JavaScript) + MUI (DataGrid + X Charts)
- **Docs**: [`docs/requirements.md`](docs/requirements.md) (goal/scope/non-goals),
  [`docs/architecture.md`](docs/architecture.md) (data model, API, design decisions),
  [`docs/ai-usage.md`](docs/ai-usage.md) (how AI tools were used building this)

## Quick start (Docker Compose)

Requires Docker. Spins up Postgres, the backend (seeding 10,000 employees on first boot), and the
frontend, all wired together:

```bash
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- Health check: http://localhost:8080/actuator/health

First boot takes a bit longer while the seed script runs (~a few seconds once the app is up).
Subsequent restarts skip seeding automatically (it only runs against an empty database).

## Running locally without Docker

**Backend** (needs a Postgres instance, or point `DB_URL` at a local H2 file for a zero-install
option):

```bash
cd backend
DB_URL="jdbc:postgresql://localhost:5432/salary_management" \
DB_USER=salary_app DB_PASSWORD=salary_app \
SEED_ON_STARTUP=true \
./mvnw spring-boot:run
```

**Frontend**:

```bash
cd frontend
cp .env.example .env.local   # points VITE_API_BASE_URL at the backend
npm install
npm run dev
```

Then open http://localhost:5173.

## Tests

```bash
cd backend && ./mvnw test      # 26 tests: unit, @DataJpaTest repository, WebMvcTest controller
cd frontend && npm run test    # 10 tests: Vitest + Testing Library
```

## Environment variables

**Backend**

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/salary_management` | JDBC connection string |
| `DB_USER` / `DB_PASSWORD` | `salary_app` / `salary_app` | DB credentials |
| `SEED_ON_STARTUP` | `false` | Generate the 10k-employee dataset on boot (only if the table is empty) |
| `SEED_EMPLOYEE_COUNT` | `10000` | How many employees to seed |
| `SEED_RANDOM_SEED` | `42` | Fixes the seed dataset so it's reproducible |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated origins allowed to call `/api/**` |
| `PORT` | `8080` | HTTP port |

**Frontend**

| Variable | Default | Purpose |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | Backend API base URL (baked in at build time) |

## Deployment

This repo is deploy-ready but not deployed by default - live hosting is a manual step (see
`docker-compose.yml` and the two `Dockerfile`s, which work as-is on any container host):

- **Backend**: any host that runs a container + Postgres (Render, Railway, Fly.io, etc.). Point
  `DB_URL`/`DB_USER`/`DB_PASSWORD` at the managed Postgres instance, set `CORS_ALLOWED_ORIGINS` to
  the deployed frontend's origin, and set `SEED_ON_STARTUP=true` for the first deploy only (or
  leave it on - it's a no-op once the table has data).
- **Frontend**: any static host (Vercel, Netlify, Render static site) built with `VITE_API_BASE_URL`
  pointing at the deployed backend, or the provided `Dockerfile` (nginx-served) on a container host.

## Demo video

_Link to be added once recorded._
