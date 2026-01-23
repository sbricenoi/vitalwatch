# Etapa 1: Construcción
FROM maven:3.9-eclipse-temurin-17 AS build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Descargar dependencias (esta capa se cachea si pom.xml no cambia)
# Nota: Algunas dependencias de Oracle no están en Maven Central, se resolverán en el build
RUN mvn dependency:resolve -B || true

# Copiar el código fuente
COPY src ./src

# Construir la aplicación (sin compilar tests)
RUN mvn clean package -Dmaven.test.skip=true

# Etapa 2: Ejecución
# Cambiado de alpine a la versión estándar para compatibilidad con ARM64 (Apple Silicon)
FROM eclipse-temurin:17-jre

# Crear usuario no-root para seguridad
RUN groupadd -r spring && useradd -r -g spring spring

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR desde la etapa de construcción
COPY --from=build /app/target/*.jar app.jar

# Crear directorio para el Wallet de Oracle
RUN mkdir -p /app/wallet && chown -R spring:spring /app

# Cambiar a usuario no-root
USER spring:spring

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# Health check
# Cambiado de wget a curl ya que la imagen base no es alpine
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

# Comando de inicio
ENTRYPOINT ["bash", "-c", "java $JAVA_OPTS -jar app.jar"]
