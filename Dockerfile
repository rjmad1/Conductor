# Use a lightweight JRE base image
FROM eclipse-temurin:21-jre-alpine

# Set up a non-root user for security
RUN addgroup -S conductor && adduser -S conductor -G conductor

WORKDIR /app

# Copy the pre-built Spring Boot executable jar
COPY --chown=conductor:conductor platform/workflow/build/libs/workflow-1.0.0.jar app.jar

# Switch to the non-root user
USER conductor

# Configure JVM options for container awareness and performance
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8090

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
