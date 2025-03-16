# ----------------
# Build Stage
# ----------------
FROM openjdk:23-jdk-slim AS builder

# Copy source code
COPY . /home/gradle/src

# Set the working directory
WORKDIR /home/gradle/src

# Ensure gradlew has execution permissions
RUN chmod +x gradlew

# Build the application and skip tests to generate the jar file
RUN ./gradlew build -x test

# ----------------
# Package Stage
# ----------------
FROM openjdk:23-jdk-slim

# Create a directory for the application
RUN mkdir /app

# Copy the generated jar file from the build stage
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/user-service.jar

# Set environment variables
ENV POSTGRES_USER=admin \
    POSTGRES_PASSWORD=password

# Expose port 8083 inside the container
EXPOSE 8083

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "/app/user-service.jar"]
