# 🔍 VitalWatch - Productor 2: Alert Processor

Microservicio que consume el stream de signos vitales, detecta anomalías médicas y publica alertas al tópico de alertas.

## 🎯 Funcionalidad

- Consume mensajes del tópico `signos-vitales-stream`
- Analiza cada signo vital en tiempo real
- Detecta anomalías comparando con rangos clínicos normales
- Calcula severidad de la alerta (BAJA, MODERADA, ALTA, CRITICA)
- Publica alertas al tópico `alertas-medicas`

## 🚀 Endpoints

### GET `/api/v1/processor/stats`
Obtiene estadísticas del procesador.

**Response:**
```json
{
  "code": "200",
  "message": "Estadísticas del Alert Processor",
  "data": {
    "messagesProcessed": 3600,
    "alertsGenerated": 540,
    "alertRate": "15.00%",
    "topicConsuming": "signos-vitales-stream",
    "topicProducing": "alertas-medicas"
  }
}
```

### GET `/api/v1/processor/health`
Health check del servicio.

## 🔢 Rangos Clínicos

| Parámetro | Normal | Anormal | Crítico |
|-----------|--------|---------|---------|
| FC | 60-100 lpm | 50-60 o 100-120 | <40 o >120 |
| PA Sistólica | 90-120 mmHg | 80-90 o 120-140 | <70 o >160 |
| PA Diastólica | 60-80 mmHg | 50-60 o 80-90 | <40 o >100 |
| Temperatura | 36.0-37.5°C | 35.5-36 o 37.5-38 | <35 o >39.5 |
| SpO2 | 95-100% | 90-95% | <90% |
| FR | 12-20 rpm | 10-12 o 20-25 | <8 o >25 |

## 📊 Lógica de Severidad

- **CRITICA:** Al menos 1 anomalía crítica
- **ALTA:** Al menos 1 anomalía alta, ninguna crítica
- **MODERADA:** Solo anomalías moderadas
- **BAJA:** Anomalías menores

## 🏃 Ejecución

### Local
```bash
mvn spring-boot:run
```

### Docker
```bash
docker build -t producer-alert-processor .
docker run -p 8082:8080 producer-alert-processor
```

## 🐳 Puerto

- **Local:** 8080
- **Docker Compose:** 8082 (mapeado)
