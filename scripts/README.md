# Scripts

## `check-prompts.mjs`

This script guards the repository rule that frontend flows must not rely on native
browser `alert`, `confirm`, or `prompt` calls outside the dedicated custom prompt layer.

Run it with:

```bash
npm run check:prompts
```

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
