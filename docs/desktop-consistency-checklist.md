# Desktop Consistency Checklist

This checklist defines the desktop-only consistency scope for the current Club Portal frontend. It is intentionally conservative: the goal is to stabilize layout and visual acceptance in fixed desktop environments without changing business behavior.

## Supported acceptance environment

- Desktop only
- Chrome / Edge latest stable
- Browser zoom: 100%
- Fixed viewport baselines:
  - `1366 x 768`
  - `1440 x 900`
  - `1920 x 1080`

For screenshot regression, the repo now uses:

- platform-neutral snapshot file names
- bundled local fonts from `frontend/assets/fonts`
- mocked API data for stability

Out of scope for this checklist:

- Mobile or tablet parity
- Firefox / Safari parity
- Pixel-perfect cross-browser equivalence
- Replacing the existing page-based or iframe-based architecture

## Shell tiers

The desktop consistency layer uses three opt-in shell tiers in [`frontend/desktop-consistency.css`](../frontend/desktop-consistency.css):

- `desktop-shell--wide`
  - used by wide workspace pages such as `club.html`, `user.html`, `club home.html`
- `desktop-shell--standard`
  - used by dashboard-style pages such as `club chat.html`, `club updates.html`
- `desktop-shell--narrow`
  - used by stable form pages such as `login.html`, `payment.html`, `reset-password.html`

Shared sizing tokens:

- `--page-max-wide`
- `--page-max-standard`
- `--page-max-narrow`
- `--page-side-padding`
- `--card-gap`
- `--rail-width`
- `--header-height`
- `--control-height`
- `--radius-md`
- `--radius-lg`

## Included pages

The desktop consistency layer is now imported by all `frontend/*.html` entry pages:

- `club bookings.html`
- `club chat.html`
- `club home.html`
- `club register.html`
- `club updates.html`
- `club.html`
- `club-admin.html`
- `club-info.html`
- `clubs.html`
- `home.html`
- `index.html`
- `join.html`
- `login.html`
- `onboarding.html`
- `onboarding-complete.html`
- `onboarding-location.html`
- `onboarding-promo.html`
- `payment.html`
- `reset-password.html`
- `user.html`
- `venue overview.html`

Shell tiers and shared classes are still applied selectively per page, but the consistency layer itself is no longer limited to a small opt-in subset.

## Screenshot regression baselines

Playwright visual baselines currently cover:

- `club.html` at `1366 x 768`
- `club.html` at `1920 x 1080`
- `club chat.html` at `1440 x 900`
- `user.html` at `1440 x 900`
- `login.html` at `1366 x 768`
- `club home.html` at `1920 x 1080`
- `club home.html` updates iframe at `1920 x 1080`

Notes:

- Baselines use fixed mocked data to avoid API drift, unread-count drift, and realtime changes.
- `club home.html` coverage now includes real embedded business pages, not placeholder iframe bodies.
- Snapshot file names no longer use platform-specific suffixes.

## Acceptance checklist

For every covered page, verify:

1. Header position is stable.
2. Primary card width is stable.
3. Rail behavior matches the viewport size.
4. Buttons and inputs align cleanly.
5. Copy does not wrap too early.
6. Only expected scrollbars appear.
7. No oversized whitespace or detached sections appear on wide screens.
8. No clipping, overlap, or horizontal overflow appears at 100% zoom.

## Commands

Static source regression:

```bash
npm run test:frontend:fast
```

Build output:

```bash
npm run build
```

Desktop screenshot regression:

```bash
npm run test:frontend:visual
```

If the screenshots intentionally change:

```bash
npm run test:frontend:visual -- --update-snapshots
```

Submission / release acceptance command:

```bash
npm run test:acceptance
```

Portable handoff note:

- if the repo arrives as a raw zip from another OS, `npm run build` and `npm run test:frontend:visual` will repair a mismatched `node_modules` with `npm ci` before running
- the visual entrypoint also installs Playwright Chromium automatically when it is missing

Policy:

- `npm test` is the fast default regression path.
- `npm run test:acceptance` is the required full desktop acceptance path because it includes visual baselines.
