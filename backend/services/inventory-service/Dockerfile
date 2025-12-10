# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy shared libraries first
COPY libs ./libs
COPY build.gradle.kts settings.gradle.kts ./
COPY services/inventory-service ./services/inventory-service

# Build the application
RUN gradle :services:inventory-service:bootJar --no-daemon

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/services/inventory-service/build/libs/*.jar app.jar

# Expose port
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
