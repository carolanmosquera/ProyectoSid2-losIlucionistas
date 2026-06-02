# Etapa 1: Construcción (Build)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compilamos el proyecto saltando los tests
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Run)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copiamos el ejecutable generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Comando para arrancar Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]