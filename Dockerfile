# Étape build : Maven + JDK 17 (Eclipse Temurin)
FROM maven:3.9.4-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Étape runtime : JRE 17 léger Alpine
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/KboTest-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
