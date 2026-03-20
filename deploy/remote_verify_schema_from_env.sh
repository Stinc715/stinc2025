#!/usr/bin/env bash
set -euo pipefail

env_file="/etc/club-portal.env"

if [[ ! -f "$env_file" ]]; then
  echo "[verify] Missing $env_file"
  exit 1
fi

read_env_kv() {
  local key="$1"
  local line
  line="$(grep -m1 "^${key}=" "$env_file" 2>/dev/null || true)"
  if [[ -z "$line" ]]; then
    return 1
  fi
  echo "${line#${key}=}"
}

DB_URL="$(read_env_kv DB_URL || true)"
DB_USERNAME="$(read_env_kv DB_USERNAME || true)"
DB_PASSWORD="$(read_env_kv DB_PASSWORD || true)"

if [[ -z "${DB_URL}" ]]; then
  DB_URL="$(read_env_kv SPRING_DATASOURCE_URL || true)"
fi
if [[ -z "${DB_USERNAME}" ]]; then
  DB_USERNAME="$(read_env_kv SPRING_DATASOURCE_USERNAME || true)"
fi
if [[ -z "${DB_PASSWORD}" ]]; then
  DB_PASSWORD="$(read_env_kv SPRING_DATASOURCE_PASSWORD || true)"
fi

if [[ -z "${DB_URL}" || -z "${DB_USERNAME}" || -z "${DB_PASSWORD}" ]]; then
  echo "[verify] Missing DB_URL/DB_USERNAME/DB_PASSWORD or SPRING_DATASOURCE_* in $env_file"
  exit 1
fi

url="${DB_URL#jdbc:mysql://}"
hostport="${url%%/*}"
db_with_params="${url#*/}"
db="${db_with_params%%\?*}"

host="${hostport%%:*}"
port="${hostport#*:}"
if [[ "$hostport" == "$host" ]]; then
  port="3306"
fi

export MYSQL_PWD="$DB_PASSWORD"

echo "[verify] Target: ${host}:${port}/${db}"
mysql -N -h "$host" -P "$port" -u "$DB_USERNAME" -D "$db" <<'SQL'
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    'password_reset_token',
    'profile_email_change_verification',
    'registration_email_verification'
  )
ORDER BY table_name;

SELECT column_name
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'user'
  AND column_name IN (
    'avatar_file_name',
    'avatar_mime_type',
    'avatar_updated_at',
    'session_version'
  )
ORDER BY column_name;
SQL
