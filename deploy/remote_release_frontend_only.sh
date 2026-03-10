#!/usr/bin/env bash
set -euo pipefail

ARCHIVE_PATH="${1:?missing archive path}"
TS="$(date +%Y%m%d%H%M%S)"
RELEASE_DIR="/home/ec2-user/deploy/releases/${TS}"
FRONTEND_ROOT="/var/www/club-portal"

mkdir -p "$RELEASE_DIR"
tar -xzf "$ARCHIVE_PATH" -C "$RELEASE_DIR"
tar -czf "/home/ec2-user/deploy/backup-www-${TS}.tgz" -C "$FRONTEND_ROOT" .
rsync -a --delete "$RELEASE_DIR"/ "$FRONTEND_ROOT"/

echo '---frontend-check---'
grep -n 'auth-modal.js?v=' \
  "$FRONTEND_ROOT/home.html" \
  "$FRONTEND_ROOT/club.html" \
  "$FRONTEND_ROOT/onboarding.html" \
  "$FRONTEND_ROOT/user.html"

echo '---auth-modal-check---'
sed -n '1,24p' "$FRONTEND_ROOT/auth-modal.js"
