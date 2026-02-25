# 📊 VitalWatch - Consumidor 2: Summary Generator

Microservicio que genera resúmenes diarios agregados del sistema de streaming.

## 🎯 Funcionalidad

- Genera resumen diario completo del sistema
- Calcula estadísticas agregadas de signos vitales
- Cuenta alertas por severidad
- Ejecuta automáticamente a medianoche (CRON)
- Actualiza resumen del día actual cada 15 minutos
- API REST para generar resúmenes bajo demanda

## 🚀 Endpoints

### POST `/api/v1/summary/generate`
Genera resumen diario para una fecha específica.

**Query params:**
- `fecha` (opcional): Fecha en formato YYYY-MM-DD. Default: hoy

**Response:**
```json
{
  "code": "201",
  "message": "Resumen diario generado exitosamente",
  "data": {
    "fecha": "2026-02-13",
    "totalPacientes": 5,
    "totalMediciones": 3600,
    "totalAlertas": 540,
    "alertasCriticas": 45,
    "promedioFC": 78.5,
    "promedioTemp": 36.8,
    "promedioSpO2": 96.2
  }
}
```

### GET `/api/v1/summary/daily/{fecha}`
Obtiene resumen de una fecha específica.

**Response:**
```json
{
  "code": "200",
  "message": "Resumen diario encontrado",
  "data": {
    "id": 1,
    "fecha": "2026-02-13",
    "totalPacientesMonitoreados": 5,
    "totalMediciones": 3600,
    "medicionesPorHora": 150.0,
    "totalAlertas": 540,
    "alertasCriticas": 45,
    "alertasAltas": 90,
    "alertasModeradas": 270,
    "alertasBajas": 135,
    "promedioFrecuenciaCardiaca": 78.5,
    "promedioTemperatura": 36.8,
    "promedioSaturacionOxigeno": 96.2
  }
}
```

### GET `/api/v1/summary/all`
Lista todos los resúmenes generados.

### GET `/api/v1/summary/health`
Health check del servicio.

## ⏰ Tareas Programadas

### Resumen diario automático
- **CRON:** `0 0 0 * * ?` (medianoche)
- **Acción:** Genera resumen del día anterior
- **Tabla:** RESUMEN_DIARIO_KAFKA

### Actualización continua
- **CRON:** `0 */15 * * * ?` (cada 15 minutos)
- **Acción:** Actualiza resumen del día actual
- **Uso:** Monitoreo en tiempo real

## 📊 Estadísticas Generadas

### Mediciones
- Total de mediciones del día
- Pacientes únicos monitoreados
- Mediciones por hora
- Promedios de todos los signos vitales
- Valores máximos y mínimos

### Alertas
- Total de alertas del día
- Alertas por severidad (CRITICA, ALTA, MODERADA, BAJA)
- Pacientes con alertas

### Kafka
- Mensajes procesados por tópico

## 🏃 Ejecución

### Local
```bash
mvn spring-boot:run
```

### Docker
```bash
docker build -t consumer-summary-generator .
docker run -p 8083:8080 -v ./Wallet_S58ONUXCX4C1QXE9:/app/wallet:ro consumer-summary-generator
```

## 🐳 Puerto

- **Local:** 8080
- **Docker Compose:** 8083 (mapeado)
