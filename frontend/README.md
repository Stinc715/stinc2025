# Frontend

## What lives here

The frontend is a Vite-built multi-page app made from static HTML, CSS, and browser-side JavaScript.

Every HTML entry page in this folder is included in the Vite build.

Current entry pages:

- `index.html`
- `home.html`
- `clubs.html`
- `club.html`
- `join.html`
- `login.html`
- `user.html`
- `payment.html`
- `reset-password.html`
- `club register.html`
- `club home.html`
- `club-info.html`
- `club-admin.html`
- `club bookings.html`
- `club updates.html`
- `club chat.html`
- `venue overview.html`
- `onboarding.html`
- `onboarding-location.html`
- `onboarding-promo.html`
- `onboarding-complete.html`

## Local development

From the repo root:

```bash
npm run dev
```

Defaults:

- frontend dev server: `http://localhost:5173`
- backend API target: `http://localhost:8080`

To point the frontend proxy at a different backend:

PowerShell:

```powershell
$env:API_TARGET="http://localhost:8081"
npm run dev
```

bash:

```bash
API_TARGET=http://localhost:8081 npm run dev
```

## Build

```bash
npm run build
```

Output goes to `../dist`.

Local preview:

```bash
npm run preview
```

## Desktop consistency layer

Desktop layout tokens and shared shell rules live in:

- [`desktop-consistency.css`](./desktop-consistency.css)

Bundled local fonts live in:

- [`local-fonts.css`](./local-fonts.css)
- [`assets/fonts/`](./assets/fonts/)

This avoids external Google Fonts dependencies during visual regression.

## Important implementation notes

- `club home.html` is still the club workspace shell.
- It embeds business pages such as `club-info.html`, `club-admin.html`, `club bookings.html`, `club updates.html`, and `club chat.html` inside `iframe`.
- `user-profile.js` holds shared account-center behavior used by `user.html`.
- Do not edit `dist/`; it is generated output.

## Related docs

- [../README.md](../README.md)
- [../docs/desktop-consistency-checklist.md](../docs/desktop-consistency-checklist.md)
- [../tests/README.md](../tests/README.md)
