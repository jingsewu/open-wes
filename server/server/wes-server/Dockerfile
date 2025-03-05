# Use a base image with Java 17
FROM openjdk:17-jre-slim

# Set the working directory
WORKDIR /app

# Copy the built .jar file from the build/libs directory
COPY build/libs/wes-server.jar app.jar

# Expose the port the application runs on
EXPOSE 9010

# Command to run the application
CMD ["java", "-jar", "app.jar"]
