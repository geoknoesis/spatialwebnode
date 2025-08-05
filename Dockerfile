# Use OpenJDK 17 as base image
FROM openjdk:17-jre-slim

# Set working directory
WORKDIR /app

# Install necessary packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r hstpd && useradd -r -g hstpd hstpd

# Copy the application JAR
COPY node/build/libs/node-*.jar app.jar

# Copy configuration files
COPY node/src/main/resources/config/ config/

# Create logs directory
RUN mkdir -p logs && chown -R hstpd:hstpd logs

# Switch to non-root user
USER hstpd

# Expose ports (adjust as needed based on your transport configurations)
EXPOSE 8080 4001 1883

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Set JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
CMD ["--config", "config/node.yml"] 