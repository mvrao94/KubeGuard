# Multi-stage Dockerfile for KubeGuard
ARG SKIP_BUILD=false

# Build stage (only used for local builds)
FROM maven:3.9.11-eclipse-temurin-25 AS builder
ARG SKIP_BUILD

WORKDIR /app

# Only copy and build if not skipping
RUN if [ "$SKIP_BUILD" = "false" ]; then \
      echo "Building from source..."; \
    else \
      echo "Skipping build, using pre-built JAR..."; \
    fi

COPY pom.xml* ./
RUN if [ "$SKIP_BUILD" = "false" ]; then mvn dependency:go-offline -B; fi

COPY src ./src
RUN if [ "$SKIP_BUILD" = "false" ]; then mvn clean package -DskipTests -B; fi

# Extract layers for Spring Boot layered JAR (only if built)
RUN if [ "$SKIP_BUILD" = "false" ]; then \
      mkdir -p target/extracted && \
      java -Djarmode=layertools -jar target/kubeguard-*.jar extract --destination target/extracted; \
    fi

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

# Install curl and dumb-init for proper signal handling
RUN apk --no-cache add curl dumb-init

# Create non-root user
RUN addgroup -g 1001 kubeguard && \
    adduser -D -u 1001 -G kubeguard kubeguard

# Create directories with proper permissions
RUN mkdir -p /app/logs /app/config /tmp && \
    chown -R kubeguard:kubeguard /app /tmp

# Switch to non-root user
USER kubeguard

WORKDIR /app

# Copy Spring Boot layers for better caching
# - In CI: copy pre-built JAR as single layer
# - Locally: copy extracted layers from builder
ARG SKIP_BUILD=false
RUN if [ "$SKIP_BUILD" = "true" ]; then \
      echo "Using pre-built JAR from CI"; \
    else \
      echo "Using layered JAR from builder"; \
    fi

COPY --chown=kubeguard:kubeguard target/kubeguard-*.jar kubeguard.jar

# Expose port
EXPOSE 8080

# Health check using actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

# Spring Boot optimized JVM flags
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.backgroundpreinitializer.ignore=true"

ENV SPRING_PROFILES_ACTIVE=docker

# Use dumb-init for proper signal handling and exec form for better signal propagation
ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar kubeguard.jar"]

# OCI labels
LABEL org.opencontainers.image.title="KubeGuard" \
      org.opencontainers.image.description="Kubernetes Security Scanner" \
      org.opencontainers.image.vendor="KubeGuard Team" \
      org.opencontainers.image.source="https://github.com/mvrao94/KubeGuard" \
      org.opencontainers.image.documentation="https://github.com/mvrao94/KubeGuard/README.md" \
      org.opencontainers.image.licenses="MIT"