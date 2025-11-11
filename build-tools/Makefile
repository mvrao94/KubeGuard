# Makefile - Development shortcuts
.PHONY: help build test run clean docker-build docker-run deploy

# Default target
help: ## Show this help message
	@echo "KubeGuard - Kubernetes Security Scanner"
	@echo ""
	@echo "Available targets:"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $1, $2}' $(MAKEFILE_LIST)

build: ## Build the application
	@echo "Building KubeGuard..."
	../build-tools/mvnw clean package -DskipTests

test: ## Run tests
	@echo "Running tests..."
	../build-tools/mvnw clean test

integration-test: ## Run integration tests
	@echo "Running integration tests..."
	../build-tools/mvnw clean verify

run: ## Run the application locally
	@echo "Starting KubeGuard..."
	java -jar target/kubeguard-*.jar

clean: ## Clean build artifacts
	@echo "Cleaning..."
	../build-tools/mvnw clean

docker-build: ## Build Docker image
	@echo "Building Docker image..."
	docker build -f ../scripts/Dockerfile -t kubeguard:latest ..

docker-run: ## Run with Docker Compose
	@echo "Starting KubeGuard with Docker Compose..."
	docker-compose -f ../scripts/docker-compose.yml up -d

docker-stop: ## Stop Docker Compose
	@echo "Stopping Docker Compose..."
	docker-compose -f ../scripts/docker-compose.yml down

docker-logs: ## View Docker logs
	docker-compose -f ../scripts/docker-compose.yml logs -f kubeguard

deploy-local: ## Deploy to local Kubernetes
	@echo "Deploying to local Kubernetes..."
	kubectl apply -f ../k8s/

undeploy-local: ## Remove from local Kubernetes
	@echo "Removing from local Kubernetes..."
	kubectl delete -f ../k8s/

port-forward: ## Port forward to local deployment
	@echo "Port forwarding to KubeGuard service..."
	kubectl port-forward svc/kubeguard 8080:80 -n kubeguard

logs: ## View application logs in Kubernetes
	kubectl logs -f deployment/kubeguard -n kubeguard

security-scan: ## Run security vulnerability scan
	@echo "Running security scan..."
	../build-tools/mvnw org.owasp:dependency-check-maven:check

format: ## Format code
	@echo "Formatting code..."
	../build-tools/mvnw spring-javaformat:apply

lint: ## Lint code
	@echo "Linting code..."
	../build-tools/mvnw spotbugs:check

docs: ## Generate documentation
	@echo "Generating documentation..."
	../build-tools/mvnw javadoc:javadoc

# Development environment setup
setup-dev: ## Setup development environment
	@echo "Setting up development environment..."
	@echo "Installing pre-commit hooks..."
	cp scripts/pre-commit .git/hooks/pre-commit
	chmod +x .git/hooks/pre-commit
	@echo "Starting development dependencies..."
	docker-compose -f ../scripts/docker-compose.yml up -d postgres
	@echo "Development environment ready!"

# Production deployment helpers
deploy-staging: ## Deploy to staging environment
	@echo "Deploying to staging..."
	kubectl apply -f ../k8s/ --context=staging

deploy-prod: ## Deploy to production environment
	@echo "Deploying to production..."
	kubectl apply -f ../k8s/ --context=production

# Monitoring
monitor: ## Open monitoring dashboard
	@echo "Opening Grafana dashboard..."
	open http://localhost:3000