# Multi-stage Dockerfile for EV Charging Platform
# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy parent POM and all module POMs first (for dependency caching)
COPY backend/pom.xml ./pom.xml
COPY backend/shared-kernel/pom.xml ./shared-kernel/pom.xml
COPY backend/gateway-module/pom.xml ./gateway-module/pom.xml
COPY backend/identity-module/pom.xml ./identity-module/pom.xml
COPY backend/station-module/pom.xml ./station-module/pom.xml
COPY backend/session-module/pom.xml ./session-module/pom.xml
COPY backend/billing-module/pom.xml ./billing-module/pom.xml
COPY backend/payment-module/pom.xml ./payment-module/pom.xml
COPY backend/vehicle-module/pom.xml ./vehicle-module/pom.xml
COPY backend/notification-module/pom.xml ./notification-module/pom.xml
COPY backend/device-gateway-module/pom.xml ./device-gateway-module/pom.xml
COPY backend/evcharging-app/pom.xml ./evcharging-app/pom.xml

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY backend/shared-kernel/src ./shared-kernel/src
COPY backend/gateway-module/src ./gateway-module/src
COPY backend/identity-module/src ./identity-module/src
COPY backend/station-module/src ./station-module/src
COPY backend/session-module/src ./session-module/src
COPY backend/billing-module/src ./billing-module/src
COPY backend/payment-module/src ./payment-module/src
COPY backend/vehicle-module/src ./vehicle-module/src
COPY backend/notification-module/src ./notification-module/src
COPY backend/device-gateway-module/src ./device-gateway-module/src
COPY backend/evcharging-app/src ./evcharging-app/src

# Build the application (skip tests for faster build, run in CI)
RUN mvn clean package -DskipTests -pl evcharging-app -am -B

# Stage 2: Runtime with distroless base
FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /app

# Copy built JAR from builder
COPY --from=builder /app/evcharging-app/target/evcharging-app-*.jar app.jar

# Expose port
EXPOSE 8080

# Health check (using curl not available in distroless, use Java)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD ["java", "-cp", "app.jar", "org.springframework.boot.loader.launch.JarLauncher", "--spring.profiles.active=docker", "--health.check"]

# Run as non-root user (distroless nonroot)
USER nonroot:nonroot

# JVM options for containerized environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseZGC -Djava.security.egd=file:/dev/./urandom"

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]