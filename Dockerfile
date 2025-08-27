# Build stage
FROM maven:3.8.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven files
COPY pom.xml .

# Download dependencies (for better caching)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]