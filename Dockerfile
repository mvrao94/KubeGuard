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

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

# Install curl for health checks
RUN apk --no-cache add curl

# Create non-root user
RUN addgroup -g 1001 kubeguard && \
    adduser -D -u 1001 -G kubeguard kubeguard

# Create directories
RUN mkdir -p /app/logs /app/config && \
    chown -R kubeguard:kubeguard /app

# Switch to non-root user
USER kubeguard

WORKDIR /app

# Copy JAR from build context
# - In CI: pre-built JAR downloaded from artifact to target/
# - Locally: built JAR from builder stage copied to target/
COPY --chown=kubeguard:kubeguard target/kubeguard-*.jar kubeguard.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Environment variables
ENV JAVA_OPTS="-Xms256m -Xmx512m -server -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar kubeguard.jar"]

# Labels for metadata
LABEL maintainer="KubeGuard Team" \
      version="1.0.0" \
      description="KubeGuard - Kubernetes Security Scanner" \
      org.opencontainers.image.source="https://github.com/mvrao94/KubeGuard" \
      org.opencontainers.image.documentation="https://github.com/mvrao94/KubeGuard/README.md" \
      org.opencontainers.image.licenses="MIT"