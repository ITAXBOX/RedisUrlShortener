# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the jar file into the container
COPY target/RedisUrlShortener-0.0.1-SNAPSHOT.jar /app/RedisUrlShortener.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "RedisUrlShortener.jar"]