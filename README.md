# 🏥 VitalWatch - Sistema de Monitoreo y Alertas de Signos Vitales

Sistema Cloud Native para monitoreo en tiempo real de signos vitales de pacientes hospitalizados con generación automática de alertas médicas.

## 🌐 Despliegue en Producción

**Estado:** ✅ DESPLEGADO EN AZURE

| Servicio | URL de Producción | Estado |
|----------|------------------|--------|
| **Frontend** | [https://vitalwatch-frontend.graycoast-fc35a2d0.southcentralus.azurecontainerapps.io](https://vitalwatch-frontend.graycoast-fc35a2d0.southcentralus.azurecontainerapps.io/) | ✅ Running |
| **Backend API** | [https://vitalwatch-backend.graycoast-fc35a2d0.southcentralus.azurecontainerapps.io](https://vitalwatch-backend.graycoast-fc35a2d0.southcentralus.azurecontainerapps.io/) | ✅ Running |
| **API Gateway** | [https://vitalwatch-api-gateway.graycoast-fc35a2d0.southcentralus.azurecontainerapps.io](https://vitalwatch-api-gateway.graycoast-fc35a2d0.southcentralus.azurecontainerapps.io/) | ✅ Running |

**Infraestructura:**
- **Cloud Provider:** Microsoft Azure (South Central US)
- **Servicios:** Azure Container Apps, ACR, Key Vault
- **Base de Datos:** Oracle Cloud Autonomous Database
- **Arquitectura:** Microservicios con auto-scaling (1-3 réplicas)

---

## 📋 Tabla de Contenidos

- [Descripción](#-descripción)
- [Arquitectura](#-arquitectura)
- [Stack Tecnológico](#-stack-tecnológico)
- [Inicio Rápido](#-inicio-rápido)
- [Documentación](#-documentación)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Endpoints API](#-endpoints-api)
- [Credenciales de Prueba](#-credenciales-de-prueba)

---

## 📖 Descripción

**VitalWatch** es un sistema integral de monitoreo hospitalario que permite:

- ✅ Gestión completa de pacientes hospitalizados
- ✅ Registro de signos vitales en tiempo real
- ✅ Generación automática de alertas médicas
- ✅ Dashboard con estadísticas y métricas
- ✅ Sistema de autenticación con roles (Admin, Médico, Enfermera)
- ✅ API RESTful documentada con OpenAPI/Swagger
- ✅ Base de datos Oracle Cloud Autonomous Database

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND                                 │
│                    Angular 17 + Bootstrap 5                      │
│                     http://localhost:4200                        │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API GATEWAY                                 │
│                    Kong (Rate Limiting,                          │
│                  CORS, Security Headers)                         │
│                     http://localhost:8000                        │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                        BACKEND                                   │
│                  Spring Boot 3.2 + Java 17                       │
│                     http://localhost:8080                        │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Controllers  │  │   Services   │  │ Repositories │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                       DATABASE                                   │
│              Oracle Cloud Autonomous Database                    │
│                    (s58onuxcx4c1qxe9)                           │
│                  Santiago, Chile Region                          │
└─────────────────────────────────────────────────────────────────┘
```

### Flujo de Datos

```
Usuario → Frontend → API Gateway → Backend → Oracle DB
                                      ↓
                                  Validación
                                      ↓
                              Generación de Alertas
                                      ↓
                                  Response
```

---

## 🛠️ Stack Tecnológico

### Backend
- **Framework**: Spring Boot 3.2.0
- **Lenguaje**: Java 17
- **ORM**: Spring Data JPA
- **Base de Datos**: Oracle Autonomous Database (19c)
- **Documentación API**: SpringDoc OpenAPI 3
- **Seguridad**: Spring Security
- **Build**: Maven

### Frontend
- **Framework**: Angular 17
- **UI**: Bootstrap 5 + Bootstrap Icons
- **HTTP Client**: Angular HttpClient
- **Routing**: Angular Router con Lazy Loading
- **Forms**: Reactive Forms

### API Gateway
- **Gateway**: Kong 3.4
- **Plugins**: CORS, Rate Limiting, Security Headers, Logging

### DevOps
- **Containerización**: Docker + Docker Compose
- **Servidor Web**: Nginx (para frontend)
- **Scripts**: Bash

---

## 🚀 Inicio Rápido

### Prerrequisitos

- Docker Desktop instalado y ejecutándose
- 4GB RAM mínimo disponible
- Puertos libres: 4200, 8080, 8000

### Instalación en 1 Comando

```bash
./deploy.sh
```

Este script automáticamente:
1. ✅ Verifica prerrequisitos (Docker, Wallet Oracle)
2. ✅ Construye las imágenes Docker
3. ✅ Levanta todos los servicios
4. ✅ Ejecuta health checks
5. ✅ Muestra las URLs de acceso

### Acceso a la Aplicación

Una vez iniciado, accede a:

- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Gateway**: http://localhost:8000

---

## 📚 Documentación

### Documentos Principales

1. **[Guía de Integración](docs/GUIA_INTEGRACION.md)** - Setup completo y configuración
2. **[Arquitectura del Sistema](docs/arquitectura.md)** - Diagramas y diseño técnico
3. **[Guía de Postman](docs/guia-postman.md)** - Testing de API
4. **[Guía Oracle Cloud](docs/guia-oracle-cloud.md)** - Configuración de BD

### 🔷 Despliegue en Azure (Nuevo!)

- **[AZURE_INDEX.md](AZURE_INDEX.md)** - 📚 Índice maestro de documentación Azure
- **[AZURE_README.md](AZURE_README.md)** - ⚡ Guía rápida y comandos comunes
- **[Resumen Ejecutivo](docs/AZURE_RESUMEN_EJECUTIVO.md)** - 📊 Visión general y costos
- **[Guía Completa Azure](docs/GUIA_DESPLIEGUE_AZURE.md)** - 📖 Despliegue paso a paso
- **[Checklist Azure](docs/AZURE_CHECKLIST.md)** - ✅ Lista de verificación
- **[Comparación Opciones](docs/AZURE_COMPARACION_OPCIONES.md)** - ⚖️ Análisis de alternativas

#### 🚀 Despliegue Rápido en Azure

```bash
# Despliegue automatizado completo
./deploy-azure.sh

# Tiempo: 1-2 horas
# Costo: $47-85/mes
```

### API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Postman Collection**: `docs/postman-collection.json`

---

## 📁 Estructura del Proyecto

```
vitalwatch/
├── backend/                    # Spring Boot API
│   ├── src/main/java/
│   │   └── com/hospital/vitalwatch/
│   │       ├── controller/     # REST Controllers
│   │       ├── service/        # Business Logic
│   │       ├── repository/     # Data Access
│   │       ├── model/          # JPA Entities
│   │       ├── dto/            # Data Transfer Objects
│   │       ├── config/         # Configuration
│   │       └── exception/      # Exception Handlers
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── frontend/                   # Angular App
│   ├── src/app/
│   │   ├── core/              # Services, Guards
│   │   ├── models/            # TypeScript Interfaces
│   │   ├── modules/           # Feature Modules
│   │   │   ├── dashboard/
│   │   │   ├── pacientes/
│   │   │   ├── signos-vitales/
│   │   │   └── alertas/
│   │   └── shared/            # Shared Components
│   └── package.json
│
├── api-manager/               # Kong Configuration
│   └── kong.yml
│
├── database/                  # SQL Scripts
│   ├── schema.sql            # Tablas
│   ├── data.sql              # Datos de prueba
│   └── usuarios.sql          # Usuarios del sistema
│
├── docs/                     # Documentación
│   ├── ARQUITECTURA.md
│   ├── GUIA_INTEGRACION.md
│   ├── guia-postman.md
│   └── postman-collection.json
│
├── scripts/                  # Automation Scripts
│   ├── start.sh
│   └── stop.sh
│
├── Wallet_S58ONUXCX4C1QXE9/  # Oracle Cloud Wallet
│
├── docker-compose.yml        # Orquestación de servicios
├── deploy.sh                 # Script de despliegue
└── README.md
```

---

## 🔌 Endpoints API

### Autenticación
```
POST   /api/v1/auth/login              # Login
GET    /api/v1/auth/check              # Verificar sesión
GET    /api/v1/auth/credentials        # Credenciales de prueba
```

### Pacientes
```
GET    /api/v1/pacientes               # Listar todos
GET    /api/v1/pacientes/{id}          # Obtener por ID
GET    /api/v1/pacientes/estado/{estado}  # Filtrar por estado
GET    /api/v1/pacientes/sala/{sala}   # Filtrar por sala
GET    /api/v1/pacientes/criticos      # Pacientes críticos
GET    /api/v1/pacientes/buscar?q=     # Buscar
POST   /api/v1/pacientes               # Crear
PUT    /api/v1/pacientes/{id}          # Actualizar
DELETE /api/v1/pacientes/{id}          # Eliminar
```

### Signos Vitales
```
GET    /api/v1/signos-vitales          # Listar todos
GET    /api/v1/signos-vitales/{id}     # Obtener por ID
GET    /api/v1/signos-vitales/paciente/{id}  # Por paciente
GET    /api/v1/signos-vitales/paciente/{id}/ultimo  # Último registro
GET    /api/v1/signos-vitales/paciente/{id}/ultimos?limite=N  # Últimos N
POST   /api/v1/signos-vitales          # Registrar
PUT    /api/v1/signos-vitales/{id}     # Actualizar
DELETE /api/v1/signos-vitales/{id}     # Eliminar
```

### Alertas
```
GET    /api/v1/alertas                 # Listar todas
GET    /api/v1/alertas/{id}            # Obtener por ID
GET    /api/v1/alertas/activas         # Alertas activas
GET    /api/v1/alertas/criticas        # Alertas críticas
GET    /api/v1/alertas/paciente/{id}   # Por paciente
GET    /api/v1/alertas/paciente/{id}/activas  # Activas por paciente
GET    /api/v1/alertas/severidad/{severidad}  # Por severidad
GET    /api/v1/alertas/recientes?limite=N     # Recientes
POST   /api/v1/alertas                 # Crear manual
PUT    /api/v1/alertas/{id}/resolver   # Resolver
PUT    /api/v1/alertas/{id}/descartar  # Descartar
DELETE /api/v1/alertas/{id}            # Eliminar
GET    /api/v1/alertas/estadisticas    # Estadísticas
```

### Dashboard
```
GET    /api/v1/dashboard/estadisticas          # Estadísticas generales
GET    /api/v1/dashboard/pacientes-por-estado  # Distribución
GET    /api/v1/dashboard/alertas-recientes     # Alertas recientes
GET    /api/v1/dashboard/pacientes-criticos    # Pacientes críticos
GET    /api/v1/dashboard/alertas-por-severidad # Distribución severidad
```

### Health Check
```
GET    /api/v1/health                  # Estado de la aplicación
GET    /api/v1/health/database         # Estado de la BD
```

---

## 🔐 Credenciales de Prueba

### Usuarios del Sistema

| Rol | Email | Password | Permisos |
|-----|-------|----------|----------|
| **Admin** | admin@vitalwatch.com | Admin123! | Acceso total |
| **Médico** | medico@vitalwatch.com | Medico123! | Lectura/Escritura |
| **Enfermera** | enfermera@vitalwatch.com | Enfermera123! | Registro de signos |

### Base de Datos Oracle

- **Usuario**: ADMIN
- **Password**: `$-123.Sb-123`
- **Service**: s58onuxcx4c1qxe9_high
- **Region**: Santiago, Chile

---

## 🧪 Testing

### Pruebas con Postman

1. Importar colección: `docs/postman-collection.json`
2. Configurar variables de entorno
3. Ejecutar tests automáticos

### Pruebas Manuales

1. **Login**: Acceder con credenciales de prueba
2. **Crear Paciente**: Formulario con validación de RUT
3. **Registrar Signos Vitales**: Con generación automática de alertas
4. **Ver Dashboard**: Estadísticas en tiempo real
5. **Gestionar Alertas**: Resolver/Descartar alertas activas

---

## 📊 Modelo de Datos

### Entidades Principales

```
PACIENTES
├── id (PK)
├── nombre, apellido, rut
├── fecha_nacimiento, edad, genero
├── sala, cama, estado
├── diagnostico
└── fecha_ingreso, fecha_alta

SIGNOS_VITALES
├── id (PK)
├── paciente_id (FK)
├── frecuencia_cardiaca
├── presion_sistolica, presion_diastolica
├── temperatura
├── saturacion_oxigeno
├── frecuencia_respiratoria
├── estado_conciencia
├── registrado_por
└── fecha_registro

ALERTAS
├── id (PK)
├── paciente_id (FK)
├── tipo, mensaje, severidad
├── estado (ACTIVA, RESUELTA, DESCARTADA)
├── fecha_creacion, fecha_resolucion
└── resuelto_por, notas_resolucion

USUARIOS
├── id (PK)
├── nombre, email
├── password_hash
├── rol (ADMIN, MEDICO, ENFERMERA)
└── activo
```

---

## 🛑 Detener la Aplicación

```bash
./scripts/stop.sh
```

O manualmente:

```bash
docker-compose down
```

---

## 🐛 Troubleshooting

### Puerto ya en uso
```bash
# Verificar puertos ocupados
lsof -i :4200
lsof -i :8080
lsof -i :8000

# Detener servicios anteriores
docker-compose down
```

### Error de conexión a Oracle
```bash
# Verificar que el Wallet existe
ls -la Wallet_S58ONUXCX4C1QXE9/

# Revisar logs del backend
docker-compose logs backend
```

### Frontend no carga
```bash
# Limpiar caché del navegador
Ctrl+Shift+R (Windows/Linux)
Cmd+Shift+R (Mac)

# Reconstruir frontend
docker-compose build frontend
docker-compose up -d frontend
```

---

## 📝 Licencia

Proyecto académico - DUOC UC  
Asignatura: Cloud Native Development  
Año: 2026

---

## 👥 Autor

Desarrollado como proyecto sumativo para la asignatura de Desarrollo Cloud Native.

---

## 🔗 Enlaces Útiles

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Angular Documentation](https://angular.io/docs)
- [Oracle Cloud Documentation](https://docs.oracle.com/en-us/iaas/Content/home.htm)
- [Kong Gateway Documentation](https://docs.konghq.com/)
- [Docker Documentation](https://docs.docker.com/)

---

**¿Necesitas ayuda?** Revisa la [Guía de Integración](docs/GUIA_INTEGRACION.md) para instrucciones detalladas.
