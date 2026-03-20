## Backend Database Setup

The deployment target for this backend is MySQL.

Important detail:

- local `dev` profile currently defaults to H2 unless datasource env vars are overridden
- deployment scripts and schema files target MySQL

### Required Environment Variables (Profile: `dev`)

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET` (recommended)
- `GOOGLE_OAUTH_CLIENT_ID` (optional; defaults to the value in `application-dev.yml`)

Example (PowerShell):

```powershell
$env:DB_URL="jdbc:mysql://HOST:3306/DB?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC"
$env:DB_USERNAME="YOUR_USER"
$env:DB_PASSWORD="YOUR_PASSWORD"
$env:JWT_SECRET="a-very-long-secret-key-..."
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
