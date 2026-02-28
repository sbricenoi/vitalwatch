# 📖 Guía de Uso - VitalWatch

## 🚀 Inicio Rápido

### Sistema RabbitMQ

```bash
# Iniciar
docker-compose up -d

# Acceder al frontend
open http://localhost

# Ver RabbitMQ
open http://localhost:15672
```

### Sistema Kafka

```bash
# Iniciar (automatizado)
./quick-start-kafka.sh

# Acceder a Kafka UI
open http://localhost:9000
```

---

## 🎮 Interfaces Disponibles

### Frontend Web (RabbitMQ)

**URL:** http://localhost

**Funciones:**
- 📊 Dashboard de pacientes
- 📈 Gráficos de signos vitales
- ⚠️ Alertas en tiempo real
- 👥 Lista de pacientes
- 📋 Historial de mediciones

### RabbitMQ Management

**URL:** http://localhost:15672
**Credenciales:** guest / guest

**Funciones:**
- Ver queues y mensajes
- Monitorear consumers
- Estadísticas de throughput
- Gestión de exchanges

### Kafka UI

**URL:** http://localhost:9000

**Funciones:**
- Ver brokers (3 activos)
- Monitorear topics
- Ver mensajes en tiempo real
- Consumer groups y LAG
- Configuración de topics

---

## 🔌 APIs REST

### Backend (RabbitMQ)

**Base URL:** http://localhost:8080

#### Signos Vitales

```bash
# Listar todos
GET /api/signos-vitales

# Buscar por paciente
GET /api/signos-vitales/paciente/{id}

# Crear nuevo
POST /api/signos-vitales
Content-Type: application/json

{
  "pacienteId": 1,
  "frecuenciaCardiaca": 75,
  "temperatura": 36.5,
  "saturacionOxigeno": 98
}
```

#### Anomalías

```bash
# Listar anomalías
GET /api/anomalias

# Por severidad
GET /api/anomalias?severidad=ALTA

# Estadísticas
GET /api/anomalies/stats
```

### Stream Generator (Kafka)

**Base URL:** http://localhost:8091

```bash
# Ver estadísticas
GET /api/v1/stream/stats

# Ver estado
GET /api/v1/stream/status

# Iniciar stream
POST /api/v1/stream/start

# Detener stream
POST /api/v1/stream/stop

# Health check
GET /actuator/health
```

### Alert Processor (Kafka)

**Base URL:** http://localhost:8092

```bash
# Estadísticas de alertas
GET /api/v1/processor/stats

# Ver última alerta
GET /api/v1/processor/last-alert

# Health check
GET /actuator/health
```

### Summary Generator (Kafka)

**Base URL:** http://localhost:8094

```bash
# Resumen de hoy
GET /api/v1/summary/today

# Resumen por fecha
GET /api/v1/summary/{fecha}
# Ejemplo: /api/v1/summary/2026-02-25

# Generar resumen manualmente
POST /api/v1/summary/generate?date=2026-02-25

# Listar resúmenes
GET /api/v1/summary/list?limit=10

# Health check
GET /actuator/health
```

---

## 📊 Consultas SQL

### Ver Datos en Oracle

#### RabbitMQ

```sql
-- Últimos signos vitales
SELECT * FROM SIGNOS_VITALES 
ORDER BY timestamp DESC 
FETCH FIRST 20 ROWS ONLY;

-- Anomalías por severidad
SELECT severidad, COUNT(*) as cantidad
FROM ANOMALIAS
GROUP BY severidad
ORDER BY cantidad DESC;

-- Resumen del día
SELECT * FROM RESUMEN_DIARIO
WHERE fecha = CURRENT_DATE;

-- Pacientes activos
SELECT * FROM PACIENTES
WHERE estado = 'ACTIVO';
```

#### Kafka

```sql
-- Signos vitales con metadata Kafka
SELECT 
    paciente_nombre,
    frecuencia_cardiaca,
    temperatura,
    kafka_partition,
    kafka_offset,
    timestamp_medicion
FROM SIGNOS_VITALES_KAFKA
ORDER BY timestamp_medicion DESC
FETCH FIRST 20 ROWS ONLY;

-- Alertas por severidad
SELECT 
    severidad,
    COUNT(*) as total,
    COUNT(DISTINCT paciente_id) as pacientes_afectados
FROM ALERTAS_KAFKA
GROUP BY severidad
ORDER BY severidad;

-- Resumen diario actual
SELECT * FROM RESUMEN_DIARIO_KAFKA
WHERE fecha = CURRENT_DATE;

-- Stats de pacientes
SELECT * FROM PACIENTES_MONITOREADOS_KAFKA
ORDER BY ultima_medicion DESC;
```

---

## 🧪 Testing con Postman

### Importar Colección

1. Abrir Postman
2. Import → File
3. Seleccionar:
   - `docs/VitalWatch.postman_collection.json` (RabbitMQ)
   - `docs/VitalWatch-Kafka.postman_collection.json` (Kafka)

### Probar Endpoints

#### RabbitMQ

1. **Health Checks** - Verificar que todos respondan 200
2. **Backend** - GET signos vitales, POST nuevos
3. **Anomaly Detector** - Ver stats
4. **Summary Generator** - Ver resumen diario

#### Kafka

1. **Stream Generator** - Ver stats, iniciar/detener
2. **Alert Processor** - Ver alertas generadas
3. **Summary Generator** - Ver resumen, generar manual

---

## 📈 Monitoreo

### Ver Logs en Tiempo Real

```bash
# RabbitMQ - Ver logs de un servicio
docker-compose logs -f backend

# Ver todos los logs
docker-compose logs -f

# Kafka - Ver logs del stream
docker logs -f vitalwatch-producer-stream

# Ver logs del alert processor
docker logs -f vitalwatch-producer-alert
```

### Métricas de Kafka

```bash
# Ver topics
docker exec vitalwatch-kafka1 kafka-topics \
  --bootstrap-server kafka1:9092 \
  --list

# Ver mensajes en un topic
docker exec vitalwatch-kafka1 kafka-console-consumer \
  --bootstrap-server kafka1:9092 \
  --topic signos-vitales-stream \
  --from-beginning \
  --max-messages 10

# Ver consumer groups
docker exec vitalwatch-kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:9092 \
  --list

# Ver LAG de un grupo
docker exec vitalwatch-kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:9092 \
  --group alert-processor-group \
  --describe
```

---

## 🎯 Casos de Uso Comunes

### Caso 1: Monitorear un Paciente Nuevo

#### RabbitMQ
1. Ir al frontend: http://localhost
2. Agregar paciente en la interfaz
3. Sistema automáticamente:
   - Crea registro en Oracle
   - Inicia monitoreo
   - Backend publica a RabbitMQ

#### Kafka
1. Stream Generator ya incluye 5 pacientes de prueba
2. Para agregar más:
   - Editar `VitalSignsGeneratorService.java`
   - Agregar paciente a la lista
   - Rebuild: `docker-compose -f docker-compose-kafka.yml build producer-stream-generator`

### Caso 2: Ver Alertas en Tiempo Real

#### RabbitMQ
```bash
# Via API
curl http://localhost:8080/api/anomalias?limit=10

# Via SQL
SELECT * FROM ANOMALIAS 
WHERE fecha = CURRENT_DATE 
ORDER BY timestamp DESC;
```

#### Kafka
```bash
# Via API
curl http://localhost:8092/api/v1/processor/stats

# Via Kafka UI
open http://localhost:9000
# Topics → alertas-medicas → Messages

# Via SQL
SELECT * FROM ALERTAS_KAFKA
WHERE DATE(timestamp_alerta) = CURRENT_DATE
ORDER BY timestamp_alerta DESC;
```

### Caso 3: Generar Resumen Diario

#### RabbitMQ
```bash
# Automático (scheduler a las 00:00)
# O manual:
curl http://localhost:8082/api/summary/generate?date=2026-02-25
```

#### Kafka
```bash
# Automático (scheduler cada 15 min + medianoche)
# O manual:
curl -X POST http://localhost:8094/api/v1/summary/generate?date=2026-02-25

# Ver resumen
curl http://localhost:8094/api/v1/summary/today
```

---

## 🔄 Mantenimiento

### Reiniciar Servicios

```bash
# Un servicio específico
docker-compose restart backend

# Kafka - Un microservicio
docker-compose -f docker-compose-kafka.yml restart producer-stream-generator

# Todo el sistema
docker-compose restart
```

### Actualizar Código

```bash
# 1. Hacer cambios en el código
# 2. Rebuild
docker-compose build backend

# 3. Reiniciar
docker-compose up -d backend

# Kafka
docker-compose -f docker-compose-kafka.yml build producer-stream-generator
docker-compose -f docker-compose-kafka.yml up -d producer-stream-generator
```

### Backup de Datos

```sql
-- Exportar datos de Oracle
expdp ADMIN/password@s58onuxcx4c1qxe9_high \
  tables=SIGNOS_VITALES,ANOMALIAS,PACIENTES \
  directory=DATA_PUMP_DIR \
  dumpfile=vitalwatch_backup.dmp

-- Importar
impdp ADMIN/password@s58onuxcx4c1qxe9_high \
  tables=SIGNOS_VITALES,ANOMALIAS,PACIENTES \
  directory=DATA_PUMP_DIR \
  dumpfile=vitalwatch_backup.dmp
```

---

## 📱 Comandos Útiles

### Docker

```bash
# Ver todos los contenedores
docker ps

# Ver uso de recursos
docker stats

# Limpiar espacio
docker system prune -a

# Ver networks
docker network ls

# Inspeccionar un contenedor
docker inspect container-name
```

### Git

```bash
# Ver ramas
git branch -a

# Cambiar rama
git checkout feature/kafka-implementation

# Ver cambios
git status

# Actualizar
git pull origin feature/kafka-implementation
```

### Kafka

```bash
# Listar topics
docker exec vitalwatch-kafka1 kafka-topics \
  --bootstrap-server kafka1:9092 \
  --list

# Describir un topic
docker exec vitalwatch-kafka1 kafka-topics \
  --bootstrap-server kafka1:9092 \
  --describe \
  --topic signos-vitales-stream

# Ver offsets
docker exec vitalwatch-kafka1 kafka-run-class kafka.tools.GetOffsetShell \
  --broker-list kafka1:9092 \
  --topic signos-vitales-stream
```

---

## 🆘 Soporte

### Logs Importantes

```bash
# Ver logs de error
docker-compose logs | grep ERROR

# Ver logs de warnings
docker-compose logs | grep WARN

# Exportar logs a archivo
docker-compose logs > system-logs.txt
```

### Información de Debug

```bash
# Ver variables de entorno
docker exec container-name env

# Ver procesos
docker exec container-name ps aux

# Acceder al contenedor
docker exec -it container-name bash
```

---

## 📊 Métricas Esperadas

### RabbitMQ (Después de 10 minutos)
- Signos vitales en DB: ~600
- Anomalías detectadas: ~90 (15%)
- Queues vacías: Sí (procesamiento rápido)
- Messages/s: ~1

### Kafka (Después de 10 minutos)
- Mensajes en topics: ~600
- Alertas generadas: ~90 (15%)
- Consumer LAG: 0
- Throughput: 1 msg/s
- Particiones balanceadas: Sí

---

## 🎓 Para Presentación

### Demostración Recomendada

1. **Mostrar Frontend** (RabbitMQ)
   - Dashboard con pacientes
   - Gráficos en tiempo real

2. **Mostrar Kafka UI** (Kafka)
   - 3 Brokers activos
   - Mensajes en topics
   - Consumer groups sin LAG

3. **Probar APIs** (Postman)
   - GET stats
   - Ver respuestas JSON

4. **Mostrar Datos** (Oracle)
   - Query de signos vitales
   - Query de alertas

5. **Mostrar GitHub**
   - Código organizado
   - Ramas separadas
   - Documentación

---

## 🔗 Enlaces Rápidos

| Recurso | URL |
|---------|-----|
| **GitHub Repo** | https://github.com/sbricenoi/vitalwatch |
| **Frontend Local** | http://localhost |
| **Backend Local** | http://localhost:8080 |
| **RabbitMQ UI** | http://localhost:15672 |
| **Kafka UI** | http://localhost:9000 |
| **Postman Collections** | Ver carpeta `docs/` |

---

**¿Necesitas ayuda?** Consulta `docs/ARQUITECTURA.md` o `docs/GUIA_DEPLOY.md`
