#!/usr/bin/env bash
set -euo pipefail

env_file="/etc/club-portal.env"

if [[ ! -f "$env_file" ]]; then
  echo "[smtp] Missing $env_file"
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

MAIL_HOST="$(read_env_kv SPRING_MAIL_HOST || true)"
MAIL_PORT="$(read_env_kv SPRING_MAIL_PORT || true)"
MAIL_USERNAME="$(read_env_kv SPRING_MAIL_USERNAME || true)"
MAIL_PASSWORD="$(read_env_kv SPRING_MAIL_PASSWORD || true)"
MAIL_FROM="$(read_env_kv APP_MAIL_FROM || true)"

if [[ -z "${MAIL_HOST}" || -z "${MAIL_PORT}" || -z "${MAIL_USERNAME}" || -z "${MAIL_PASSWORD}" ]]; then
  echo "[smtp] Missing SPRING_MAIL_* credentials in $env_file"
  exit 1
fi

echo "[smtp] Checking ${MAIL_HOST}:${MAIL_PORT} as ${MAIL_USERNAME} from ${MAIL_FROM:-<unset>}"

SMTP_HOST="$MAIL_HOST" \
SMTP_PORT="$MAIL_PORT" \
SMTP_USERNAME="$MAIL_USERNAME" \
SMTP_PASSWORD="$MAIL_PASSWORD" \
python3 <<'PY'
import os
import smtplib
import ssl

host = os.environ["SMTP_HOST"]
port = int(os.environ["SMTP_PORT"])
username = os.environ["SMTP_USERNAME"]
password = os.environ["SMTP_PASSWORD"]

server = smtplib.SMTP(host, port, timeout=15)
server.ehlo()
if port == 587:
    context = ssl.create_default_context()
    server.starttls(context=context)
    server.ehlo()
server.login(username, password)
server.quit()
print("[smtp] Authentication succeeded.")
PY
