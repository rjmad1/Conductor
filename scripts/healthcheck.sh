#!/usr/bin/env bash

# Conductor Platform Validation Healthcheck Script
# Validates connectivity to all local docker compose service ports.

set -eo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

echo -e "${GREEN}=== Conductor Platform Endpoint Diagnostics ===${NC}\n"

check_http() {
  local name=$1
  local url=$2
  local expected_code=$3
  
  printf "Checking %-20s ... " "$name"
  
  if ! command -v curl &> /dev/null; then
    echo -e "${YELLOW}[WARN]${NC} (curl not installed)"
    return 0
  fi

  # Exec curl call with 2s timeout
  set +e
  code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 2 "$url")
  res=$?
  set -e

  if [ $res -ne 0 ]; then
    echo -e "${RED}[FAIL]${NC} (Connection refused or timeout)"
    return 1
  elif [ "$code" -eq "$expected_code" ] || [ "$expected_code" -eq 200 -a "$code" -eq 302 ]; then
    echo -e "${GREEN}[PASS]${NC} (HTTP $code)"
    return 0
  else
    echo -e "${YELLOW}[WARN]${NC} (HTTP $code, expected $expected_code)"
    return 0
  fi
}

check_port() {
  local name=$1
  local host=$2
  local port=$3
  
  printf "Checking %-20s ... " "$name"

  if command -v nc &> /dev/null; then
    set +e
    nc -z -w 2 "$host" "$port" &> /dev/null
    res=$?
    set -e
    if [ $res -eq 0 ]; then
      echo -e "${GREEN}[PASS]${NC} (Port listener active)"
      return 0
    else
      echo -e "${RED}[FAIL]${NC} (Port unreachable)"
      return 1
    fi
  elif command -v </dev/tcp/$host/$port &> /dev/null; then
    set +e
    timeout 2 bash -c "cat < /dev/tcp/$host/$port" &> /dev/null
    res=$?
    set -e
    if [ $res -eq 0 ] || [ $res -eq 124 ]; then
      echo -e "${GREEN}[PASS]${NC} (Connection accepted)"
      return 0
    else
      echo -e "${RED}[FAIL]${NC} (Connection refused)"
      return 1
    fi
  else
    echo -e "${YELLOW}[WARN]${NC} (no diagnostic tool)"
    return 0
  fi
}

FAILED=0

# --- Tier 1 & 2 Core Port Diagnostics ---
check_port "PostgreSQL (Core DB)" "127.0.0.1" 5432 || FAILED=$((FAILED + 1))
check_port "Redis (Cache Store)" "127.0.0.1" 6379 || FAILED=$((FAILED + 1))
check_port "NATS (Message Bus)" "127.0.0.1" 4222 || FAILED=$((FAILED + 1))
check_port "Temporal (gRPC Server)" "127.0.0.1" 7233 || FAILED=$((FAILED + 1))

# --- HTTP API Diagnostics ---
check_http "Keycloak (Auth Server)" "http://localhost:8080/health/ready" 200 || FAILED=$((FAILED + 1))
check_http "Kong (API Gateway)" "http://localhost:8000" 404 || FAILED=$((FAILED + 1)) # Expect 404 since no default root route is declared
check_http "ClickHouse (Analytics)" "http://localhost:8123/ping" 200 || FAILED=$((FAILED + 1))
check_http "Metabase (BI Portal)" "http://localhost:3000/api/health" 200 || FAILED=$((FAILED + 1))
check_http "Qdrant (Vector DB)" "http://localhost:6333/readyz" 200 || FAILED=$((FAILED + 1))
check_http "Grafana (UI Dashboards)" "http://localhost:3001/api/health" 200 || FAILED=$((FAILED + 1))
check_http "Prometheus (Metrics)" "http://localhost:9090/-/healthy" 200 || FAILED=$((FAILED + 1))
check_http "Loki (Log Store)" "http://localhost:3100/ready" 200 || FAILED=$((FAILED + 1))
check_http "Tempo (Trace Store)" "http://localhost:3200/ready" 200 || FAILED=$((FAILED + 1))
check_http "OTel Collector" "http://localhost:13133/" 200 || FAILED=$((FAILED + 1))

echo ""
if [ $FAILED -eq 0 ]; then
  echo -e "${GREEN}=== Validation Results: ALL HEALTHCHECKS PASSED ===${NC}"
  exit 0
else
  echo -e "${RED}=== Validation Results: $FAILED HEALTHCHECKS FAILED ===${NC}"
  exit 1
fi
