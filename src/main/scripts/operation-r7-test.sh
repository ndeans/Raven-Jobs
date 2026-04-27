#!/usr/bin/env bash
# operation-r7-test.sh — Runner for Raven Operation R7 (Reconstruct Conversation Thread).
# Usage: operation-r7-test.sh <upload_id>

set -uo pipefail

if [[ "$#" -ne 1 ]]; then
  echo "Usage: operation-r7-test.sh <upload_id>" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OPS_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

JAR_PATH="${RAVEN_JAR_PATH:-$OPS_ROOT/lib/raven-jobs-1.0-SNAPSHOT.jar}"
LOG_DIR="${RAVEN_LOG_DIR:-$OPS_ROOT/logs}"
LOG_LEVEL="${RAVEN_LOG_LEVEL:-INFO}"
RAVEN_ENV_VALUE="${RAVEN_ENV:-dev}"
RUN_ID="$(date -u +%Y%m%dT%H%M%SZ)"
LOG_BASENAME="operation-r7_${RUN_ID}"
RUNNER_LOG_FILE="$LOG_DIR/operation-r7_runner.ndjson"
UPLOAD_ID="$1"

mkdir -p "$LOG_DIR"

if [[ ! -f "$JAR_PATH" ]]; then
  printf '{"@timestamp":"%s","app":"raven-jobs","script":"operation-r7-test.sh","operation":"R7","run_id":"%s","event":"startup","status":"failed","error":"jar_not_found","jar_path":"%s"}\n' \
    "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$RUN_ID" "$JAR_PATH" >> "$RUNNER_LOG_FILE"
  echo "ERROR: JAR not found at $JAR_PATH" >&2
  exit 1
fi

printf '{"@timestamp":"%s","app":"raven-jobs","script":"operation-r7-test.sh","operation":"R7","run_id":"%s","event":"startup","status":"ok","jar_path":"%s","log_dir":"%s","upload_id":"%s"}\n' \
  "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$RUN_ID" "$JAR_PATH" "$LOG_DIR" "$UPLOAD_ID" >> "$RUNNER_LOG_FILE"

java \
  -Draven.operation=R7 \
  -Draven.run.id="$RUN_ID" \
  -Draven.env="$RAVEN_ENV_VALUE" \
  -Draven.log.level="$LOG_LEVEL" \
  -Draven.log.dir="$LOG_DIR" \
  -Draven.log.file.basename="$LOG_BASENAME" \
  -jar "$JAR_PATH" \
  R7 "$UPLOAD_ID"

EXIT_CODE=$?

printf '{"@timestamp":"%s","app":"raven-jobs","script":"operation-r7-test.sh","operation":"R7","run_id":"%s","event":"completion","status":"done","exit_code":%d,"upload_id":"%s","app_log_file":"%s/%s.ndjson"}\n' \
  "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$RUN_ID" "$EXIT_CODE" "$UPLOAD_ID" "$LOG_DIR" "$LOG_BASENAME" >> "$RUNNER_LOG_FILE"

exit "$EXIT_CODE"
