# VitalWatch - Consumidor 2: Generador de Archivos JSON

Microservicio consumidor que escucha la cola `vital-signs-alerts` de RabbitMQ y genera archivos JSON individuales para cada alerta.

## Funcionalidad

- Consume mensajes de la cola `vital-signs-alerts`
- Deserializa alertas de JSON
- Genera archivo JSON individual por cada alerta
- Nombres de archivo únicos con timestamp
- Almacenamiento en volumen montado

## Formato de Archivos

### Nomenclatura
```
alert_YYYYMMDD_HHmmss_SSS_P{pacienteId}_{severity}.json
```

Ejemplos:
- `alert_20260126_223015_123_P1_critica.json`
- `alert_20260126_223045_456_P5_alta.json`
- `alert_20260126_223112_789_P3_media.json`

### Contenido del Archivo
```json
{
  "pacienteId": 1,
  "pacienteNombre": "Juan Pérez",
  "sala": "UCI",
  "cama": "A-01",
  "severity": "CRITICA",
  "detectedAt": "2026-01-26T22:30:15.123",
  "deviceId": "DEVICE-001",
  "anomalies": [
    {
      "tipo": "CRITICA",
      "parametro": "Frecuencia Cardíaca",
      "valorActual": "125 lpm",
      "rangoNormal": "60-100 lpm",
      "mensaje": "Frecuencia cardíaca de 125 lpm está en rango crítico"
    }
  ],
  "vitalSigns": {
    "frecuenciaCardiaca": 125,
    "presionSistolica": 165,
    "presionDiastolica": 95,
    "temperatura": "38.9",
    "saturacionOxigeno": 88,
    "frecuenciaRespiratoria": 28
  }
}
```

## Configuración

### Variables de Entorno
- `RABBITMQ_HOST`: Host de RabbitMQ
- `RABBITMQ_PORT`: Puerto de RabbitMQ
- `RABBITMQ_USERNAME`: Usuario de RabbitMQ
- `RABBITMQ_PASSWORD`: Contraseña de RabbitMQ
- `QUEUE_ALERTS`: Nombre de la cola (vital-signs-alerts)
- `JSON_OUTPUT_PATH`: Ruta de salida para archivos JSON

### Volumen de Salida
Los archivos JSON se generan en la ruta configurada (por defecto `/app/data/alerts`).

En Docker Compose, este directorio se monta en `./alerts-json` del host.

## Ejecución

### Local
```bash
mvn spring-boot:run
```

### Docker
```bash
docker build -t consumer-json-generator .
docker run -v $(pwd)/alerts-json:/app/data/alerts consumer-json-generator
```

### Docker Compose
```bash
docker-compose up consumer-json-generator
```

## Ubicación de Archivos

Los archivos JSON generados estarán disponibles en:
```
./alerts-json/
├── alert_20260126_223015_123_P1_critica.json
├── alert_20260126_223045_456_P5_alta.json
└── alert_20260126_223112_789_P3_media.json
```

## Características

- **Procesamiento Asíncrono**: Consume mensajes de forma asíncrona
- **Nombres Únicos**: Timestamp con milisegundos evita colisiones
- **JSON Formateado**: Archivos con indentación para legibilidad
- **Retry Logic**: 3 intentos con backoff
- **Monitoring**: Estadísticas de archivos generados/fallidos
