FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=builder /build/target/auth-service-*.jar app.jar
COPY --from=builder /build/src/main/resources/keys /app/keys
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=dev", "/app/app.jar"]