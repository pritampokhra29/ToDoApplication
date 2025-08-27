# =============================================================================
# Multi-stage Dockerfile for ToDo Application
# Optimized for production deployment with PostgreSQL
# =============================================================================

# Build stage
FROM maven:3.8.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven files for dependency caching
COPY pom.xml .
COPY mvnw* ./
COPY .mvn/ .mvn/

# Download dependencies first (better Docker layer caching)
RUN mvn dependency:go-offline -B || true

# Copy source code
COPY src src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -Dmaven.javadoc.skip=true

# Runtime stage - Production optimized
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create app user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Environment variables with defaults
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    JAVA_OPTS="-Xmx512m -Xms256m" \
    TZ=UTC

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

# Start the application with production configuration
CMD ["sh", "-c", "exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]