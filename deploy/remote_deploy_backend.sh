#!/usr/bin/env bash
set -euo pipefail

SRC="/home/ec2-user/deploy/club-portal-backend-1.0-SNAPSHOT.jar"
DEST="/home/ec2-user/app/club-portal-backend-1.0-SNAPSHOT.jar"
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
sudo systemctl restart club-portal

echo "[deploy] Waiting for backend..."
for i in {1..30}; do
  if curl -fsS http://127.0.0.1:8080/api/clubs >/dev/null 2>&1; then
    echo "[deploy] Backend healthy."
    exit 0
  fi
  sleep 1
done

echo "[deploy] Backend did not become healthy in time."
sudo systemctl status club-portal --no-pager -l || true
sudo journalctl -u club-portal -n 120 --no-pager || true
exit 1

