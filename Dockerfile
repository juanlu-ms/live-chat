FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src

RUN mvn package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/target/live-chat-1.0-SNAPSHOT.jar app.jar
EXPOSE 8025
CMD ["java", "-jar", "app.jar"]