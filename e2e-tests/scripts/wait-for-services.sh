#!/bin/bash
# Wait for all services to be healthy before running tests

set -e

MAX_WAIT=300  # 5 minutes
INTERVAL=5
ELAPSED=0

check_service() {
  local url=$1
  local name=$2
  curl -s -o /dev/null -w "%{http_code}" "$url" | grep -q "200\|302\|401" && \
    echo "  ✓ $name is ready" || \
    echo "  ✗ $name not ready"
}

echo "Waiting for services to be ready..."
echo ""

while [ $ELAPSED -lt $MAX_WAIT ]; do
  ALL_READY=true

  # Check Gateway (login page returns 200/302)
  GW_STATUS=$(check_service "http://localhost:8090" "Gateway")
  echo "$GW_STATUS"
  [[ "$GW_STATUS" == *"✗"* ]] && ALL_READY=false

  # Check Frontend (nginx returns 200)
  FE_STATUS=$(check_service "http://localhost:80" "Frontend")
  echo "$FE_STATUS"
  [[ "$FE_STATUS" == *"✗"* ]] && ALL_READY=false

  if $ALL_READY; then
    echo ""
    echo "All services are ready! ($ELAPSED seconds)"
    exit 0
  fi

  sleep $INTERVAL
  ELAPSED=$((ELAPSED + INTERVAL))
done

echo ""
echo "ERROR: Services did not become ready within ${MAX_WAIT}s"
exit 1
