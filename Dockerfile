# Étape 1 : build avec Maven + JDK 18
FROM maven:3.9.4-eclipse-temurin-18 as builder

WORKDIR /app

# Copier les fichiers pom.xml + sources
COPY pom.xml .
COPY src ./src

# Build le jar, sans les tests
RUN mvn clean package -DskipTests

# Étape 2 : runtime avec JRE 18 léger
FROM eclipse-temurin:18-jre-alpine

WORKDIR /app

# Copier le jar construit depuis la phase builder
COPY --from=builder /app/target/KboTest-1.0-SNAPSHOT.jar app.jar

# Expose le port que ton app écoute (à adapter si besoin)
EXPOSE 8080

# Commande pour lancer ton app
CMD ["java", "-jar", "app.jar"]
