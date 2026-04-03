#!/usr/bin/env bash
set -euo pipefail

ARCHIVE_PATH="${1:?missing archive path}"
DEPLOY_HOME="${DEPLOY_HOME:-$HOME/deploy}"
FRONTEND_ROOT="${FRONTEND_ROOT:-/var/www/club-portal}"
TS="$(date +%Y%m%d%H%M%S)"
RELEASE_DIR="${DEPLOY_HOME}/releases/${TS}"
NGINX_SRC="${DEPLOY_HOME}/nginx.conf"

mkdir -p "$RELEASE_DIR"
tar -xzf "$ARCHIVE_PATH" -C "$RELEASE_DIR"
tar -czf "${DEPLOY_HOME}/backup-www-${TS}.tgz" -C "$FRONTEND_ROOT" .
sudo rsync -a --delete "$RELEASE_DIR"/ "$FRONTEND_ROOT"/

if [[ -f "$NGINX_SRC" ]]; then
  if [[ -d /etc/nginx/sites-available ]]; then
    NGINX_DEST="/etc/nginx/sites-available/club-portal"
    sudo install -m 644 "$NGINX_SRC" "$NGINX_DEST"
    if [[ -d /etc/nginx/sites-enabled ]]; then
      sudo ln -sfn "$NGINX_DEST" /etc/nginx/sites-enabled/club-portal
    fi
  else
    NGINX_DEST="/etc/nginx/conf.d/club-portal.conf"
    sudo install -m 644 "$NGINX_SRC" "$NGINX_DEST"
  fi
  sudo nginx -t
  sudo systemctl reload nginx
fi

echo '---frontend-check---'
grep -n 'auth-session.js?v=' \
  "$FRONTEND_ROOT/home.html" \
  "$FRONTEND_ROOT/club.html" \
  "$FRONTEND_ROOT/onboarding.html" \
  "$FRONTEND_ROOT/user.html" || true

ls -1 "$FRONTEND_ROOT/assets" | grep 'desktop-consistency' || true

echo '---auth-session-check---'
sed -n '1,24p' "$FRONTEND_ROOT/auth-session.js"
