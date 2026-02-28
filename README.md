# 🏥 VitalWatch - Sistema de Monitoreo Hospitalario

Sistema de monitoreo en tiempo real de signos vitales con arquitectura de microservicios, implementado con **RabbitMQ** y **Apache Kafka**.

---

## 📊 Descripción del Proyecto

VitalWatch es un sistema de monitoreo hospitalario que procesa signos vitales de pacientes en tiempo real, detecta anomalías, genera alertas y persiste datos en Oracle Cloud Database.

### 🎯 Características Principales

- ✅ **Monitoreo en tiempo real** de signos vitales
- ✅ **Detección automática de anomalías** con algoritmos de ML
- ✅ **Generación de alertas** por severidad (Baja, Media, Alta, Crítica)
- ✅ **Persistencia en Oracle Cloud** con Spring Data JPA
- ✅ **Arquitectura de microservicios** escalable
- ✅ **Doble implementación**: RabbitMQ y Apache Kafka
- ✅ **Frontend web** interactivo con Angular
- ✅ **API Gateway** con Kong
- ✅ **Containerización** con Docker

---

## 🏗️ Arquitectura

### Sistema RabbitMQ (Semanas 1-7)

```
┌─────────────┐     ┌─────────────┐     ┌──────────────┐
│  Frontend   │────▶│ API Gateway │────▶│   Backend    │
│  (Angular)  │     │   (Kong)    │     │ (Spring Boot)│
└─────────────┘     └─────────────┘     └──────────────┘
                                               │
                                               ▼
┌──────────────────────────────────────────────────────────┐
│                       RabbitMQ                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │  Queue   │  │  Queue   │  │  Queue   │              │
│  │  Signos  │  │ Anomalías│  │ Resumen  │              │
│  └──────────┘  └──────────┘  └──────────┘              │
└──────────────────────────────────────────────────────────┘
       │                │              │
       ▼                ▼              ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  Producer   │  │  Producer   │  │  Consumer   │
│  Anomaly    │  │  Summary    │  │     DB      │
└─────────────┘  └─────────────┘  └─────────────┘
                                         │
                                         ▼
                            ┌────────────────────────┐
                            │  Oracle Cloud Database │
                            └────────────────────────┘
```

### Sistema Kafka (Semana 8)

```
┌────────────────────┐
│ Stream Generator   │ Genera signos vitales cada 1s
└─────────┬──────────┘
          │ Produce
          ▼
┌─────────────────────────────────────────────────────────┐
│               KAFKA CLUSTER                              │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐       │
│  │  Broker 1  │  │  Broker 2  │  │  Broker 3  │       │
│  └────────────┘  └────────────┘  └────────────┘       │
│  Topic: signos-vitales-stream (3 partitions)           │
│  Topic: alertas-medicas (3 partitions)                 │
└─────────────────────────────────────────────────────────┘
          │ Consume
          ▼
┌────────────────────┐
│ Alert Processor    │ Detecta anomalías
└─────────┬──────────┘
          │ Produce alertas
          ▼
    ┌─────────┴─────────┐
    ▼                   ▼
┌──────────────┐  ┌──────────────┐
│ DB Saver     │  │ Summary Gen  │
└──────┬───────┘  └──────┬───────┘
       │                 │
       └────────┬────────┘
                ▼
    ┌─────────────────────┐
    │ Oracle Cloud Database│
    └─────────────────────┘
```

---

## 🚀 Inicio Rápido

### Prerrequisitos

- Docker y Docker Compose
- Java 17
- Maven 3.9+
- Node.js 18+ (para frontend)
- Oracle Cloud Database (configurado)

### Opción 1: Sistema RabbitMQ

```bash
# Clonar repositorio
git clone https://github.com/sbricenoi/vitalwatch.git
cd vitalwatch

# Cambiar a rama RabbitMQ
git checkout feature/rabbitmq-integration

# Iniciar sistema completo
docker-compose up -d

# Acceder
Frontend:   http://localhost
Backend:    http://localhost:8080
RabbitMQ:   http://localhost:15672
```

### Opción 2: Sistema Kafka

```bash
# Cambiar a rama Kafka
git checkout feature/kafka-implementation

# Iniciar sistema completo (automatizado)
./quick-start-kafka.sh

# Acceder
Kafka UI:         http://localhost:9000
Stream Generator: http://localhost:8091
Alert Processor:  http://localhost:8092
Summary Generator: http://localhost:8094
```

---

## 📁 Estructura del Proyecto

```
vitalwatch/
│
├── 🎨 Frontend y Backend
│   ├── frontend/                    # Angular 17
│   ├── backend/                     # Spring Boot backend
│   └── api-manager/                 # Kong API Gateway
│
├── 🐰 Microservicios RabbitMQ
│   ├── producer-anomaly-detector/   # Detecta anomalías
│   ├── producer-summary/            # Genera resúmenes
│   ├── consumer-db-saver/           # Persiste en Oracle
│   └── consumer-json-generator/     # Genera JSONs
│
├── 📨 Microservicios Kafka
│   ├── producer-stream-generator/   # Genera streams (1 msg/s)
│   ├── producer-alert-processor/    # Detecta y alerta
│   ├── consumer-database-saver/     # Persiste con metadata
│   └── consumer-summary-generator/  # Resúmenes con scheduler
│
├── 🗄️ Base de Datos
│   └── database/
│       ├── schema.sql               # Tablas RabbitMQ
│       ├── create_tables_kafka.sql  # Tablas Kafka
│       └── data.sql                 # Datos de prueba
│
├── 📚 Documentación
│   └── docs/
│       ├── ARQUITECTURA.md          # Diseño técnico
│       ├── GUIA_DEPLOY.md           # Cómo desplegar
│       ├── GUIA_USO.md              # Cómo usar
│       ├── postman-collection.json  # Tests RabbitMQ
│       ├── VitalWatch-Kafka.postman_collection.json
│       └── evaluacion/              # Pautas y guiones
│
├── 🔧 Scripts
│   └── scripts/
│       ├── quick-start-kafka.sh           # ⭐ Inicio rápido Kafka
│       ├── start-kafka-cluster.sh         # Solo cluster
│       ├── create-kafka-topics.sh         # Crear topics
│       ├── deploy-kafka-azure-rapido.sh   # Deploy Azure
│       └── README.md                      # Índice de scripts
│
├── 🐳 Docker Compose
│   ├── docker-compose.yml           # Stack RabbitMQ completo
│   └── docker-compose-kafka.yml     # Stack Kafka completo
│
└── 📄 Configuración
    ├── .gitignore
    ├── Wallet_S58ONUXCX4C1QXE9/     # Oracle Wallet
    └── README.md                     # Este archivo
```

---

## 🛠️ Tecnologías Utilizadas

### Backend
- **Spring Boot 3.2.1** - Framework principal
- **Spring Data JPA** - ORM
- **Spring AMQP** - RabbitMQ integration
- **Spring Kafka** - Kafka integration
- **Lombok** - Reduce boilerplate

### Mensajería
- **RabbitMQ 3.12** - Message broker (sistema principal)
- **Apache Kafka 7.5.0** - Streaming platform (semana 8)
- **Zookeeper 7.5.0** - Kafka coordination

### Base de Datos
- **Oracle Cloud Autonomous Database**
- **Wallet TCPS** - Conexión segura

### Frontend
- **Angular 17**
- **Bootstrap 5**
- **RxJS**

### Infraestructura
- **Docker & Docker Compose**
- **Kong API Gateway**
- **Kafka UI** - Monitoring

### Cloud
- **Azure Container Apps** (deployment)
- **Azure Event Hubs** (Kafka-compatible)
- **Azure Container Registry**

---

## 📊 Microservicios

### Sistema RabbitMQ

| Microservicio | Puerto | Descripción |
|---------------|--------|-------------|
| **Backend** | 8080 | API principal, manejo de signos vitales |
| **Producer Anomaly** | 8081 | Detecta anomalías en signos vitales |
| **Producer Summary** | 8082 | Genera resúmenes diarios |
| **Consumer DB** | N/A | Persiste datos en Oracle |
| **Consumer JSON** | N/A | Genera archivos JSON |
| **Frontend** | 80/443 | Interfaz web Angular |
| **API Gateway** | 8000 | Kong gateway |
| **RabbitMQ** | 5672/15672 | Message broker + UI |

### Sistema Kafka

| Microservicio | Puerto | Descripción |
|---------------|--------|-------------|
| **Stream Generator** | 8091 | Genera signos vitales (1 msg/s) |
| **Alert Processor** | 8092 | Detecta y publica alertas |
| **Database Saver** | 8093 | Persiste en Oracle (2 consumers) |
| **Summary Generator** | 8094 | Resúmenes con scheduler |
| **Kafka UI** | 9000 | Interfaz de monitoreo |
| **Zookeeper 1-3** | 2181-2183 | Coordinación |
| **Kafka 1-3** | 19092-19094 | Brokers |

---

## 🗄️ Base de Datos

### Tablas RabbitMQ

- `SIGNOS_VITALES` - Registro de signos vitales
- `ANOMALIAS` - Anomalías detectadas
- `PACIENTES` - Catálogo de pacientes
- `RESUMEN_DIARIO` - Resúmenes por día

### Tablas Kafka

- `SIGNOS_VITALES_KAFKA` - Con metadatos Kafka (topic, partition, offset)
- `ALERTAS_KAFKA` - Alertas con severidad
- `RESUMEN_DIARIO_KAFKA` - Agregaciones automáticas
- `PACIENTES_MONITOREADOS_KAFKA` - Stats en tiempo real

---

## 🧪 Testing

### Postman Collections

Disponibles en `docs/`:
- `VitalWatch.postman_collection.json` (RabbitMQ)
- `VitalWatch-Kafka.postman_collection.json` (Kafka)

### Pruebas Automatizadas

```bash
# RabbitMQ
curl http://localhost:8080/actuator/health
curl http://localhost:8081/api/anomalies/stats
curl http://localhost:8082/api/summary/today

# Kafka
curl http://localhost:8091/api/v1/stream/stats
curl http://localhost:8092/api/v1/processor/stats
curl http://localhost:8094/api/v1/summary/today
```

### Queries Oracle

```sql
-- Ver últimos signos vitales
SELECT * FROM SIGNOS_VITALES_KAFKA 
ORDER BY timestamp_medicion DESC 
FETCH FIRST 10 ROWS ONLY;

-- Ver alertas críticas
SELECT * FROM ALERTAS_KAFKA 
WHERE severidad = 'CRITICA' 
ORDER BY timestamp_alerta DESC;

-- Resumen diario
SELECT * FROM RESUMEN_DIARIO_KAFKA 
WHERE fecha = CURRENT_DATE;
```

---

## 🚢 Deployment

### Local (Docker Compose)

```bash
# RabbitMQ
docker-compose up -d

# Kafka
./quick-start-kafka.sh
```

### Azure Container Apps

```bash
# Configurar Azure CLI
az login

# Deploy RabbitMQ (si aplica)
./deploy-azure.sh

# Deploy Kafka
./deploy-kafka-azure-rapido.sh
```

Ver `docs/GUIA_DEPLOY.md` para detalles completos.

---

## 📈 Monitoreo

### RabbitMQ Management UI
- URL: http://localhost:15672
- Usuario: `guest`
- Password: `guest`

### Kafka UI
- URL: http://localhost:9000
- Sin autenticación en local
- Visualiza: Topics, Brokers, Consumer Groups, Messages

### Health Checks

Todos los microservicios exponen `/actuator/health`:
```bash
curl http://localhost:8080/actuator/health
```

---

## 🔧 Configuración

### Variables de Entorno Principales

```env
# Oracle Database
ORACLE_DB_URL=jdbc:oracle:thin:@...
ORACLE_DB_USERNAME=ADMIN
ORACLE_DB_PASSWORD=your-password

# RabbitMQ
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka1:9092,kafka2:9092,kafka3:9092
```

Ver archivos `application.properties` y `application-docker.properties` en cada microservicio.

---

## 📚 Documentación Adicional

- **[Arquitectura Técnica](docs/ARQUITECTURA.md)** - Diseño detallado del sistema
- **[Guía de Deployment](docs/GUIA_DEPLOY.md)** - Cómo desplegar a producción
- **[Guía de Uso](docs/GUIA_USO.md)** - Manual de usuario y APIs

---

## 👥 Equipo

**Desarrollador:** Sebastián Briceño  
**Institución:** DuocUC  
**Curso:** Cloud Native - Semana 8  
**Profesor:** [Nombre del profesor]

---

## 📝 Licencia

Este proyecto es parte de un trabajo académico para DuocUC.

---

## 🔗 Enlaces

- **GitHub:** https://github.com/sbricenoi/vitalwatch
- **Ramas:**
  - `main` - Rama principal
  - `feature/rabbitmq-integration` - Sistema RabbitMQ
  - `feature/kafka-implementation` - Sistema Kafka

---

## 🎓 Presentación

Para presentación del proyecto, consultar:
- Código completo en GitHub
- Sistema funcionando localmente
- Documentación en `docs/`
- Postman collections en `docs/`

---

## 📊 Estadísticas del Proyecto

- **Líneas de código:** ~13,000
- **Microservicios:** 12 (8 RabbitMQ + 4 Kafka)
- **Tablas Oracle:** 8
- **APIs REST:** 25+ endpoints
- **Tiempo desarrollo:** ~40 horas

---

**¿Preguntas?** Revisar documentación en `docs/` o contactar al equipo.
