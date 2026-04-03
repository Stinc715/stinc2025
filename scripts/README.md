# Scripts

## `check-prompts.mjs`

This script scans the frontend source and build output for native browser dialogs:

- `window.alert`
- `window.confirm`
- `window.prompt`

Run it with:

```bash
npm run check:prompts
```

The intended standard is to use the app prompt layer instead of native browser dialogs.

## `prepare-submission.mjs`

This script creates a clean source-only submission tree under `artifacts/submission/`.

Run it with:

```bash
npm run package:submission
```

The staged tree intentionally excludes local or platform-bound material such as:

- `.git/`
- `.vscode/`
- `node_modules/`
- `dist/`
- `backend/target/`
- logs, archives, and `.pem` files
