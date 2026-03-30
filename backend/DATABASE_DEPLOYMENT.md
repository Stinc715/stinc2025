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
- `APP_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS` (optional; defaults to the production domains)

Example (PowerShell):

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:mysql://HOST:3306/DB?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC"
$env:DB_USERNAME="YOUR_USER"
$env:DB_PASSWORD="YOUR_PASSWORD"
$env:JWT_SECRET="a-very-long-secret-key-..."
```

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
