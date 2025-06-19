# Étape build
FROM maven:3.9.4-openjdk-18 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Étape runtime
FROM eclipse-temurin:18-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/KboTest-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
