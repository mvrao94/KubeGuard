# KubeGuard Documentation

## 📚 Documentation Index

### Getting Started
- **[README.md](README.md)** - Project overview, quick start, installation
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Contribution guidelines
- **[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)** - Community standards
- **[SECURITY.md](SECURITY.md)** - Security policy and reporting

### Essential Guides
- **[Security Configuration](docs/SECURITY_CONFIGURATION.md)** - **REQUIRED** - Mandatory authentication setup
- **[Native Image Build](docs/NATIVE_IMAGE_BUILD.md)** - GraalVM build guide (experimental, untested)
- **[API Reference](docs/API.md)** - Complete REST API documentation

### Architecture
- **[Rule Engine Architecture](docs/RULE_ENGINE_ARCHITECTURE.md)** - Extensible rule system design
- **[Security Integrations](docs/SECURITY_INTEGRATIONS.md)** - NIST NVD & MITRE ATT&CK integration
- **[Security Rules Reference](docs/SECURITY_RULES_REFERENCE.md)** - Complete catalog of 70+ rules
- **[Project Structure](docs/PROJECT_STRUCTURE.md)** - Codebase organization

### Operations
- **[Observability Guide](docs/OBSERVABILITY.md)** - Monitoring, metrics, and alerting
- **[Performance Analysis](PERFORMANCE.md)** - Honest JVM vs Go comparison

### Deployment
- **[Kubernetes Manifests](k8s/)** - Production-ready K8s deployment files
- **[Helm Charts](helm/)** - Helm deployment option
- **[Docker Compose](scripts/docker-compose.yml)** - Local development setup

---

## 🚀 Quick Links

**First Time Users**:
1. Read [README.md](README.md) for overview
2. Follow [Security Configuration](docs/SECURITY_CONFIGURATION.md) to set up authentication
3. Optionally try [Native Image Build](docs/NATIVE_IMAGE_BUILD.md) (experimental)

**Developers**:
1. Review [Project Structure](docs/PROJECT_STRUCTURE.md)
2. Understand [Rule Engine Architecture](docs/RULE_ENGINE_ARCHITECTURE.md)
3. Check [API Reference](docs/API.md) for endpoints

**Operators**:
1. Set up [Observability](docs/OBSERVABILITY.md)
2. Review [Performance Analysis](PERFORMANCE.md)
3. Deploy using [Kubernetes Manifests](k8s/)

---

## 📊 Project Stats

- **70+ Security Rules**: CIS, NSA/CISA, MITRE ATT&CK, OWASP
- **14 REST API Endpoints**: Fully documented with Swagger
- **2 External Integrations**: NIST NVD + MITRE ATT&CK
- **Mandatory Authentication**: API key required, no bypass
- **Native Image Support**: GraalVM profile configured (untested)

---

## 🎯 Key Features

**Security First**:
- Mandatory API key authentication
- Fail-fast validation (app won't start without key)
- No bypass mechanism

**Comprehensive Rules**:
- 50+ base security rules
- 20+ MITRE ATT&CK techniques
- Mapped to 4 compliance frameworks

**Performance**:
- JVM mode: 166 MB container (measured)
- Native Image: Profile configured (untested)

**Production Ready**:
- Prometheus metrics
- Grafana dashboards
- Health checks
- Comprehensive documentation

---

**Last Updated**: 2025-11-30
