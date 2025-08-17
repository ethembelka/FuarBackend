# Multi-stage build for Spring Boot application
FROM gradle:8.5-jdk17-alpine AS build

# Set working directory
WORKDIR /app

# Copy Gradle files
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .

# Copy Gradle wrapper
COPY gradlew .
COPY gradle/ gradle/

# Download dependencies first for better caching
RUN gradle dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the application
RUN gradle clean build --no-daemon -x test

# Production stage
FROM openjdk:17-jdk-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create app user for security
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -s /bin/sh -D appuser

# Set working directory
WORKDIR /app

# Create uploads directory
RUN mkdir -p /app/uploads && \
    chown -R appuser:appgroup /app

# Copy JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership
RUN chown appuser:appgroup app.jar

# Disable JMX and metrics to avoid Docker cgroup issues
ENV JAVA_TOOL_OPTIONS="-Dspring.jmx.enabled=false -Dmanagement.metrics.export.simple.enabled=false -Dmanagement.endpoints.web.exposure.include=none -Dmanagement.endpoint.health.enabled=false"

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "/app/app.jar"]
