# 🧪 VitalWatch - Guía de Pruebas Kafka

Guía completa para probar el sistema de streaming con Apache Kafka.

## 📋 Pre-requisitos

✅ Docker y Docker Compose instalados  
✅ Cluster Kafka levantado (`./start-kafka-cluster.sh`)  
✅ Tópicos creados (`./create-kafka-topics.sh`)  
✅ Microservicios corriendo (`docker-compose -f docker-compose-kafka.yml up -d`)  
✅ Tablas de Oracle creadas (`create_tables_kafka.sql`)  

## 🎯 Secuencia de Pruebas

### 1. Verificar Estado del Cluster Kafka

#### Opción A: Kafka UI (Recomendado)
```
🌐 http://localhost:8080
```

Verificar:
- ✅ 3 Brokers online (kafka1, kafka2, kafka3)
- ✅ 2 Tópicos creados
- ✅ Particiones y replicación correctas

#### Opción B: Línea de comandos
```bash
# Ver tópicos
docker exec -it vitalwatch-kafka1 kafka-topics \
  --bootstrap-server kafka1:9092 \
  --list

# Ver consumer groups
docker exec -it vitalwatch-kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:9092 \
  --list
```

### 2. Verificar Health de Microservicios

```bash
# Stream Generator (Producer 1)
curl http://localhost:8081/api/v1/stream/health

# Alert Processor (Producer 2)
curl http://localhost:8082/api/v1/processor/health

# Summary Generator (Consumer 2)
curl http://localhost:8083/api/v1/summary/health
```

**Respuesta esperada (todos):**
```json
{
  "code": "200",
  "message": "Servicio operativo",
  "data": {
    "service": "...",
    "status": "UP"
  }
}
```

### 3. Iniciar el Stream de Signos Vitales

```bash
curl -X POST http://localhost:8081/api/v1/stream/start
```

**Respuesta esperada:**
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

### 4. Verificar Stream en Tiempo Real

#### Opción A: Kafka UI
1. Ir a http://localhost:8080
2. Click en "Topics"
3. Click en "signos-vitales-stream"
4. Click en "Messages"
5. Ver mensajes llegando cada segundo

#### Opción B: Ver logs del productor
```bash
docker logs -f vitalwatch-producer-stream
```

**Output esperado:**
```
✅ Stream: 10 mensajes publicados | Último: Juan Pérez - FC:75 | Partición: 1 | Offset: 123
✅ Stream: 20 mensajes publicados | Último: María García - FC:82 | Partición: 0 | Offset: 98
```

### 5. Verificar Detección de Alertas

```bash
# Ver logs del Alert Processor
docker logs -f vitalwatch-producer-alert
```

**Output esperado (cuando detecta anomalía):**
```
🚨 Alerta generada #5 - Paciente: Carlos López - Severidad: CRITICA - 2 anomalías
📊 Stream procesado: 60 mensajes | Alertas: 9 (15.00 %)
```

#### Ver alertas en Kafka UI
1. Ir a http://localhost:8080
2. Click en "Topics"
3. Click en "alertas-medicas"
4. Verificar que hay mensajes de alertas

### 6. Verificar Guardado en Oracle

```sql
-- Conectarse a Oracle SQL Developer

-- Ver últimas mediciones
SELECT 
    paciente_nombre,
    frecuencia_cardiaca,
    temperatura,
    saturacion_oxigeno,
    timestamp_medicion,
    kafka_partition,
    kafka_offset
FROM SIGNOS_VITALES_KAFKA
ORDER BY timestamp_medicion DESC
FETCH FIRST 20 ROWS ONLY;

-- Ver alertas recientes
SELECT 
    alert_id,
    paciente_nombre,
    severidad,
    tipo_alerta,
    cantidad_anomalias,
    detected_at
FROM ALERTAS_KAFKA
ORDER BY detected_at DESC
FETCH FIRST 10 ROWS ONLY;

-- Contar mensajes del día
SELECT 
    COUNT(*) as total_mediciones,
    COUNT(DISTINCT paciente_id) as pacientes_unicos,
    ROUND(AVG(frecuencia_cardiaca), 2) as avg_fc,
    ROUND(AVG(temperatura), 2) as avg_temp
FROM SIGNOS_VITALES_KAFKA
WHERE TRUNC(timestamp_medicion) = TRUNC(SYSDATE);

-- Ver alertas por severidad
SELECT 
    severidad,
    COUNT(*) as total,
    COUNT(DISTINCT paciente_id) as pacientes
FROM ALERTAS_KAFKA
WHERE TRUNC(detected_at) = TRUNC(SYSDATE)
GROUP BY severidad
ORDER BY 
    CASE severidad
        WHEN 'CRITICA' THEN 1
        WHEN 'ALTA' THEN 2
        WHEN 'MODERADA' THEN 3
        WHEN 'BAJA' THEN 4
    END;
```

### 7. Probar Estadísticas via API

```bash
# Estadísticas del Stream Generator
curl http://localhost:8081/api/v1/stream/stats
```

**Respuesta esperada:**
```json
{
  "code": "200",
  "message": "Estadísticas del stream generator",
  "data": {
    "totalMessagesSent": 3600,
    "streamEnabled": true,
    "topic": "signos-vitales-stream",
    "intervalMs": 1000,
    "messagesPerMinute": 60,
    "messagesPerHour": 3600
  }
}
```

```bash
# Estadísticas del Alert Processor
curl http://localhost:8082/api/v1/processor/stats
```

**Respuesta esperada:**
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

### 8. Generar Resumen Diario

```bash
# Generar resumen del día actual
curl -X POST http://localhost:8083/api/v1/summary/generate
```

**Respuesta esperada:**
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

```bash
# Obtener resumen generado
curl http://localhost:8083/api/v1/summary/daily/2026-02-13
```

### 9. Pausar y Reiniciar Stream

```bash
# Pausar stream
curl -X POST http://localhost:8081/api/v1/stream/stop

# Esperar 10 segundos y verificar que no llegan mensajes nuevos

# Reiniciar stream
curl -X POST http://localhost:8081/api/v1/stream/start

# Verificar que mensajes vuelven a llegar
```

### 10. Verificar Consumer Groups en Kafka

```bash
# Ver consumer groups activos
docker exec -it vitalwatch-kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:9092 \
  --list

# Ver detalles de un consumer group
docker exec -it vitalwatch-kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:9092 \
  --group alert-processor-group \
  --describe
```

**Output esperado:**
```
TOPIC                    PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
signos-vitales-stream    0          1234            1234            0
signos-vitales-stream    1          1189            1189            0
signos-vitales-stream    2          1177            1177            0
```

## 📊 Pruebas de Rendimiento

### Test 1: Throughput del Stream (1 minuto)

```bash
# 1. Reiniciar contadores
curl -X POST http://localhost:8081/api/v1/stream/start

# 2. Esperar 60 segundos
sleep 60

# 3. Ver estadísticas
curl http://localhost:8081/api/v1/stream/stats
```

**Resultado esperado:**
- Total mensajes: ~60 (1 por segundo)
- Mensajes por minuto: 60

### Test 2: Tasa de Alertas (5 minutos)

```bash
# 1. Iniciar stream
curl -X POST http://localhost:8081/api/v1/stream/start

# 2. Esperar 5 minutos
sleep 300

# 3. Ver estadísticas del processor
curl http://localhost:8082/api/v1/processor/stats
```

**Resultado esperado:**
- Mensajes procesados: ~300
- Alertas generadas: ~45 (15%)
- Alert rate: 15%

### Test 3: Persistencia en Oracle (10 minutos)

```bash
# 1. Iniciar stream
curl -X POST http://localhost:8081/api/v1/stream/start

# 2. Esperar 10 minutos
sleep 600

# 3. Consultar Oracle
```

```sql
SELECT COUNT(*) FROM SIGNOS_VITALES_KAFKA 
WHERE timestamp_medicion >= SYSDATE - INTERVAL '10' MINUTE;

SELECT COUNT(*) FROM ALERTAS_KAFKA 
WHERE detected_at >= SYSDATE - INTERVAL '10' MINUTE;
```

**Resultado esperado:**
- SIGNOS_VITALES_KAFKA: ~600 registros
- ALERTAS_KAFKA: ~90 registros

## 🔍 Troubleshooting

### Problema: Stream no inicia

```bash
# Verificar estado del servicio
docker logs vitalwatch-producer-stream

# Verificar conectividad con Kafka
docker exec -it vitalwatch-producer-stream sh
nc -zv kafka1 9092
```

### Problema: No se detectan alertas

```bash
# Ver logs del Alert Processor
docker logs vitalwatch-producer-alert

# Verificar que está consumiendo
docker exec -it vitalwatch-kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:9092 \
  --group alert-processor-group \
  --describe
```

### Problema: No se guarda en Oracle

```bash
# Ver logs del Database Saver
docker logs vitalwatch-consumer-db-kafka

# Verificar conexión a Oracle
docker exec -it vitalwatch-consumer-db-kafka sh
ls -la /app/wallet
```

### Problema: Lag en consumer groups

```bash
# Ver lag de todos los grupos
docker exec -it vitalwatch-kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:9092 \
  --all-groups \
  --describe
```

Si hay LAG alto (>100):
- Aumentar concurrencia en el consumidor
- Verificar rendimiento de Oracle
- Revisar logs por errores

## ✅ Checklist de Validación Completa

- [ ] Cluster Kafka con 3 brokers activos
- [ ] 2 Tópicos creados (signos-vitales-stream, alertas-medicas)
- [ ] Kafka UI accesible en puerto 8080
- [ ] Stream Generator iniciado y generando mensajes
- [ ] Alert Processor consumiendo y detectando anomalías
- [ ] Database Saver guardando en Oracle
- [ ] Summary Generator generando resúmenes
- [ ] Tasa de alertas aproximadamente 15%
- [ ] Sin LAG en consumer groups
- [ ] Datos visibles en Oracle Cloud

## 📸 Capturas para Presentación

1. Kafka UI mostrando tópicos
2. Kafka UI mostrando mensajes en tiempo real
3. Postman con respuesta de `/stream/stats`
4. Postman con respuesta de `/processor/stats`
5. Oracle SQL Developer con consulta de últimas mediciones
6. Oracle SQL Developer con alertas críticas
7. Logs de Docker mostrando procesamiento
8. Kafka UI mostrando consumer groups sin LAG

## 🎥 Flujo de Demostración

1. Abrir Kafka UI y mostrar cluster vacío
2. Ejecutar `./create-kafka-topics.sh` y mostrar tópicos creados
3. Iniciar microservicios con Docker Compose
4. Iniciar stream con Postman
5. Mostrar mensajes llegando en Kafka UI
6. Esperar 1-2 minutos
7. Mostrar estadísticas en Postman
8. Consultar Oracle y mostrar datos guardados
9. Generar resumen diario
10. Mostrar resumen completo

## ⏱️ Timing de Pruebas

- Inicio de cluster: ~1 minuto
- Creación de tópicos: 10 segundos
- Inicio de microservicios: ~2 minutos
- Generación de datos: 1-5 minutos (según demo)
- Consultas Oracle: 5-10 segundos por query

Total estimado: **5-10 minutos** para demo completa
