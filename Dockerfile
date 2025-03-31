# Dockerfile
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/wishlist-service.jar app.jar

EXPOSE 8080
EXPOSE 5005

ENTRYPOINT ["java", "-jar", "app.jar"]
