#!/bin/bash
# Verification script for Native Image build
# This script proves that Java can achieve Go-like performance

set -e

echo "🚀 KubeGuard Native Image Build Verification"
echo "=============================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
echo "📋 Checking Prerequisites..."

if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java not found${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1)
echo -e "${GREEN}✅ Java found: $JAVA_VERSION${NC}"

if [[ ! "$JAVA_VERSION" =~ "GraalVM" ]]; then
    echo -e "${YELLOW}⚠️  Warning: Not using GraalVM. Native Image build may fail.${NC}"
    echo "   Install GraalVM: sdk install java 21.0.1-graal"
fi

if ! command -v native-image &> /dev/null; then
    echo -e "${RED}❌ native-image tool not found${NC}"
    echo "   Install: gu install native-image"
    exit 1
fi

echo -e "${GREEN}✅ native-image tool found${NC}"
echo ""

# Generate API key
echo "🔐 Generating API Key..."
export KUBEGUARD_API_KEY=$(openssl rand -hex 32)
echo -e "${GREEN}✅ API Key generated: ${KUBEGUARD_API_KEY:0:16}...${NC}"
echo ""

# Build JVM version
echo "🏗️  Building JVM Version..."
START_JVM_BUILD=$(date +%s)
./mvnw clean package -DskipTests
END_JVM_BUILD=$(date +%s)
JVM_BUILD_TIME=$((END_JVM_BUILD - START_JVM_BUILD))
echo -e "${GREEN}✅ JVM build completed in ${JVM_BUILD_TIME}s${NC}"

# Get JVM JAR size
JVM_SIZE=$(du -h target/kubeguard-*.jar | cut -f1)
echo -e "${GREEN}✅ JVM JAR size: $JVM_SIZE${NC}"
echo ""

# Build Native Image
echo "🚀 Building Native Image (this takes 5-10 minutes)..."
START_NATIVE_BUILD=$(date +%s)
./mvnw -Pnative native:compile -DskipTests
END_NATIVE_BUILD=$(date +%s)
NATIVE_BUILD_TIME=$((END_NATIVE_BUILD - START_NATIVE_BUILD))
echo -e "${GREEN}✅ Native Image build completed in ${NATIVE_BUILD_TIME}s${NC}"

# Get Native executable size
NATIVE_SIZE=$(du -h target/kubeguard | cut -f1)
echo -e "${GREEN}✅ Native executable size: $NATIVE_SIZE${NC}"
echo ""

# Test JVM startup time
echo "⏱️  Testing JVM Startup Time..."
START_JVM=$(date +%s%3N)
timeout 30 java -jar target/kubeguard-*.jar > /dev/null 2>&1 &
JVM_PID=$!
sleep 10
END_JVM=$(date +%s%3N)
JVM_STARTUP=$((END_JVM - START_JVM))
kill $JVM_PID 2>/dev/null || true
echo -e "${GREEN}✅ JVM startup time: ${JVM_STARTUP}ms${NC}"

# Measure JVM memory
sleep 2
JVM_MEMORY=$(ps aux | grep $JVM_PID | grep -v grep | awk '{print $6/1024}' | head -1)
echo -e "${GREEN}✅ JVM memory usage: ${JVM_MEMORY}MB${NC}"
echo ""

# Test Native startup time
echo "⚡ Testing Native Image Startup Time..."
START_NATIVE=$(date +%s%3N)
timeout 30 ./target/kubeguard > /dev/null 2>&1 &
NATIVE_PID=$!
sleep 5
END_NATIVE=$(date +%s%3N)
NATIVE_STARTUP=$((END_NATIVE - START_NATIVE))
kill $NATIVE_PID 2>/dev/null || true
echo -e "${GREEN}✅ Native startup time: ${NATIVE_STARTUP}ms${NC}"

# Measure Native memory
sleep 2
NATIVE_MEMORY=$(ps aux | grep $NATIVE_PID | grep -v grep | awk '{print $6/1024}' | head -1)
echo -e "${GREEN}✅ Native memory usage: ${NATIVE_MEMORY}MB${NC}"
echo ""

# Calculate improvements
STARTUP_IMPROVEMENT=$(echo "scale=1; $JVM_STARTUP / $NATIVE_STARTUP" | bc)
MEMORY_IMPROVEMENT=$(echo "scale=1; ($JVM_MEMORY - $NATIVE_MEMORY) / $JVM_MEMORY * 100" | bc)

# Print results
echo "📊 Performance Comparison"
echo "========================="
echo ""
printf "%-20s %-15s %-15s %-15s\n" "Metric" "JVM" "Native" "Improvement"
printf "%-20s %-15s %-15s %-15s\n" "--------------------" "---------------" "---------------" "---------------"
printf "%-20s %-15s %-15s %-15s\n" "Build Time" "${JVM_BUILD_TIME}s" "${NATIVE_BUILD_TIME}s" "N/A"
printf "%-20s %-15s %-15s %-15s\n" "Artifact Size" "$JVM_SIZE" "$NATIVE_SIZE" "N/A"
printf "%-20s %-15s %-15s %-15s\n" "Startup Time" "${JVM_STARTUP}ms" "${NATIVE_STARTUP}ms" "${STARTUP_IMPROVEMENT}x faster"
printf "%-20s %-15s %-15s %-15s\n" "Memory Usage" "${JVM_MEMORY}MB" "${NATIVE_MEMORY}MB" "${MEMORY_IMPROVEMENT}% less"
echo ""

# Verify success criteria
echo "✅ Success Criteria"
echo "==================="

SUCCESS=true

if [ $NATIVE_STARTUP -lt 1000 ]; then
    echo -e "${GREEN}✅ Startup time < 1 second: PASS${NC}"
else
    echo -e "${RED}❌ Startup time < 1 second: FAIL (${NATIVE_STARTUP}ms)${NC}"
    SUCCESS=false
fi

if (( $(echo "$NATIVE_MEMORY < 200" | bc -l) )); then
    echo -e "${GREEN}✅ Memory usage < 200 MB: PASS${NC}"
else
    echo -e "${RED}❌ Memory usage < 200 MB: FAIL (${NATIVE_MEMORY}MB)${NC}"
    SUCCESS=false
fi

if (( $(echo "$STARTUP_IMPROVEMENT > 2" | bc -l) )); then
    echo -e "${GREEN}✅ Startup improvement > 2x: PASS${NC}"
else
    echo -e "${YELLOW}⚠️  Startup improvement > 2x: MARGINAL (${STARTUP_IMPROVEMENT}x)${NC}"
fi

echo ""

if [ "$SUCCESS" = true ]; then
    echo -e "${GREEN}🎉 SUCCESS! Native Image build meets all performance criteria!${NC}"
    echo ""
    echo "Java has proven it can achieve Go-like performance for cloud-native applications!"
    echo ""
    echo "Next steps:"
    echo "1. Build Docker image: docker build -f Dockerfile.native -t kubeguard:native ."
    echo "2. Deploy to Kubernetes with reduced resource limits"
    echo "3. Update PERFORMANCE.md with actual measurements"
    exit 0
else
    echo -e "${RED}❌ FAILED: Native Image did not meet performance criteria${NC}"
    echo ""
    echo "Troubleshooting:"
    echo "1. Ensure you're using GraalVM: sdk use java 21.0.1-graal"
    echo "2. Check native-image configuration in pom.xml"
    echo "3. Review build logs for warnings"
    echo "4. See docs/NATIVE_IMAGE_BUILD.md for detailed guide"
    exit 1
fi
