#!/bin/bash
# demo-loop.sh — 持续向 RCS 模拟器发送任务，让机器人一直动
#
# 用法:
#   ./scripts/demo-loop.sh                         # 默认 localhost:8091, 每15秒一批
#   INTERVAL=10 ./scripts/demo-loop.sh             # 每10秒一批
#   BATCH_SIZE=4 SIMULATOR_URL=http://host:8091 ./scripts/demo-loop.sh
#
# 停止: Ctrl+C

SIMULATOR_URL="${SIMULATOR_URL:-http://localhost:8091}"
INTERVAL="${INTERVAL:-15}"
BATCH_SIZE="${BATCH_SIZE:-8}"

# 仓库位置（循环使用）
SHELF_LOCATIONS=(
  "A01-01" "A02-01" "A03-01" "B01-01" "B02-01"
  "B03-01" "C01-01" "C02-01" "D01-01" "D02-01"
)
WORKSTATIONS=("WS-01" "WS-02" "WS-03" "WS-04")

echo "=== RCS Simulator Demo Loop ==="
echo "  URL:       $SIMULATOR_URL"
echo "  Interval:  ${INTERVAL}s per batch"
echo "  Batch:     $BATCH_SIZE tasks"
echo "  Press Ctrl+C to stop"
echo ""

cleanup() {
  echo ""
  echo "Stopped."
  exit 0
}
trap cleanup SIGINT SIGTERM

BATCH=0
while true; do
  BATCH=$((BATCH + 1))
  TIMESTAMP=$(date +%s)
  TASKS_JSON=""

  for i in $(seq 1 $BATCH_SIZE); do
    TASK_IDX=$(( (BATCH * BATCH_SIZE + i) % ${#SHELF_LOCATIONS[@]} ))
    WS_IDX=$(( (BATCH + i) % ${#WORKSTATIONS[@]} ))

    SHELF="${SHELF_LOCATIONS[$TASK_IDX]}"
    WS="${WORKSTATIONS[$WS_IDX]}"

    # 前一半 PICKING（货架→工作站），后一半 RECEIVING（工作站→货架）
    if [ $i -le $((BATCH_SIZE / 2)) ]; then
      START="$SHELF"
      DEST="$WS"
      TYPE="PICKING"
    else
      START="$WS"
      DEST="$SHELF"
      TYPE="RECEIVING"
    fi

    if [ -n "$TASKS_JSON" ]; then
      TASKS_JSON="$TASKS_JSON,"
    fi

    TASKS_JSON="${TASKS_JSON}
    {
      \"customerTaskId\": ${BATCH}${i},
      \"businessTaskType\": \"${TYPE}\",
      \"containerTaskType\": \"MOVE_CONTAINER\",
      \"taskCode\": \"DEMO-${TIMESTAMP}-${i}\",
      \"taskGroupCode\": \"DEMO-BATCH-${BATCH}\",
      \"containerCode\": \"CONT-${BATCH}-${i}\",
      \"containerFace\": \"FACE_A\",
      \"startLocation\": \"${START}\",
      \"destinations\": [\"${DEST}\"]
    }"
  done

  echo "[$(date '+%H:%M:%S')] Batch $BATCH — sending $BATCH_SIZE tasks ..."

  RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$SIMULATOR_URL/api/tasks/create" \
    -H "Content-Type: application/json" \
    -d "{
  \"data\": [$TASKS_JSON]
}")

  HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
  if [ "$HTTP_CODE" = "200" ]; then
    echo "  ✓ Batch $BATCH accepted"
  else
    BODY=$(echo "$RESPONSE" | sed '$d')
    echo "  ✗ HTTP $HTTP_CODE — $BODY"
  fi

  sleep "$INTERVAL"
done
