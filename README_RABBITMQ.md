# VitalWatch - Integración RabbitMQ

## Descripción del Proyecto

Integración de mensajería asíncrona con RabbitMQ para el sistema VitalWatch, implementando productores y consumidores que procesan alertas médicas en tiempo real.

### Evaluación: Semana 6 - DSY2206

**Integrante:** Sebastián Briceño  
**Institución:** DUOC UC  
**Fecha:** Enero 2026

---

## Arquitectura del Sistema

### Componentes

#### 1. RabbitMQ (Message Broker)
- **Puerto AMQP:** 5672
- **Puerto Management:** 15672  
- **Usuario:** vitalwatch
- **Password:** hospital123
- **Colas:**
  - `vital-signs-alerts`: Alertas de anomalías en signos vitales
  - `vital-signs-summary`: Resúmenes periódicos del sistema

#### 2. Productores (Publishers)

##### P1: Detector de Anomalías
- **Puerto:** 8081
- **Endpoint:** `POST /api/v1/vital-signs/check`
- **Función:** Recibe signos vitales, detecta anomalías, publica alertas
- **Cola destino:** `vital-signs-alerts`

##### P2: Generador de Resúmenes
- **Puerto:** 8082
- **Endpoints:** 
  - `POST /api/v1/summary/generate` (manual)
  - Tarea programada (cada 5 minutos)
- **Función:** Genera resúmenes estadísticos del sistema
- **Cola destino:** `vital-signs-summary`

#### 3. Consumidores (Subscribers)

##### C1: Guardador en Oracle Cloud
- **Cola origen:** `vital-signs-alerts`
- **Función:** Guarda alertas en tabla `ALERTAS_MQ` de Oracle
- **Base de Datos:** Oracle Autonomous Database (OCI)

##### C2: Generador de Archivos JSON
- **Cola origen:** `vital-signs-alerts`
- **Función:** Genera archivos JSON individuales por alerta
- **Directorio:** `./alerts-json/`

---

## Flujo de Datos

```
┌─────────────────┐
│ Dispositivo     │
│ Médico          │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────┐
│ P1: Detector de Anomalías       │
│ (Puerto 8081)                   │
│ - Recibe signos vitales         │
│ - Analiza rangos normales       │
│ - Detecta anomalías              │
└────────┬────────────────────────┘
         │ (si hay anomalías)
         ▼
    ┌─────────────┐
    │  RabbitMQ   │
    │ Cola:       │
    │ vital-signs │
    │   -alerts   │
    └──┬──────┬───┘
       │      │
       ▼      ▼
   ┌───────┐ ┌────────────┐
   │  C1   │ │    C2      │
   │Oracle │ │  JSON Gen  │
   └───────┘ └────────────┘
```

```
┌────────────────────┐
│ P2: Generador      │
│ de Resúmenes       │
│ (Puerto 8082)      │
│ - Cada 5 minutos   │
│ - Manual: POST     │
└─────────┬──────────┘
          │
          ▼
     ┌──────────┐
     │ RabbitMQ │
     │  Cola:   │
     │ summary  │
     └──────────┘
```

---

## Requisitos Previos

### Software Necesario
- Docker Desktop
- Docker Compose
- Oracle Cloud cuenta activa
- Maven 3.8+ (para desarrollo local)
- Java 17 (para desarrollo local)

### Configuración de Base de Datos

1. **Crear Tabla en Oracle Cloud:**
   ```bash
   # Ubicación: database/create_alertas_mq_table.sql
   # Ejecutar en Oracle Cloud Console → Database Actions → SQL
   ```

2. **Verificar Oracle Wallet:**
   ```bash
   ls -la backend/wallet/
   # Debe contener: ewallet.p12, cwallet.sso, tnsnames.ora, etc.
   ```

---

## Instalación y Ejecución

### 1. Clonar Repositorio
```bash
git clone <repository-url>
cd "Semana 3 Sumativa 2 v2"
git checkout feature/rabbitmq-integration
```

### 2. Configurar Variables de Entorno (Opcional)
Las variables ya están configuradas en `docker-compose-rabbitmq.yml`, pero puedes ajustarlas:

```yaml
# En docker-compose-rabbitmq.yml
environment:
  RABBITMQ_HOST: rabbitmq
  RABBITMQ_USERNAME: vitalwatch
  RABBITMQ_PASSWORD: hospital123
  ORACLE_USERNAME: ADMIN
  ORACLE_PASSWORD: $$-123.Sb-123
```

### 3. Levantar Servicios
```bash
docker-compose -f docker-compose-rabbitmq.yml up --build
```

**Servicios que se levantan:**
- `vitalwatch-rabbitmq`: RabbitMQ Message Broker
- `vitalwatch-producer-anomaly`: Productor 1 (Detector)
- `vitalwatch-producer-summary`: Productor 2 (Resúmenes)
- `vitalwatch-consumer-db`: Consumidor 1 (Oracle)
- `vitalwatch-consumer-json`: Consumidor 2 (JSON)

### 4. Verificar Servicios
```bash
# Verificar contenedores corriendo
docker ps

# Ver logs
docker-compose -f docker-compose-rabbitmq.yml logs -f

# RabbitMQ Management UI
open http://localhost:15672
# Usuario: vitalwatch / Password: hospital123
```

---

## Pruebas

### Prueba 1: Health Checks

```bash
# Productor 1
curl http://localhost:8081/api/v1/vital-signs/health | jq

# Productor 2
curl http://localhost:8082/api/v1/summary/health | jq
```

### Prueba 2: Generar Alerta Crítica

```bash
curl -X POST http://localhost:8081/api/v1/vital-signs/check \
  -H "Content-Type: application/json" \
  -d '{
    "pacienteId": 1,
    "pacienteNombre": "Juan Pérez",
    "sala": "UCI",
    "cama": "A-01",
    "frecuenciaCardiaca": 125,
    "presionSistolica": 165,
    "presionDiastolica": 95,
    "temperatura": 38.9,
    "saturacionOxigeno": 88,
    "frecuenciaRespiratoria": 28,
    "deviceId": "DEVICE-001"
  }' | jq
```

**Resultado Esperado:**
```json
{
  "code": "201",
  "message": "Anomalías detectadas. Alerta publicada a RabbitMQ",
  "data": {
    "hasAnomalies": true,
    "anomaliesCount": 6,
    "severity": "CRITICA",
    "alertPublished": true
  }
}
```

### Prueba 3: Verificar en Oracle

```sql
SELECT * FROM ALERTAS_MQ 
ORDER BY DETECTED_AT DESC 
FETCH FIRST 10 ROWS ONLY;
```

### Prueba 4: Verificar Archivos JSON

```bash
ls -lh alerts-json/
cat alerts-json/alert_*.json | jq
```

### Prueba 5: Generar Resumen

```bash
curl -X POST http://localhost:8082/api/v1/summary/generate | jq
```

---

## Estructura del Proyecto

```
.
├── docker-compose-rabbitmq.yml          # Orquestación de servicios
├── alerts-json/                         # Archivos JSON generados (volumen)
├── database/
│   ├── create_alertas_mq_table.sql     # Script creación tabla Oracle
│   └── README.md
├── producer-anomaly-detector/           # Productor 1
│   ├── src/main/java/...
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
├── producer-summary/                    # Productor 2
│   ├── src/main/java/...
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
├── consumer-db-saver/                   # Consumidor 1
│   ├── src/main/java/...
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
├── consumer-json-generator/             # Consumidor 2
│   ├── src/main/java/...
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
├── docs/
│   └── postman-collection.json         # Colección Postman actualizada
├── TESTING_RABBITMQ.md                 # Plan de pruebas completo
└── README_RABBITMQ.md                  # Este archivo
```

---

## Endpoints API

### Productor 1: Detector de Anomalías (Puerto 8081)

#### POST `/api/v1/vital-signs/check`
Verifica signos vitales y detecta anomalías.

**Request Body:**
```json
{
  "pacienteId": 1,
  "pacienteNombre": "Juan Pérez",
  "sala": "UCI",
  "cama": "A-01",
  "frecuenciaCardiaca": 75,
  "presionSistolica": 120,
  "presionDiastolica": 80,
  "temperatura": 36.5,
  "saturacionOxigeno": 98,
  "frecuenciaRespiratoria": 16,
  "deviceId": "DEVICE-001"
}
```

#### GET `/api/v1/vital-signs/health`
Health check del productor.

---

### Productor 2: Generador de Resúmenes (Puerto 8082)

#### POST `/api/v1/summary/generate`
Genera resumen manualmente.

#### GET `/api/v1/summary/stats`
Obtiene estadísticas del generador.

#### GET `/api/v1/summary/health`
Health check del productor.

---

### RabbitMQ Management API (Puerto 15672)

#### GET `/api/queues`
Lista todas las colas.

**Headers:**
```
Authorization: Basic dml0YWx3YXRjaDpob3NwaXRhbDEyMw==
```

---

## Rangos de Signos Vitales

| Parámetro | Normal | Crítico |
|-----------|--------|---------|
| Frecuencia Cardíaca | 60-100 lpm | <40 o >120 lpm |
| Presión Sistólica | 90-120 mmHg | <70 o >160 mmHg |
| Presión Diastólica | 60-80 mmHg | <40 o >100 mmHg |
| Temperatura | 36.0-37.5°C | <35.0 o >39.5°C |
| Saturación O2 | 95-100% | <90% |
| Frecuencia Respiratoria | 12-20 rpm | <8 o >25 rpm |

---

## Monitoreo

### RabbitMQ Management UI
```bash
open http://localhost:15672
```

**Métricas disponibles:**
- Mensajes publicados/consumidos
- Tasas de procesamiento
- Consumidores activos
- Estado de colas

### Docker Logs
```bash
# Todos los servicios
docker-compose -f docker-compose-rabbitmq.yml logs -f

# Servicio específico
docker logs vitalwatch-producer-anomaly -f
docker logs vitalwatch-consumer-db -f
docker logs vitalwatch-consumer-json -f
```

### Oracle Cloud Console
```sql
-- Estadísticas de alertas
SELECT 
    SEVERITY,
    COUNT(*) as TOTAL,
    MIN(DETECTED_AT) as PRIMERA,
    MAX(DETECTED_AT) as ULTIMA
FROM ALERTAS_MQ
GROUP BY SEVERITY
ORDER BY TOTAL DESC;

-- Alertas recientes
SELECT * FROM ALERTAS_MQ 
WHERE DETECTED_AT >= SYSTIMESTAMP - INTERVAL '1' HOUR
ORDER BY DETECTED_AT DESC;
```

---

## Troubleshooting

### Problema: RabbitMQ no levanta
```bash
docker-compose -f docker-compose-rabbitmq.yml down -v
docker-compose -f docker-compose-rabbitmq.yml up rabbitmq
```

### Problema: Consumidor no guarda en Oracle
**Verificar:**
1. Wallet montado: `docker exec vitalwatch-consumer-db ls /app/wallet`
2. Logs: `docker logs vitalwatch-consumer-db`
3. Tabla existe en Oracle

### Problema: No se generan archivos JSON
**Verificar:**
1. Directorio existe: `ls -la alerts-json/`
2. Logs: `docker logs vitalwatch-consumer-json`
3. Permisos: `chmod -R 755 alerts-json/`

---

## Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 3.2.1**
- **RabbitMQ 3.12** (Management)
- **Oracle Cloud Autonomous Database**
- **Docker & Docker Compose**
- **Maven 3.9**
- **Lombok**
- **Jackson (JSON)**
- **Spring AMQP**
- **Spring Data JPA**

---

## Documentación Adicional

- **Plan de Pruebas Completo:** `TESTING_RABBITMQ.md`
- **Colección Postman:** `docs/postman-collection.json`
- **Scripts SQL:** `database/create_alertas_mq_table.sql`
- **READMEs Individuales:**
  - `producer-anomaly-detector/README.md`
  - `producer-summary/README.md`
  - `consumer-db-saver/README.md`
  - `consumer-json-generator/README.md`

---

## Cumplimiento de Pauta

### ✅ Elementos Requeridos

- [x] **1 Docker Compose** con RabbitMQ y 4 microservicios
- [x] **2 Productores:**
  - P1: Detector de Anomalías (signos vitales)
  - P2: Generador de Resúmenes (periódico)
- [x] **2 Consumidores:**
  - C1: Guardador en BD Oracle
  - C2: Generador de archivos JSON
- [x] **2 Colas:**
  - `vital-signs-alerts`
  - `vital-signs-summary`
- [x] **Base de Datos:** Oracle Cloud (tabla ALERTAS_MQ)
- [x] **Archivos JSON:** Generados en `./alerts-json/`
- [x] **Documentación:** Completa y detallada
- [x] **Pruebas:** Plan de testing exhaustivo

---

## Autor

**Sebastián Briceño**  
DUOC UC - DSY2206  
Enero 2026
