#!/usr/bin/env bash
set -euo pipefail

# Apply the staged schema to the DB configured in the backend environment file.
# Works for both local MySQL and AWS RDS.
# NOTE: /etc/club-portal.env is a systemd EnvironmentFile, not a shell script.
# It may contain characters like '&' in values (e.g. JDBC query params), so we MUST NOT `source` it.

env_file="${ENV_FILE:-/etc/club-portal.env}"
deploy_home="${DEPLOY_HOME:-$HOME/deploy}"

if [[ ! -f "$env_file" ]]; then
  echo "[schema] Missing environment file: $env_file"
  exit 1
fi

read_env_kv() {
  local key="$1"
  local line
  if [[ -r "$env_file" ]]; then
    line="$(grep -m1 "^${key}=" "$env_file" 2>/dev/null || true)"
  else
    line="$(sudo grep -m1 "^${key}=" "$env_file" 2>/dev/null || true)"
  fi
  if [[ -z "$line" ]]; then
    return 1
  fi
  # Strip only the leading "KEY=" and keep the remainder verbatim.
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
  echo "[schema] Missing DB_URL/DB_USERNAME/DB_PASSWORD or SPRING_DATASOURCE_* in $env_file"
  exit 1
fi

schema_path="$deploy_home/mysql_schema.sql"
if [[ ! -f "$schema_path" ]]; then
  echo "[schema] Missing schema file: $schema_path"
  exit 1
fi

migration_dir="$deploy_home/migrations"

# Parse: jdbc:mysql://HOST:PORT/DB?params
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

echo "[schema] Target: ${host}:${port}/${db}"
echo "[schema] Applying schema..."
mysql -h "$host" -P "$port" -u "$DB_USERNAME" -D "$db" < "$schema_path"

if [[ -d "$migration_dir" ]]; then
  shopt -s nullglob
  migration_files=("$migration_dir"/*.sql)
  shopt -u nullglob
  for migration_path in "${migration_files[@]}"; do
    echo "[schema] Applying migration $(basename "$migration_path")..."
    mysql -h "$host" -P "$port" -u "$DB_USERNAME" -D "$db" < "$migration_path"
  done
fi

echo "[schema] Done."
