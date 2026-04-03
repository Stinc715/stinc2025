# Club Portal

Club Portal is a club portal / booking / chat / payment / membership / community Q&A project. The frontend is a static multi-page app built by Vite, the backend is Spring Boot, and the current delivery focus is desktop consistency.

This README is the repo entry point, local run entry point, and acceptance entry point. It does not repeat every deep detail; it tells reviewers and new machines what this project is, how to run it, how to test it, what counts as pass, and where the deeper docs live.

## Project summary

- `frontend/`: static HTML, CSS, and browser-side JavaScript entry pages
- `backend/`: Spring Boot API, auth, booking, payment, membership, chat, and Q&A logic
- `deploy/`: release scripts, schema, nginx config, and migrations
- `docs/`: implementation notes and desktop acceptance rules
- `tests/`: frontend regression tests and Playwright visual baselines
- `scripts/`: repo utility scripts
- `server/`: legacy fallback static server, not the main workflow

## Tech stack

- Frontend: Vite, static HTML, CSS, and browser-side JavaScript
- Backend: Spring Boot 3.2, Java 17, Spring Security, Spring Data JPA
- Testing: `node:test`, Playwright desktop visual regression, Maven/Spring Boot tests

## Supported acceptance scope

This repo currently optimizes for the following acceptance environment:

- Desktop only
- Chrome / Edge latest stable
- Browser zoom: `100%`
- Windows is the main acceptance environment
- Fixed desktop viewport baselines:
  - `1366 x 768`
  - `1440 x 900`
  - `1920 x 1080`

## Prerequisites

- Node.js `20+`
- npm `10+`
- JDK `17`
- Maven is bundled through the repo wrapper: [mvnw](./mvnw) and [mvnw.cmd](./mvnw.cmd)
- Playwright Chromium for screenshot regression

First-time setup:

```bash
npm ci
npx playwright install chromium
```

Portable zip handoff behavior:

- `npm run build`, `npm run dev`, and `npm run test:frontend:visual` now self-check `node_modules`
- if a zip was created on another OS and bundled a mismatched `node_modules`, the command reruns `npm ci` against `package-lock.json` before continuing
- frontend npm scripts invoke local Node entry files directly, so Unix execute bits on `node_modules/.bin/vite` and `node_modules/.bin/playwright` are not required
- backend npm entrypoints call `mvnw` through `sh` on Unix, so a missing execute bit on `mvnw` does not block the npm workflow
- `npm run test:frontend:visual` installs Playwright Chromium automatically if it is missing

Recommended handoff rule:

- do not rely on a bundled `node_modules` as the portable artifact
- ship source plus lockfiles; the supported reproduction path is to let the target machine reconcile dependencies for its own OS/arch on first run

Optional config template:

- copy values from [`.env.example`](./.env.example) into your shell, CI, or server env file
- local review can usually start with `SPRING_PROFILES_ACTIVE=dev` and no MySQL credentials

## Quick start

Fastest local path:

1. `npm ci`
2. `npm run dev:backend`
3. `npm run dev`

Fastest review path without starting local dev servers:

1. `npm ci`
2. `npm run test:frontend`
3. `npm run build`

## Local run

Fastest local path is frontend + backend dev profile with local H2.

### 1. Start the backend

```bash
npm run dev:backend
```

Equivalent direct wrapper command:

```bash
sh ./mvnw -f backend/pom.xml -Dspring-boot.run.profiles=dev spring-boot:run
```

PowerShell equivalent:

```powershell
.\mvnw.cmd -f backend/pom.xml -Dspring-boot.run.profiles=dev spring-boot:run
```

Backend default local URL:

- `http://localhost:8080`

### 2. Start the frontend

```bash
npm run dev
```

Frontend dev URL:

- `http://localhost:5173`

Common entry pages:

- public landing page: `http://localhost:5173/`
- club discovery: `http://localhost:5173/home.html`
- login / registration: `http://localhost:5173/login.html`
- public club page: `http://localhost:5173/club.html?id=<clubId>`

## Core commands

```bash
npm run dev
npm run dev:backend
npm run build
npm test
npm run test:frontend
npm run test:frontend:fast
npm run test:frontend:visual
npm run test:backend
npm run test:acceptance
npm run package:submission
npm run verify:submission
```

Meaning:

- daily fast regression: `npm test`
- quick frontend static regression: `npm run test:frontend` / `npm run test:frontend:fast`
- desktop visual regression: `npm run test:frontend:visual`
- backend regression: `npm run test:backend`
- full acceptance before submission or demo: `npm run test:acceptance`
- clean submission staging only: `npm run package:submission`
- final delivery self-check: `npm run verify:submission`

## Backend configuration overview

The most important runtime variables are:

- `JWT_SECRET`
- `APP_PUBLIC_BASE_URL`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `APP_MAIL_FROM`
- `GOOGLE_OAUTH_CLIENT_ID`
- `OPENAI_API_KEY`
- `APP_PAYMENTS_MODE`
- `STRIPE_SECRET_KEY`
- `STRIPE_WEBHOOK_SECRET`

Useful notes:

- local review can use `SPRING_PROFILES_ACTIVE=dev`
- production-style backend runs use MySQL and `prod`
- Stripe is implemented but default payment mode is still `VIRTUAL`

More detail:

- database setup: [backend/DATABASE_DEPLOYMENT.md](./backend/DATABASE_DEPLOYMENT.md)
- API contract: [backend/API_CONTRACT.md](./backend/API_CONTRACT.md)
- backend module guide: [backend/README.md](./backend/README.md)

## Desktop consistency and visual regression

Desktop consistency now uses the shared layer [frontend/desktop-consistency.css](./frontend/desktop-consistency.css).

Current visual baselines cover:

- `club.html`
- `club chat.html`
- `club home.html`
- `club home.html` updates iframe state
- `user.html`
- `login.html`

Important current truth:

- baselines use bundled local fonts and platform-neutral snapshot names
- visual regression covers key desktop pages, not every possible business state
- `club home.html` is no longer shell-only in visual tests, but not every embedded sub-page/state is locked yet

Detailed rules and acceptance notes:

- [docs/desktop-consistency-checklist.md](./docs/desktop-consistency-checklist.md)
- [tests/README.md](./tests/README.md)

## What counts as pass

A change is submission-ready when all of the following pass:

- `npm test`
- `npm run test:frontend:visual`
- `npm run build`

Shortcut:

```bash
npm run test:acceptance
```

Use this rule in practice:

- `npm test` for normal local iteration
- `npm run test:acceptance` before commit, release, review handoff, or demo
- `npm run verify:submission` before final packaging or delivery handoff
- if the repo was handed over as a raw zip from another machine, let the first `npm run build` or `npm run test:frontend:visual` complete its dependency self-repair before judging the package

## Clean submission package

Prepare a source-only handoff tree with:

```bash
npm run package:submission
```

The staged output is written to `artifacts/submission/club-portal-submission/`.

It keeps the reviewable source and wrapper files, and excludes local or platform-bound material such as:

- `.git/`
- `.vscode/`
- `node_modules/`
- `dist/`
- `backend/target/`
- logs, temporary archives, and `.pem` files

Each staged submission tree also includes:

- `SUBMISSION_MANIFEST.md` for package identity, scope, and freeze status
- `verification-report.md` / `verification-report.json` after `npm run verify:submission`, so the final handoff stays self-contained

For the final frozen delivery check, run:

```bash
npm run verify:submission
```

That command:

- regenerates the submission tree
- checks required and forbidden assets
- reruns the full fresh submission command chain
- writes `artifacts/submission/verification-report.md` and `artifacts/submission/verification-report.json`
- copies the verification report into the staged submission tree for standalone handoff
- restores a clean submission tree after verification

## Known limitations

- Desktop consistency is not guaranteed for Safari or Firefox.
- Mobile and tablet visual parity are not guaranteed.
- The project does not aim for pixel-identical rendering across every OS/browser combination.
- Visual baselines cover key desktop states, not every page/state pair in the product.
- `club home.html` still uses an iframe workspace architecture, which remains a more fragile integration seam than fully native single-page layouts.
- Some runtime/business acceptance is still verified by regression tests rather than full end-to-end visual coverage.

## Known technical debt

- `npm run build` still emits a non-blocking Vite warning for `auth-session.js` because it is intentionally kept as a classic external head script to preserve current storage-patch timing and `window.AuthSession` availability.
- Remaining complex-page `.btn` duplication is also intentionally retained. The current project favors stable page-local button semantics over forced sharing on pages with tighter state or layout coupling.
- Future cleanup should start with page-level dependency audits and a single-page pilot, not a batch migration of scripts or button systems.

## Deployment

Supported release entry point:

```powershell
powershell -ExecutionPolicy Bypass -File deploy\upload.ps1 `
  -ServerHost <server-host> `
  -ServerUser <server-user> `
  -SshKey <path-to-ssh-key>
```

Current release behavior:

- runs validation by default unless `-SkipTests` is explicitly provided
- builds frontend into `dist/`
- packages the backend JAR through the Maven wrapper entrypoint
- uploads build artifacts, schema, migrations, and nginx config
- applies remote release scripts

More detail:

- [deploy/README.md](./deploy/README.md)
- [docs/IMPLEMENTATION_GUIDE.md](./docs/IMPLEMENTATION_GUIDE.md)
- [docs/release-freeze-summary.md](./docs/release-freeze-summary.md)

## Documentation index

- final delivery guide: [docs/final-delivery-guide.md](./docs/final-delivery-guide.md)
- desktop consistency: [docs/desktop-consistency-checklist.md](./docs/desktop-consistency-checklist.md)
- implementation guide: [docs/IMPLEMENTATION_GUIDE.md](./docs/IMPLEMENTATION_GUIDE.md)
- API contract: [backend/API_CONTRACT.md](./backend/API_CONTRACT.md)
- database deployment: [backend/DATABASE_DEPLOYMENT.md](./backend/DATABASE_DEPLOYMENT.md)
- auth session loading audit: [docs/auth-session-loading-audit.md](./docs/auth-session-loading-audit.md)
- button system audit: [docs/button-system-audit.md](./docs/button-system-audit.md)
- release freeze summary: [docs/release-freeze-summary.md](./docs/release-freeze-summary.md)
- frontend module notes: [frontend/README.md](./frontend/README.md)
- backend module notes: [backend/README.md](./backend/README.md)
- deploy notes: [deploy/README.md](./deploy/README.md)
- tests guide: [tests/README.md](./tests/README.md)

## Notes

- `dist/` is build output and should not be edited directly.
- `server/` is a legacy fallback server and is not part of the main npm workflow or deployment path.
