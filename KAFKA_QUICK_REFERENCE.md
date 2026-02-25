# 🚀 Kafka Quick Reference - VitalWatch

Referencia rápida de comandos y configuraciones para el sistema Kafka.

## ⚡ Inicio Rápido (1 Comando)

```bash
./quick-start-kafka.sh
```

## 📝 Comandos Esenciales

### Cluster Management

```bash
# Iniciar cluster completo
./start-kafka-cluster.sh

# Crear tópicos
./create-kafka-topics.sh

# Iniciar microservicios
docker-compose -f docker-compose-kafka.yml up -d

# Ver estado
docker-compose -f docker-compose-kafka.yml ps

# Detener todo
docker-compose -f docker-compose-kafka.yml down

# Detener y eliminar volúmenes
docker-compose -f docker-compose-kafka.yml down -v
```

### Kafka Topics

```bash
# Listar tópicos
docker exec -it vitalwatch-kafka1 kafka-topics \
  --bootstrap-server kafka1:9092 \
  --list

# Describir un tópico
docker exec -it vitalwatch-kafka1 kafka-topics \
  --bootstrap-server kafka1:9092 \
  --describe \
  --topic signos-vitales-stream

# Eliminar un tópico
docker exec -it vitalwatch-kafka1 kafka-topics \
  --bootstrap-server kafka1:9092 \
  --delete \
  --topic nombre-topico
```

### Consumer Groups

```bash
# Listar consumer groups
docker exec -it vitalwatch-kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:9092 \
  --list

# Ver detalles y LAG
docker exec -it vitalwatch-kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:9092 \
  --group alert-processor-group \
  --describe

# Reset offsets (CUIDADO: reprocessa todos los mensajes)
docker exec -it vitalwatch-kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:9092 \
  --group mi-grupo \
  --topic mi-topico \
  --reset-offsets \
  --to-earliest \
  --execute
```

### Ver Mensajes

```bash
# Consumir mensajes desde el principio
docker exec -it vitalwatch-kafka1 kafka-console-consumer \
  --bootstrap-server kafka1:9092 \
  --topic signos-vitales-stream \
  --from-beginning \
  --max-messages 10

# Consumir mensajes en tiempo real
docker exec -it vitalwatch-kafka1 kafka-console-consumer \
  --bootstrap-server kafka1:9092 \
  --topic signos-vitales-stream
```

## 🌐 URLs y Puertos

| Servicio | URL | Descripción |
|----------|-----|-------------|
| Kafka UI | http://localhost:8080 | Interfaz web |
| Stream Generator | http://localhost:8081 | Producer 1 |
| Alert Processor | http://localhost:8082 | Producer 2 |
| Summary Generator | http://localhost:8083 | Consumer 2 |
| Kafka Broker 1 | localhost:19092 | Conexión externa |
| Kafka Broker 2 | localhost:19093 | Conexión externa |
| Kafka Broker 3 | localhost:19094 | Conexión externa |

## 📊 API Endpoints

### Stream Generator (8081)

```bash
# Health
curl http://localhost:8081/api/v1/stream/health

# Iniciar stream
curl -X POST http://localhost:8081/api/v1/stream/start

# Pausar stream
curl -X POST http://localhost:8081/api/v1/stream/stop

# Ver estado
curl http://localhost:8081/api/v1/stream/status

# Enviar mensaje manual
curl -X POST http://localhost:8081/api/v1/stream/send-manual

# Estadísticas
curl http://localhost:8081/api/v1/stream/stats
```

### Alert Processor (8082)

```bash
# Health
curl http://localhost:8082/api/v1/processor/health

# Estadísticas
curl http://localhost:8082/api/v1/processor/stats
```

### Summary Generator (8083)

```bash
# Health
curl http://localhost:8083/api/v1/summary/health

# Generar resumen de hoy
curl -X POST http://localhost:8083/api/v1/summary/generate

# Generar resumen de fecha específica
curl -X POST "http://localhost:8083/api/v1/summary/generate?fecha=2026-02-13"

# Obtener resumen
curl http://localhost:8083/api/v1/summary/daily/2026-02-13

# Ver todos los resúmenes
curl http://localhost:8083/api/v1/summary/all
```

## 🔍 Logs y Debugging

```bash
# Ver logs de un servicio
docker logs vitalwatch-producer-stream

# Seguir logs en tiempo real
docker logs -f vitalwatch-producer-stream

# Ver últimas 100 líneas
docker logs --tail 100 vitalwatch-producer-stream

# Ver logs de todos los servicios
docker-compose -f docker-compose-kafka.yml logs

# Ver logs de Kafka broker
docker logs vitalwatch-kafka1 | grep ERROR
```

## 💾 Oracle Queries Útiles

```sql
-- Estadísticas generales
SELECT 
    (SELECT COUNT(*) FROM SIGNOS_VITALES_KAFKA) as total_mediciones,
    (SELECT COUNT(*) FROM ALERTAS_KAFKA) as total_alertas,
    (SELECT COUNT(*) FROM RESUMEN_DIARIO_KAFKA) as total_resumenes,
    (SELECT COUNT(*) FROM PACIENTES_MONITOREADOS_KAFKA) as total_pacientes
FROM DUAL;

-- Últimas 20 mediciones
SELECT 
    paciente_nombre,
    frecuencia_cardiaca as FC,
    temperatura as TEMP,
    saturacion_oxigeno as SPO2,
    TO_CHAR(timestamp_medicion, 'HH24:MI:SS') as HORA
FROM SIGNOS_VITALES_KAFKA
ORDER BY timestamp_medicion DESC
FETCH FIRST 20 ROWS ONLY;

-- Alertas del día por severidad
SELECT 
    severidad,
    COUNT(*) as total,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as porcentaje
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

-- Resumen del día actual
SELECT * FROM RESUMEN_DIARIO_KAFKA 
WHERE fecha = TRUNC(SYSDATE);

-- Últimas alertas críticas
SELECT 
    paciente_nombre,
    mensaje,
    cantidad_anomalias,
    TO_CHAR(detected_at, 'HH24:MI:SS') as hora
FROM ALERTAS_KAFKA
WHERE severidad = 'CRITICA'
ORDER BY detected_at DESC
FETCH FIRST 10 ROWS ONLY;

-- Usar vistas
SELECT * FROM V_ULTIMAS_MEDICIONES_KAFKA WHERE rn = 1;
SELECT * FROM V_ALERTAS_ACTIVAS_KAFKA;
SELECT * FROM V_ESTADISTICAS_TIEMPO_REAL_KAFKA;
```

## 🐳 Docker Commands

```bash
# Rebuild un servicio
docker-compose -f docker-compose-kafka.yml build producer-stream-generator

# Restart un servicio
docker-compose -f docker-compose-kafka.yml restart producer-stream-generator

# Ver uso de recursos
docker stats

# Limpiar imágenes antiguas
docker system prune -a

# Ver volúmenes
docker volume ls | grep vitalwatch

# Eliminar volúmenes
docker volume rm $(docker volume ls -q | grep kafka)
```

## 📈 Verificación de Rendimiento

```bash
# 1. Iniciar stream
curl -X POST http://localhost:8081/api/v1/stream/start

# 2. Esperar 1 minuto
sleep 60

# 3. Ver estadísticas
echo "=== Stream Generator ===" && \
curl -s http://localhost:8081/api/v1/stream/stats | jq '.data'

echo -e "\n=== Alert Processor ===" && \
curl -s http://localhost:8082/api/v1/processor/stats | jq '.data'

# 4. Verificar LAG
docker exec -it vitalwatch-kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:9092 \
  --all-groups \
  --describe
```

**Resultado esperado:**
- totalMessagesSent: ~60
- alertsGenerated: ~9
- alertRate: ~15%
- LAG: 0 en todas las particiones

## 🔧 Configuración Rápida

### Variables de Entorno

```bash
# Para desarrollo local
export KAFKA_BOOTSTRAP=localhost:19092,localhost:19093,localhost:19094
export KAFKA_TOPIC_VITALS=signos-vitales-stream
export KAFKA_TOPIC_ALERTS=alertas-medicas

# Para Azure Event Hubs
export KAFKA_BOOTSTRAP=my-eventhub.servicebus.windows.net:9093
export KAFKA_SASL_JAAS_CONFIG="..."
```

### Propiedades Kafka Importantes

```properties
# Producer
kafka.bootstrap-servers=localhost:19092,localhost:19093,localhost:19094
acks=all                    # Durabilidad máxima
retries=3                   # Reintentos
compression.type=snappy     # Compresión

# Consumer
group.id=mi-grupo
auto.offset.reset=latest    # Empezar desde ahora
enable.auto.commit=true     # Commit automático
max.poll.records=100        # Mensajes por poll
```

## 🎯 Troubleshooting Rápido

| Problema | Solución |
|----------|----------|
| Cluster no inicia | Verificar Docker running, ports disponibles |
| Tópicos no aparecen | Esperar 10s, recrear con script |
| Stream no genera | Verificar status, iniciar con POST /start |
| No hay alertas | Normal si solo valores normales, esperar más |
| LAG alto | Aumentar concurrencia o número de consumers |
| Error Oracle | Verificar wallet en /app/wallet |
| Consumer no avanza | Ver logs, verificar offset no atascado |

## 📚 Documentos Principales

| Archivo | Para qué sirve |
|---------|----------------|
| `README_KAFKA.md` | Guía completa de uso |
| `docs/ARQUITECTURA_KAFKA.md` | Detalles técnicos profundos |
| `GUIA_PRUEBAS_KAFKA.md` | Cómo probar el sistema |
| `DIALOGO_PRESENTACION_KAFKA.md` | Guión para video |
| `IMPLEMENTACION_KAFKA_COMPLETA.md` | Resumen de lo implementado |
| `KAFKA_QUICK_REFERENCE.md` | Este documento |

## 🆘 Help

```bash
# Ver ayuda de kafka-topics
docker exec -it vitalwatch-kafka1 kafka-topics --help

# Ver ayuda de consumer-groups
docker exec -it vitalwatch-kafka1 kafka-consumer-groups --help

# Ver ayuda de docker-compose
docker-compose --help
```

## 🎓 Conceptos Clave

- **Broker:** Servidor Kafka que almacena mensajes
- **Topic:** Canal lógico de mensajes
- **Partition:** División de un tópico para paralelismo
- **Offset:** Posición de un mensaje en una partición
- **Consumer Group:** Grupo de consumidores que procesan en paralelo
- **Replication Factor (RF):** Número de copias de cada partición
- **LAG:** Mensajes pendientes de procesar en un consumer
- **Zookeeper:** Coordina el cluster Kafka

---

**TIP:** Guarda este archivo como favorito para referencia rápida durante desarrollo y troubleshooting.
