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
