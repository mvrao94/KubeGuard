#!/bin/bash
# Docker build script with multiple modes

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
MODE="local"
PUSH=false
PLATFORM="linux/amd64"
TAG="latest"
REGISTRY="docker.io"

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --mode)
      MODE="$2"
      shift 2
      ;;
    --tag)
      TAG="$2"
      shift 2
      ;;
    --push)
      PUSH=true
      shift
      ;;
    --multi-arch)
      PLATFORM="linux/amd64,linux/arm64"
      shift
      ;;
    --registry)
      REGISTRY="$2"
      shift 2
      ;;
    --help)
      echo "Usage: $0 [OPTIONS]"
      echo ""
      echo "Options:"
      echo "  --mode MODE        Build mode: local (default) or ci"
      echo "  --tag TAG          Image tag (default: latest)"
      echo "  --push             Push image to registry"
      echo "  --multi-arch       Build for multiple architectures"
      echo "  --registry REG     Registry URL (default: docker.io)"
      echo "  --help             Show this help message"
      echo ""
      echo "Examples:"
      echo "  $0 --mode local --tag dev"
      echo "  $0 --mode ci --tag 1.0.0 --push --multi-arch"
      exit 0
      ;;
    *)
      echo -e "${RED}Unknown option: $1${NC}"
      exit 1
      ;;
  esac
done

# Get version from pom.xml
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null || echo "unknown")
BUILD_DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
VCS_REF=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")

IMAGE_NAME="${REGISTRY}/kubeguard"
FULL_TAG="${IMAGE_NAME}:${TAG}"

echo -e "${GREEN}Building KubeGuard Docker Image${NC}"
echo "================================"
echo "Mode:         $MODE"
echo "Version:      $VERSION"
echo "Tag:          $TAG"
echo "Platform:     $PLATFORM"
echo "Push:         $PUSH"
echo "Build Date:   $BUILD_DATE"
echo "VCS Ref:      $VCS_REF"
echo ""

# Change to repo root
cd "$(dirname "$0")/.."

if [ "$MODE" = "ci" ]; then
  echo -e "${YELLOW}Building in CI mode (using pre-built JAR)...${NC}"
  
  # Check if JAR exists
  if ! ls target/kubeguard-*.jar 1> /dev/null 2>&1; then
    echo -e "${RED}Error: JAR file not found in target/. Build with Maven first:${NC}"
    echo -e "${YELLOW}  mvn clean package${NC}"
    exit 1
  fi
  
  docker buildx build \
    --platform "$PLATFORM" \
    --build-arg SKIP_BUILD=true \
    --build-arg APP_VERSION="$VERSION" \
    --build-arg BUILD_DATE="$BUILD_DATE" \
    --build-arg VCS_REF="$VCS_REF" \
    --tag "$FULL_TAG" \
    --file scripts/Dockerfile \
    $([ "$PUSH" = true ] && echo "--push" || echo "--load") \
    .
    
else
  echo -e "${YELLOW}Building in local mode (building from source)...${NC}"
  
  # Ensure target directory exists (can be empty)
  mkdir -p target
  
  docker buildx build \
    --platform "$PLATFORM" \
    --build-arg SKIP_BUILD=false \
    --build-arg APP_VERSION="$VERSION" \
    --build-arg BUILD_DATE="$BUILD_DATE" \
    --build-arg VCS_REF="$VCS_REF" \
    --tag "$FULL_TAG" \
    --file scripts/Dockerfile \
    $([ "$PUSH" = true ] && echo "--push" || echo "--load") \
    .
fi

if [ $? -eq 0 ]; then
  echo ""
  echo -e "${GREEN}✓ Build successful!${NC}"
  echo ""
  echo "Image: $FULL_TAG"
  echo ""
  echo "Run with:"
  echo "  docker run -p 8080:8080 $FULL_TAG"
  echo ""
  echo "Or use docker-compose:"
  echo "  docker-compose -f scripts/docker-compose.yml up"
else
  echo -e "${RED}✗ Build failed${NC}"
  exit 1
fi
