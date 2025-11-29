# Docker Build Guide

## Quick Start

### Local Development Build
```bash
docker build -t kubeguard:latest -f scripts/Dockerfile .
```

### CI/CD Build (with pre-built JAR)
```bash
# Build JAR first
mvn clean package

# Build Docker image
docker build \
  --build-arg SKIP_BUILD=true \
  --build-arg APP_VERSION=1.0.0 \
  --build-arg BUILD_DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ") \
  --build-arg VCS_REF=$(git rev-parse --short HEAD) \
  -t kubeguard:1.0.0 \
  -f scripts/Dockerfile .
```

### Multi-Architecture Build
```bash
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  --build-arg APP_VERSION=1.0.0 \
  -t kubeguard:1.0.0 \
  -f scripts/Dockerfile \
  --push .
```

## Running the Container

### Basic Run
```bash
docker run -p 8080:8080 kubeguard:latest
```

### With Custom Configuration
```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SERVER_PORT=8080 \
  -v $(pwd)/config:/app/config \
  kubeguard:latest
```

### With Debug Mode
```bash
docker run -p 8080:8080 -p 5005:5005 \
  -e ENABLE_DEBUG=true \
  kubeguard:latest
```

### With Resource Limits
```bash
docker run -p 8080:8080 \
  --memory=512m \
  --cpus=1 \
  -e JAVA_OPTS="-XX:MaxRAMPercentage=75.0" \
  kubeguard:latest
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | 8080 | Application HTTP port |
| `SPRING_PROFILES_ACTIVE` | docker | Spring profile to activate |
| `ENABLE_DEBUG` | false | Enable remote debugging |
| `DEBUG_PORT` | 5005 | Remote debug port |
| `JAVA_OPTS` | (see Dockerfile) | JVM options |
| `TZ` | UTC | Container timezone |

## Build Arguments

| Argument | Default | Description |
|----------|---------|-------------|
| `SKIP_BUILD` | false | Skip Maven build (use pre-built JAR) |
| `APP_VERSION` | latest | Application version for labels |
| `BUILD_DATE` | - | Build timestamp (ISO 8601) |
| `VCS_REF` | - | Git commit hash |

## Optimizations

- **Layer caching**: Dependencies cached separately from application code
- **Maven cache**: Uses BuildKit cache mounts for faster rebuilds
- **Multi-stage**: Minimal runtime image (JRE only, no build tools)
- **Non-root user**: Runs as UID 1001 for security
- **Health checks**: Built-in liveness probe

## Recommended Container Resources

| Heap Size | Memory Limit | CPU |
|-----------|--------------|-----|
| < 512MB | 768MB | 0.5 |
| 512MB - 1GB | 1.5GB | 1.0 |
| 1GB - 2GB | 3GB | 2.0 |

Memory limit should be ~1.5x heap size to account for non-heap memory.
