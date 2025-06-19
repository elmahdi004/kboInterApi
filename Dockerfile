# Étape 1 : build avec Maven + OpenJDK 17
FROM maven:3.9.4-openjdk-17 AS builder

WORKDIR /app

# Copier pom.xml et sources
COPY pom.xml .
COPY src ./src

# Build le jar sans tests
RUN mvn clean package -DskipTests

# Étape 2 : runtime avec JRE 17 léger
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier le jar généré depuis builder
COPY --from=builder /app/target/KboTest-1.0-SNAPSHOT.jar app.jar

# Expose le port 8080 (à adapter si besoin)
EXPOSE 8080

# Lancer l’application
CMD ["java", "-jar", "app.jar"]
