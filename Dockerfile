# Stage 1: Build stage
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy pom.xml and mvnw + .mvn folder first for dependency caching
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Copy source code
COPY src ./src

# Make Maven wrapper executable and build without tests
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

# Stage 2: Run stage (smaller image)
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the jar from builder
COPY --from=builder /app/target/colordetector-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
