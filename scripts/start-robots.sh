#!/bin/bash
# start-robots.sh — 向 RCS 模拟器发送任务，激活所有机器人
#
# 用法:
#   ./scripts/start-robots.sh                    # 默认 localhost:8091
#   SIMULATOR_URL=http://192.168.1.100:8091 ./scripts/start-robots.sh
#
# 验证:
#   curl http://localhost:8091/api/simulator/tasks | jq
#   curl http://localhost:8091/api/simulator/robots | jq '.[] | {robotCode, status, position}'

SIMULATOR_URL="${SIMULATOR_URL:-http://localhost:8091}"

echo "=== Sending 8 tasks to RCS Simulator at $SIMULATOR_URL ==="
echo ""

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$SIMULATOR_URL/api/tasks/create" \
  -H "Content-Type: application/json" \
  -d '{
  "data": [
    {
      "customerTaskId": 1,
      "businessTaskType": "PICKING",
      "containerTaskType": "MOVE_CONTAINER",
      "taskCode": "DEMO-001",
      "taskGroupCode": "DEMO-GROUP-1",
      "containerCode": "CONT-001",
      "containerFace": "FACE_A",
      "startLocation": "A01-01",
      "destinations": ["WS-01"]
    },
    {
      "customerTaskId": 2,
      "businessTaskType": "PICKING",
      "containerTaskType": "MOVE_CONTAINER",
      "taskCode": "DEMO-002",
      "taskGroupCode": "DEMO-GROUP-1",
      "containerCode": "CONT-002",
      "containerFace": "FACE_A",
      "startLocation": "B01-01",
      "destinations": ["WS-02"]
    },
    {
      "customerTaskId": 3,
      "businessTaskType": "PICKING",
      "containerTaskType": "MOVE_CONTAINER",
      "taskCode": "DEMO-003",
      "taskGroupCode": "DEMO-GROUP-1",
      "containerCode": "CONT-003",
      "containerFace": "FACE_B",
      "startLocation": "C01-01",
      "destinations": ["WS-01"]
    },
    {
      "customerTaskId": 4,
      "businessTaskType": "PICKING",
      "containerTaskType": "MOVE_CONTAINER",
      "taskCode": "DEMO-004",
      "taskGroupCode": "DEMO-GROUP-1",
      "containerCode": "CONT-004",
      "containerFace": "FACE_B",
      "startLocation": "D01-01",
      "destinations": ["WS-02"]
    },
    {
      "customerTaskId": 5,
      "businessTaskType": "RECEIVING",
      "containerTaskType": "MOVE_CONTAINER",
      "taskCode": "DEMO-005",
      "taskGroupCode": "DEMO-GROUP-2",
      "containerCode": "CONT-005",
      "containerFace": "FACE_A",
      "startLocation": "WS-03",
      "destinations": ["A02-01"]
    },
    {
      "customerTaskId": 6,
      "businessTaskType": "RECEIVING",
      "containerTaskType": "MOVE_CONTAINER",
      "taskCode": "DEMO-006",
      "taskGroupCode": "DEMO-GROUP-2",
      "containerCode": "CONT-006",
      "containerFace": "FACE_A",
      "startLocation": "WS-04",
      "destinations": ["B02-01"]
    },
    {
      "customerTaskId": 7,
      "businessTaskType": "RECEIVING",
      "containerTaskType": "MOVE_CONTAINER",
      "taskCode": "DEMO-007",
      "taskGroupCode": "DEMO-GROUP-2",
      "containerCode": "CONT-007",
      "containerFace": "FACE_B",
      "startLocation": "WS-03",
      "destinations": ["C02-01"]
    },
    {
      "customerTaskId": 8,
      "businessTaskType": "RECEIVING",
      "containerTaskType": "MOVE_CONTAINER",
      "taskCode": "DEMO-008",
      "taskGroupCode": "DEMO-GROUP-2",
      "containerCode": "CONT-008",
      "containerFace": "FACE_B",
      "startLocation": "WS-04",
      "destinations": ["D02-01"]
    }
  ]
}')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
  echo "✓ All 8 tasks sent successfully!"
  echo "  Response: $BODY"
  echo ""
  echo "Next steps:"
  echo "  Open 3D Viewer:        http://localhost:8092"
  echo "  Check robot status:    curl $SIMULATOR_URL/api/simulator/robots | jq"
  echo "  Check task status:     curl $SIMULATOR_URL/api/simulator/tasks | jq"
  echo "  Reset simulator:       curl -X POST $SIMULATOR_URL/api/simulator/reset"
else
  echo "✗ Failed (HTTP $HTTP_CODE)"
  echo "  Response: $BODY"
  echo ""
  echo "Troubleshooting:"
  echo "  Is the simulator running? Check: curl $SIMULATOR_URL/api/simulator/robots"
  echo "  In Docker: docker compose ps | grep rcs-simulator"
  exit 1
fi
