# Etapa 1: Construcción
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar todo y compilar
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre

# Crear usuario no-root
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

# Copiar JAR desde etapa de build
COPY --from=build /app/target/*.jar app.jar

# Copiar el Oracle Wallet
COPY wallet /app/wallet

# Cambiar permisos (wallet necesita permisos de lectura)
RUN chown -R spring:spring /app && \
    chmod 700 /app/wallet && \
    chmod 600 /app/wallet/*

# Cambiar a usuario no-root
USER spring:spring

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV TNS_ADMIN=/app/wallet

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
