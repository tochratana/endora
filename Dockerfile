# Optimized Dockerfile using pre-built JAR
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the pre-built JAR file from local build (using wildcard to match the actual JAR)
COPY build/libs/*.jar app.jar

# Create a non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring
RUN chown -R spring:spring /app
USER spring

# Expose the port that your Spring Boot app runs on
EXPOSE 8080

# Set the entry point to run the JAR file with optimized JVM settings
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
