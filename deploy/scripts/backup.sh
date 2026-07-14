#!/bin/sh
set -eu

ENV_FILE=${ENV_FILE:-.env}
test -f "$ENV_FILE" || { echo "environment file not found: $ENV_FILE" >&2; exit 1; }
set -a
. "$ENV_FILE"
set +a
: "${DB_NAME:?set DB_NAME}"
: "${DB_USERNAME:?set DB_USERNAME}"
: "${DB_PASSWORD:?set DB_PASSWORD}"

BACKUP_DIR=${BACKUP_DIR:-./backups}
BACKUP_RETENTION_DAYS=${BACKUP_RETENTION_DAYS:-14}
timestamp=$(date +%Y%m%d_%H%M%S)
target="$BACKUP_DIR/${DB_NAME}_${timestamp}.sql.gz"
temporary="$BACKUP_DIR/.${DB_NAME}_${timestamp}.sql"

umask 077
mkdir -p "$BACKUP_DIR"
trap 'rm -f "$temporary"' EXIT INT TERM
docker compose -f compose.prod.yml exec -T -e MYSQL_PWD="$DB_PASSWORD" mysql \
  mysqldump --single-transaction --routines --triggers --default-character-set=utf8mb4 \
  -u "$DB_USERNAME" "$DB_NAME" > "$temporary"
gzip -c "$temporary" > "$target"
test -s "$target"
find "$BACKUP_DIR" -type f -name "${DB_NAME}_*.sql.gz" -mtime "+$BACKUP_RETENTION_DAYS" -delete
echo "backup created: $target"
