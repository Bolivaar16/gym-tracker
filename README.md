# Gym Tracker

A full-stack personal gym tracking app built as a portfolio project to demonstrate backend
engineering skills: REST API design, SQL analytics, third-party API integration, and a
React Native mobile frontend.

**Backend:** Java 17 · Spring Boot 3.3 · PostgreSQL 16 · Flyway · Spring Security (JWT)
**Mobile:** React Native · Expo SDK 56 · TypeScript · React Query
**AI:** Anthropic Claude Haiku (contextual coaching)

---

## Features

- **Exercise catalog** — searchable, filterable by muscle group, soft-delete (archive)
- **Workout logging** — track exercises and sets (reps, weight) in real time
- **Templates** — reusable workout plans; starting one materialises a live session
- **Analytics** — volume trends, estimated 1RM time series (Epley formula), personal records, streak tracking — all computed in PostgreSQL
- **AI Coach** — ask questions; the backend injects your recent workouts and PRs as context before calling Claude
- **Mobile app** — 5-tab Expo app with auth gate, pull-to-refresh, muscle-group chips, bar chart, and a chat UI for the coach

---

## Architecture

```
gym-tracker/
├── api/            # OpenAPI 3 spec — single source of truth for the contract
├── backend/        # Spring Boot application
│   └── src/main/java/dev/alex/gymtracker/
│       ├── auth/       # JWT issuance & validation
│       ├── exercise/   # Exercise catalog
│       ├── workout/    # Workout sessions & sets
│       ├── template/   # Workout templates
│       ├── stats/      # Analytics (native SQL, no entity loading)
│       ├── coach/      # AI coach (Anthropic SDK, JdbcTemplate context queries)
│       └── common/     # Shared exceptions & handlers
└── mobile/         # Expo React Native app
```

Package-by-feature; each package owns its controller → service → repository stack.
`stats/` and `coach/` are isolated from domain packages — enforced by ArchUnit tests.
The OpenAPI spec drives code generation (`openapi-generator-maven-plugin`, `interfaceOnly=true`).

---

## Quick start

### Prerequisites

- Java 17, Maven
- Docker (for PostgreSQL)
- Node.js 18+ (for the mobile app)

### Backend

```bash
docker compose up -d          # starts PostgreSQL 16

cd backend
cp .env.example .env          # fill in APP_PASSWORD_HASH, JWT_SECRET
./mvnw spring-boot:run
```

Swagger UI: http://localhost:8080/swagger-ui.html

To get a JWT:

```bash
curl -s -X POST http://localhost:8080/api/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"<your-password>"}' | jq .access_token
```

### Mobile app

```bash
cd mobile
cp .env.example .env          # set EXPO_PUBLIC_API_URL (see below)
npx expo start
```

| Target | `EXPO_PUBLIC_API_URL` |
|--------|-----------------------|
| iOS Simulator / web | `http://localhost:8080` |
| Android Emulator | `http://10.0.2.2:8080` |
| Physical device (Expo Go) | `http://<your-LAN-IP>:8080` |

Restart Metro after editing `.env` (env vars are inlined at bundle time).

### AI Coach (optional)

Set `ANTHROPIC_API_KEY=sk-ant-...` in the backend environment. Without it the `/api/coach/ask`
endpoint returns 503 and the mobile app shows a graceful fallback message.

---

## API reference

All endpoints except `POST /api/auth/token` require `Authorization: Bearer <jwt>`.

### Auth
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/token` | Issue a JWT |

### Exercises
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/exercises` | List (filter: `muscleGroup`, `includeArchived`) |
| POST | `/api/exercises` | Create |
| GET | `/api/exercises/{id}` | Get by id |
| PUT | `/api/exercises/{id}` | Full update |
| PATCH | `/api/exercises/{id}` | Partial update (`defaultRestSeconds`) |
| DELETE | `/api/exercises/{id}` | Archive (soft delete) |

### Workouts
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/workouts` | Start a new session |
| GET | `/api/workouts` | List (paginated, most recent first) |
| GET | `/api/workouts/{id}` | Full detail with exercises and sets |
| PUT | `/api/workouts/{id}/finish` | Mark as finished |
| DELETE | `/api/workouts/{id}` | Delete |

### Sets
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/workouts/{wId}/exercises/{eId}/sets` | Log a set |
| PUT | `/api/workouts/{wId}/exercises/{eId}/sets/{sId}` | Update a set |
| DELETE | `/api/workouts/{wId}/exercises/{eId}/sets/{sId}` | Delete a set |

### Templates
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/templates` | List summaries |
| POST | `/api/templates` | Create |
| GET | `/api/templates/{id}` | Full detail |
| PUT | `/api/templates/{id}` | Full replace |
| DELETE | `/api/templates/{id}` | Delete |
| POST | `/api/templates/{id}/start` | Start a workout from template |

### Stats
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/stats/volume` | Volume per week or month (`groupBy`, `muscleGroup`, `from`, `to`) |
| GET | `/api/stats/exercises/{id}/one-rm` | Epley 1RM time series (`from`, `to`) |
| GET | `/api/stats/prs` | Personal records per exercise |
| GET | `/api/stats/summary` | Dashboard: workouts this week/month, volume, streak |

### Coach
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/coach/ask` | Ask the AI coach (injects recent workouts + PRs as context) |

---

## Testing

```bash
cd backend
./mvnw verify          # runs unit tests + integration tests (requires Docker for Testcontainers)
./mvnw test -Dtest="*ArchitectureTest"   # ArchUnit isolation rules only (no Docker needed)
```

Integration tests use Testcontainers (PostgreSQL 16). ArchUnit tests enforce that `stats/`
and `coach/` packages have no compile-time dependencies on other domain packages.

```bash
cd mobile
npm run typecheck      # TypeScript strict check (no Jest in this phase)
```

---

## Notable implementation details

- **OpenAPI-first**: the spec in `api/openapi.yaml` is the contract; `mvn generate-sources`
  produces all controller interfaces and model classes. Implementations never diverge from the spec.
- **Stats queries**: all aggregation is in SQL — `TO_CHAR(..., 'IYYY-"W"IW')` for ISO weeks,
  `FILTER (WHERE rn = 1)` with window functions for PRs, gaps-and-islands for the streak.
  No Java-side aggregation loops.
- **Template copy semantics**: starting a workout from a template materialises `WorkoutExercise`
  rows at that instant. Later edits to the template do not affect past workouts.
- **AI coach isolation**: the `coach/` package uses `JdbcTemplate` with its own SQL queries
  rather than importing from `stats/`. Package isolation is enforced by ArchUnit.
- **Auth flow (mobile)**: an axios response interceptor clears the JWT and calls back into
  React context on any 401, which swaps the navigator to the Login screen without any
  imperative navigation call from the interceptor.
