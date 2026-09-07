.DEFAULT_GOAL := help

# Shell configuration
SHELL := /usr/bin/env bash
.SHELLFLAGS := -eu -o pipefail -c

MVNW := ./mvnw
APP := ./bin/expense-tracker
TARGET_JAR := target/expense-tracker-app-0.0.1-SNAPSHOT.jar
TARGET_CP := target/classpath.txt

.PHONY: help build compile test it verify format format-check clean cli run add list remove sync-push sync-pull compose-up compose-down compose-reset compose-logs
help: ## Display this help message
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}'
$(TARGET_JAR):
	@$(MVNW) package -DskipTests

$(TARGET_CP):
	@$(MVNW) dependency:build-classpath -Dmdep.outputFile=$(TARGET_CP) -Dsilent=true

build: $(TARGET_JAR) $(TARGET_CP) ## Build application JAR and generate cached classpath

compile: ## Compile main source code
	@$(MVNW) compile

test: ## Run unit tests with Surefire
	@$(MVNW) test

it: ## Run integration tests with Failsafe (Testcontainers MinIO)
	@$(MVNW) verify -Dtest=SmokeTest

verify: ## Run full verification pipeline (Spotless, ErrorProne, tests, ITs, JaCoCo)
	@$(MVNW) clean verify

format: ## Apply Spotless code formatting
	@$(MVNW) spotless:apply

format-check: ## Verify code formatting with Spotless
	@$(MVNW) spotless:check

clean: ## Clean build target directory
	@$(MVNW) clean
	@rm -f target/classpath.txt

cli: build ## Ensure wrapper scripts and classpath are built

run: build ## Run CLI application (e.g. make run ARGS="list")
	@$(APP) $(ARGS)

add: build ## Add an expense (e.g. make add ARGS="-a 12.50 -c GBP --category FOOD -d Lunch")
	@$(APP) add $(ARGS)

list: build ## List all expenses
	@$(APP) list

remove: build ## Remove an expense by ID (e.g. make remove ARGS="--id <UUID>")
	@$(APP) remove $(ARGS)

sync-push: build ## Upload local expenses to S3/MinIO
	@$(APP) sync push

sync-pull: build ## Download expenses from S3/MinIO (use ARGS="--force" to overwrite)
	@$(APP) sync pull $(ARGS)

compose-up: ## Start local MinIO development environment
	@docker compose up -d

compose-down: ## Stop local MinIO development environment
	@docker compose down

compose-reset: ## Stop local MinIO and delete persistent volume data
	@docker compose down -v

compose-logs: ## Follow local MinIO container logs
	@docker compose logs -f
