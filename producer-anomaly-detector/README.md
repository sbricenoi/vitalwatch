# VitalWatch - Productor 1: Detector de Anomalías

Microservicio productor que recibe signos vitales de dispositivos médicos, detecta anomalías y publica alertas a RabbitMQ.

## Funcionalidad

- Recibe signos vitales mediante endpoint REST
- Analiza cada parámetro vital comparándolo con rangos normales
- Detecta anomalías y calcula severidad
- Publica alertas a la cola `vital-signs-alerts` de RabbitMQ

## Endpoints

### POST /api/v1/vital-signs/check
Verifica signos vitales y detecta anomalías.

**Request:**
```json
{
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
}
```

**Response (con anomalías):**
```json
{
  "code": "201",
  "message": "Anomalías detectadas. Alerta publicada a RabbitMQ",
  "data": {
    "hasAnomalies": true,
    "anomaliesCount": 5,
    "severity": "CRITICA",
    "alertPublished": true
  }
}
```

## Rangos Normales

- **Frecuencia Cardíaca:** 60-100 lpm (crítico: <40 o >120)
- **Presión Sistólica:** 90-120 mmHg (crítico: <70 o >160)
- **Presión Diastólica:** 60-80 mmHg (crítico: <40 o >100)
- **Temperatura:** 36.0-37.5 °C (crítico: <35.0 o >39.5)
- **Saturación O2:** 95-100% (crítico: <90%)
- **Frecuencia Respiratoria:** 12-20 rpm (crítico: <8 o >25)

## Ejecución

### Local
```bash
mvn spring-boot:run
```

### Docker
```bash
docker build -t producer-anomaly-detector .
docker run -p 8081:8080 producer-anomaly-detector
```

### Docker Compose
```bash
docker-compose up producer-anomaly-detector
```

## Puerto

- Local: 8080
- Docker Compose: 8081 (mapeado)
