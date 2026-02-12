# Etapa 1: Construcción
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar pom.xml y descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre

# Crear usuario no-root
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

# Copiar JAR desde etapa de build
COPY --from=build /app/target/*.jar app.jar

# Crear directorio para wallet (se monta como volumen)
RUN mkdir -p /app/wallet && chown -R spring:spring /app

# Cambiar a usuario no-root
USER spring:spring

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV TNS_ADMIN=/app/wallet

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
