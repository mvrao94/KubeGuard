# Project Structure

This document outlines the organized structure of the KubeGuard project.

## Root Directory

The root directory now contains only essential files:

- **Configuration**: `pom.xml`, `.gitignore`
- **Documentation**: `README.md`, `*.md` files

- **Convenience Scripts**: `build.sh/cmd`
- **Legacy Compatibility**: `docker-compose.yml` (points to scripts/)

## Organized Directories

### `/build-tools/`
Build and development tools:
- `mvnw`, `mvnw.cmd` - Maven wrapper scripts
- `.mvn/` - Maven wrapper configuration
- `Makefile` - Build automation

### `/scripts/`
Deployment and utility scripts:
- `docker-compose.yml` - Main Docker Compose configuration
- `Dockerfile` - Container build configuration
- `run-local.sh/cmd` - Local development environment setup

### `/k8s/`
Kubernetes manifests for production deployment

### `/helm/`
Helm charts for Kubernetes deployment

### `/monitoring/`
Observability configuration:
- Prometheus configuration
- Grafana dashboards
- Alert rules

### `/docs/`
Detailed documentation and GitHub Pages:
- `index.html` - GitHub Pages landing page
- Technical documentation files

### `/src/`
Source code (Java/Spring Boot)

## Quick Commands

```bash
# Build the project
./build.sh

# Run locally with all services
./scripts/run-local.sh

# Build with Docker
docker build -f scripts/Dockerfile .

# Run with Docker Compose
docker-compose -f scripts/docker-compose.yml up -d
```

## Migration Notes

- Maven wrapper moved to `build-tools/`
- Docker files moved to `scripts/`
- Convenience scripts created for backward compatibility
- All documentation updated to reflect new paths