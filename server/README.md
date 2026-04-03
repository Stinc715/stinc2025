# Legacy Local Server

This folder contains an older Node-based static server and `/api` proxy.

## Current status

- not used by the root npm scripts
- not used by frontend regression tests
- not used by the deployment flow
- kept only as a fallback local preview option

## Preferred local workflow

Use the main repo scripts instead:

```bash
npm run dev
```

## If you still need this server

```bash
node server/server.js
```

Defaults:

- serves files from `frontend/`
- listens on `http://localhost:5173`
- proxies `/api` to `http://localhost:8080` unless `API_TARGET` is set

If the team no longer needs this fallback, this folder is a candidate for full removal rather than partial cleanup.
