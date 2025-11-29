# Docker Changes Testing Checklist

Use this checklist to verify all Docker optimizations work correctly.

## Prerequisites
- [ ] Docker installed and running
- [ ] Docker Buildx enabled (for multi-arch)
- [ ] Maven installed (for CI mode tests)
- [ ] Git repository is clean

## 1. Local Build Tests

### Test 1.1: Basic Local Build
```bash
# Linux/Mac
./scripts/build-docker.sh

# Windows
scripts\build-docker.cmd
```
- [ ] Build completes successfully
- [ ] Image is created with tag `kubeguard:latest`
- [ ] No errors in output

### Test 1.2: Run Built Image
```bash
docker run -d -p 8080:8080 --name kubeguard-test kubeguard:latest
sleep 30
curl http://localhost:8080/actuator/health
docker logs kubeguard-test
docker stop kubeguard-test
docker rm kubeguard-test
```
- [ ] Container starts successfully
- [ ] Health endpoint returns 200 OK
- [ ] No errors in logs
- [ ] Application is accessible

## 2. CI Build Tests

### Test 2.1: Build JAR First
```bash
mvn clean package -DskipTests
```
- [ ] Build completes successfully
- [ ] JAR file exists in `target/kubeguard-*.jar`

### Test 2.2: CI Mode Build
```bash
# Linux/Mac
./scripts/build-docker.sh --mode ci --tag test-ci

# Windows
scripts\build-docker.cmd --mode ci --tag test-ci
```
- [ ] Build completes in <2 minutes
- [ ] Image is created with tag `kubeguard:test-ci`
- [ ] Build skips Maven compilation

### Test 2.3: Run CI Built Image
```bash
docker run -d -p 8080:8080 --name kubeguard-ci kubeguard:test-ci
sleep 30
curl http://localhost:8080/actuator/health
docker stop kubeguard-ci
docker rm kubeguard-ci
```
- [ ] Container starts successfully
- [ ] Application works correctly

## 3. Docker Compose Tests

### Test 3.1: Start Full Stack
```bash
# Linux/Mac
./scripts/run-local.sh

# Windows
scripts\run-local.cmd

# Or manually
docker-compose -f scripts/docker-compose.yml up -d
```
- [ ] All services start (kubeguard, postgres, prometheus, grafana)
- [ ] No errors in logs

### Test 3.2: Verify Services
```bash
# Wait for services to be ready
sleep 60

# Test KubeGuard
curl http://localhost:8080/actuator/health

# Test Prometheus
curl http://localhost:9090/-/healthy

# Test Grafana
curl http://localhost:3000/api/health
```
- [ ] KubeGuard is healthy
- [ ] Prometheus is accessible
- [ ] Grafana is accessible
- [ ] Database connection works

### Test 3.3: View Logs
```bash
docker-compose -f scripts/docker-compose.yml logs kubeguard
```
- [ ] No error messages
- [ ] Application started successfully
- [ ] Database connection established

### Test 3.4: Cleanup
```bash
docker-compose -f scripts/docker-compose.yml down -v
```
- [ ] All services stopped
- [ ] Volumes removed

## 4. Debug Mode Tests

### Test 4.1: Enable Debug Mode
```bash
docker run -d -p 8080:8080 -p 5005:5005 \
  -e ENABLE_DEBUG=true \
  --name kubeguard-debug \
  kubeguard:latest

docker logs kubeguard-debug | grep -i debug
```
- [ ] Container starts with debug enabled
- [ ] Debug port message appears in logs
- [ ] Port 5005 is listening

### Test 4.2: Connect Debugger (Optional)
- [ ] IDE can connect to localhost:5005
- [ ] Breakpoints work
- [ ] Can step through code

### Test 4.3: Cleanup
```bash
docker stop kubeguard-debug
docker rm kubeguard-debug
```

## 5. Environment Variable Tests

### Test 5.1: Custom Port
```bash
docker run -d -p 9000:9000 \
  -e SERVER_PORT=9000 \
  --name kubeguard-port \
  kubeguard:latest

sleep 30
curl http://localhost:9000/actuator/health
docker stop kubeguard-port
docker rm kubeguard-port
```
- [ ] Application runs on custom port
- [ ] Health check works on new port

### Test 5.2: Custom Profile
```bash
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  --name kubeguard-profile \
  kubeguard:latest

docker logs kubeguard-profile | grep -i "active profiles"
docker stop kubeguard-profile
docker rm kubeguard-profile
```
- [ ] Custom profile is activated
- [ ] Application starts correctly

### Test 5.3: Custom Timezone
```bash
docker run -d -p 8080:8080 \
  -e TZ=America/New_York \
  --name kubeguard-tz \
  kubeguard:latest

docker exec kubeguard-tz date
docker stop kubeguard-tz
docker rm kubeguard-tz
```
- [ ] Timezone is set correctly
- [ ] Date shows correct timezone

## 6. Build Cache Tests

### Test 6.1: First Build Time
```bash
docker builder prune -af
time ./scripts/build-docker.sh --tag cache-test-1
```
- [ ] Record build time: _______ seconds

### Test 6.2: Rebuild Without Changes
```bash
time ./scripts/build-docker.sh --tag cache-test-2
```
- [ ] Record build time: _______ seconds
- [ ] Build is significantly faster (should be <2 min)
- [ ] Uses cached layers

### Test 6.3: Rebuild With Code Change
```bash
# Make a small change to a Java file
echo "// test comment" >> src/main/java/com/kubeguard/KubeGuardApplication.java
time ./scripts/build-docker.sh --tag cache-test-3
git checkout src/main/java/com/kubeguard/KubeGuardApplication.java
```
- [ ] Record build time: _______ seconds
- [ ] Dependencies are cached
- [ ] Only application layer rebuilds

## 7. Image Metadata Tests

### Test 7.1: Check Labels
```bash
docker inspect kubeguard:latest | grep -A 20 Labels
```
- [ ] `org.opencontainers.image.title` is set
- [ ] `org.opencontainers.image.version` is set
- [ ] `org.opencontainers.image.created` is set (if built with args)
- [ ] `org.opencontainers.image.revision` is set (if built with args)

### Test 7.2: Check Image Size
```bash
docker images kubeguard:latest
```
- [ ] Image size is reasonable (<500MB)
- [ ] Record size: _______ MB

## 8. Multi-Architecture Tests (Optional)

### Test 8.1: Setup Buildx
```bash
docker buildx create --name multiarch --use
docker buildx inspect --bootstrap
```
- [ ] Buildx builder created
- [ ] Multiple platforms available

### Test 8.2: Multi-Arch Build
```bash
mvn clean package -DskipTests
./scripts/build-docker.sh --mode ci --tag multiarch-test --multi-arch
```
- [ ] Build completes for both architectures
- [ ] No errors

### Test 8.3: Verify Platforms
```bash
docker buildx imagetools inspect kubeguard:multiarch-test
```
- [ ] linux/amd64 present
- [ ] linux/arm64 present

## 9. CI/CD Integration Tests

### Test 9.1: Simulate CI Build
```bash
# Simulate GitHub Actions workflow
mvn clean package
docker build \
  --build-arg SKIP_BUILD=true \
  --build-arg APP_VERSION=1.0.0-test \
  --build-arg BUILD_DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ") \
  --build-arg VCS_REF=$(git rev-parse --short HEAD) \
  -t kubeguard:ci-test \
  -f scripts/Dockerfile \
  .
```
- [ ] Build completes successfully
- [ ] All build args are passed correctly

### Test 9.2: Verify CI Image
```bash
docker inspect kubeguard:ci-test | grep -A 20 Labels
```
- [ ] Version label matches
- [ ] Build date is set
- [ ] VCS ref matches git commit

## 10. Documentation Tests

### Test 10.1: Verify Documentation Files
- [ ] `scripts/DOCKER_QUICK_START.md` exists and is readable
- [ ] `scripts/README_DOCKER.md` exists and is readable
- [ ] `DOCKER_OPTIMIZATION_CHANGES.md` exists and is readable
- [ ] `README.md` has updated Docker section

### Test 10.2: Follow Quick Start Guide
- [ ] Follow steps in DOCKER_QUICK_START.md
- [ ] All commands work as documented
- [ ] Examples are accurate

## 11. Cleanup

### Test 11.1: Remove Test Images
```bash
docker rmi kubeguard:latest kubeguard:test-ci kubeguard:cache-test-1 \
  kubeguard:cache-test-2 kubeguard:cache-test-3 kubeguard:ci-test \
  kubeguard:multiarch-test 2>/dev/null || true
```
- [ ] Test images removed

### Test 11.2: Remove Test Containers
```bash
docker ps -a | grep kubeguard | awk '{print $1}' | xargs docker rm -f 2>/dev/null || true
```
- [ ] Test containers removed

## Summary

### Build Performance
- First build time: _______ seconds
- Cached rebuild time: _______ seconds
- CI mode build time: _______ seconds
- Improvement: _______ %

### Issues Found
List any issues encountered:
1. 
2. 
3. 

### Overall Status
- [ ] All tests passed
- [ ] Ready for production use
- [ ] Issues need to be addressed

### Notes
Add any additional observations:
