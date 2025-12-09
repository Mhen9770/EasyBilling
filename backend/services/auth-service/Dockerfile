# Multi-stage build for Auth Service
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

# Copy library modules
COPY libs libs

# Copy auth-service
COPY services/auth-service services/auth-service

# Build the application
RUN chmod +x gradlew
RUN ./gradlew :services:auth-service:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built JAR from builder
COPY --from=builder /app/services/auth-service/build/libs/*.jar app.jar

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/auth-service/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
