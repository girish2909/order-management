# Use a lightweight base image with Java 21
FROM eclipse-temurin:21-jdk-alpine

# Set metadata
LABEL maintainer="your-email@example.com"

# Create a volume for temporary files (optional but good practice for Tomcat)
VOLUME /tmp

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from the target directory to the container
# Ensure you run 'mvn clean install' before building the Docker image
COPY target/order-management-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]