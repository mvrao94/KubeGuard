# KubeGuard: Lightweight Kubernetes Security Scanner

[![CI/CD Pipeline](https://github.com/mvrao94/KubeGuard/actions/workflows/ci.yml/badge.svg)](https://github.com/mvrao94/KubeGuard/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?logo=opensourceinitiative&logoColor=white)](https://opensource.org/licenses/MIT)
[![Coverage](https://codecov.io/gh/mvrao94/KubeGuard/graph/badge.svg?token=4ONAKZD2ZQ)](https://codecov.io/gh/mvrao94/KubeGuard)
[![Docker Pulls](https://img.shields.io/docker/pulls/mvrao94/kubeguard?logo=docker)](https://hub.docker.com/r/mvrao94/kubeguard)

KubeGuard is a light-weight, comprehensive, self-hosted Kubernetes security scanner built with Java and Spring Boot. It provides developers and DevOps engineers with actionable insights into security misconfigurations, helping to harden Kubernetes applications before and after deployment.

## 🚀 Features

### Core Functionality
- **📄 Static Manifest Scanning**: Analyze Kubernetes YAML files for security issues before deployment
- **🔍 Live Cluster Scanning**: Scan running resources within Kubernetes clusters for active vulnerabilities
- **🎯 Comprehensive Rule Engine**: 25+ security rules covering CIS Kubernetes Benchmark recommendations
- **📊 Detailed Reporting**: Rich JSON reports with severity levels, remediation advice, and actionable insights
- **🔄 Async Processing**: Non-blocking scan execution with real-time status tracking

### Enterprise Features
- **🏗️ Production-Ready Architecture**: Built with Spring Boot, PostgreSQL, and containerized deployment
- **🔐 Security-First Design**: RBAC, network policies, security contexts, and vulnerability scanning
- **📈 Observability**: Comprehensive monitoring with Prometheus metrics and health checks
- **🚀 CI/CD Integration**: GitHub Actions pipeline with automated testing and deployment
- **🐳 Container Native**: Multi-architecture Docker images with security best practices
- **☸️ Kubernetes Native**: Complete Kubernetes manifests with HPA, PDB, and network policies

### Developer Experience
- **📋 REST API**: Clean, well-documented RESTful API with OpenAPI 3.0 specification
- **📚 Interactive Documentation**: Swagger UI for API exploration and testing
- **🧪 Comprehensive Testing**: Unit, integration, and security tests with 80%+ coverage
- **🔧 Local Development**: Docker Compose setup for easy local development

## 📋 Table of Contents

- [Quick Start](#-quick-start)
- [Installation](#-installation)
- [API Reference](#-api-reference)
- [Security Rules](#-security-rules)
- [Configuration](#-configuration)
- [Deployment](#-deployment)
- [Monitoring](#-monitoring)
- [Development](#-development)
- [Contributing](#-contributing)

## 🚀 Quick Start

### Using Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/mvrao94/KubeGuard.git
cd KubeGuard

# Start KubeGuard with all dependencies
docker-compose -f scripts/docker-compose.yml up -d

# Wait for services to be ready
docker-compose -f scripts/docker-compose.yml logs -f kubeguard

# Access the application
open http://localhost:8080/swagger-ui.html
```

### Using Kubernetes

```bash
# Deploy to Kubernetes
kubectl apply -f https://raw.githubusercontent.com/mvrao94/KubeGuard/main/k8s/namespace.yaml
kubectl apply -f https://raw.githubusercontent.com/mvrao94/KubeGuard/main/k8s/rbac.yaml
kubectl apply -f https://raw.githubusercontent.com/mvrao94/KubeGuard/main/k8s/postgres.yaml
kubectl apply -f https://raw.githubusercontent.com/mvrao94/KubeGuard/main/k8s/deployment.yaml
kubectl apply -f https://raw.githubusercontent.com/mvrao94/KubeGuard/main/k8s/service.yaml

# Port forward to access the service
kubectl port-forward svc/kubeguard 8080:80 -n kubeguard

# Access the application
open http://localhost:8080/swagger-ui.html
```

## 📦 Installation

### Prerequisites

- **Java 25** (for building from source)
- **Maven 3.9+** (for building from source)
- **Docker** (for containerized deployment)
- **Kubernetes cluster** (for cluster scanning)
- **kubectl** configured to access your cluster

### Building from Source

```bash
# Clone the repository
git clone https://github.com/mvrao94/KubeGuard.git
cd KubeGuard

# Build the application
./build-tools/mvnw clean package

# Run the application
java -jar target/kubeguard-1.0.0.jar
```

### Using Pre-built Docker Image

```bash
# Pull the latest image
docker pull mvrao94/kubeguard:latest

# Run with default configuration
docker run -d -p 8080:8080 mvrao94/kubeguard:latest
```

## 🔗 API Reference

### Authentication
Currently, KubeGuard runs without authentication for demo purposes. In production, implement proper authentication and authorization.

### Core Endpoints

#### Start Manifest Scan
```http
POST /api/v1/scan/manifests
Content-Type: application/json

{
  "path": "/path/to/kubernetes/manifests",
  "description": "Optional scan description"
}
```

**Response:**
```json
{
  "scanId": "123e4567-e89b-12d3-a456-426614174000",
  "message": "Manifest scan started successfully",
  "status": "RUNNING"
}
```

#### Start Cluster Scan
```http
GET /api/v1/scan/cluster/{namespace}
```

**Response:**
```json
{
  "scanId": "123e4567-e89b-12d3-a456-426614174000",
  "message": "Cluster scan started successfully",
  "status": "RUNNING"
}
```

#### Get Scan Status
```http
GET /api/v1/scan/status/{scanId}
```

**Response:**
```json
{
  "id": 1,
  "scanId": "123e4567-e89b-12d3-a456-426614174000",
  "scanType": "MANIFEST",
  "target": "/path/to/manifests",
  "timestamp": "2024-01-15T10:30:00",
  "status": "COMPLETED",
  "totalResources": 5,
  "criticalIssues": 2,
  "highIssues": 3,
  "mediumIssues": 1,
  "lowIssues": 0,
  "findings": [
    {
      "resourceName": "nginx-deployment",
      "resourceType": "Deployment",
      "namespace": "default",
      "ruleId": "CON001",
      "title": "Privileged Container Detected",
      "description": "Container is running in privileged mode...",
      "severity": "CRITICAL",
      "category": "Container Security",
      "remediation": "Remove privileged: true from container security context...",
      "location": "Container: nginx"
    }
  ]
}
```

#### Get All Reports
```http
GET /api/v1/reports?page=0&size=10&sortBy=timestamp&sortDir=desc
```

#### Get Security Metrics
```http
GET /api/v1/reports/analytics/summary
```

**Response:**
```json
{
  "totalReports": 25,
  "completedReports": 23,
  "failedReports": 1,
  "runningReports": 1,
  "totalCriticalFindings": 15,
  "totalHighFindings": 42
}
```

### Example Usage

```bash
# Start a manifest scan
curl -X POST http://localhost:8080/api/v1/scan/manifests \
  -H "Content-Type: application/json" \
  -d '{"path": "./sample-manifests"}'

# Get scan status
curl http://localhost:8080/api/v1/scan/status/{scanId}

# Start a cluster scan
curl http://localhost:8080/api/v1/scan/cluster/default

# Get security metrics
curl http://localhost:8080/api/v1/reports/analytics/summary
```

## 🛡️ Security Rules

KubeGuard implements 27+ comprehensive security rules based on CIS Kubernetes Benchmark and industry best practices:

### Container Security Rules (12 rules)
- **CON001**: Privileged Container Detection (Critical)
- **CON002**: Container Running as Root (High)
- **CON003**: Missing Resource Limits (Medium)
- **CON004**: Missing Readiness Probe (Low)
- **CON005**: Missing Liveness Probe (Low)
- **CON006**: Using Latest Tag (Medium)
- **CON007**: Root Filesystem Not Read-Only (Medium)
- **CON008**: RunAsNonRoot Not Enforced (High)
- **CON009**: Privilege Escalation Allowed (High)
- **CON010**: Additional Capabilities Added (Medium)
- **CON011**: Capabilities Not Dropped (Medium)
- **CON012**: Missing Startup Probe (Low)

### Pod Security Rules (7 rules)
- **POD001**: Missing Pod Security Context (Medium)
- **POD002**: Pod Running as Root (High)
- **POD003**: Missing FSGroup (Low)
- **POD004**: Host Network Enabled (High)
- **POD005**: Host PID Namespace Enabled (High)
- **POD006**: Host IPC Namespace Enabled (High)
- **POD007**: HostPath Volume Detected (Critical)

### Service Security Rules (1 rule)
- **SVC001**: LoadBalancer Service Exposure (Medium)

### Network Security Rules (2 rules)
- **NET001**: Missing Network Policies (Medium)
- **NET002**: Ingress Without TLS (High)

### RBAC Rules (2 rules)
- **RBAC001**: Overly Permissive Roles (High)
- **RBAC002**: Use of Default Service Account (Medium)

### Secret Management Rules (2 rules)
- **SEC001**: Hardcoded Secret in Environment Variable (Critical)
- **SEC002**: Service Account Token Auto-Mount Enabled (Low)

### Resource Management Rules (1 rule)
- **RES001**: Missing Resource Requests (Medium)

For detailed information about each rule, including remediation steps and compliance mapping, see the [Security Rules Reference](docs/SECURITY_RULES_REFERENCE.md).

## ⚙️ Configuration

### Application Properties

```yaml
# application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/kubeguard
    username: kubeguard
    password: ${DB_PASSWORD}
  
kubeguard:
  security:
    rules:
      enabled: true
      strict-mode: false
  scan:
    max-concurrent: 10
    timeout-minutes: 30
    cleanup-days: 7
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profiles | `default` |
| `DB_PASSWORD` | Database password | `changeme` |
| `JAVA_OPTS` | JVM options | `-Xmx512m -Xms256m` |

## 🚀 Deployment

### Kubernetes Deployment (Production)

```bash
# Create namespace and RBAC
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/rbac.yaml

# Deploy database
kubectl apply -f k8s/postgres.yaml

# Deploy application
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml

# Apply production configurations
kubectl apply -f k8s/hpa.yaml
kubectl apply -f k8s/poddisruptionbudget.yaml
kubectl apply -f k8s/networkpolicy.yaml
```

### Helm Deployment (Alternative)

```bash
# Add Helm repository
helm repo add kubeguard https://mvrao94.github.io/KubeGuard

# Install KubeGuard
helm install kubeguard kubeguard/kubeguard \
  --namespace kubeguard \
  --create-namespace \
  --set image.tag=latest \
  --set ingress.enabled=true \
  --set ingress.hosts[0].host=kubeguard.yourdomain.com
```

### Docker Swarm Deployment

```yaml
# docker-stack.yml
version: '3.8'

services:
  kubeguard:
    image: mvrao94/kubeguard:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - DB_PASSWORD_FILE=/run/secrets/db_password
    secrets:
      - db_password
    deploy:
      replicas: 3
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3

secrets:
  db_password:
    external: true
```

## 📊 Monitoring & Observability

KubeGuard includes comprehensive observability with Prometheus metrics, health checks, and Grafana dashboards.

### Quick Start

```bash
# Start with full monitoring stack
docker-compose -f scripts/docker-compose.yml up -d

# Access monitoring tools
# - Prometheus: http://localhost:9090
# - Grafana: http://localhost:3000 (admin/admin)
# - Metrics: http://localhost:8080/actuator/prometheus
```

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Kubernetes readiness probe
curl http://localhost:8080/actuator/health/readiness

# Kubernetes liveness probe
curl http://localhost:8080/actuator/health/liveness
```

### Key Metrics

**Application Metrics:**
- `kubeguard_scans_total` - Total scans by type and status
- `kubeguard_scans_active` - Currently running scans
- `kubeguard_scans_completed` - Completed scans
- `kubeguard_scans_failed` - Failed scans
- `kubeguard_scan_duration_seconds` - Scan duration histogram
- `kubeguard_findings_total` - Security findings by severity
- `kubeguard_findings_count` - Total findings count
- `kubeguard_scan_errors` - Scan errors by type

**System Metrics:**
- JVM memory, GC, and thread metrics
- HTTP request duration and counts
- CPU and system resource usage

### Grafana Dashboard

The pre-configured dashboard includes:
- Real-time scan statistics
- Performance metrics (p50, p95 latencies)
- Security findings trends
- JVM and system resource monitoring
- Alert status

**Import Dashboard:**
1. Access Grafana at http://localhost:3000
2. Login with admin/admin
3. Dashboard is auto-provisioned as "KubeGuard Monitoring Dashboard"

### Alerts

Pre-configured Prometheus alerts:
- High scan failure rate
- Service availability issues
- Memory usage warnings
- Performance degradation
- Critical security findings

### Documentation

- 📖 [Full Observability Guide](docs/OBSERVABILITY.md)
- 🚀 [Quick Start Guide](docs/OBSERVABILITY_QUICKSTART.md)
- 🔧 [Monitoring Setup](monitoring/README.md)

## 🧪 Development

### Local Development Setup

```bash
# Clone the repository
git clone https://github.com/mvrao94/KubeGuard.git
cd KubeGuard

# Setup development environment
make setup-dev

# Run tests
make test

# Start the application
make run
```

### Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Security scan
make security-scan

# Generate coverage report
mvn jacoco:report
```

### Code Quality

```bash
# Format code
make format

# Lint code
make lint

# Generate documentation
make docs
```

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Quick Contribution Guide

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Make your changes and add tests**
4. **Run the test suite**: `make test`
5. **Commit your changes**: `git commit -m 'Add amazing feature'`
6. **Push to your branch**: `git push origin feature/amazing-feature`
7. **Open a Pull Request**

### Development Standards

- **Code Coverage**: Maintain >80% test coverage
- **Documentation**: Update docs for any API changes
- **Security**: Follow secure coding practices
- **Performance**: Consider performance impact of changes

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Issues**: [GitHub Issues](https://github.com/mvrao94/KubeGuard/issues)
- **Discussions**: [GitHub Discussions](https://github.com/mvrao94/KubeGuard/discussions)
- **Security Issues**: [Security Policy](SECURITY.md)

## 🙏 Acknowledgments

- **CIS Kubernetes Benchmark** for security guidelines
- **Kubernetes Community** for excellent documentation
- **Spring Boot Team** for the fantastic framework
- **All Contributors** who have helped improve KubeGuard

---

**Made with ❤️ by the KubeGuard Team**