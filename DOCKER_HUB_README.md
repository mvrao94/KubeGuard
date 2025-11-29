# KubeGuard - Kubernetes Security Scanner

[![CI/CD Pipeline](https://github.com/mvrao94/KubeGuard/actions/workflows/ci.yml/badge.svg)](https://github.com/mvrao94/KubeGuard/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A lightweight, comprehensive Kubernetes security scanner built with Java and Spring Boot. Provides actionable insights into security misconfigurations for both static manifests and live clusters.

## Quick Start

### Run with Docker

```bash
# Pull and run the latest version
docker pull mvrao94/kubeguard:latest
docker run -d -p 8080:8080 --name kubeguard mvrao94/kubeguard:latest

# Access the application
curl http://localhost:8080/actuator/health
```

### Run with Docker Compose

```bash
# Download docker-compose.yml
curl -O https://raw.githubusercontent.com/mvrao94/KubeGuard/main/scripts/docker-compose.yml

# Start all services (KubeGuard + PostgreSQL + Monitoring)
docker-compose up -d
```

## Supported Tags

- `latest` - Latest stable release from main branch
- `1.x.x` - Specific version tags
- `develop` - Development branch (may be unstable)

## Supported Architectures

- `linux/amd64` - x86_64 / AMD64
- `linux/arm64` - ARM64 / Apple Silicon

## Features

- 📄 **Static Manifest Scanning** - Analyze YAML files before deployment
- 🔍 **Live Cluster Scanning** - Scan running Kubernetes resources
- 🎯 **25+ Security Rules** - Based on CIS Kubernetes Benchmark
- 📊 **Detailed Reports** - JSON reports with severity levels and remediation
- 🔄 **Async Processing** - Non-blocking scan execution
- 📈 **Observability** - Prometheus metrics and health checks
- 🔒 **SBOM & Provenance** - Full supply chain transparency

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | 8080 | Application HTTP port |
| `SPRING_PROFILES_ACTIVE` | docker | Spring profile (docker/prod/dev) |
| `ENABLE_DEBUG` | false | Enable remote debugging on port 5005 |
| `DEBUG_PORT` | 5005 | Remote debug port |
| `TZ` | UTC | Container timezone |

### Example with Custom Configuration

```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e TZ=America/New_York \
  --memory=1g \
  --name kubeguard \
  mvrao94/kubeguard:latest
```

### With PostgreSQL Database

```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/kubeguard \
  -e SPRING_DATASOURCE_USERNAME=kubeguard \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  --link postgres:postgres \
  --name kubeguard \
  mvrao94/kubeguard:latest
```

## Debug Mode

Enable remote debugging for development:

```bash
docker run -d \
  -p 8080:8080 \
  -p 5005:5005 \
  -e ENABLE_DEBUG=true \
  --name kubeguard \
  mvrao94/kubeguard:latest
```

Connect your IDE debugger to `localhost:5005`.

## Health Checks

The image includes built-in health checks:

```bash
# Check health status
docker inspect --format='{{.State.Health.Status}}' kubeguard

# Via API
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

## API Endpoints

Once running, access:

- **API Base**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Health Check**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`

### Example API Usage

```bash
# Scan a Kubernetes manifest
curl -X POST http://localhost:8080/api/v1/scans/manifest \
  -H "Content-Type: application/json" \
  -d '{
    "manifestContent": "apiVersion: v1\nkind: Pod\nmetadata:\n  name: test-pod\nspec:\n  containers:\n  - name: nginx\n    image: nginx:latest"
  }'

# Get scan results
curl http://localhost:8080/api/v1/scans/{scanId}
```

## Resource Requirements

### Minimum
- **Memory**: 512MB
- **CPU**: 0.5 cores

### Recommended
- **Memory**: 1GB
- **CPU**: 1 core

### Production
- **Memory**: 2GB
- **CPU**: 2 cores

```bash
# Run with resource limits
docker run -d \
  -p 8080:8080 \
  --memory=1g \
  --cpus=1 \
  --name kubeguard \
  mvrao94/kubeguard:latest
```

## Volumes

Mount volumes for persistent data:

```bash
docker run -d \
  -p 8080:8080 \
  -v $(pwd)/logs:/app/logs \
  -v $(pwd)/config:/app/config \
  -v $(pwd)/manifests:/app/manifests:ro \
  --name kubeguard \
  mvrao94/kubeguard:latest
```

## Docker Compose Example

Complete stack with PostgreSQL, Prometheus, and Grafana:

```yaml
version: '3.8'

services:
  kubeguard:
    image: mvrao94/kubeguard:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/kubeguard
      - SPRING_DATASOURCE_USERNAME=kubeguard
      - SPRING_DATASOURCE_PASSWORD=kubeguard123
    depends_on:
      - postgres
    restart: unless-stopped

  postgres:
    image: postgres:17-alpine
    environment:
      - POSTGRES_DB=kubeguard
      - POSTGRES_USER=kubeguard
      - POSTGRES_PASSWORD=kubeguard123
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  postgres_data:
```

## Security

### SBOM & Provenance
All images include comprehensive security attestations:

**Software Bill of Materials (SBOM)**
- Complete inventory of all software components
- SPDX 2.3 format
- Includes licenses and dependencies
- Cryptographically signed

**Provenance Attestation**
- SLSA Level 3 compliant
- Signed record of build process
- Source repository and commit tracking
- Build environment details

**Verify attestations:**
```bash
# View SBOM
docker buildx imagetools inspect mvrao94/kubeguard:latest \
  --format '{{ json .SBOM }}'

# View Provenance
docker buildx imagetools inspect mvrao94/kubeguard:latest \
  --format '{{ json .Provenance }}'

# Scan for vulnerabilities using SBOM
docker scout cves mvrao94/kubeguard:latest
```

For detailed documentation, see: [SBOM_AND_PROVENANCE.md](https://github.com/mvrao94/KubeGuard/blob/main/SBOM_AND_PROVENANCE.md)

### Non-Root User
The container runs as a non-root user (UID 1001) for enhanced security.

### Security Scanning
- Multi-stage builds (minimal attack surface)
- Automated vulnerability scanning in CI/CD
- Regular security updates
- Minimal base image (Alpine Linux)

### Best Practices
```bash
# Run with read-only root filesystem
docker run -d \
  -p 8080:8080 \
  --read-only \
  --tmpfs /tmp \
  --tmpfs /app/logs \
  --name kubeguard \
  mvrao94/kubeguard:latest

# Drop all capabilities
docker run -d \
  -p 8080:8080 \
  --cap-drop=ALL \
  --security-opt=no-new-privileges:true \
  --name kubeguard \
  mvrao94/kubeguard:latest
```

## Kubernetes Deployment

Deploy to Kubernetes:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kubeguard
spec:
  replicas: 2
  selector:
    matchLabels:
      app: kubeguard
  template:
    metadata:
      labels:
        app: kubeguard
    spec:
      containers:
      - name: kubeguard
        image: mvrao94/kubeguard:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: kubeguard
spec:
  selector:
    app: kubeguard
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

## Monitoring

### Prometheus Metrics

The application exposes Prometheus metrics at `/actuator/prometheus`:

```bash
# Scrape metrics
curl http://localhost:8080/actuator/prometheus
```

### Grafana Dashboard

Import the pre-built Grafana dashboard from the repository:
- Dashboard ID: Available in the GitHub repository
- Metrics include: request rates, response times, JVM metrics, scan statistics

## Troubleshooting

### Container Exits Immediately
```bash
# Check logs
docker logs kubeguard

# Common issues:
# - Database not ready (add depends_on with health check)
# - Port already in use (change port mapping)
# - Out of memory (increase container memory)
```

### Cannot Connect to Database
```bash
# Verify database is running
docker ps | grep postgres

# Check network connectivity
docker exec kubeguard ping postgres

# Verify environment variables
docker exec kubeguard env | grep DATASOURCE
```

### High Memory Usage
```bash
# Check current usage
docker stats kubeguard

# Adjust JVM settings
docker run -d -p 8080:8080 \
  -e JAVA_OPTS="-XX:MaxRAMPercentage=50.0" \
  mvrao94/kubeguard:latest
```

### Debug Logs
```bash
# Enable debug logging
docker run -d -p 8080:8080 \
  -e LOGGING_LEVEL_COM_KUBEGUARD=DEBUG \
  mvrao94/kubeguard:latest

# View logs
docker logs -f kubeguard
```

## Building from Source

```bash
# Clone repository
git clone https://github.com/mvrao94/KubeGuard.git
cd KubeGuard

# Build with Maven
mvn clean package

# Build Docker image
docker build -t kubeguard:custom -f scripts/Dockerfile .
```

## Image Details

- **Base Image**: eclipse-temurin:25-jre-alpine
- **Java Version**: 25
- **Spring Boot**: Latest
- **Size**: ~300-400MB (compressed)
- **Layers**: Optimized for caching
- **SBOM**: Attached (SPDX 2.3 format)
- **Provenance**: Attached (SLSA Level 3)
- **Signatures**: Cryptographically signed

## Links

- **GitHub**: https://github.com/mvrao94/KubeGuard
- **Documentation**: https://github.com/mvrao94/KubeGuard/blob/main/README.md
- **SBOM & Provenance**: https://github.com/mvrao94/KubeGuard/blob/main/SBOM_AND_PROVENANCE.md
- **Issues**: https://github.com/mvrao94/KubeGuard/issues
- **License**: MIT

## Support

For issues, questions, or contributions:
- Open an issue: https://github.com/mvrao94/KubeGuard/issues
- Pull requests welcome: https://github.com/mvrao94/KubeGuard/pulls
- Documentation: https://github.com/mvrao94/KubeGuard/tree/main/docs

## License

MIT License - see [LICENSE](https://github.com/mvrao94/KubeGuard/blob/main/LICENSE) for details.

---

**Note**: This is a security scanning tool. Always review scan results and apply fixes according to your organization's security policies.
