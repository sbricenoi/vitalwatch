# VitalWatch - Productor 2: Generador de Resúmenes Periódicos

Microservicio productor que genera resúmenes periódicos de signos vitales y estadísticas del sistema cada 5 minutos.

## Funcionalidad

- Genera resúmenes automáticamente cada 5 minutos (configurable)
- Recopila estadísticas de pacientes y alertas
- Calcula promedios de signos vitales
- Publica resúmenes a la cola `vital-signs-summary` de RabbitMQ

## Endpoints

### POST /api/v1/summary/generate
Genera un resumen manualmente (para pruebas).

**Response:**
```json
{
  "code": "200",
  "message": "Resumen generado y publicado a RabbitMQ",
  "data": {
    "timestamp": "2026-01-26T22:00:00",
    "summaryType": "PERIODIC_SUMMARY",
    "totalPacientes": 9,
    "pacientesCriticos": 3,
    "alertasGeneradas": 12,
    "alertasCriticas": 5,
    "promedioFrecuenciaCardiaca": 78.5,
    "promedioTemperatura": 36.8,
    "promedioSaturacionOxigeno": 96.5
  }
}
```

### GET /api/v1/summary/stats
Obtiene estadísticas del generador.

## Configuración

### Intervalo de Generación
Por defecto: 5 minutos (300000 ms)

Para cambiar:
```properties
summary.interval.ms=60000  # 1 minuto para pruebas
```

## Ejecución

### Local
```bash
mvn spring-boot:run
```

### Docker
```bash
docker build -t producer-summary .
docker run -p 8082:8080 producer-summary
```

### Docker Compose
```bash
docker-compose up producer-summary
```

## Puerto

- Local: 8080
- Docker Compose: 8082 (mapeado)
