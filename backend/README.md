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
- `JWT_SECRET`
- `APP_PUBLIC_BASE_URL`
- `GOOGLE_OAUTH_CLIENT_ID`
- `OPENAI_API_KEY` or `APP_LLM_API_KEY`
- `APP_PAYMENTS_MODE`
- `STRIPE_SECRET_KEY`
- `STRIPE_WEBHOOK_SECRET`

## Backend reference docs

- API contract: [API_CONTRACT.md](./API_CONTRACT.md)
- Database setup and deployment: [DATABASE_DEPLOYMENT.md](./DATABASE_DEPLOYMENT.md)
- Runtime implementation notes: [../docs/IMPLEMENTATION_GUIDE.md](../docs/IMPLEMENTATION_GUIDE.md)
