FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copier ton JAR compilé localement dans l'image
COPY target/KboTest-1.0-SNAPSHOT.jar app.jar

# Expose le port 8080 (même si Render utilisera la variable $PORT)
EXPOSE 8080

# Démarrer l'app avec le port dynamique fourni par Render
CMD ["java", "-jar", "app.jar"]
