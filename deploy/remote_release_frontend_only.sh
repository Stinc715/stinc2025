#!/usr/bin/env bash
set -euo pipefail

ARCHIVE_PATH="${1:?missing archive path}"
DEPLOY_HOME="${DEPLOY_HOME:-$HOME/deploy}"
FRONTEND_ROOT="${FRONTEND_ROOT:-/var/www/club-portal}"
ENV_FILE="${ENV_FILE:-/etc/club-portal.env}"
PUBLIC_BASE_URL="${APP_PUBLIC_BASE_URL:-}"
TS="$(date +%Y%m%d%H%M%S)"
RELEASE_DIR="${DEPLOY_HOME}/releases/${TS}"
NGINX_SRC="${DEPLOY_HOME}/nginx.conf"

read_public_base_url() {
  if [[ -n "$PUBLIC_BASE_URL" ]]; then
    printf '%s\n' "$PUBLIC_BASE_URL"
    return 0
  fi

  if [[ -f "$ENV_FILE" ]]; then
    grep '^APP_PUBLIC_BASE_URL=' "$ENV_FILE" | tail -n 1 | cut -d= -f2- || true
    return 0
  fi

  printf '\n'
}

derive_public_host() {
  local public_base_url
  public_base_url="$(read_public_base_url)"
  public_base_url="${public_base_url#http://}"
  public_base_url="${public_base_url#https://}"
  public_base_url="${public_base_url%%/*}"
  public_base_url="${public_base_url%%:*}"
  printf '%s\n' "${public_base_url#www.}"
}

render_nginx_config() {
  local template_path="$1"
  local output_path="$2"
  local primary_host="$3"
  local www_host="www.${primary_host#www.}"
  local cert_host="${primary_host#www.}"

  sed \
    -e "s|www\.example\.invalid|${www_host}|g" \
    -e "s|example\.invalid|${primary_host}|g" \
    -e "s|/live/${primary_host}/|/live/${cert_host}/|g" \
    "$template_path" > "$output_path"
}

mkdir -p "$RELEASE_DIR"
tar -xzf "$ARCHIVE_PATH" -C "$RELEASE_DIR"
tar -czf "${DEPLOY_HOME}/backup-www-${TS}.tgz" -C "$FRONTEND_ROOT" .
sudo rsync -a --delete "$RELEASE_DIR"/ "$FRONTEND_ROOT"/

if [[ -f "$NGINX_SRC" ]]; then
  NGINX_RENDERED="${RELEASE_DIR}/nginx.rendered.conf"
  PUBLIC_HOST="$(derive_public_host)"
  if [[ -n "$PUBLIC_HOST" ]]; then
    render_nginx_config "$NGINX_SRC" "$NGINX_RENDERED" "$PUBLIC_HOST"
    NGINX_INSTALL_SRC="$NGINX_RENDERED"
  else
    echo "[deploy] APP_PUBLIC_BASE_URL is not configured; skipping nginx template install."
    NGINX_INSTALL_SRC=""
  fi
fi

if [[ -n "${NGINX_INSTALL_SRC:-}" ]]; then
  if [[ -d /etc/nginx/sites-available ]]; then
    NGINX_DEST="/etc/nginx/sites-available/club-portal"
    sudo install -m 644 "$NGINX_INSTALL_SRC" "$NGINX_DEST"
    if [[ -d /etc/nginx/sites-enabled ]]; then
      sudo ln -sfn "$NGINX_DEST" /etc/nginx/sites-enabled/club-portal
    fi
  else
    NGINX_DEST="/etc/nginx/conf.d/club-portal.conf"
    sudo install -m 644 "$NGINX_INSTALL_SRC" "$NGINX_DEST"
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
