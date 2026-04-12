#!/usr/bin/env bash
set -euo pipefail

DEPLOY_HOME="${DEPLOY_HOME:-$HOME/deploy}"
APP_HOME="${APP_HOME:-$HOME/app}"
BACKEND_JAR_NAME="${BACKEND_JAR_NAME:-club-portal-backend-1.0-SNAPSHOT.jar}"
SERVICE_NAME="${SERVICE_NAME:-club-portal}"
BACKEND_HEALTHCHECK_URL="${BACKEND_HEALTHCHECK_URL:-http://127.0.0.1:8080/api/clubs}"

SRC="${DEPLOY_HOME}/${BACKEND_JAR_NAME}"
DEST="${APP_HOME}/${BACKEND_JAR_NAME}"
TS="$(date +%Y%m%d%H%M%S)"

if [[ ! -f "$SRC" ]]; then
  echo "[deploy] Missing backend jar at $SRC"
  exit 1
fi

echo "[deploy] Backing up current jar..."
if [[ -f "$DEST" ]]; then
  cp "$DEST" "$DEST.bak-$TS"
fi

echo "[deploy] Installing new jar..."
mv "$SRC" "$DEST"

echo "[deploy] Restarting systemd service..."
sudo systemctl restart "$SERVICE_NAME"

echo "[deploy] Waiting for backend..."
for i in {1..120}; do
  if curl -fsS "$BACKEND_HEALTHCHECK_URL" >/dev/null 2>&1; then
    echo "[deploy] Backend healthy."
    exit 0
  fi
  sleep 1
done

echo "[deploy] Backend did not become healthy in time."
sudo systemctl status "$SERVICE_NAME" --no-pager -l || true
sudo journalctl -u "$SERVICE_NAME" -n 120 --no-pager || true
exit 1
