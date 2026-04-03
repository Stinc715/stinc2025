# Tests

## Frontend static regression

```bash
npm run test:frontend
```

This checks source-level frontend constraints and regression assertions.

## Frontend visual regression

```bash
npm run test:frontend:visual
```

On a fresh machine or a zip handed over from another OS, this command may first rerun `npm ci` and install Playwright Chromium before the screenshots run.

To intentionally update snapshots:

```bash
npm run test:frontend:visual -- --update-snapshots
```

Notes:

- snapshots use stable, platform-neutral file names
- local bundled fonts are used to reduce screenshot drift
- `club home.html` visual coverage includes real embedded business pages, not placeholder iframe bodies
- the visual test entrypoint does not rely on execute bits from `node_modules/.bin`

Visual snapshots live in:

- `tests/frontend/desktop-visual.spec.mjs-snapshots/`

## Backend tests

```bash
npm run test:backend
```

## Full test pass

```bash
npm test
```

## Main test files

- `tests/frontend/regression.test.mjs`
- `tests/frontend/desktop-visual.spec.mjs`
