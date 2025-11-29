# Docker Quick Start Guide

## Quick Commands

### Local Development
```bash
# Build and run everything with docker-compose
./scripts/run-local.sh        # Linux/Mac
scripts\run-local.cmd         # Windows

# Or manually
docker-compose -f scripts/docker-compose.yml up -d
```

### Build Docker Image

**Linux/Mac:**
```bash
# Local build (builds from source)
./scripts/build-docker.sh

# CI build (uses pre-built JAR)
mvn clean package
./scripts/build-docker.sh --mode ci --tag 1.0.0

# Multi-architecture build and push
./scripts/build-docker.sh --mode ci --tag 1.0.0 --multi-arch --push
```

**Windows:**
```cmd
REM Local build
scripts\build-docker.cmd

REM CI build
mvn clean package
scripts\build-docker.cmd --mode ci --tag 1.0.0

REM Multi-architecture build and push
scripts\build-docker.cmd --mode ci --tag 1.0.0 --multi-arch --push
```

## Build Modes

### Local Mode (Default)
- Builds JAR from source inside Docker
- No Maven/Java required on host
- Slower but self-contained
- Uses BuildKit cache for dependencies
- Target directory can be empty or non-existent

```bash
./scripts/build-docker.sh --mode local
```

### CI Mode
- Uses pre-built JAR from target/
- Much faster (skips Maven build)
- **Important**: JAR must exist in target/ before building
- Used in GitHub Actions

```bash
# Build JAR first
mvn clean package

# Then build Docker image
./scripts/build-docker.sh --mode ci
```

## Environment Variables

Set these when running the container:

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SERVER_PORT=8080 \
  -e ENABLE_DEBUG=false \
  -e TZ=America/New_York \
  kubeguard:latest
```

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | 8080 | HTTP port |
| `SPRING_PROFILES_ACTIVE` | docker | Spring profile |
| `ENABLE_DEBUG` | false | Remote debugging |
| `DEBUG_PORT` | 5005 | Debug port |
| `TZ` | UTC | Timezone |
| `JAVA_OPTS` | (optimized) | JVM options |

## Debug Mode

Enable remote debugging:

```bash
docker run -p 8080:8080 -p 5005:5005 \
  -e ENABLE_DEBUG=true \
  kubeguard:latest
```

Connect your IDE debugger to `localhost:5005`.

## Docker Compose

The `docker-compose.yml` includes:
- KubeGuard application
- PostgreSQL database
- Prometheus monitoring
- Grafana dashboards

```bash
# Start all services
docker-compose -f scripts/docker-compose.yml up -d

# View logs
docker-compose -f scripts/docker-compose.yml logs -f kubeguard

# Stop all services
docker-compose -f scripts/docker-compose.yml down

# Stop and remove volumes
docker-compose -f scripts/docker-compose.yml down -v
```

## Access Points

After starting with docker-compose:

- **KubeGuard API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Actuator Health**: http://localhost:8080/actuator/health
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

## Troubleshooting

### Build fails with "JAR not found"
```bash
# In CI mode, build JAR first
mvn clean package
./scripts/build-docker.sh --mode ci
```

### Container exits immediately
```bash
# Check logs
docker logs <container-id>

# Common issues:
# - Database not ready (wait for postgres healthcheck)
# - Port already in use (change SERVER_PORT)
# - Out of memory (increase container memory)
```

### Slow builds
```bash
# Use CI mode after building JAR
mvn clean package
./scripts/build-docker.sh --mode ci

# Or enable BuildKit for better caching
export DOCKER_BUILDKIT=1
```

### Debug not working
```bash
# Ensure debug port is exposed and enabled
docker run -p 8080:8080 -p 5005:5005 \
  -e ENABLE_DEBUG=true \
  kubeguard:latest
```

## CI/CD Integration

The GitHub Actions workflow automatically:
1. Builds and tests with Maven
2. Downloads the built JAR
3. Builds multi-arch Docker image with metadata
4. Pushes to Docker registry
5. Includes SBOM and provenance

See `.github/workflows/ci.yml` for details.

## Performance Tips

1. **Use layer caching**: Dependencies rarely change, so they're cached separately
2. **BuildKit cache**: Enabled by default in CI, speeds up Maven downloads
3. **Multi-stage builds**: Only runtime dependencies in final image
4. **Resource limits**: Set appropriate memory limits (1.5x heap size)

```bash
docker run -p 8080:8080 \
  --memory=1g \
  --cpus=1 \
  -e JAVA_OPTS="-XX:MaxRAMPercentage=75.0" \
  kubeguard:latest
```
