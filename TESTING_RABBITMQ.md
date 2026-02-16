# VitalWatch - Plan de Pruebas RabbitMQ

Este documento detalla el plan de pruebas end-to-end para la integración de RabbitMQ en VitalWatch.

## Requisitos Previos

### 1. Oracle Cloud - Crear Tabla
Ejecutar el script SQL en Oracle Cloud Console:
```bash
# Ubicación: database/create_alertas_mq_table.sql
```

### 2. Levantar Servicios
```bash
docker-compose -f docker-compose-rabbitmq.yml up --build
```

Servicios que deben estar corriendo:
- ✅ RabbitMQ (puertos 5672, 15672)
- ✅ Producer Anomaly Detector (puerto 8081)
- ✅ Producer Summary (puerto 8082)
- ✅ Consumer DB Saver (sin puerto)
- ✅ Consumer JSON Generator (sin puerto)

## Tests

### Test 1: Health Checks

#### 1.1 RabbitMQ Management UI
```bash
# Abrir en navegador
open http://localhost:15672

# Credenciales:
# Usuario: vitalwatch
# Password: hospital123
```

**Verificar:**
- [x] Interfaz de RabbitMQ carga correctamente
- [x] Login exitoso
- [x] Ver sección "Queues"

#### 1.2 Productor 1 - Detector de Anomalías
```bash
curl http://localhost:8081/api/v1/vital-signs/health | jq
```

**Resultado Esperado:**
```json
{
  "code": "200",
  "message": "Productor operativo",
  "data": {
    "status": "UP",
    "service": "Anomaly Detector Producer",
    "timestamp": "2026-01-26T22:00:00"
  }
}
```

#### 1.3 Productor 2 - Generador de Resúmenes
```bash
curl http://localhost:8082/api/v1/summary/health | jq
```

**Resultado Esperado:**
```json
{
  "code": "200",
  "message": "Productor operativo",
  "data": {
    "status": "UP",
    "service": "Summary Generator Producer",
    "timestamp": "2026-01-26T22:00:00"
  }
}
```

---

### Test 2: Productor 1 - Signos Vitales Normales

#### 2.1 Enviar signos vitales normales
```bash
curl -X POST http://localhost:8081/api/v1/vital-signs/check \
  -H "Content-Type: application/json" \
  -d '{
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
  }' | jq
```

**Resultado Esperado:**
```json
{
  "code": "200",
  "message": "Signos vitales verificados correctamente",
  "data": {
    "hasAnomalies": false,
    "anomaliesCount": 0,
    "alertPublished": false,
    "message": "Signos vitales dentro de rangos normales"
  }
}
```

**Verificaciones:**
- [x] Response code: 200
- [x] `hasAnomalies`: false
- [x] `alertPublished`: false
- [x] No se publicó mensaje a RabbitMQ

---

### Test 3: Productor 1 - Signos Vitales CRÍTICOS

#### 3.1 Enviar signos vitales críticos
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
    "alertPublished": true,
    "queueName": "vital-signs-alerts"
  }
}
```

**Verificaciones:**
- [x] Response code: 201
- [x] `hasAnomalies`: true
- [x] `anomaliesCount`: >= 5
- [x] `severity`: "CRITICA"
- [x] `alertPublished`: true

#### 3.2 Verificar mensaje en RabbitMQ
```bash
# Abrir Management UI
open http://localhost:15672

# Navegar a: Queues → vital-signs-alerts
# Verificar:
# - Messages ready: >= 1
# - Message rate: > 0
```

**Verificar en cola:**
- [x] Cola `vital-signs-alerts` tiene mensajes
- [x] Publish rate > 0
- [x] Consumer rate > 0 (consumidores activos)

---

### Test 4: Consumidor 1 - Verificar Guardado en Oracle

#### 4.1 Conectar a Oracle Cloud
```sql
-- Oracle Cloud Console → Database Actions → SQL
SELECT * FROM ALERTAS_MQ 
ORDER BY DETECTED_AT DESC 
FETCH FIRST 10 ROWS ONLY;
```

**Verificaciones:**
- [x] Tabla `ALERTAS_MQ` contiene registros
- [x] `PACIENTE_ID` = 1
- [x] `SEVERITY` = 'CRITICA'
- [x] `ANOMALIAS_COUNT` >= 5
- [x] `DETECTED_AT` es reciente (últimos minutos)
- [x] `ANOMALIAS_JSON` contiene JSON válido
- [x] Signos vitales están guardados correctamente

#### 4.2 Verificar logs del Consumidor DB Saver
```bash
docker logs vitalwatch-consumer-db -f
```

**Logs Esperados:**
```
📥 Alerta recibida desde RabbitMQ: Paciente 1 - Severidad: CRITICA - 6 anomalías
✅ Alerta guardada en Oracle con ID: 1 - Total procesadas: 1
```

---

### Test 5: Consumidor 2 - Verificar Generación de JSON

#### 5.1 Verificar archivos JSON generados
```bash
ls -lh alerts-json/

# Ejemplo de archivo generado:
# alert_20260126_223015_123_P1_critica.json
```

**Verificaciones:**
- [x] Directorio `alerts-json/` contiene archivos
- [x] Nombre de archivo sigue formato: `alert_YYYYMMDD_HHmmss_SSS_P{id}_{severity}.json`
- [x] Cantidad de archivos coincide con alertas generadas

#### 5.2 Verificar contenido del JSON
```bash
cat alerts-json/alert_*.json | jq
```

**Contenido Esperado:**
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

**Verificaciones:**
- [x] JSON válido y bien formateado
- [x] Todos los campos presentes
- [x] `anomalies` array contiene las anomalías
- [x] `vitalSigns` contiene los valores medidos

#### 5.3 Verificar logs del Consumidor JSON Generator
```bash
docker logs vitalwatch-consumer-json -f
```

**Logs Esperados:**
```
📥 Alerta recibida desde RabbitMQ: Paciente 1 - Severidad: CRITICA - 6 anomalías
✅ Archivo JSON generado: alert_20260126_223015_123_P1_critica.json - Total generados: 1
```

---

### Test 6: Productor 2 - Generador de Resúmenes

#### 6.1 Generar resumen manualmente
```bash
curl -X POST http://localhost:8082/api/v1/summary/generate | jq
```

**Resultado Esperado:**
```json
{
  "code": "200",
  "message": "Resumen generado y publicado a RabbitMQ",
  "data": {
    "timestamp": "2026-01-26T22:35:00",
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

**Verificaciones:**
- [x] Response code: 200
- [x] `summaryType`: "PERIODIC_SUMMARY"
- [x] Estadísticas presentes

#### 6.2 Verificar estadísticas del generador
```bash
curl http://localhost:8082/api/v1/summary/stats | jq
```

**Resultado Esperado:**
```json
{
  "code": "200",
  "message": "Estadísticas obtenidas",
  "data": {
    "totalSummariesGenerated": 1,
    "lastCheck": "2026-01-26T22:35:00"
  }
}
```

#### 6.3 Esperar generación automática (5 minutos)
```bash
# Esperar 5 minutos y verificar logs
docker logs vitalwatch-producer-summary -f
```

**Logs Esperados (cada 5 minutos):**
```
📊 Generando resumen periódico #2
📤 Resumen publicado a RabbitMQ: 9 pacientes, 5 alertas críticas
```

---

### Test 7: Múltiples Alertas Simultáneas

#### 7.1 Generar múltiples alertas rápidamente
```bash
# Script para generar 10 alertas
for i in {1..10}; do
  curl -X POST http://localhost:8081/api/v1/vital-signs/check \
    -H "Content-Type: application/json" \
    -d "{
      \"pacienteId\": $i,
      \"pacienteNombre\": \"Paciente $i\",
      \"sala\": \"UCI\",
      \"cama\": \"A-0$i\",
      \"frecuenciaCardiaca\": 125,
      \"presionSistolica\": 165,
      \"presionDiastolica\": 95,
      \"temperatura\": 38.9,
      \"saturacionOxigeno\": 88,
      \"frecuenciaRespiratoria\": 28,
      \"deviceId\": \"DEVICE-00$i\"
    }"
  echo ""
  sleep 1
done
```

**Verificaciones:**
- [x] 10 alertas generadas
- [x] RabbitMQ procesó todos los mensajes
- [x] Oracle contiene 10 nuevos registros
- [x] `alerts-json/` contiene 10 nuevos archivos
- [x] Sin errores en logs de consumidores

#### 7.2 Verificar en Oracle
```sql
SELECT COUNT(*) FROM ALERTAS_MQ WHERE DETECTED_AT >= SYSTIMESTAMP - INTERVAL '1' MINUTE;
-- Debe retornar: 10
```

#### 7.3 Verificar archivos JSON
```bash
ls -l alerts-json/ | wc -l
# Debe mostrar al menos 10 archivos nuevos
```

---

### Test 8: Pruebas de Carga

#### 8.1 Generar 100 alertas
```bash
for i in {1..100}; do
  curl -s -X POST http://localhost:8081/api/v1/vital-signs/check \
    -H "Content-Type: application/json" \
    -d "{
      \"pacienteId\": $((i % 10 + 1)),
      \"pacienteNombre\": \"Paciente Test\",
      \"sala\": \"UCI\",
      \"cama\": \"A-01\",
      \"frecuenciaCardiaca\": 130,
      \"presionSistolica\": 170,
      \"presionDiastolica\": 100,
      \"temperatura\": 39.0,
      \"saturacionOxigeno\": 85,
      \"frecuenciaRespiratoria\": 30,
      \"deviceId\": \"DEVICE-TEST\"
    }" > /dev/null
done

echo "✅ 100 alertas generadas"
```

#### 8.2 Verificar métricas en RabbitMQ
```bash
# Management UI → Overview
# Verificar:
# - Message rate
# - Publish rate
# - Delivery rate
```

#### 8.3 Verificar logs sin errores
```bash
docker-compose -f docker-compose-rabbitmq.yml logs --tail=50 | grep -E "ERROR|WARN"
# No debe mostrar errores críticos
```

---

### Test 9: Validaciones y Errores

#### 9.1 Enviar datos inválidos (sin campos requeridos)
```bash
curl -X POST http://localhost:8081/api/v1/vital-signs/check \
  -H "Content-Type: application/json" \
  -d '{
    "pacienteId": 1
  }' | jq
```

**Resultado Esperado:**
```json
{
  "code": "400",
  "message": "Error de validación...",
  ...
}
```

#### 9.2 Enviar valores fuera de rango
```bash
curl -X POST http://localhost:8081/api/v1/vital-signs/check \
  -H "Content-Type: application/json" \
  -d '{
    "pacienteId": 1,
    "pacienteNombre": "Test",
    "sala": "UCI",
    "cama": "A-01",
    "frecuenciaCardiaca": 500,
    "presionSistolica": 120,
    "presionDiastolica": 80,
    "temperatura": 36.5,
    "saturacionOxigeno": 98,
    "frecuenciaRespiratoria": 16,
    "deviceId": "DEVICE-001"
  }' | jq
```

**Resultado Esperado:**
```json
{
  "code": "400",
  "message": "La frecuencia cardíaca debe ser menor o igual a 300"
}
```

---

## Resumen de Verificaciones

### Checklist General

#### Infraestructura
- [ ] RabbitMQ corriendo (puertos 5672, 15672)
- [ ] Management UI accesible
- [ ] Colas creadas: `vital-signs-alerts`, `vital-signs-summary`

#### Productores
- [ ] Productor 1 (Anomaly Detector) operativo en puerto 8081
- [ ] Productor 2 (Summary Generator) operativo en puerto 8082
- [ ] Health checks responden correctamente

#### Consumidores
- [ ] Consumidor 1 (DB Saver) guardando en Oracle
- [ ] Consumidor 2 (JSON Generator) generando archivos
- [ ] Sin errores en logs

#### Funcionalidad End-to-End
- [ ] Signos vitales normales NO generan alertas
- [ ] Signos vitales críticos SÍ generan alertas
- [ ] Alertas se publican a RabbitMQ
- [ ] Alertas se guardan en Oracle (tabla ALERTAS_MQ)
- [ ] Archivos JSON se generan en `alerts-json/`
- [ ] Resúmenes periódicos se generan cada 5 minutos
- [ ] Generación manual de resúmenes funciona

#### Performance
- [ ] Sistema procesa 100+ alertas sin errores
- [ ] Logs sin mensajes de error crítico
- [ ] Latencia aceptable (<2s por alerta)

---

## Comandos Útiles

### Docker Compose
```bash
# Levantar servicios
docker-compose -f docker-compose-rabbitmq.yml up --build

# Ver logs en tiempo real
docker-compose -f docker-compose-rabbitmq.yml logs -f

# Ver logs de un servicio específico
docker logs vitalwatch-producer-anomaly -f
docker logs vitalwatch-consumer-db -f

# Detener servicios
docker-compose -f docker-compose-rabbitmq.yml down

# Detener y limpiar volúmenes
docker-compose -f docker-compose-rabbitmq.yml down -v
```

### RabbitMQ CLI
```bash
# Entrar al contenedor de RabbitMQ
docker exec -it vitalwatch-rabbitmq bash

# Listar colas
rabbitmqctl list_queues

# Ver consumidores
rabbitmqctl list_consumers

# Ver estadísticas
rabbitmqctl list_queues name messages_ready messages_unacknowledged
```

### Oracle SQL
```sql
-- Ver últimas alertas
SELECT * FROM ALERTAS_MQ ORDER BY DETECTED_AT DESC FETCH FIRST 10 ROWS ONLY;

-- Contar alertas por severidad
SELECT SEVERITY, COUNT(*) FROM ALERTAS_MQ GROUP BY SEVERITY;

-- Ver alertas críticas de hoy
SELECT * FROM ALERTAS_MQ 
WHERE SEVERITY = 'CRITICA' 
  AND TRUNC(DETECTED_AT) = TRUNC(SYSDATE);

-- Limpiar tabla (para tests)
-- DELETE FROM ALERTAS_MQ;
-- COMMIT;
```

---

## Troubleshooting

### Problema: RabbitMQ no levanta
**Solución:**
```bash
docker-compose -f docker-compose-rabbitmq.yml down -v
docker-compose -f docker-compose-rabbitmq.yml up rabbitmq
# Esperar a que esté healthy
docker ps
```

### Problema: Consumidor no guarda en Oracle
**Verificar:**
1. Wallet montado correctamente
2. Credenciales de Oracle correctas
3. Tabla `ALERTAS_MQ` existe
```bash
docker logs vitalwatch-consumer-db
```

### Problema: No se generan archivos JSON
**Verificar:**
1. Directorio `alerts-json/` existe
2. Permisos correctos
```bash
ls -la alerts-json/
docker logs vitalwatch-consumer-json
```

### Problema: Productor no puede conectar a RabbitMQ
**Verificar:**
1. RabbitMQ está corriendo
2. Credenciales correctas
3. Health check de RabbitMQ
```bash
docker ps | grep rabbitmq
curl http://localhost:15672/api/health/checks/alarms
```

---

## Conclusión

Este plan de pruebas cubre:
- ✅ Health checks de todos los componentes
- ✅ Flujo end-to-end completo
- ✅ Productores y Consumidores
- ✅ Persistencia en Oracle
- ✅ Generación de archivos JSON
- ✅ Pruebas de carga
- ✅ Validaciones y manejo de errores
- ✅ Troubleshooting común
