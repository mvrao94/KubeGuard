# GraalVM Native Image Build Guide

## 🚀 Why Native Image?

GraalVM Native Image compiles Java applications ahead-of-time into native executables, providing:

| Metric | JVM Mode | Native Image | Improvement |
|--------|----------|--------------|-------------|
| **Cold Start** | 2-5 seconds | < 1 second | **5-10x faster** |
| **Memory Usage** | 300-500 MB | < 200 MB | **60% reduction** |
| **Container Size** | 166 MB | ~100 MB | **40% smaller** |
| **Startup Time** | Slow | Instant | **Critical for serverless** |

---

## 📋 Prerequisites

### Option 1: Install GraalVM Locally

```bash
# Using SDKMAN (recommended)
sdk install java 21.0.1-graal
sdk use java 21.0.1-graal

# Verify installation
java -version
# Should show: GraalVM CE 21.0.1

# Install native-image tool
gu install native-image
```

### Option 2: Use Docker (No Local Install)

```bash
# Build using Docker (recommended for CI/CD)
docker build -f Dockerfile.native -t kubeguard:native .
```

---

## 🔨 Building Native Image

### Method 1: Maven Build (Local)

```bash
# Generate secure API key (REQUIRED)
export KUBEGUARD_API_KEY=$(openssl rand -hex 32)

# Build native executable
./mvnw -Pnative native:compile

# Result: target/kubeguard (native executable)
ls -lh target/kubeguard
# Expected: ~80-100 MB

# Run native executable
./target/kubeguard

# Startup in ~500ms! 🚀
```

### Method 2: Docker Build (Recommended)

```bash
# Build native image container
docker build -f Dockerfile.native -t kubeguard:native .

# Run native container
docker run -d -p 8080:8080 \
  -e KUBEGUARD_API_KEY=$(openssl rand -hex 32) \
  kubeguard:native

# Check startup time
docker logs -f <container-id>
# Should see: "Started KubeguardApplication in 0.5 seconds"
```

### Method 3: Spring Boot Buildpacks

```bash
# Build using Cloud Native Buildpacks
./mvnw -Pnative spring-boot:build-image

# Run the image
docker run -d -p 8080:8080 \
  -e KUBEGUARD_API_KEY=$(openssl rand -hex 32) \
  kubeguard:0.0.4-SNAPSHOT
```

---

## 🧪 Verifying Native Image

### Test Startup Time

```bash
# JVM mode
time java -jar target/kubeguard-0.0.4-SNAPSHOT.jar
# Expected: 2-5 seconds

# Native mode
time ./target/kubeguard
# Expected: < 1 second
```

### Test Memory Usage

```bash
# Start native executable
./target/kubeguard &
PID=$!

# Wait for startup
sleep 5

# Check memory (Linux)
ps aux | grep $PID | awk '{print $6/1024 " MB"}'
# Expected: < 200 MB

# Check memory (macOS)
ps -o rss= -p $PID | awk '{print $1/1024 " MB"}'
# Expected: < 200 MB
```

### Test Functionality

```bash
# Generate API key
export KUBEGUARD_API_KEY=$(openssl rand -hex 32)

# Start native executable
./target/kubeguard &

# Wait for startup
sleep 5

# Test health endpoint
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# Test authenticated endpoint
curl -H "X-API-Key: $KUBEGUARD_API_KEY" \
  http://localhost:8080/api/v1/reports/analytics/summary
# Expected: JSON response with metrics
```

---

## 🐛 Troubleshooting

### Build Fails with Reflection Errors

**Problem**: Native Image doesn't support dynamic reflection by default.

**Solution**: Add reflection configuration:

```bash
# Generate reflection config
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image \
  -jar target/kubeguard-0.0.4-SNAPSHOT.jar

# Rebuild with config
./mvnw -Pnative native:compile
```

### Build Fails with "Out of Memory"

**Problem**: Native Image compilation requires significant memory.

**Solution**: Increase heap size:

```bash
export MAVEN_OPTS="-Xmx8g"
./mvnw -Pnative native:compile
```

### Runtime Fails with ClassNotFoundException

**Problem**: Some classes are loaded dynamically and not included in native image.

**Solution**: Add to `native-image.properties`:

```properties
Args = --initialize-at-build-time=org.slf4j \
       --initialize-at-run-time=io.netty \
       --enable-url-protocols=http,https
```

### Spring Boot Specific Issues

**Problem**: Spring Boot uses reflection and dynamic proxies extensively.

**Solution**: Spring Boot 3.x has better Native Image support. Ensure you're using:
- Spring Boot 3.0+
- Spring Native hints
- AOT processing enabled

---

## 📊 Performance Comparison

### Actual Measurements (After Build)

Run these commands to measure real performance:

```bash
# Build both versions
./mvnw clean package
./mvnw -Pnative native:compile

# Measure JVM startup
time java -jar target/kubeguard-0.0.4-SNAPSHOT.jar
# Record: _____ seconds

# Measure Native startup
time ./target/kubeguard
# Record: _____ seconds

# Measure JVM memory
java -jar target/kubeguard-0.0.4-SNAPSHOT.jar &
sleep 10
ps aux | grep kubeguard
# Record: _____ MB

# Measure Native memory
./target/kubeguard &
sleep 10
ps aux | grep kubeguard
# Record: _____ MB

# Measure container sizes
docker images | grep kubeguard
# Record: JVM _____ MB, Native _____ MB
```

### Expected Results

| Metric | JVM | Native | Winner |
|--------|-----|--------|--------|
| **Startup** | 2-5s | < 1s | ✅ Native |
| **Memory** | 300-500 MB | < 200 MB | ✅ Native |
| **Image Size** | 166 MB | ~100 MB | ✅ Native |
| **Build Time** | 30s | 5-10 min | ✅ JVM |
| **Throughput** | High | Similar | ≈ Tie |

---

## 🚀 Deployment

### Kubernetes Deployment (Native)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kubeguard-native
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: kubeguard
        image: kubeguard:native
        resources:
          requests:
            memory: "128Mi"  # 60% less than JVM
            cpu: "250m"      # 50% less than JVM
          limits:
            memory: "256Mi"
            cpu: "1000m"
        env:
        - name: KUBEGUARD_API_KEY
          valueFrom:
            secretKeyRef:
              name: kubeguard-secrets
              key: api-key
```

### Docker Compose (Native)

```yaml
version: '3.8'

services:
  kubeguard-native:
    image: kubeguard:native
    ports:
      - "8080:8080"
    environment:
      - KUBEGUARD_API_KEY=${KUBEGUARD_API_KEY}
    deploy:
      resources:
        limits:
          memory: 256M
        reservations:
          memory: 128M
```

---

## 🎯 When to Use Native Image

### ✅ Use Native Image If:

- **Fast startup is critical** (serverless, FaaS, CLI tools)
- **Memory is constrained** (edge computing, IoT)
- **Container density matters** (cost optimization)
- **Instant scale-up needed** (auto-scaling workloads)

### ❌ Use JVM If:

- **Build time matters** (rapid development cycles)
- **Dynamic features needed** (heavy reflection, dynamic proxies)
- **Peak throughput critical** (JIT optimizations)
- **Debugging required** (better tooling for JVM)

---

## 📚 Resources

- **GraalVM Documentation**: https://www.graalvm.org/latest/docs/
- **Spring Native**: https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html
- **Native Image Compatibility**: https://www.graalvm.org/latest/reference-manual/native-image/metadata/Compatibility/

---

## ✅ Success Criteria

After building Native Image, you should achieve:

- ✅ Startup time < 1 second
- ✅ Memory usage < 200 MB
- ✅ Container image < 120 MB
- ✅ All API endpoints functional
- ✅ Authentication working
- ✅ Database connectivity working

**If you achieve these metrics, you've successfully proven Java can compete with Go for cloud-native applications!** 🎉

---

**Last Updated**: 2025-11-30  
**Status**: Ready for testing  
**Build Time**: ~5-10 minutes  
**Expected Improvement**: 5-10x faster startup, 60% less memory
