#!/usr/bin/env bash
# operation-m1.sh — Runner for Raven Operation M1 (Remove Duplicates).

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OPS_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

JAR_PATH="${RAVEN_JAR_PATH:-$OPS_ROOT/lib/raven-jobs-1.0-SNAPSHOT.jar}"
LOG_DIR="${RAVEN_LOG_DIR:-$OPS_ROOT/logs}"
LOG_LEVEL="${RAVEN_LOG_LEVEL:-INFO}"
RAVEN_ENV_VALUE="${RAVEN_ENV:-dev}"
RUN_ID="$(date -u +%Y%m%dT%H%M%SZ)"
LOG_BASENAME="operation-m1_${RUN_ID}"
RUNNER_LOG_FILE="$LOG_DIR/operation-m1_runner.ndjson"

mkdir -p "$LOG_DIR"

if [[ ! -f "$JAR_PATH" ]]; then
  printf '{"@timestamp":"%s","app":"raven-jobs","script":"operation-m1.sh","operation":"M1","run_id":"%s","event":"startup","status":"failed","error":"jar_not_found","jar_path":"%s"}\n' \
    "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$RUN_ID" "$JAR_PATH" >> "$RUNNER_LOG_FILE"
  echo "ERROR: JAR not found at $JAR_PATH" >&2
  exit 1
fi

printf '{"@timestamp":"%s","app":"raven-jobs","script":"operation-m1.sh","operation":"M1","run_id":"%s","event":"startup","status":"ok","jar_path":"%s","log_dir":"%s"}\n' \
  "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$RUN_ID" "$JAR_PATH" "$LOG_DIR" >> "$RUNNER_LOG_FILE"

java \
  -Draven.operation=M1 \
  -Draven.run.id="$RUN_ID" \
  -Draven.env="$RAVEN_ENV_VALUE" \
  -Draven.log.level="$LOG_LEVEL" \
  -Draven.log.dir="$LOG_DIR" \
  -Draven.log.file.basename="$LOG_BASENAME" \
  -jar "$JAR_PATH" \
  M1

EXIT_CODE=$?

printf '{"@timestamp":"%s","app":"raven-jobs","script":"operation-m1.sh","operation":"M1","run_id":"%s","event":"completion","status":"done","exit_code":%d,"app_log_file":"%s/%s.ndjson"}\n' \
  "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$RUN_ID" "$EXIT_CODE" "$LOG_DIR" "$LOG_BASENAME" >> "$RUNNER_LOG_FILE"

if [[ "$EXIT_CODE" -eq 0 ]]; then
  KDECONNECT_DEVICE="2adf1fdef97446eab62372b7b0fabd19"
  if kdeconnect-cli --list-available 2>/dev/null | grep -q "$KDECONNECT_DEVICE"; then
    kdeconnect-cli -d "$KDECONNECT_DEVICE" --ping-msg \
      "raven-jobs | M1 | run_id=${RUN_ID} | event=completion | status=done | $(date -u +%Y-%m-%dT%H:%M:%SZ)" \
      2>/dev/null || true
  fi
fi

exit "$EXIT_CODE"
