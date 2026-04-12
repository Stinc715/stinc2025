# Deployment

## Supported release entry point

The supported deployment command is:

```powershell
powershell -ExecutionPolicy Bypass -File deploy\upload.ps1 `
  -ServerHost <server-host> `
  -ServerUser <server-user> `
  -SshKey <path-to-ssh-key>
```

`deploy/upload.ps1` is a template-style script. It does not ship with a default host, default user, or default private key path.

## What `upload.ps1` does

`deploy/upload.ps1` is the current release path. It:

1. runs the acceptance chain by default
2. builds the backend JAR through the Maven wrapper entrypoint
3. packages the frontend build output
4. uploads the frontend archive, backend JAR, schema, and migrations
5. runs the remote release script

Safe defaults:

- tests are enabled by default
- `-SkipTests` is explicit opt-in and should only be used when the same commit has already passed validation elsewhere
- connection settings can come from PowerShell parameters or these environment variables:
  - `CLUB_PORTAL_DEPLOY_HOST`
  - `CLUB_PORTAL_DEPLOY_USER`
  - `CLUB_PORTAL_DEPLOY_SSH_KEY`
  - `CLUB_PORTAL_DEPLOY_DIR`

## Deployment source of truth

The deployment source of truth is the built frontend output in `dist/`, not raw files from `frontend/`.

- `upload.ps1` packages `dist/`
- `deploy.sh` now also prefers `dist/` first
- `frontend/` remains only as a legacy fallback for manual bootstrap cases

## Response-header hardening

`deploy/nginx.conf` now adds deployment-level response headers in addition to any page-level meta tags:

- `X-Content-Type-Options: nosniff`
- `Referrer-Policy: strict-origin-when-cross-origin`
- `Permissions-Policy: camera=(), microphone=(), geolocation=(), payment=()`
- `X-Frame-Options: SAMEORIGIN`

## Common options

```powershell
powershell -ExecutionPolicy Bypass -File deploy\upload.ps1 `
  -ServerHost <server-host> `
  -ServerUser <server-user> `
  -SshKey <path-to-key.pem>
```

Optional flags:

- `-SkipBuild`: upload existing build artifacts without rebuilding
- `-SkipTests`: explicitly skip validation while building
- `-UploadOnly`: upload artifacts but do not trigger the remote release

## Files in this folder

- `upload.ps1`: primary local release entry point
- `remote_release_full.sh`: full remote release
- `remote_release_frontend_only.sh`: remote frontend-only release
- `remote_deploy_backend.sh`: backend replacement / restart helper
- `remote_apply_schema_from_env.sh`: applies schema and migrations using remote env
- `mysql_schema.sql`: base schema
- `migrations/`: incremental SQL migrations
- `nginx.conf`: nginx config reference

## Legacy script

- `deploy.sh` is an older manual bootstrap helper.
- It is kept as reference only and is not the primary deployment path for the current project.

## Typical release workflow

1. Run tests locally.
2. If you intentionally want to replace the online club dataset with the prepared 20 clubs, set `APP_SEED_PRODUCTION_CLUBS_ENABLED=true` in the remote env file together with the expected guard values before release.
3. If the 20 clubs already exist online and you only want fresher prices, timeslots, plans, and Q&A, set `APP_SEED_PRODUCTION_CLUB_CONTENT_REFRESH_ENABLED=true` for one release instead.
4. Run `deploy/upload.ps1` with the correct SSH key.
5. Confirm the remote service is healthy.
6. Confirm the target health endpoint returns `200`.

## Related docs

- [../README.md](../README.md)
- [../backend/DATABASE_DEPLOYMENT.md](../backend/DATABASE_DEPLOYMENT.md)
- [../docs/IMPLEMENTATION_GUIDE.md](../docs/IMPLEMENTATION_GUIDE.md)
