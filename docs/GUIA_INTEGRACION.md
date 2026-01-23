# 🚀 Guía de Integración - VitalWatch

## 📋 Índice

1. [Prerrequisitos](#prerrequisitos)
2. [Instalación Rápida](#instalación-rápida)
3. [Configuración Manual](#configuración-manual)
4. [Verificación](#verificación)
5. [Troubleshooting](#troubleshooting)

---

## ✅ Prerrequisitos

### Software Requerido

| Software | Versión Mínima | Verificación |
|----------|----------------|--------------|
| Docker Desktop | 20.10+ | `docker --version` |
| Docker Compose | 2.0+ | `docker-compose --version` |
| Git | 2.0+ | `git --version` |

### Recursos del Sistema

- **RAM**: 4GB mínimo (8GB recomendado)
- **Disco**: 5GB libres
- **CPU**: 2 cores mínimo

### Puertos Requeridos

Asegúrate de que estos puertos estén libres:

```bash
# Verificar puertos
lsof -i :4200  # Frontend
lsof -i :8080  # Backend
lsof -i :8000  # API Gateway
```

---

## 🎯 Instalación Rápida

### Opción 1: Script Automático (Recomendado)

```bash
# 1. Clonar o navegar al proyecto
cd "Semana 3 Sumativa 2 v2"

# 2. Ejecutar script de despliegue
./deploy.sh
```

El script automáticamente:
- ✅ Verifica prerrequisitos
- ✅ Valida configuración de Oracle
- ✅ Construye imágenes Docker
- ✅ Inicia todos los servicios
- ✅ Ejecuta health checks
- ✅ Muestra URLs de acceso

### Opción 2: Docker Compose Manual

```bash
# 1. Construir imágenes
docker-compose build

# 2. Iniciar servicios
docker-compose up -d

# 3. Ver logs
docker-compose logs -f
```

---

## ⚙️ Configuración Manual

### 1. Configuración de Oracle Cloud

#### A. Verificar Wallet

```bash
# El Wallet debe estar en la raíz del proyecto
ls -la Wallet_S58ONUXCX4C1QXE9/

# Archivos requeridos:
# - cwallet.sso
# - keystore.jks
# - truststore.jks
# - tnsnames.ora
# - sqlnet.ora
# - ojdbc.properties
```

#### B. Configurar Contraseña

Editar `backend/src/main/resources/application.properties`:

```properties
spring.datasource.password=$-123.Sb-123
```

#### C. Ejecutar Scripts de Base de Datos

```sql
-- 1. Conectarse a Oracle Cloud
sqlplus ADMIN/"$-123.Sb-123"@s58onuxcx4c1qxe9_high

-- 2. Ejecutar scripts en orden
@database/schema.sql
@database/data.sql
@database/usuarios.sql
```

### 2. Configuración del Backend

#### application.properties

```properties
# Perfil activo
spring.profiles.active=dev

# Oracle Database
spring.datasource.url=jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCPS)(HOST=adb.sa-santiago-1.oraclecloud.com)(PORT=1522))(CONNECT_DATA=(SERVICE_NAME=s58onuxcx4c1qxe9_high.adb.oraclecloud.com))(SECURITY=(SSL_SERVER_DN_MATCH=YES)))
spring.datasource.username=ADMIN
spring.datasource.password=$-123.Sb-123
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# JPA
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect

# Server
server.port=8080

# Logging
logging.level.com.hospital.vitalwatch=INFO
```

#### Wallet Configuration (docker-compose.yml)

```yaml
environment:
  JAVA_TOOL_OPTIONS: >-
    -Djavax.net.ssl.trustStore=/app/wallet/truststore.jks
    -Djavax.net.ssl.trustStorePassword=$-123.Sb-123
    -Djavax.net.ssl.keyStore=/app/wallet/keystore.jks
    -Djavax.net.ssl.keyStorePassword=$-123.Sb-123
    -Doracle.net.tns_admin=/app/wallet
    -Doracle.net.wallet_location=/app/wallet

volumes:
  - ./Wallet_S58ONUXCX4C1QXE9:/app/wallet:ro
```

### 3. Configuración del Frontend

#### environment.ts

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1'
};
```

#### environment.prod.ts

```typescript
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080/api/v1'
};
```

### 4. Configuración del API Gateway

#### kong.yml

```yaml
_format_version: "3.0"

services:
  - name: vitalwatch-backend
    url: http://backend:8080
    routes:
      - name: backend-route
        paths:
          - /api
    plugins:
      - name: cors
        config:
          origins:
            - "*"
          methods:
            - GET
            - POST
            - PUT
            - DELETE
            - OPTIONS
          headers:
            - Accept
            - Authorization
            - Content-Type
          exposed_headers:
            - X-Auth-Token
          credentials: true
          max_age: 3600
      
      - name: rate-limiting
        config:
          minute: 100
          policy: local
      
      - name: request-size-limiting
        config:
          allowed_payload_size: 10
```

---

## 🔍 Verificación

### 1. Verificar Servicios Docker

```bash
# Ver contenedores en ejecución
docker-compose ps

# Salida esperada:
# NAME                    STATUS              PORTS
# vitalwatch-frontend     Up 2 minutes        0.0.0.0:4200->80/tcp
# vitalwatch-backend      Up 2 minutes        0.0.0.0:8080->8080/tcp
# vitalwatch-api-gateway  Up 2 minutes        0.0.0.0:8000->8000/tcp
```

### 2. Health Checks

```bash
# Backend Health
curl http://localhost:8080/api/v1/health

# Respuesta esperada:
# {
#   "status": "UP",
#   "components": {
#     "db": { "status": "UP" }
#   }
# }

# Database Health
curl http://localhost:8080/api/v1/health/database

# Frontend
curl http://localhost:4200

# API Gateway
curl http://localhost:8000/api/v1/health
```

### 3. Verificar Logs

```bash
# Logs del backend
docker-compose logs backend | tail -50

# Buscar líneas como:
# "Started VitalWatchApplication in X seconds"
# "HikariPool-1 - Start completed"

# Logs del frontend
docker-compose logs frontend | tail -20

# Logs del API Gateway
docker-compose logs api-gateway | tail -20
```

### 4. Prueba de Login

```bash
# Test de login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@vitalwatch.com",
    "password": "Admin123!"
  }'

# Respuesta esperada:
# {
#   "traceId": "...",
#   "code": "200",
#   "message": "Login exitoso",
#   "data": {
#     "token": "...",
#     "tipo": "Bearer",
#     "id": 1,
#     "nombre": "Administrador Principal",
#     "email": "admin@vitalwatch.com",
#     "rol": "ADMIN"
#   }
# }
```

### 5. Verificar Swagger UI

Abrir en navegador:
```
http://localhost:8080/swagger-ui.html
```

Deberías ver la documentación interactiva de la API con todos los endpoints.

---

## 🐛 Troubleshooting

### Problema 1: Puerto ya en uso

```bash
# Identificar proceso usando el puerto
lsof -i :8080

# Detener el proceso
kill -9 <PID>

# O cambiar el puerto en docker-compose.yml
ports:
  - "8081:8080"  # Usar 8081 en lugar de 8080
```

### Problema 2: Error de conexión a Oracle

**Síntoma**: `ORA-17957: Unable to initialize the key store`

**Solución**:

```bash
# 1. Verificar que el Wallet existe
ls -la Wallet_S58ONUXCX4C1QXE9/

# 2. Verificar permisos
chmod -R 644 Wallet_S58ONUXCX4C1QXE9/*

# 3. Verificar configuración en docker-compose.yml
# Debe tener JAVA_TOOL_OPTIONS correctamente configurado

# 4. Reconstruir backend
docker-compose build backend
docker-compose up -d backend
```

### Problema 3: Frontend muestra página en blanco

**Solución**:

```bash
# 1. Limpiar caché del navegador
# Ctrl+Shift+R (Windows/Linux)
# Cmd+Shift+R (Mac)

# 2. Verificar logs del frontend
docker-compose logs frontend

# 3. Reconstruir frontend
docker-compose build frontend
docker-compose up -d frontend

# 4. Verificar que el backend esté corriendo
curl http://localhost:8080/api/v1/health
```

### Problema 4: Error 500 en el backend

**Solución**:

```bash
# 1. Ver logs detallados
docker-compose logs backend | grep ERROR

# 2. Verificar conexión a BD
docker-compose exec backend sh
# Dentro del contenedor:
curl http://localhost:8080/api/v1/health/database

# 3. Verificar que los scripts SQL se ejecutaron
# Conectarse a Oracle y verificar:
SELECT table_name FROM user_tables;
```

### Problema 5: CORS Error en el navegador

**Síntoma**: `Access to XMLHttpRequest blocked by CORS policy`

**Solución**:

```bash
# 1. Verificar configuración de Kong
cat api-manager/kong.yml | grep -A 10 cors

# 2. Verificar que API Gateway está corriendo
docker-compose ps api-gateway

# 3. Reiniciar API Gateway
docker-compose restart api-gateway
```

### Problema 6: Docker build falla

**Solución**:

```bash
# 1. Limpiar imágenes y contenedores antiguos
docker system prune -a

# 2. Reconstruir sin caché
docker-compose build --no-cache

# 3. Si el problema persiste, verificar espacio en disco
df -h
```

---

## 📊 Comandos Útiles

### Docker

```bash
# Ver todos los contenedores
docker ps -a

# Ver logs en tiempo real
docker-compose logs -f

# Reiniciar un servicio específico
docker-compose restart backend

# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v

# Reconstruir y reiniciar
docker-compose up -d --build

# Ejecutar comando en contenedor
docker-compose exec backend sh
```

### Base de Datos

```bash
# Conectarse a Oracle desde el contenedor
docker-compose exec backend sh
# Luego:
sqlplus ADMIN/"$-123.Sb-123"@s58onuxcx4c1qxe9_high

# Verificar tablas
SELECT table_name FROM user_tables;

# Contar registros
SELECT COUNT(*) FROM PACIENTES;
SELECT COUNT(*) FROM SIGNOS_VITALES;
SELECT COUNT(*) FROM ALERTAS;
SELECT COUNT(*) FROM USUARIOS;
```

### Debugging

```bash
# Ver variables de entorno del backend
docker-compose exec backend env | grep SPRING

# Ver configuración de Java
docker-compose exec backend env | grep JAVA

# Verificar conectividad de red entre contenedores
docker-compose exec frontend ping backend
docker-compose exec backend ping api-gateway
```

---

## 🔄 Actualización del Sistema

### Actualizar Backend

```bash
# 1. Hacer cambios en el código
# 2. Reconstruir
docker-compose build backend
# 3. Reiniciar
docker-compose up -d backend
```

### Actualizar Frontend

```bash
# 1. Hacer cambios en el código
# 2. Reconstruir
docker-compose build frontend
# 3. Reiniciar
docker-compose up -d frontend
# 4. Limpiar caché del navegador
```

### Actualizar Base de Datos

```bash
# 1. Crear script de migración
# database/migrations/V2__add_new_column.sql

# 2. Ejecutar manualmente
sqlplus ADMIN/"$-123.Sb-123"@s58onuxcx4c1qxe9_high @database/migrations/V2__add_new_column.sql
```

---

## 📚 Recursos Adicionales

- **Documentación de Oracle Cloud**: https://docs.oracle.com/en-us/iaas/
- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Angular Docs**: https://angular.io/docs
- **Kong Gateway Docs**: https://docs.konghq.com/
- **Docker Compose Docs**: https://docs.docker.com/compose/

---

## 🆘 Soporte

Si encuentras problemas no cubiertos en esta guía:

1. Revisa los logs: `docker-compose logs`
2. Verifica el estado de los servicios: `docker-compose ps`
3. Consulta la documentación de arquitectura: `docs/ARQUITECTURA.md`
4. Revisa los issues conocidos en el repositorio

---

**Última actualización**: 2026-01-23  
**Versión**: 1.0.0
