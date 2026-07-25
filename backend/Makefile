JAVA_HOME := /Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
export JAVA_HOME
MAVEN := mvn

.PHONY: help build test format check checkstyle coverage archunit ci clean

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-15s\033[0m %s\n", $$1, $$2}'

build: ## Compile without tests
	$(MAVEN) clean compile -DskipTests

test: ## Run unit tests
	$(MAVEN) test

format: ## Fix formatting violations
	$(MAVEN) spotless:apply

check-format: ## Check formatting (CI gate)
	$(MAVEN) spotless:check

checkstyle: ## Run Checkstyle
	$(MAVEN) checkstyle:check

coverage: ## Generate JaCoCo coverage report
	$(MAVEN) jacoco:report

archunit: ## Run architecture tests only
	$(MAVEN) test -pl evcharging-app -Dtest="ArchitectureTest"

ci: format check-format checkstyle test ## Full local CI (format + checks + tests)
	@echo "All CI checks passed."

clean: ## Clean build artifacts
	$(MAVEN) clean
