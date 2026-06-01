# ── Stage 1 : Build ──────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copier le pom.xml en premier pour profiter du cache des dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier les sources et compiler
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2 : Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier uniquement le JAR final
COPY --from=build /app/target/shopflow-backend-1.0.0.jar app.jar

# Port exposé (Render utilise la variable PORT)
EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
