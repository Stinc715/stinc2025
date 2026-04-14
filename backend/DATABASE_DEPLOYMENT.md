## Backend Database Setup

The default runtime profile for this backend is now `prod`, and that profile targets MySQL.

Important detail:

- `prod` uses MySQL and `ddl-auto: validate`
- local H2 is still available, but only when you explicitly set `SPRING_PROFILES_ACTIVE=dev`
- deployment scripts and schema files target MySQL and now write `SPRING_PROFILES_ACTIVE=prod`

### Required Environment Variables (Profile: `prod`)

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET` (recommended)
- `GOOGLE_OAUTH_CLIENT_ID` (optional; defaults to the value in `application.yml`)
- `APP_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS` (optional; defaults to `https://example.invalid,https://www.example.invalid`)

Example (PowerShell):

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:mysql://HOST:3306/DB?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC"
$env:DB_USERNAME="YOUR_USER"
$env:DB_PASSWORD="YOUR_PASSWORD"
$env:JWT_SECRET="a-very-long-secret-key-..."
```

### One-off Production Club Bootstrap

To replace the current production club dataset with the prepared 20 clubs on the next backend start, add these environment variables to the host env file:

```powershell
$env:APP_SEED_PRODUCTION_CLUBS_ENABLED="true"
$env:APP_SEED_PRODUCTION_CLUBS_EXPECTED_EXISTING_CLUB_COUNT="2"
$env:APP_SEED_PRODUCTION_CLUBS_EXPECTED_EXISTING_CLUB_USER_COUNT="2"
$env:APP_SEED_PRODUCTION_CLUBS_ALLOW_EMPTY_DATASET="false"
```

Notes:

- this runs only outside the `dev` profile
- it refuses to wipe production data unless the current database has the expected number of clubs and club accounts
- if the 20-club dataset is already present, it skips without rewriting it
- the backend log prints the replaced club names and club emails so you can identify the removed accounts

### One-off Production Community Q&A Backfill

To add starter Q&A content to clubs that currently have an empty board, enable this for one backend start:

```powershell
$env:APP_SEED_PRODUCTION_COMMUNITY_QA_ENABLED="true"
```

Notes:

- this does not wipe clubs or bookings
- it only fills clubs whose community board is currently empty
- it also ensures a small set of seed member accounts exists so the questions have visible member authors

### One-off Production Canonical Club Content Refresh

If the 20 prepared clubs are already in production and you want to make their prices, membership plans, timeslot patterns, and community Q&A more varied without replacing the clubs themselves, enable this for one backend start:

```powershell
$env:APP_SEED_PRODUCTION_CLUB_CONTENT_REFRESH_ENABLED="true"
```

Notes:

- this runs only outside the `dev` profile
- it requires the canonical 20-club dataset to already exist
- it refuses to run if those clubs already have active memberships, booking records, or unreleased booking holds
- it rewrites plans, venues/timeslots, and community Q&A in place so existing club ids and club images stay intact

### Local H2 Development

Use H2 only when you intentionally opt into the dev profile:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
```

### Database Schema (9 Core Tables)

This project uses 9 core business tables:

- `user`
- `club`
- `venue`
- `timeslot`
- `membership_plan`
- `user_membership`
- `booking_record`
- `club_admin`
- `transaction`

Create/update them by running `deploy/mysql_schema.sql` against your AWS RDS database.

Notes:

- `user.password_hash` stores a BCrypt hash (not plaintext).
- The backend uses quoted identifiers (Hibernate `globally_quoted_identifiers=true`) so table names like `user` and `transaction` work reliably.

Related:

- API baseline contract: `backend/API_CONTRACT.md`
