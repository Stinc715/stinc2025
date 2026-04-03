# Auth Session Loading Audit

## Scope

This document records the current loading contract for `frontend/auth-session.js`.
It is an audit document, not a migration plan. The current classic-script behavior is
intentional and stable. This round does not change `auth-session.js`, its HTML tags,
or any page execution order.

## Current contract

- `auth-session.js` is loaded by 20 HTML pages.
- Every current reference is a classic external script tag of the form
  `<script src="auth-session.js?..."></script>`.
- Every current reference appears in `<head>`, before `<body>`.
- The script mutates `Storage.prototype`, migrates legacy token state, and exposes
  `window.AuthSession` before later page scripts run.
- This is why the current loading order matters more than the Vite warning.

## Page inventory

| Page | Auth script location | Runs before | Dependency class | Current dependency shape |
| --- | --- | --- | --- | --- |
| `club bookings.html` | `<head>` | inline booking management script | Indirect storage dependency | Later code reads `localStorage.getItem('token')` for authenticated requests. |
| `club chat.html` | `<head>` | `ui-prompt.js` and inline chat script | Indirect storage dependency | Later code reads `localStorage.getItem('token')` for chat fetches and actions. |
| `club home.html` | `<head>` | inline dashboard bootstrap | Direct `window.AuthSession` dependency | Later code calls `getToken`, `clearAll`, and `logout`, and also falls back to `localStorage.getItem('token')`. |
| `club register.html` | `<head>` | inline registration bootstrap | Direct `window.AuthSession` dependency | Later code calls `setToken` and `setStoredUser`, then writes token-bearing login state. |
| `club updates.html` | `<head>` | inline updates management script | Indirect storage dependency | Later code reads `localStorage.getItem('token')` for authenticated API calls. |
| `club-admin.html` | `<head>` | `ui-prompt.js` and inline admin script | Indirect storage dependency | Later code reads `localStorage.getItem('token')` in admin flows. |
| `club-info.html` | `<head>` | `ui-prompt.js` and inline club info script | Indirect storage dependency | Later code reads `localStorage.getItem('token')` for info and upload actions. |
| `club.html` | `<head>` | `ui-prompt.js`, main inline page bootstrap, then `auth-modal.js` | Direct `window.AuthSession` dependency | Later code calls `getToken`, `authFetch`, `setStoredUser`, and `setToken`, and also checks storage fallback keys. |
| `clubs.html` | `<head>` | inline directory script | Indirect storage dependency | Later code reads `localStorage.getItem('token')` for authenticated joins/follows. |
| `home.html` | `<head>` | inline home bootstrap, then `auth-modal.js` | Direct `window.AuthSession` dependency | Later code calls `clearAll` and `logout`, and reads `localStorage.getItem('token')` through helper logic. |
| `join.html` | `<head>` | inline membership / booking option script | Indirect storage dependency | Later code reads `localStorage.getItem('token')` for checkout and gated actions. |
| `login.html` | `<head>` | Google GSI client and inline auth bootstrap | Direct `window.AuthSession` dependency | Later code calls `setToken` and `setStoredUser`, then persists sanitized login state. |
| `onboarding-complete.html` | `<head>` | `onboarding-complete.js` and `auth-modal.js` | No visible page-local dependency | Current page-local code does not visibly call `window.AuthSession` or read the token. |
| `onboarding-location.html` | `<head>` | `ui-prompt.js`, `onboarding-location.js`, and `auth-modal.js` | No visible page-local dependency | Current page-local code does not visibly call `window.AuthSession` or read the token. |
| `onboarding-promo.html` | `<head>` | `onboarding-promo.js` and `auth-modal.js` | Indirect storage dependency | The module reads `localStorage.getItem('token')` before onboarding API calls. |
| `onboarding.html` | `<head>` | `onboarding.js` and `auth-modal.js` | Indirect storage dependency | The module reads `localStorage.getItem('token')` for onboarding bootstrap and save flows. |
| `payment.html` | `<head>` | inline payment status / polling script | Indirect storage dependency | Later code reads `localStorage.getItem('token')` inside `authFetch`. |
| `reset-password.html` | `<head>` | inline reset-password form script | No visible page-local dependency | Current inline code does not visibly call `window.AuthSession` or read the token. |
| `user.html` | `<head>` | `user-profile.js` and `auth-modal.js` | Direct `window.AuthSession` dependency | The module calls `setToken` and relies on token-backed profile flows. |
| `venue overview.html` | `<head>` | inline venue overview script | Indirect storage dependency | Later code reads `localStorage.getItem('token')` for gated actions. |

## Dependency classes

### 1. Direct `window.AuthSession` dependency

These pages or their first-party modules visibly call the API exposed by
`auth-session.js`:

- `club home.html`
- `club register.html`
- `club.html`
- `home.html`
- `login.html`
- `user.html` via `user-profile.js`

Why this matters:

- execution must happen before page code expects `window.AuthSession` to exist
- converting to module loading could change when the global becomes visible
- moving the tag later in the document could change logout, token migration, or
  token write timing

### 2. Indirect session/storage dependency

These pages do not visibly call `window.AuthSession`, but they do read token state
through storage APIs that `auth-session.js` deliberately normalizes:

- `club bookings.html`
- `club chat.html`
- `club updates.html`
- `club-admin.html`
- `club-info.html`
- `clubs.html`
- `join.html`
- `onboarding.html`
- `onboarding-promo.html`
- `payment.html`
- `venue overview.html`

Why this matters:

- `auth-session.js` rewrites legacy token access from local storage to session storage
- the later page code assumes `localStorage.getItem('token')` reflects the migrated view
- changing load order could make early token reads observe a different storage state

### 3. No visible page-local dependency

These pages currently do not show a direct token or `window.AuthSession` use in their
page-local scripts:

- `onboarding-location.html`
- `onboarding-complete.html`
- `reset-password.html`

Why they still remain in scope:

- the project currently applies `auth-session.js` uniformly across the authenticated
  shell pages
- even pages without visible page-local calls may still benefit from token migration
  side effects before shared code or future modules run
- removing or moving the tag without a full page audit would create unnecessary drift

## Why Vite warns

Vite builds the frontend around the module graph. A classic external script tag such as
`<script src="auth-session.js?..."></script>` is outside that graph, so Vite reports:

- the script "can't be bundled without `type=\"module\"`"

Current behavior:

- the warning is non-blocking
- build output is still emitted successfully
- the raw HTML script tag is preserved, so runtime behavior remains intact

## Why the project currently keeps the classic script

The current delivery path is intentionally stability-first:

- `auth-session.js` must run before later page scripts read token state
- it mutates `Storage.prototype` early, not lazily
- it exposes `window.AuthSession` as a classic global, which multiple pages call
- a bulk switch to module loading would require re-checking execution order,
  top-level scope, and fallback storage behavior page by page

## Migration guidance for a future round

Do this first:

1. audit one page at a time for exact token reads, token writes, and global usage
2. separate direct `window.AuthSession` consumers from storage-only consumers
3. pick one low-risk page as a pilot and add page-specific regression coverage first

Do not do this first:

- do not batch-convert all `auth-session.js` tags to `type="module"`
- do not move the script tag later in the document
- do not change the `window.AuthSession` API shape before page-level audits are complete

Recommended pilot order:

1. start with a storage-only page that has a small inline script surface
2. validate build output and browser behavior
3. only then consider pages that call `window.AuthSession` directly
