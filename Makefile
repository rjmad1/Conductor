.PHONY: help bootstrap start stop restart logs health clean reset test-platform docs

help:
	@echo "Conductor Platform Developer Commands:"
	@echo "  make bootstrap      - Validate local system requirements and environment variables"
	@echo "  make start          - Start all local docker-compose OSS backend services"
	@echo "  make stop           - Shutdown all local backend services and preserve data"
	@echo "  make restart        - Restart all local backend services"
	@echo "  make logs           - View real-time container log output"
	@echo "  make health         - Execute automated validation healthchecks against endpoints"
	@echo "  make clean          - Delete all local container volumes and cached state"
	@echo "  make reset          - Hard reset (clean + bootstrap + start)"
	@echo "  make test-platform  - Validate active services ping connectivity tests"
	@echo "  make docs           - Compile/update platform markdown specifications indexes"

bootstrap:
	@echo "Running environment bootstrap verification..."
	@bash ./bootstrap.sh

start:
	@echo "Starting Conductor local environment..."
	@docker compose -f docker-compose.local.yml up -d
	@echo "Environment started. Run 'make health' to monitor startup health."

stop:
	@echo "Shutting down local environment..."
	@docker compose -f docker-compose.local.yml down

restart:
	@echo "Restarting local environment..."
	@docker compose -f docker-compose.local.yml restart

logs:
	@docker compose -f docker-compose.local.yml logs -f

health:
	@echo "Executing automated health validation..."
	@bash ./scripts/healthcheck.sh

clean:
	@echo "Cleaning local environment and persistent volumes..."
	@docker compose -f docker-compose.local.yml down -v

reset: clean bootstrap start

test-platform: health

docs:
	@echo "Updating platform specification directory records..."
	@ls -la *.md docs/*.md
