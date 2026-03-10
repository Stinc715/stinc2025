#!/usr/bin/env bash
set -euo pipefail

TS="$(date +%Y%m%d%H%M%S)"
RELEASE_DIR="/home/ec2-user/deploy/releases/${TS}"
ARCHIVE="/home/ec2-user/deploy/dist-deploy-20260308.tgz"
FRONTEND_ROOT="/var/www/club-portal"
ENV_FILE="/etc/club-portal.env"

mkdir -p "$RELEASE_DIR"
tar -xzf "$ARCHIVE" -C "$RELEASE_DIR"
tar -czf "/home/ec2-user/deploy/backup-www-${TS}.tgz" -C "$FRONTEND_ROOT" .
rsync -a --delete "$RELEASE_DIR"/ "$FRONTEND_ROOT"/

if sudo grep -q '^APP_PUBLIC_BASE_URL=' "$ENV_FILE"; then
  sudo sed -i 's|^APP_PUBLIC_BASE_URL=.*|APP_PUBLIC_BASE_URL=https://club-portal.xyz|' "$ENV_FILE"
else
  echo 'APP_PUBLIC_BASE_URL=https://club-portal.xyz' | sudo tee -a "$ENV_FILE" >/dev/null
fi

/home/ec2-user/deploy/remote_deploy_backend.sh

echo '---frontend-check---'
grep -n 'auth-modal.js?v=20260308a' \
  "$FRONTEND_ROOT/home.html" \
  "$FRONTEND_ROOT/club.html" \
  "$FRONTEND_ROOT/onboarding.html" \
  "$FRONTEND_ROOT/user.html"

echo '---auth-modal-check---'
sed -n '1,20p' "$FRONTEND_ROOT/auth-modal.js"

echo '---backend-config---'
curl -fsS http://127.0.0.1:8080/api/public/config
echo

echo '---env-check---'
sudo grep '^APP_PUBLIC_BASE_URL=' "$ENV_FILE"
