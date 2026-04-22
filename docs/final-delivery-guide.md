# Final Delivery Guide

## Project overview

Club Portal is a club-focused portal project that covers user sign-in and registration,
member and club home pages, bookings, payments, chat, Community Q&A, and onboarding.

This submission uses a separated frontend/backend multi-page architecture:

- frontend: static multi-page frontend built with Vite
- backend: business APIs and authentication built with Spring Boot 3.2 / Java 17
- tests: frontend static regression, desktop visual regression, and backend regression
- scripts: packaging, self-verification, and cross-platform runtime helpers

## What this submission already completes

This submission already completes and verifies the following:

- end-to-end core frontend/backend feature coverage
- clean handoff packaging that produces a source-only submission tree
- reproducible build and test flow in a fresh submission directory when first-run
  network access is available for Playwright Chromium and Maven Wrapper downloads
- shared desktop consistency styling
- Playwright desktop visual regression
- automated submission self-verification via `npm run verify:submission`
- audit notes, freeze notes, and low-risk regression guardrails

## Supported acceptance scope

The supported acceptance scope for this submission is:

- Desktop only
- Chrome / Edge latest stable
- Browser zoom 100%
- Windows is the main review environment
- Fixed viewport baselines:
  - 1366x768
  - 1440x900
  - 1920x1080

## How to validate this handoff

For review or handoff, start with these commands:

- `npm run check:prompts`
  - purpose: verify that frontend flows use the shared prompt layer instead of native browser prompts

- `npm run test:acceptance`
  - purpose: run the full acceptance chain in the current repository, covering frontend static regression, backend regression, and build
- `npm run verify:submission`
  - purpose: regenerate the submission tree and run `npm ci`, `npm run check:prompts`,
    `npm run build`, frontend regression, visual regression, backend regression,
    and acceptance inside that fresh submission directory, while producing a
    self-contained verification report

Important clarification: "fresh submission reproducible" does not mean "first run is fully offline".

- the review machine still needs Java 17 installed first; Maven Wrapper downloads and fixes Maven, but it does not replace Java itself
- the first full acceptance run needs network access to fetch Playwright Chromium and the Maven distribution used by Maven Wrapper
- this affects `npm run test:frontend:visual`, `npm run test:backend`, `npm run test:acceptance`, and `npm run verify:submission`
- later runs can reuse local caches; once those caches exist, the same commands can be repeated offline
- therefore a PASS result in the delivery report means the machine either already had those caches or was allowed to perform the first-run downloads

## Intentionally retained items

The items below are intentionally frozen after audit and are not treated as unfinished work:

- `auth-session.js` still triggers a classic-script warning during Vite build, but it is a known non-blocking warning
- high-risk complex-page `.btn` systems remain page-local instead of being force-merged

These remain frozen because stability, zero functional regression, and zero visual regression are currently more important than further cleanup in those high-risk areas.

## Document index

- repo entry point and run guide: `README.md`
- desktop consistency checklist: `docs/desktop-consistency-checklist.md`
- auth-session loading audit: `docs/auth-session-loading-audit.md`
- button system audit: `docs/button-system-audit.md`
- release freeze summary: `docs/release-freeze-summary.md`
- API contract: `backend/API_CONTRACT.md`
- backend guide: `backend/README.md`
- deploy guide: `deploy/README.md`
- tests guide: `tests/README.md`
 
