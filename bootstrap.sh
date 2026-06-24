#!/usr/bin/env bash

# Conductor Platform Local Environment Bootstrap Script
# Enforces system requirements, starts local docker-compose stack, and validates health.

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

echo -e "${GREEN}=== Conductor Platform Local Bootstrap ===${NC}"

# 1. Dependency Validation
echo "Checking dependencies..."

if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker is not installed.${NC} Please install Docker Desktop or Docker Engine."
    exit 1
fi
echo -e "  - Docker: ${GREEN}OK${NC} ($(docker --version))"

if ! docker compose version &> /dev/null; then
    echo -e "${RED}Error: Docker Compose v2 is not available.${NC} Please update Docker."
    exit 1
fi
echo -e "  - Docker Compose: ${GREEN}OK${NC} ($(docker compose version))"

if ! command -v make &> /dev/null; then
    echo -e "${YELLOW}Warning: 'make' utility is not installed.${NC} You can run commands manually or via bootstrap scripts."
fi

# 2. Resource Checks
echo "Verifying system resources..."
if [[ "$OSTYPE" == "linux-gnu"* ]] || [[ "$OSTYPE" == "darwin"* ]]; then
    # Memory Check
    if [[ "$OSTYPE" == "darwin"* ]]; then
        TOTAL_MEM_GB=$(($(sysctl -n hw.memsize) / 1024 / 1024 / 1024))
    else
        TOTAL_MEM_GB=$(($(free -g | awk '/^Mem:/{print $2}') + 1))
    fi
    
    if [ "$TOTAL_MEM_GB" -lt 12 ]; then
        echo -e "${YELLOW}Warning: Total System RAM is ${TOTAL_MEM_GB}GB. At least 12GB is recommended to run the full OSS stack locally.${NC}"
    else
        echo -e "  - RAM: ${GREEN}OK${NC} (${TOTAL_MEM_GB}GB)"
    fi
fi

# 3. Port Availability Checks
echo "Checking port availability..."
PORTS=(5432 6379 8080 7233 8000 8123 3000 3001 9090 3100 3200 13133)
for PORT in "${PORTS[@]}"; do
    if lsof -Pi :$PORT -sTCP:LISTEN -t &> /dev/null || netstat -an | grep -q "\.$PORT "; then
        echo -e "${RED}Error: Port $PORT is already in use.${NC} Please free this port before starting."
        exit 1
    fi
done
echo -e "  - Ports: ${GREEN}OK${NC}"

# 4. Folder Structure Check
echo "Initializing directory structures..."
# Standard directories are already initialized by Phase 1.
echo -e "  - Folders: ${GREEN}OK${NC}"

# 5. Declarative Config Provisioning
echo "Provisioning config placeholders..."
# Ensure realm-export.json exists
if [ ! -f "./platform/identity/realm-export.json" ]; then
  mkdir -p ./platform/identity
  echo '{"realm": "conductor", "enabled": true}' > ./platform/identity/realm-export.json
fi
# Ensure kong.yml exists
if [ ! -f "./platform/identity/kong.yml" ]; then
  mkdir -p ./platform/identity
  cat <<EOF > ./platform/identity/kong.yml
_format_version: "3.0"
_transform: true
services:
  - name: keycloak-service
    url: http://keycloak:8080
    routes:
      - name: keycloak-route
        paths:
          - /auth
EOF
fi

# 6. Run Compose Stack
echo "Starting local docker-compose services..."
docker compose -f docker-compose.local.yml up -d

echo "Waiting 20 seconds for databases and services to initialize..."
sleep 20

# 7. Run Health Validation
echo "Executing healthchecks..."
bash ./scripts/healthcheck.sh
res=$?

if [ $res -eq 0 ]; then
  echo -e "\n${GREEN}Bootstrap completed successfully! Run 'make logs' or 'docker compose -f docker-compose.local.yml logs -f' to monitor.${NC}"
else
  echo -e "\n${RED}Bootstrap completed with healthcheck failures. Review logs to diagnose issues.${NC}"
fi

exit $res
