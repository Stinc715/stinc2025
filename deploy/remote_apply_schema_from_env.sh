#!/usr/bin/env bash
set -euo pipefail

# Apply /home/ec2-user/deploy/mysql_schema.sql to the DB configured in /etc/club-portal.env
# Works for both local MySQL and AWS RDS.
# NOTE: /etc/club-portal.env is a systemd EnvironmentFile, not a shell script.
# It may contain characters like '&' in values (e.g. JDBC query params), so we MUST NOT `source` it.

env_file="/etc/club-portal.env"

if [[ ! -f "$env_file" ]]; then
  echo "[schema] Missing /etc/club-portal.env"
  exit 1
fi

read_env_kv() {
  local key="$1"
  local line
  line="$(grep -m1 "^${key}=" "$env_file" 2>/dev/null || true)"
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
  echo "[schema] Missing DB_URL/DB_USERNAME/DB_PASSWORD or SPRING_DATASOURCE_* in /etc/club-portal.env"
  exit 1
fi

schema_path="/home/ec2-user/deploy/mysql_schema.sql"
if [[ ! -f "$schema_path" ]]; then
  echo "[schema] Missing schema file: $schema_path"
  exit 1
fi

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

echo "[schema] Done."
