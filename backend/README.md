# 🔧 Backend - VitalWatch

## Spring Boot Microservicio BFF

### 📋 Descripción
Backend del sistema VitalWatch implementado con Spring Boot siguiendo el patrón BFF (Backend For Frontend). Proporciona una API RESTful completa para la gestión de pacientes, signos vitales y alertas médicas.

---

## 🚀 Tecnologías

- **Java 17+**
- **Spring Boot 3.2+**
- **Spring Security** - Seguridad y autenticación JWT
- **Spring Data JPA** - Persistencia de datos
- **Oracle JDBC Driver** - Conexión a Oracle Database
- **Hibernate** - ORM
- **Lombok** - Reducción de boilerplate
- **SpringDoc OpenAPI** - Documentación Swagger
- **Maven** - Gestión de dependencias

---

## 📁 Estructura del Proyecto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/hospital/vitalwatch/
│   │   │   ├── config/          # Configuraciones
│   │   │   ├── controller/      # Controladores REST
│   │   │   ├── service/         # Lógica de negocio
│   │   │   ├── repository/      # Acceso a datos
│   │   │   ├── model/           # Entidades JPA
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exception/       # Manejo de excepciones
│   │   │   └── util/            # Utilidades
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql
│   │
│   └── test/                    # Tests unitarios
│
├── pom.xml
├── Dockerfile
└── README.md
```

---

## ⚙️ Configuración

### Prerequisites
- JDK 17 o superior
- Maven 3.9+
- Oracle Database (local o cloud)
- IDE (IntelliJ IDEA recomendado)

### Variables de Entorno

Configura en `application.properties`:

```properties
# Oracle Cloud Database con Wallet
spring.datasource.url=jdbc:oracle:thin:@s58onuxcx4c1qxe9_high?TNS_ADMIN=./Wallet_S58ONUXCX4C1QXE9
spring.datasource.username=ADMIN
spring.datasource.password=[TU_PASSWORD]

# JWT/OAuth2
spring.security.oauth2.resourceserver.jwt.issuer-uri=[AUTH0_DOMAIN]
```

**Nota:** El Wallet de Oracle Cloud (`Wallet_S58ONUXCX4C1QXE9/`) ya está incluido en el proyecto.

**Servicios disponibles:**
- `s58onuxcx4c1qxe9_high` - Alto rendimiento (recomendado para producción)
- `s58onuxcx4c1qxe9_medium` - Rendimiento medio
- `s58onuxcx4c1qxe9_low` - Bajo (recomendado para desarrollo)
- `s58onuxcx4c1qxe9_tp` - Transaction processing
- `s58onuxcx4c1qxe9_tpurgent` - Transaction processing urgente

---

## 🏃 Ejecutar Localmente

### Con Maven

```bash
# Compilar
./mvnw clean install

# Ejecutar
./mvnw spring-boot:run

# Ejecutar con perfil específico
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Con Java

```bash
# Compilar
./mvnw clean package

# Ejecutar JAR
java -jar target/vitalwatch-backend-1.0.0.jar
```

---

## 🐳 Docker

### Build

```bash
docker build -t vitalwatch-backend:latest .
```

### Run

```bash
docker run -p 8080:8080 \
  -e ORACLE_URL="jdbc:oracle:thin:@host:1521/service" \
  -e ORACLE_USER="user" \
  -e ORACLE_PASSWORD="password" \
  vitalwatch-backend:latest
```

---

## 📡 Endpoints API

### Base URL
```
http://localhost:8080/api/v1
```

### Pacientes
- `GET    /pacientes` - Listar todos
- `GET    /pacientes/{id}` - Obtener uno
- `POST   /pacientes` - Crear nuevo
- `PUT    /pacientes/{id}` - Actualizar
- `DELETE /pacientes/{id}` - Eliminar

### Signos Vitales
- `GET    /signos-vitales/paciente/{id}` - Por paciente
- `GET    /signos-vitales/{id}` - Obtener uno
- `POST   /signos-vitales` - Registrar
- `PUT    /signos-vitales/{id}` - Actualizar
- `DELETE /signos-vitales/{id}` - Eliminar

### Alertas
- `GET    /alertas` - Todas
- `GET    /alertas/activas` - Solo activas
- `GET    /alertas/paciente/{id}` - Por paciente
- `POST   /alertas` - Crear
- `PUT    /alertas/{id}/resolver` - Resolver

### Health Check
- `GET    /actuator/health`

---

## 📖 Documentación API

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON
```
http://localhost:8080/v3/api-docs
```

---

## 🧪 Testing

### Ejecutar Tests

```bash
# Todos los tests
./mvnw test

# Test específico
./mvnw test -Dtest=PacienteServiceTest

# Con cobertura
./mvnw test jacoco:report
```

### Reporte de Cobertura
```
target/site/jacoco/index.html
```

---

## 🔐 Seguridad

### JWT Token
Todos los endpoints (excepto actuator) requieren autenticación JWT:

```
Authorization: Bearer <token>
```

### Roles
- `ADMIN` - Acceso completo
- `DOCTOR` - Gestión de pacientes y alertas
- `ENFERMERA` - Registro de signos vitales

---

## 📝 Formato de Respuesta

Todas las respuestas siguen el formato:

```json
{
  "traceId": "uuid",
  "code": "200",
  "message": "Mensaje descriptivo",
  "data": { }
}
```

---

## 🚧 Troubleshooting

### Error de Conexión a BD
```
Verificar:
- URL de conexión correcta
- Usuario y contraseña
- BD accesible desde la red
- Driver Oracle en el classpath
```

### Error JWT
```
Verificar:
- Configuración del issuer-uri
- Token válido y no expirado
- Configuración de Spring Security
```

---

## 📚 Referencias

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Oracle JDBC Driver](https://www.oracle.com/database/technologies/appdev/jdbc.html)

---

**Desarrollado por:** [Nombre del Equipo]
**Fecha:** Enero 2026
