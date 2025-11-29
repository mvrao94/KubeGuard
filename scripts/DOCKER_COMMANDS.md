# Docker Commands Quick Reference

## Build Commands

### Local Development Build
```bash
# Linux/Mac
./scripts/build-docker.sh

# Windows
scripts\build-docker.cmd
```

### CI Build (Faster)
```bash
# Build JAR first
mvn clean package

# Linux/Mac
./scripts/build-docker.sh --mode ci --tag 1.0.0

# Windows
scripts\build-docker.cmd --mode ci --tag 1.0.0
```

### Multi-Architecture Build
```bash
# Linux/Mac
./scripts/build-docker.sh --mode ci --tag 1.0.0 --multi-arch --push

# Windows
scripts\build-docker.cmd --mode ci --tag 1.0.0 --multi-arch --push
```

## Run Commands

### Quick Start with Docker Compose
```bash
# Linux/Mac
./scripts/run-local.sh

# Windows
scripts\run-local.cmd

# Or manually
docker-compose -f scripts/docker-compose.yml up -d
```

### Run Single Container
```bash
# Basic
docker run -d -p 8080:8080 --name kubeguard kubeguard:latest

# With custom settings
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e TZ=America/New_York \
  --memory=1g \
  --name kubeguard \
  kubeguard:latest

# With debug mode
docker run -d -p 8080:8080 -p 5005:5005 \
  -e ENABLE_DEBUG=true \
  --name kubeguard \
  kubeguard:latest
```

## Management Commands

### View Logs
```bash
# Docker Compose
docker-compose -f scripts/docker-compose.yml logs -f kubeguard

# Single container
docker logs -f kubeguard
```

### Stop Services
```bash
# Docker Compose
docker-compose -f scripts/docker-compose.yml down

# Single container
docker stop kubeguard
docker rm kubeguard
```

### Restart Services
```bash
# Docker Compose
docker-compose -f scripts/docker-compose.yml restart kubeguard

# Single container
docker restart kubeguard
```

### Execute Commands in Container
```bash
# Get a shell
docker exec -it kubeguard sh

# Check Java version
docker exec kubeguard java -version

# Check environment
docker exec kubeguard env

# Check timezone
docker exec kubeguard date
```

## Debugging Commands

### Check Container Status
```bash
docker ps -a | grep kubeguard
```

### Inspect Container
```bash
docker inspect kubeguard
```

### Check Health
```bash
# Health status
docker inspect --format='{{.State.Health.Status}}' kubeguard

# Health check logs
docker inspect --format='{{json .State.Health}}' kubeguard | jq

# Via API
curl http://localhost:8080/actuator/health
```

### View Resource Usage
```bash
docker stats kubeguard
```

### Check Port Mappings
```bash
docker port kubeguard
```

## Cleanup Commands

### Remove Containers
```bash
# Stop and remove specific container
docker stop kubeguard && docker rm kubeguard

# Remove all stopped containers
docker container prune -f
```

### Remove Images
```bash
# Remove specific image
docker rmi kubeguard:latest

# Remove all unused images
docker image prune -a -f
```

### Remove Everything (Docker Compose)
```bash
# Stop and remove containers, networks, volumes
docker-compose -f scripts/docker-compose.yml down -v

# Also remove images
docker-compose -f scripts/docker-compose.yml down -v --rmi all
```

### Clean Build Cache
```bash
# Remove build cache
docker builder prune -af

# Remove everything (nuclear option)
docker system prune -af --volumes
```

## Troubleshooting Commands

### Check Docker Status
```bash
docker version
docker info
```

### Test Network Connectivity
```bash
# From host to container
curl http://localhost:8080/actuator/health

# From container to outside
docker exec kubeguard curl -I https://google.com

# Between containers (Docker Compose)
docker exec kubeguard ping postgres
```

### Check Database Connection
```bash
# Via Docker Compose
docker exec kubeguard curl http://localhost:8080/actuator/health/db
```

### View Application Logs
```bash
# Last 100 lines
docker logs --tail 100 kubeguard

# Follow logs
docker logs -f kubeguard

# With timestamps
docker logs -f --timestamps kubeguard

# Since specific time
docker logs --since 10m kubeguard
```

### Check Environment Variables
```bash
docker exec kubeguard env | grep -E '(SPRING|JAVA|SERVER)'
```

## Validation Commands

### Validate Dockerfile
```bash
# Linux/Mac
./scripts/validate-dockerfile.sh

# Windows
scripts\validate-dockerfile.cmd
```

### Check Image Metadata
```bash
# View all labels
docker inspect kubeguard:latest | jq '.[0].Config.Labels'

# View specific label
docker inspect kubeguard:latest | jq -r '.[0].Config.Labels["org.opencontainers.image.version"]'

# View image size
docker images kubeguard:latest
```

### Test Build Without Cache
```bash
docker build --no-cache -t kubeguard:test -f scripts/Dockerfile .
```

## Performance Commands

### Measure Build Time
```bash
# Linux/Mac
time ./scripts/build-docker.sh

# Windows (PowerShell)
Measure-Command { scripts\build-docker.cmd }
```

### Check Layer Sizes
```bash
docker history kubeguard:latest
```

### Analyze Image
```bash
# Using dive (if installed)
dive kubeguard:latest
```

## CI/CD Commands

### Simulate GitHub Actions Build
```bash
# Build JAR
mvn clean package

# Build Docker image with metadata
docker build \
  --build-arg SKIP_BUILD=true \
  --build-arg APP_VERSION=1.0.0 \
  --build-arg BUILD_DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ") \
  --build-arg VCS_REF=$(git rev-parse --short HEAD) \
  -t kubeguard:ci-test \
  -f scripts/Dockerfile \
  .
```

### Push to Registry
```bash
# Login
docker login

# Tag
docker tag kubeguard:latest yourusername/kubeguard:1.0.0

# Push
docker push yourusername/kubeguard:1.0.0
```

## Useful Aliases

Add these to your `.bashrc` or `.zshrc`:

```bash
# Quick aliases
alias kgbuild='./scripts/build-docker.sh'
alias kgup='docker-compose -f scripts/docker-compose.yml up -d'
alias kgdown='docker-compose -f scripts/docker-compose.yml down'
alias kglogs='docker-compose -f scripts/docker-compose.yml logs -f kubeguard'
alias kgsh='docker exec -it kubeguard sh'
alias kghealth='curl http://localhost:8080/actuator/health'
```

## Environment Variables Reference

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | 8080 | Application HTTP port |
| `SPRING_PROFILES_ACTIVE` | docker | Spring profile |
| `ENABLE_DEBUG` | false | Enable remote debugging |
| `DEBUG_PORT` | 5005 | Remote debug port |
| `TZ` | UTC | Container timezone |
| `JAVA_OPTS` | (optimized) | JVM options |

## Common Issues & Solutions

### Issue: "Cannot connect to Docker daemon"
```bash
# Check if Docker is running
docker ps

# Start Docker Desktop (Windows/Mac)
# Or start Docker service (Linux)
sudo systemctl start docker
```

### Issue: "Port already in use"
```bash
# Find what's using the port
# Linux/Mac
lsof -i :8080

# Windows
netstat -ano | findstr :8080

# Use different port
docker run -p 9000:8080 -e SERVER_PORT=8080 kubeguard:latest
```

### Issue: "JAR not found" in CI mode
```bash
# Build JAR first
mvn clean package

# Verify it exists
ls -la target/kubeguard-*.jar
```

### Issue: Container exits immediately
```bash
# Check logs
docker logs kubeguard

# Run in foreground to see errors
docker run --rm -p 8080:8080 kubeguard:latest
```

### Issue: Out of memory
```bash
# Increase container memory
docker run -p 8080:8080 --memory=2g kubeguard:latest

# Or adjust JVM settings
docker run -p 8080:8080 \
  -e JAVA_OPTS="-XX:MaxRAMPercentage=50.0" \
  kubeguard:latest
```
