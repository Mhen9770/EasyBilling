# Multi-stage build for Customer Service
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy gradle files
COPY build.gradle.kts settings.gradle.kts ./
COPY backend/libs ./backend/libs
COPY backend/services/customer-service ./backend/services/customer-service

# Build the application
RUN gradle :backend:services:customer-service:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar
COPY --from=build /app/backend/services/customer-service/build/libs/*.jar app.jar

# Expose port
EXPOSE 8085

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
