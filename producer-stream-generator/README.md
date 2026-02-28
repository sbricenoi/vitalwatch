# 📊 VitalWatch - Productor 1: Stream Generator

Microservicio productor que genera un stream continuo de signos vitales simulados cada 1 segundo y los publica a Apache Kafka.

## 🎯 Funcionalidad

- Genera signos vitales aleatorios cada 1 segundo usando Spring Scheduler
- 15% de probabilidad de generar valores anormales
- Publica mensajes al tópico `signos-vitales-stream` de Kafka
- Simula 5 pacientes monitoreados en UCI
- API REST para control del stream

## 🚀 Endpoints

### POST `/api/v1/stream/start`
Inicia la generación automática de signos vitales.

**Response:**
```json
{
  "code": "200",
  "message": "Stream de signos vitales iniciado",
  "data": {
    "status": "RUNNING",
    "intervalMs": 1000,
    "topic": "signos-vitales-stream"
  }
}
```

### POST `/api/v1/stream/stop`
Pausa la generación automática.

### GET `/api/v1/stream/status`
Obtiene el estado actual del stream.

### POST `/api/v1/stream/send-manual`
Envía un mensaje único de forma manual.

### GET `/api/v1/stream/stats`
Obtiene estadísticas del generador.

### GET `/api/v1/stream/health`
Health check del servicio.

## 📦 Configuración

### application.properties
```properties
kafka.bootstrap-servers=localhost:19092,localhost:19093,localhost:19094
kafka.topic.vital-signs=signos-vitales-stream
stream.generation.enabled=true
stream.interval.ms=1000
```

## 🏃 Ejecución

### Local
```bash
mvn spring-boot:run
```

### Docker
```bash
docker build -t producer-stream-generator .
docker run -p 8081:8080 producer-stream-generator
```

### Docker Compose
```bash
docker-compose -f docker-compose-kafka.yml up producer-stream-generator
```

## 📊 Pacientes Simulados

| ID | Nombre | Sala | Cama | Device |
|----|--------|------|------|--------|
| P001 | Juan Pérez | UCI-A | 101 | DEVICE-001 |
| P002 | María García | UCI-A | 102 | DEVICE-002 |
| P003 | Carlos López | UCI-B | 201 | DEVICE-003 |
| P004 | Ana Martínez | UCI-B | 202 | DEVICE-004 |
| P005 | Pedro Sánchez | UCI-C | 301 | DEVICE-005 |

## 🔢 Rangos de Valores

### Normales (85% de probabilidad)
- Frecuencia Cardíaca: 60-100 lpm
- Presión Sistólica: 90-130 mmHg
- Presión Diastólica: 60-85 mmHg
- Temperatura: 36.0-37.5 °C
- Saturación O2: 95-100%
- Frecuencia Respiratoria: 12-20 rpm

### Anormales (15% de probabilidad)
- Frecuencia Cardíaca: 40-60 o 120-160 lpm
- Presión Sistólica: 70-90 o 140-190 mmHg
- Temperatura: <36.0 o >38.0 °C
- Saturación O2: 80-94%

## 📈 Rendimiento

- **Mensajes por segundo:** 1
- **Mensajes por minuto:** 60
- **Mensajes por hora:** 3,600
- **Mensajes por día:** 86,400

## 🐳 Puerto

- **Local:** 8080
- **Docker Compose:** 8081 (mapeado)
