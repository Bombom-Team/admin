#!/usr/bin/env bash
set -euo pipefail

AWS_REGION="${AWS_REGION:-ap-northeast-2}"
PARAMETER_PATH="${PARAMETER_PATH:-/bom-bom/admin}"
DATABASE_PARAMETER_PATH="${DATABASE_PARAMETER_PATH:-/bom-bom/prod}"
DEPLOY_DIR="${DEPLOY_DIR:-$HOME/bombom-admin/deploy}"
ENV_FILE="${ENV_FILE:-$DEPLOY_DIR/.env}"
APPLICATION_YML_FILE="${APPLICATION_YML_FILE:-$DEPLOY_DIR/application.yml}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose-admin.yml}"

: "${DOCKER_IMAGE:?DOCKER_IMAGE is required}"
: "${PERSONAL_ACCESS_TOKEN:?PERSONAL_ACCESS_TOKEN is required}"
: "${MAIL_SERVER_INTERNAL_API_KEY:?MAIL_SERVER_INTERNAL_API_KEY is required}"

get_parameter() {
  local name="$1"
  aws ssm get-parameter \
    --name "$name" \
    --with-decryption \
    --query "Parameter.Value" \
    --output text \
    --region "$AWS_REGION"
}

if [[ ! "$DOCKER_IMAGE" =~ ^[0-9]{12}\.dkr\.ecr\.ap-northeast-2\.amazonaws\.com/[a-z0-9._/-]+:admin-[0-9a-f]{40}$ ]]; then
  echo "Invalid DOCKER_IMAGE: $DOCKER_IMAGE" >&2
  exit 1
fi

RDS_ENDPOINT="$(get_parameter "$DATABASE_PARAMETER_PATH/RDS_ENDPOINT")"
DATABASE="$(get_parameter "$DATABASE_PARAMETER_PATH/DATABASE")"
MYSQL_USER="$(get_parameter "$DATABASE_PARAMETER_PATH/MYSQL_USER")"
MYSQL_PASSWORD="$(get_parameter "$DATABASE_PARAMETER_PATH/MYSQL_PASSWORD")"

aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin \
    "${DOCKER_IMAGE%%/*}"

mkdir -p "$DEPLOY_DIR"

install -m 600 /dev/null "$APPLICATION_YML_FILE"
get_parameter "$PARAMETER_PATH/APPLICATION_YML" > "$APPLICATION_YML_FILE"
if [ ! -s "$APPLICATION_YML_FILE" ]; then
  echo "APPLICATION_YML is empty" >&2
  exit 1
fi

install -m 600 /dev/null "$ENV_FILE"
{
  printf 'DOCKER_IMAGE=%s\n' "$DOCKER_IMAGE"
  printf 'APPLICATION_YML_FILE=%s\n' "$APPLICATION_YML_FILE"
  printf 'RDS_ENDPOINT=%s\n' "$RDS_ENDPOINT"
  printf 'DATABASE=%s\n' "$DATABASE"
  printf 'MYSQL_USER=%s\n' "$MYSQL_USER"
  printf 'MYSQL_PASSWORD=%s\n' "$MYSQL_PASSWORD"
  printf 'PERSONAL_ACCESS_TOKEN=%s\n' "$PERSONAL_ACCESS_TOKEN"
  printf 'MAIL_SERVER_INTERNAL_API_KEY=%s\n' "$MAIL_SERVER_INTERNAL_API_KEY"
} > "$ENV_FILE"

docker compose \
  -f "$COMPOSE_FILE" \
  --env-file "$ENV_FILE" \
  config >/dev/null

docker compose \
  -f "$COMPOSE_FILE" \
  --env-file "$ENV_FILE" \
  down || true

docker compose \
  -f "$COMPOSE_FILE" \
  --env-file "$ENV_FILE" \
  up -d --remove-orphans --pull=always --force-recreate
