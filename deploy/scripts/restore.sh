#!/bin/sh
set -eu

if [ "$#" -ne 1 ] || [ ! -f "$1" ]; then
  echo "usage: RESTORE_CONFIRM=YES $0 <backup.sql.gz>" >&2
  exit 1
fi
if [ "${RESTORE_CONFIRM:-}" != "YES" ]; then
  echo "restore replaces current data; set RESTORE_CONFIRM=YES after confirming a maintenance window" >&2
  exit 1
fi

ENV_FILE=${ENV_FILE:-.env}
test -f "$ENV_FILE" || { echo "environment file not found: $ENV_FILE" >&2; exit 1; }
set -a
. "$ENV_FILE"
set +a
: "${DB_NAME:?set DB_NAME}"
: "${DB_USERNAME:?set DB_USERNAME}"
: "${DB_PASSWORD:?set DB_PASSWORD}"

temporary=$(mktemp "${TMPDIR:-/tmp}/shike-restore.XXXXXX.sql")
trap 'rm -f "$temporary"' EXIT INT TERM
gzip -dc "$1" > "$temporary"
test -s "$temporary"
docker compose -f compose.prod.yml exec -T -e MYSQL_PWD="$DB_PASSWORD" mysql \
  mysql --default-character-set=utf8mb4 -u "$DB_USERNAME" "$DB_NAME" < "$temporary"
echo "restore completed from: $1"
