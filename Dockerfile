FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY auth-service/target/auth-service-*.jar app.jar
COPY auth-service/src/main/resources/keys /app/keys
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=dev", "/app/app.jar"]