#!/bin/bash
# Validate Dockerfile syntax without building

set -e

echo "Validating Dockerfile syntax..."

# Check if docker is available
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed or not in PATH"
    exit 1
fi

# Check if Dockerfile exists
if [ ! -f "scripts/Dockerfile" ]; then
    echo "❌ Dockerfile not found at scripts/Dockerfile"
    exit 1
fi

# Validate syntax using docker build with --check flag (if available)
# Or use hadolint if installed
if command -v hadolint &> /dev/null; then
    echo "Running hadolint..."
    hadolint scripts/Dockerfile
    echo "✓ Hadolint validation passed"
else
    echo "ℹ️  Hadolint not installed, skipping linting"
    echo "   Install with: brew install hadolint (Mac) or see https://github.com/hadolint/hadolint"
fi

# Basic syntax check - try to parse the Dockerfile
echo "Checking basic Dockerfile syntax..."

# Check for common issues
if grep -q "COPY.*2>/dev/null" scripts/Dockerfile; then
    echo "❌ Found shell redirection in COPY command (not supported)"
    exit 1
fi

if grep -q "COPY.*||" scripts/Dockerfile; then
    echo "❌ Found shell operator in COPY command (not supported)"
    exit 1
fi

# Check for required stages
if ! grep -q "FROM.*AS builder" scripts/Dockerfile; then
    echo "❌ Builder stage not found"
    exit 1
fi

if ! grep -q "FROM.*AS runtime" scripts/Dockerfile; then
    echo "❌ Runtime stage not found"
    exit 1
fi

# Check for required instructions
if ! grep -q "WORKDIR" scripts/Dockerfile; then
    echo "❌ WORKDIR instruction not found"
    exit 1
fi

if ! grep -q "EXPOSE" scripts/Dockerfile; then
    echo "❌ EXPOSE instruction not found"
    exit 1
fi

if ! grep -q "HEALTHCHECK" scripts/Dockerfile; then
    echo "⚠️  Warning: HEALTHCHECK instruction not found"
fi

echo ""
echo "✓ Dockerfile validation passed!"
echo ""
echo "To build the image:"
echo "  Local mode:  ./scripts/build-docker.sh"
echo "  CI mode:     mvn clean package && ./scripts/build-docker.sh --mode ci"
