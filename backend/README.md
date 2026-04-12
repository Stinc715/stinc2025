# Backend

## Stack

- Spring Boot 3.2
- Java 17
- Spring Data JPA
- Spring Security
- H2 for local `dev`
- MySQL for `prod`

## Profiles

- `dev`: local H2, fastest setup for reviewers and local development
- `prod`: MySQL, schema validation, production-style runtime

The top-level `application.yml` defaults to `prod`, so set `SPRING_PROFILES_ACTIVE=dev` when you want the local H2 path.

## Runtime prerequisites

- Java 17 must already be installed locally
- Maven itself is handled through the repo wrapper scripts `mvnw` / `mvnw.cmd`
- on a machine without a cached wrapper distribution, the first backend run/test/package command downloads Maven 3.9.11 once and reuses that cache later

## Run locally

### Dev profile with H2

From the repo root:

```bash
npm run dev:backend
```

Equivalent direct wrapper command:

```bash
sh ./mvnw -f backend/pom.xml -Dspring-boot.run.profiles=dev spring-boot:run
```

### Prod-style profile with MySQL

From the repo root:

```bash
sh ./mvnw -f backend/pom.xml -Dspring-boot.run.profiles=prod spring-boot:run
```

Set the matching database and secret environment variables first. See [../.env.example](../.env.example) and [DATABASE_DEPLOYMENT.md](./DATABASE_DEPLOYMENT.md).

## Package

```bash
sh ./mvnw -f backend/pom.xml clean package
```

The packaged JAR is written to:

- `backend/target/club-portal-backend-1.0-SNAPSHOT.jar`

## Tests

From the repo root:

```bash
npm run test:backend
```

Or directly:

```bash
sh ./mvnw -f backend/pom.xml test
```

## Important environment variables

- `SPRING_PROFILES_ACTIVE`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_SEED_PRODUCTION_CLUBS_ENABLED`
- `APP_SEED_PRODUCTION_CLUBS_EXPECTED_EXISTING_CLUB_COUNT`
- `APP_SEED_PRODUCTION_CLUBS_ALLOW_EMPTY_DATASET`
- `APP_SEED_PRODUCTION_COMMUNITY_QA_ENABLED`
- `APP_SEED_PRODUCTION_CLUB_CONTENT_REFRESH_ENABLED`
- `JWT_SECRET`
- `APP_PUBLIC_BASE_URL`
- `GOOGLE_OAUTH_CLIENT_ID`
- `OPENAI_API_KEY` or `APP_LLM_API_KEY`
- `APP_PAYMENTS_MODE`
- `STRIPE_SECRET_KEY`
- `STRIPE_WEBHOOK_SECRET`

## One-off production club bootstrap

If you need the production server to replace its current club dataset with the 20 prepared clubs, enable these environment variables for one deployment:

- `APP_SEED_PRODUCTION_CLUBS_ENABLED=true`
- `APP_SEED_PRODUCTION_CLUBS_EXPECTED_EXISTING_CLUB_COUNT=2`
- `APP_SEED_PRODUCTION_CLUBS_EXPECTED_EXISTING_CLUB_USER_COUNT=2`
- `APP_SEED_PRODUCTION_CLUBS_ALLOW_EMPTY_DATASET=false`

Behavior:

- the bootstrap runs only outside `dev`
- it refuses to run unless the current database has exactly the expected number of clubs and club accounts
- it skips itself when the canonical 20-club dataset is already present
- it logs the replaced club names and club emails so you can see which two old accounts were removed

## Backend reference docs

## One-off production canonical club content refresh

If the prepared 20 clubs are already online and you only want to refresh their prices, plan mix, timeslot patterns, and community Q&A without replacing club ids or images, enable this for one deployment:

- `APP_SEED_PRODUCTION_CLUB_CONTENT_REFRESH_ENABLED=true`

Behavior:

- the refresh runs only outside `dev`
- it requires the canonical 20-club dataset to already exist
- it refuses to run if any of those clubs already have active memberships, booking records, or unreleased booking holds
- it rewrites membership plans, venues/timeslots, and community Q&A in place so existing club images remain attached

- API contract: [API_CONTRACT.md](./API_CONTRACT.md)
- Database setup and deployment: [DATABASE_DEPLOYMENT.md](./DATABASE_DEPLOYMENT.md)
- Runtime implementation notes: [../docs/IMPLEMENTATION_GUIDE.md](../docs/IMPLEMENTATION_GUIDE.md)
