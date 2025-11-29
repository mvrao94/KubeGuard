# Multi-stage Dockerfile for KubeGuard
# Supports both local builds and CI/CD with pre-built artifacts

# Build arguments
ARG SKIP_BUILD=false
ARG APP_VERSION=latest
ARG BUILD_DATE
ARG VCS_REF

# ============================================================================
# Build stage - Maven compilation with dependency caching
# ============================================================================
FROM maven:3.9.11-eclipse-temurin-25 AS builder
ARG SKIP_BUILD

WORKDIR /app

# Copy POM first for better layer caching
COPY pom.xml* ./

# Download dependencies with cache mount for faster rebuilds
RUN --mount=type=cache,target=/root/.m2 \
    if [ "$SKIP_BUILD" = "false" ]; then \
      mvn dependency:go-offline -B; \
    fi

# Copy source and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    if [ "$SKIP_BUILD" = "false" ]; then \
      mvn clean package -DskipTests -B; \
    fi

# Extract Spring Boot layers for optimal Docker caching
RUN if [ "$SKIP_BUILD" = "false" ]; then \
      mkdir -p target/extracted && \
      java -Djarmode=layertools -jar target/kubeguard-*.jar extract --destination target/extracted; \
    fi

# ============================================================================
# Runtime stage - Minimal JRE image
# ============================================================================
FROM eclipse-temurin:25-jre-alpine AS runtime

# Install runtime dependencies
RUN apk --no-cache add \
    curl \
    dumb-init \
    tzdata && \
    rm -rf /var/cache/apk/*

# Set timezone (configurable)
ENV TZ=UTC

# Create non-root user with fixed UID/GID for security
RUN addgroup -g 1001 kubeguard && \
    adduser -D -u 1001 -G kubeguard kubeguard

# Create directories with proper permissions
RUN mkdir -p /app/logs /app/config /tmp && \
    chown -R kubeguard:kubeguard /app /tmp

WORKDIR /app

# Copy application layers for better caching
# Layer order: dependencies -> spring-boot-loader -> snapshot-dependencies -> application
ARG SKIP_BUILD=false
COPY --from=builder --chown=kubeguard:kubeguard \
    /app/target/extracted/dependencies/ ./
COPY --from=builder --chown=kubeguard:kubeguard \
    /app/target/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=kubeguard:kubeguard \
    /app/target/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=kubeguard:kubeguard \
    /app/target/extracted/application/ ./

# For CI/CD: copy pre-built JAR if SKIP_BUILD=true
# Usage: COPY target/kubeguard-*.jar /app/kubeguard.jar before building with SKIP_BUILD=true
COPY --chown=kubeguard:kubeguard target/kubeguard-*.jar* ./kubeguard.jar* 2>/dev/null || true

# Switch to non-root user
USER kubeguard

# Configuration via environment variables
ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=docker \
    JAVA_TOOL_OPTIONS="" \
    ENABLE_DEBUG=false \
    DEBUG_PORT=5005

# JVM optimization flags
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.backgroundpreinitializer.ignore=true"

# Expose application and debug ports
EXPOSE ${SERVER_PORT}
EXPOSE ${DEBUG_PORT}

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT}/actuator/health/liveness || exit 1

# OCI standard labels with build metadata
LABEL org.opencontainers.image.title="KubeGuard" \
      org.opencontainers.image.description="Kubernetes Security Scanner and Policy Enforcement" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.revision="${VCS_REF}" \
      org.opencontainers.image.vendor="KubeGuard Team" \
      org.opencontainers.image.source="https://github.com/mvrao94/KubeGuard" \
      org.opencontainers.image.documentation="https://github.com/mvrao94/KubeGuard/blob/main/README.md" \
      org.opencontainers.image.licenses="MIT" \
      org.opencontainers.image.url="https://github.com/mvrao94/KubeGuard" \
      maintainer="KubeGuard Team"

# Entrypoint with conditional debug mode
ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["sh", "-c", "\
    if [ \"$ENABLE_DEBUG\" = \"true\" ]; then \
        export JAVA_TOOL_OPTIONS=\"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${DEBUG_PORT} ${JAVA_TOOL_OPTIONS}\"; \
        echo \"Debug mode enabled on port ${DEBUG_PORT}\"; \
    fi; \
    exec java $JAVA_OPTS -jar kubeguard.jar"]