#!/usr/bin/env bash
set -euo pipefail

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-club_portal}"
DB_USERNAME_INPUT="${DB_USERNAME:-}"
DB_PASSWORD_INPUT="${DB_PASSWORD:-}"
DEPLOY_HOME="${DEPLOY_HOME:-$HOME/deploy}"
APP_HOME="${APP_HOME:-$HOME/app}"
SERVICE_USER="${SERVICE_USER:-$USER}"

APP_USER="${DB_USERNAME_INPUT:-club_portal_app}"
APP_PW="${DB_PASSWORD_INPUT}"
JWT_SECRET="${JWT_SECRET:-$(openssl rand -hex 32)}"
APP_PUBLIC_BASE_URL="${APP_PUBLIC_BASE_URL:-https://example.invalid}"
APP_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS="${APP_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS:-https://example.invalid}"

is_local_mysql_target=false
if [[ "$DB_HOST" == "127.0.0.1" || "$DB_HOST" == "localhost" ]]; then
  is_local_mysql_target=true
fi

if [[ "$is_local_mysql_target" == true ]]; then
  if [[ -z "$APP_PW" ]]; then
    APP_PW="$(openssl rand -base64 24 | tr -d '\n')"
  fi

  echo "[setup] Local MySQL target detected; applying schema..."
  sudo mysql -e "CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
  sudo mysql -D "$DB_NAME" < "${DEPLOY_HOME}/mysql_schema.sql"
  if compgen -G "${DEPLOY_HOME}/migrations/*.sql" > /dev/null; then
    for migration_path in "${DEPLOY_HOME}"/migrations/*.sql; do
      sudo mysql -D "$DB_NAME" < "$migration_path"
    done
  fi

  echo "[setup] Creating/refreshing local DB user..."
  sudo mysql -e "CREATE USER IF NOT EXISTS '${APP_USER}'@'localhost' IDENTIFIED BY '${APP_PW}';"
  sudo mysql -e "CREATE USER IF NOT EXISTS '${APP_USER}'@'127.0.0.1' IDENTIFIED BY '${APP_PW}';"
  sudo mysql -e "ALTER USER '${APP_USER}'@'localhost' IDENTIFIED BY '${APP_PW}';"
  sudo mysql -e "ALTER USER '${APP_USER}'@'127.0.0.1' IDENTIFIED BY '${APP_PW}';"
  sudo mysql -e "GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${APP_USER}'@'localhost';"
  sudo mysql -e "GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${APP_USER}'@'127.0.0.1';"
  sudo mysql -e "FLUSH PRIVILEGES;"
else
  echo "[setup] Remote MySQL target detected at ${DB_HOST}:${DB_PORT}/${DB_NAME}."
  if [[ -z "$DB_USERNAME_INPUT" || -z "$DB_PASSWORD_INPUT" ]]; then
    echo "[setup] For remote MySQL targets, export DB_USERNAME and DB_PASSWORD before running this script."
    exit 1
  fi
  echo "[setup] Skipping local MySQL provisioning."
fi

DB_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC"

echo "[setup] Writing /etc/club-portal.env ..."
sudo tee /etc/club-portal.env >/dev/null <<EOF
SPRING_PROFILES_ACTIVE=prod
DB_URL=$DB_URL
DB_USERNAME=$APP_USER
DB_PASSWORD=$APP_PW
SPRING_DATASOURCE_URL=$DB_URL
SPRING_DATASOURCE_USERNAME=$APP_USER
SPRING_DATASOURCE_PASSWORD=$APP_PW
JWT_SECRET=$JWT_SECRET
APP_PUBLIC_BASE_URL=$APP_PUBLIC_BASE_URL
APP_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS=$APP_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS
EOF
sudo chmod 600 /etc/club-portal.env

echo "[setup] Updating systemd service to load env file..."
sudo tee /etc/systemd/system/club-portal.service >/dev/null <<'EOF'
[Unit]
Description=Club Portal Backend
After=network.target

[Service]
Type=simple
User=$SERVICE_USER
WorkingDirectory=$APP_HOME
EnvironmentFile=/etc/club-portal.env
ExecStart=/usr/bin/java -jar $APP_HOME/club-portal-backend-1.0-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload

echo "[setup] Done. Environment is configured for the prod profile."
