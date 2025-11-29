# Scripts Directory

This directory contains all Docker-related scripts and configuration files for KubeGuard.

## 📁 Files Overview

### 🐳 Docker Files
- **`Dockerfile`** - Optimized multi-stage Dockerfile with BuildKit caching
- **`.dockerignore`** - Excludes unnecessary files from Docker build context
- **`docker-compose.yml`** - Full stack setup (app + database + monitoring)

### 🔨 Build Scripts
- **`build-docker.sh`** - Unified build script for Linux/Mac
- **`build-docker.cmd`** - Unified build script for Windows
- **`validate-dockerfile.sh`** - Dockerfile validation for Linux/Mac
- **`validate-dockerfile.cmd`** - Dockerfile validation for Windows

### 🚀 Run Scripts
- **`run-local.sh`** - Quick start script for Linux/Mac
- **`run-local.cmd`** - Quick start script for Windows

### 📚 Documentation
- **`README.md`** - This file (comprehensive guide)
- **`DOCKER_COMMANDS.md`** - Command reference
- **`TEST_DOCKER_CHANGES.md`** - Testing checklist

---

## 🚀 Quick Start

### Run Everything (Recommended)
```bash
# Linux/Mac
./scripts/run-local.sh

# Windows
scripts\run-local.cmd
```

This starts:
- KubeGuard application (port 8080)
- PostgreSQL database (port 5432)
- Prometheus (port 9090)
- Grafana (port 3000)

### Build Docker Image
```bash
# Linux/Mac
./scripts/build-docker.sh

# Windows
scripts\build-docker.cmd
```

### Build for CI/CD
```bash
# Build JAR first
mvn clean package

# Linux/Mac
./scripts/build-docker.sh --mode ci --tag 1.0.0

# Windows
scripts\build-docker.cmd --mode ci --tag 1.0.0
```

---

## 📖 Documentation Guide

### Quick Reference
This README contains everything you need to get started.

### Command Reference
For detailed command examples: **`DOCKER_COMMANDS.md`**

### Testing
For comprehensive testing: **`TEST_DOCKER_CHANGES.md`**

---

## 🔧 Build Modes

### Local Mode (Default)
- Builds JAR from source inside Docker
- No Maven/Java required on host
- Slower but self-contained
- Uses BuildKit cache for speed

```bash
./scripts/build-docker.sh
```

### CI Mode
- Uses pre-built JAR from target/
- Much faster (skips Maven build)
- Requires JAR to exist first
- Used in GitHub Actions

```bash
mvn clean package
./scripts/build-docker.sh --mode ci
```

---

## 🎯 Common Tasks

### Start Full Stack
```bash
docker-compose -f scripts/docker-compose.yml up -d
```

### View Logs
```bash
docker-compose -f scripts/docker-compose.yml logs -f kubeguard
```

### Stop Everything
```bash
docker-compose -f scripts/docker-compose.yml down
```

### Rebuild and Restart
```bash
docker-compose -f scripts/docker-compose.yml up -d --build
```

### Clean Everything
```bash
docker-compose -f scripts/docker-compose.yml down -v --rmi all
```

---

## 🐛 Troubleshooting

### Build Fails
```bash
# Validate Dockerfile
./scripts/validate-dockerfile.sh  # Linux/Mac
scripts\validate-dockerfile.cmd   # Windows

# Check Docker is running
docker ps

# Clean build cache
docker builder prune -af
```

### Container Won't Start
```bash
# Check logs
docker logs kubeguard

# Check if port is in use
# Linux/Mac
lsof -i :8080

# Windows
netstat -ano | findstr :8080
```

### Database Connection Issues
```bash
# Check if postgres is running
docker ps | grep postgres

# Check database logs
docker logs kubeguard-postgres
```

---

## 📊 Performance

### Build Times
- **First build**: ~5-7 minutes
- **Cached rebuild**: ~1-2 minutes (60-75% faster!)
- **CI mode**: ~30-60 seconds (90% faster!)

### Optimizations
- ✅ BuildKit cache mounts for Maven dependencies
- ✅ Layer caching for Docker images
- ✅ Multi-stage builds (minimal final image)
- ✅ .dockerignore reduces build context

---

## 🔒 Security

### Image Security
- Non-root user (UID 1001)
- Minimal base image (Alpine)
- No unnecessary packages
- Security scanning in CI/CD

### Best Practices
```bash
# Run with read-only filesystem
docker run --read-only --tmpfs /tmp kubeguard:latest

# Drop all capabilities
docker run --cap-drop=ALL kubeguard:latest

# Set resource limits
docker run --memory=1g --cpus=1 kubeguard:latest
```

---

## 🌐 Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | 8080 | Application HTTP port |
| `SPRING_PROFILES_ACTIVE` | docker | Spring profile |
| `ENABLE_DEBUG` | false | Enable remote debugging |
| `DEBUG_PORT` | 5005 | Remote debug port |
| `TZ` | UTC | Container timezone |

Example:
```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e TZ=America/New_York \
  kubeguard:latest
```

---

## 🧪 Testing

### Quick Test
```bash
# Start container
docker run -d -p 8080:8080 --name test kubeguard:latest

# Wait for startup
sleep 30

# Test health endpoint
curl http://localhost:8080/actuator/health

# Cleanup
docker stop test && docker rm test
```

### Full Test Suite
Follow the checklist in: **`TEST_DOCKER_CHANGES.md`**

---

## 📦 What's Included

### Docker Compose Stack
- **KubeGuard**: Main application
- **PostgreSQL**: Database (persistent storage)
- **Prometheus**: Metrics collection
- **Grafana**: Dashboards and visualization

### Access Points
- KubeGuard: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

---

## 🔗 Links

- **Main README**: [../README.md](../README.md)
- **Docker Hub**: https://hub.docker.com/r/mvrao94/kubeguard
- **GitHub**: https://github.com/mvrao94/KubeGuard
- **CI/CD**: https://github.com/mvrao94/KubeGuard/actions

---

## 📞 Support

Need help?
1. Check this README for quick answers
2. Review **`DOCKER_COMMANDS.md`** for command reference
3. Follow **`TEST_DOCKER_CHANGES.md`** for testing
4. Open an issue: https://github.com/mvrao94/KubeGuard/issues

---

## ✅ Quick Checklist

- [ ] Docker installed and running
- [ ] Read this README
- [ ] Tested `run-local.sh` or `run-local.cmd`
- [ ] Verified health check works
- [ ] Explored Swagger UI
- [ ] Checked Prometheus metrics
- [ ] Viewed Grafana dashboards

---

**Happy Dockering! 🐳**
