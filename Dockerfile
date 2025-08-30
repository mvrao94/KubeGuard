# Multi-stage Dockerfile for KubeGuard

# Build stage
FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

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

# Copy the built jar from build stage
COPY --from=build --chown=kubeguard:kubeguard /app/target/kubeguard-*.jar kubeguard.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m -server -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
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