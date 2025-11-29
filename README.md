# KubeGuard: Kubernetes Security Scanner

[![CI/CD](https://github.com/mvrao94/KubeGuard/actions/workflows/ci.yml/badge.svg)](https://github.com/mvrao94/KubeGuard/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Coverage](https://codecov.io/gh/mvrao94/KubeGuard/graph/badge.svg?token=4ONAKZD2ZQ)](https://codecov.io/gh/mvrao94/KubeGuard)

A comprehensive Kubernetes security scanner built with Java 25 and Spring Boot. Scans manifests and live clusters for security misconfigurations with 70+ rules mapped to CIS, NSA/CISA, MITRE ATT&CK, and OWASP standards.

## 🚀 Quick Start

### Prerequisites

**⚠️ MANDATORY**: Generate API key (application fails without it)

```bash
export KUBEGUARD_API_KEY=$(openssl rand -hex 32)
```

### Run with Docker

```bash
# JVM mode
docker run -d -p 8080:8080 \
  -e KUBEGUARD_API_KEY=$KUBEGUARD_API_KEY \
  mvrao94/kubeguard:latest

# Native Image mode (requires GraalVM build)
docker run -d -p 8080:8080 \
  -e KUBEGUARD_API_KEY=$KUBEGUARD_API_KEY \
  mvrao94/kubeguard:native
```

### Test the API

```bash
# Health check (no auth required)
curl http://localhost:8080/actuator/health

# Start a scan (auth required)
curl -H "X-API-Key: $KUBEGUARD_API_KEY" \
  -X POST http://localhost:8080/api/v1/scan/manifests \
  -H "Content-Type: application/json" \
  -d '{"path": "./manifests"}'

# View Swagger UI
open http://localhost:8080/swagger-ui.html
```

## 📋 Features

- **70+ Security Rules**: CIS Benchmark, NSA/CISA, MITRE ATT&CK, OWASP K8s Top 10
- **Dual Scanning**: Static manifest analysis + live cluster scanning
- **External Integrations**: NIST NVD (CVE database) + MITRE ATT&CK (attack patterns)
- **Mandatory Authentication**: API key required, application fails without it
- **Native Image Support**: GraalVM Native Image profile configured (untested)
- **Production Ready**: Prometheus metrics, health checks, comprehensive docs

## 🔧 Installation

### Option 1: Docker (Recommended)

```bash
# Pull and run JVM version
docker pull mvrao94/kubeguard:latest
docker run -d -p 8080:8080 \
  -e KUBEGUARD_API_KEY=$(openssl rand -hex 32) \
  mvrao94/kubeguard:latest
```

### Option 2: Build from Source (JVM)

```bash
git clone https://github.com/mvrao94/KubeGuard.git
cd KubeGuard
export KUBEGUARD_API_KEY=$(openssl rand -hex 32)
./mvnw clean package
java -jar target/kubeguard-0.0.4-SNAPSHOT.jar
```

### Option 3: Build Native Image (Experimental)

```bash
# Install GraalVM
sdk install java 21.0.1-graal
sdk use java 21.0.1-graal
gu install native-image

# Build native executable
export KUBEGUARD_API_KEY=$(openssl rand -hex 32)
./mvnw -Pnative native:compile

# Run
./target/kubeguard
```

**Note**: Native Image profile is configured but not tested. May require additional configuration.

See [Native Image Build Guide](docs/NATIVE_IMAGE_BUILD.md) for detailed instructions.

### Option 4: Kubernetes

```bash
kubectl apply -f https://raw.githubusercontent.com/mvrao94/KubeGuard/main/k8s/namespace.yaml
kubectl apply -f https://raw.githubusercontent.com/mvrao94/KubeGuard/main/k8s/rbac.yaml
kubectl apply -f https://raw.githubusercontent.com/mvrao94/KubeGuard/main/k8s/deployment.yaml
kubectl apply -f https://raw.githubusercontent.com/mvrao94/KubeGuard/main/k8s/service.yaml
```

## 🔐 Security

**Mandatory Authentication**: KubeGuard requires API key authentication. The application **WILL FAIL TO START** without `KUBEGUARD_API_KEY` environment variable.

```bash
# Generate secure key
export KUBEGUARD_API_KEY=$(openssl rand -hex 32)

# All API requests require X-API-Key header
curl -H "X-API-Key: $KUBEGUARD_API_KEY" http://localhost:8080/api/v1/reports
```

**No bypass mechanism exists**. This is a security scanner that is actually secure.

## 📖 API Reference

### Core Endpoints

**Start Manifest Scan**
```bash
POST /api/v1/scan/manifests
{
  "path": "/path/to/manifests",
  "description": "Optional description"
}
```

**Start Cluster Scan**
```bash
GET /api/v1/scan/cluster/{namespace}
```

**Get Scan Status**
```bash
GET /api/v1/scan/status/{scanId}
```

**Get Reports**
```bash
GET /api/v1/reports
GET /api/v1/reports/analytics/summary
```

**Interactive Documentation**: http://localhost:8080/swagger-ui.html

Full API reference: [docs/API.md](docs/API.md)

## 🛡️ Security Rules

**70+ comprehensive rules** across 7 categories:

- **Container Security** (12 rules): Privileged containers, root users, resource limits
- **Pod Security** (7 rules): Security contexts, host namespaces, hostPath volumes
- **Network Security** (2 rules): Network policies, TLS configuration
- **RBAC** (2 rules): Overly permissive roles, service account usage
- **Secret Management** (2 rules): Hardcoded secrets, token auto-mount
- **Resource Management** (1 rule): Missing resource requests
- **MITRE ATT&CK** (20+ techniques): Container-specific attack patterns

**Compliance Mappings**:
- CIS Kubernetes Benchmark: 30+ rules
- NSA/CISA Hardening Guide: 25+ rules
- MITRE ATT&CK for Containers: 20+ techniques
- OWASP Kubernetes Top 10: 10+ rules

See [Security Rules Reference](docs/SECURITY_RULES_REFERENCE.md) for complete list.

## ⚡ Performance

### Measured Facts

| Metric | Value | Status |
|--------|-------|--------|
| **Container Image (JVM)** | 166 MB | ✅ Measured |
| **Native Image Profile** | Configured | ⚠️ Untested |
| **Virtual Threads** | Implemented | ✅ Verified |

### Comparison with Go Tools

| Tool | Language | Image Size |
|------|----------|------------|
| **KubeGuard (JVM)** | Java | 166 MB |
| Kubesec | Go | ~20 MB |
| Polaris | Go | ~40 MB |
| Kube-bench | Go | ~30 MB |

**Reality**: Java containers are 4-8x larger than Go alternatives. Native Image may reduce this but is untested.

See [Performance Analysis](PERFORMANCE.md) for honest assessment.

## 📊 Monitoring

Built-in Prometheus metrics and Grafana dashboards:

```bash
# Start with monitoring stack
docker-compose -f scripts/docker-compose.yml up -d

# Access
# - Application: http://localhost:8080
# - Prometheus: http://localhost:9090
# - Grafana: http://localhost:3000 (admin/admin)
```

**Key Metrics**:
- `kubeguard_scans_total` - Total scans by type/status
- `kubeguard_scan_duration_seconds` - Scan duration
- `kubeguard_findings_total` - Security findings by severity

See [Observability Guide](docs/OBSERVABILITY.md) for details.

## 📚 Documentation

**Essential**:
- [Security Configuration](docs/SECURITY_CONFIGURATION.md) - Mandatory auth setup
- [Native Image Build](docs/NATIVE_IMAGE_BUILD.md) - GraalVM build guide (experimental)
- [API Reference](docs/API.md) - Complete REST API docs

**Architecture**:
- [Rule Engine](docs/RULE_ENGINE_ARCHITECTURE.md) - Extensible rule system
- [Security Integrations](docs/SECURITY_INTEGRATIONS.md) - NVD & MITRE ATT&CK
- [Security Rules](docs/SECURITY_RULES_REFERENCE.md) - 70+ rules catalog

**Operations**:
- [Observability](docs/OBSERVABILITY.md) - Monitoring & metrics
- [Performance](PERFORMANCE.md) - Honest JVM vs Go comparison

## 🧪 Development

```bash
# Run tests
./mvnw test

# Run with coverage
./mvnw verify

# Build Docker images
docker build -t kubeguard:jvm .
docker build -f Dockerfile.native -t kubeguard:native .

# Verify Native Image performance
./scripts/verify-native-build.sh
```

## 🤝 Contributing

Contributions welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Make changes and add tests
4. Run test suite: `./mvnw test`
5. Commit: `git commit -m 'Add amazing feature'`
6. Push: `git push origin feature/amazing-feature`
7. Open Pull Request

## 📄 License

MIT License - see [LICENSE](LICENSE) file.

## 🆘 Support

- **Issues**: [GitHub Issues](https://github.com/mvrao94/KubeGuard/issues)
- **Discussions**: [GitHub Discussions](https://github.com/mvrao94/KubeGuard/discussions)
- **Security**: [Security Policy](SECURITY.md)

---

**Made with ❤️ by the KubeGuard Team**
