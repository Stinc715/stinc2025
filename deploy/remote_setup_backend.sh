#!/usr/bin/env bash
set -euo pipefail

echo "[setup] Applying MySQL schema (if needed)..."
sudo mysql -e "CREATE DATABASE IF NOT EXISTS \`club_portal\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
sudo mysql -D club_portal < /home/ec2-user/deploy/mysql_schema.sql

APP_USER="club_portal_app"
APP_PW="$(openssl rand -base64 24 | tr -d '\n')"
JWT_SECRET="$(openssl rand -hex 32)"

DB_URL="jdbc:mysql://127.0.0.1:3306/club_portal?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC"

echo "[setup] Creating/refreshing DB user..."
sudo mysql -e "CREATE USER IF NOT EXISTS '${APP_USER}'@'localhost' IDENTIFIED BY '${APP_PW}';"
sudo mysql -e "CREATE USER IF NOT EXISTS '${APP_USER}'@'127.0.0.1' IDENTIFIED BY '${APP_PW}';"
sudo mysql -e "ALTER USER '${APP_USER}'@'localhost' IDENTIFIED BY '${APP_PW}';"
sudo mysql -e "ALTER USER '${APP_USER}'@'127.0.0.1' IDENTIFIED BY '${APP_PW}';"
sudo mysql -e "GRANT ALL PRIVILEGES ON club_portal.* TO '${APP_USER}'@'localhost';"
sudo mysql -e "GRANT ALL PRIVILEGES ON club_portal.* TO '${APP_USER}'@'127.0.0.1';"
sudo mysql -e "FLUSH PRIVILEGES;"

echo "[setup] Writing /etc/club-portal.env ..."
sudo tee /etc/club-portal.env >/dev/null <<EOF
DB_URL=$DB_URL
DB_USERNAME=$APP_USER
DB_PASSWORD=$APP_PW
JWT_SECRET=$JWT_SECRET
EOF
sudo chmod 600 /etc/club-portal.env

echo "[setup] Updating systemd service to load env file..."
sudo tee /etc/systemd/system/club-portal.service >/dev/null <<'EOF'
[Unit]
Description=Club Portal Backend
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/app
EnvironmentFile=/etc/club-portal.env
ExecStart=/usr/bin/java -jar /home/ec2-user/app/club-portal-backend-1.0-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload

echo "[setup] Done. (Secrets are stored in /etc/club-portal.env)"
