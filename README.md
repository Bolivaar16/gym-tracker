# Gym Tracker

Personal single-user gym tracker with a future AI coach layer.

**Stack:** Java 21 · Spring Boot 3 · PostgreSQL 16 · React Native (Expo) · OpenAPI 3

## Quick start

```bash
docker compose up -d
cd backend
cp .env.example .env   # fill in secrets
./mvnw spring-boot:run
# Swagger UI: http://localhost:8080/swagger-ui.html
```

## Project structure

- `api/` — OpenAPI 3 spec (source of truth)
- `backend/` — Spring Boot application
- `mobile/` — React Native (Expo) app

## API Endpoints

All endpoints except `/api/auth/token` require a Bearer JWT.

### Auth
- `POST /api/auth/token` — Issue a JWT token

### Exercises
- `GET /api/exercises` — List exercises (filter by muscle group, optionally include archived)
- `POST /api/exercises` — Create an exercise
- `GET /api/exercises/{id}` — Get an exercise
- `PUT /api/exercises/{id}` — Update an exercise
- `PATCH /api/exercises/{id}` — Partially update an exercise (e.g., `defaultRestSeconds`)
- `DELETE /api/exercises/{id}` — Archive an exercise (soft delete)

### Workouts
- `POST /api/workouts` — Start a new workout session
- `GET /api/workouts` — List workouts (paginated, most recent first)
- `GET /api/workouts/{id}` — Get full workout detail with exercises and sets
- `PUT /api/workouts/{id}` — Update workout (notes, finish time)
- `DELETE /api/workouts/{id}` — Delete a workout and all its sets

### Sets
- `POST /api/workouts/{workoutId}/exercises/{exerciseId}/sets` — Log a set
- `PUT /api/workouts/{workoutId}/exercises/{exerciseId}/sets/{setId}` — Update a set
- `DELETE /api/workouts/{workoutId}/exercises/{exerciseId}/sets/{setId}` — Delete a set

### Templates
- `GET /api/templates` — List templates
- `POST /api/templates` — Create a template
- `GET /api/templates/{id}` — Get template detail with ordered exercises and targets
- `PUT /api/templates/{id}` — Update a template (full replace of exercise list)
- `DELETE /api/templates/{id}` — Delete a template
- `POST /api/templates/{id}/start` — Start a workout from a template (pre-populates exercises)

### Stats (Phase 3)
- `GET /api/stats/volume?groupBy=week|month&muscleGroup=<optional>&from=<date>&to=<date>` — Total training volume (Σ reps × weight_kg) aggregated per ISO week or calendar month. Returns array of `{period, volumeKg}`.
- `GET /api/stats/exercises/{id}/one-rm?from=<date>&to=<date>` — Estimated 1RM time series for an exercise using Epley formula (`weight × (1 + reps/30)`). One data point per workout containing the exercise. Returns 404 if exercise not found.
- `GET /api/stats/prs` — Personal records: max weight, max estimated 1RM, and max reps per exercise, each with date and workout ID.
- `GET /api/stats/summary` — Dashboard snapshot: workouts this week, workouts this month, total volume this week (kg), current consecutive training streak (days). Only finished workouts count.
