# Multi-stage Dockerfile: Build stage + Runtime stage
FROM eclipse-temurin:21-jdk-jammy AS build

# Set the working directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew gradlew.bat ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage (exclude plain JAR)
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

# Create a non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring
RUN chown -R spring:spring /app
USER spring

# Expose the port that your Spring Boot app runs on
EXPOSE 8080

# Set the entry point to run the JAR file with optimized JVM settings
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
