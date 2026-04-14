# Security Operations

This document records the additional operational controls that now exist in the repository for production-facing deployments, and the review steps that still need an owner during live operation.

## 1. Technical controls now present in the codebase

- HTTPS is enforced at the edge by `deploy/nginx.conf`, with HTTP redirected to HTTPS.
- HSTS is enabled in the HTTPS server block so repeat visitors stay on secure transport.
- Authentication cookies are `HttpOnly`, `SameSite=Lax`, and marked `Secure` when requests arrive over HTTPS, see `StreamAuthCookieService.java`.
- Session TTL is now configurable through:
  - `APP_SECURITY_SESSION_AUTH_TOKEN_TTL_DAYS`
  - `APP_SECURITY_SESSION_STREAM_TOKEN_TTL_DAYS`
- Users can rotate their session from the profile area, which bumps `sessionVersion` and invalidates previously issued tokens.
- High-value security events are written to `security_event_log`.
- High-severity events can also trigger an operational alert email when `APP_SECURITY_ALERT_EMAIL_TO` is configured.

## 2. Security events now audited

The application now records structured security events for actions such as:

- account registration
- login success
- login failure
- login throttle blocks
- invalid token use
- rejected sessions due to token/session-version mismatch
- authentication-required and access-denied responses
- Google sign-in failures and errors
- password changes
- email changes
- profile export requests
- profile deletion requests
- explicit session rotation

These records are stored in the `security_event_log` table and are also emitted to application logs with the `SECURITY_EVENT` prefix.

High-severity events additionally emit `SECURITY_ALERT` style operational log lines and can send email if alert delivery is configured.

## 3. Alerting expectations

Production should set:

- `APP_SECURITY_ALERTS_ENABLED=true`
- `APP_SECURITY_ALERT_EMAIL_TO=<security-or-ops-mailbox>`

Recommended recipients are an owned operational mailbox rather than a personal inbox. If email alert delivery is not configured, the system still records the event in logs and in `security_event_log`, but no out-of-band alert is sent.

## 4. Session management expectations

The session model remains stateless JWT plus server-side session version invalidation:

- login issues fresh auth and stream cookies
- password change bumps `sessionVersion`
- email change bumps `sessionVersion`
- explicit session rotation bumps `sessionVersion`

Operationally, this means token invalidation is handled by version mismatch rather than a central session store.

For production, keep auth-token lifetime short enough to limit exposure, and rotate it if shared-device or credential-compromise risk is suspected.

## 5. Organisational access review

Technical controls are not enough on their own. Production should also run a documented access review covering:

- application admin accounts
- club admin accounts with live member data access
- server SSH access
- database access
- AWS access
- SMTP provider access
- Stripe / Google / OpenAI console access where enabled

Minimum review cadence:

- quarterly scheduled review
- immediate review after role changes, team departures, or suspected compromise

Each review should confirm:

- who still needs access
- whether the assigned role is still least-privilege
- whether shared credentials or mailboxes should be rotated
- whether stale accounts or dormant club-admin users should be removed

## 6. Evidence to retain

For each production review cycle, keep:

- reviewer name
- review date
- systems reviewed
- accounts removed or downgraded
- unresolved follow-ups

That evidence can live outside the codebase, but the deployment owner should maintain it as part of operational compliance.
