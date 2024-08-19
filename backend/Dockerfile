# First stage: Build the application
FROM gradle:8.8-jdk17 AS build

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle

# Copy the project files
COPY src src
COPY build.gradle .
COPY settings.gradle .

# Build the project
RUN ./gradlew build

# Second stage: Create the runtime image
FROM eclipse-temurin:17-jre-alpine

# Create app directory
RUN mkdir /app

# Set working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
