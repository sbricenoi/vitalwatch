# 🏗️ VitalWatch - Arquitectura Apache Kafka

Documentación técnica completa de la arquitectura de streaming con Apache Kafka.

## 📊 Visión General

VitalWatch Kafka es un sistema de streaming en tiempo real para monitoreo de signos vitales hospitalarios, construido sobre Apache Kafka para garantizar:

- **Alta disponibilidad**: Cluster de 3 brokers con replicación
- **Alto throughput**: Hasta 1M mensajes/segundo
- **Procesamiento en tiempo real**: Latencia <100ms
- **Persistencia**: Retención configurable (7-30 días)
- **Escalabilidad horizontal**: Particiones y consumer groups

## 🎯 Componentes del Sistema

### 1. Cluster Kafka

#### Zookeeper Ensemble (3 nodos)
- **Función**: Coordinación del cluster, gestión de configuración
- **Puerto**: 2181-2183
- **Replicación**: Quorum de 3 nodos
- **Configuración**:
  - Tick time: 2000ms
  - Init limit: 5
  - Sync limit: 2

#### Kafka Brokers (3 nodos)
- **Función**: Almacenamiento y distribución de mensajes
- **Puertos internos**: 9092 (cada broker)
- **Puertos externos**: 19092, 19093, 19094
- **Configuración**:
  - Offsets topic replication: 2
  - Transaction log replication: 2
  - Auto create topics: enabled
  - Log retention: 168 horas

#### Kafka UI
- **Función**: Interfaz web para monitoreo
- **Puerto**: 8080
- **Features**:
  - Visualización de tópicos y mensajes
  - Monitoreo de consumer groups
  - Estadísticas de brokers
  - Configuración de tópicos

### 2. Tópicos Kafka

#### signos-vitales-stream
```yaml
Nombre: signos-vitales-stream
Particiones: 3
Replication Factor: 2
Retention: 7 días (604800000 ms)
Compression: snappy
Throughput: 1 mensaje/segundo
Tamaño mensaje: ~500 bytes
Volumen diario: ~86,400 mensajes (~43 MB)
```

**Esquema del mensaje:**
```json
{
  "messageId": "uuid",
  "pacienteId": "P001",
  "pacienteNombre": "Juan Pérez",
  "sala": "UCI-A",
  "cama": "101",
  "frecuenciaCardiaca": 75,
  "presionSistolica": 120,
  "presionDiastolica": 80,
  "temperatura": 36.6,
  "saturacionOxigeno": 98,
  "frecuenciaRespiratoria": 16,
  "deviceId": "DEVICE-001",
  "timestamp": "2026-02-13T10:30:45.123",
  "source": "STREAM_GENERATOR"
}
```

#### alertas-medicas
```yaml
Nombre: alertas-medicas
Particiones: 3
Replication Factor: 2
Retention: 30 días (2592000000 ms)
Compression: snappy
Throughput: ~0.15 mensajes/segundo (15% anomalías)
Tamaño mensaje: ~1-2 KB
Volumen diario: ~13,000 mensajes (~20 MB)
```

**Esquema del mensaje:**
```json
{
  "alertId": "ALERT-uuid",
  "pacienteId": "P001",
  "pacienteNombre": "Juan Pérez",
  "sala": "UCI-A",
  "cama": "101",
  "tipoAlerta": "SIGNOS_VITALES_ANORMALES",
  "mensaje": "ALERTA MÉDICA: Se detectaron 2 anomalías...",
  "severidad": "CRITICA",
  "frecuenciaCardiaca": 145,
  "presionSistolica": 165,
  "presionDiastolica": 95,
  "temperatura": 38.9,
  "saturacionOxigeno": 88,
  "frecuenciaRespiratoria": 26,
  "anomalias": [
    {
      "tipo": "CRITICA",
      "parametro": "Frecuencia Cardíaca",
      "valorActual": "145 lpm",
      "rangoNormal": "60-100 lpm"
    },
    {
      "tipo": "CRITICA",
      "parametro": "Saturación O2",
      "valorActual": "88 %",
      "rangoNormal": "95-100 %"
    }
  ],
  "cantidadAnomalias": 2,
  "deviceId": "DEVICE-001",
  "detectedAt": "2026-02-13T10:30:46.234",
  "source": "ALERT_PROCESSOR"
}
```

### 3. Microservicios

#### Producer 1: Stream Generator
```yaml
Nombre: producer-stream-generator
Tecnología: Spring Boot 3.2.1 + Spring Kafka
Puerto: 8081
Función: Generar stream continuo de signos vitales

Características:
- Scheduler con fixedRate=1000ms (1 mensaje/segundo)
- Generación aleatoria de signos vitales
- 15% probabilidad de generar anomalías
- 5 pacientes simulados
- API REST para control (start, stop, stats)
- KafkaTemplate asíncrono
- Compresión snappy
- Acks=all para durabilidad

Endpoints:
- POST /api/v1/stream/start
- POST /api/v1/stream/stop
- GET /api/v1/stream/status
- POST /api/v1/stream/send-manual
- GET /api/v1/stream/stats
- GET /api/v1/stream/health

Configuración Kafka:
- Bootstrap servers: kafka1:9092,kafka2:9092,kafka3:9092
- Tópico: signos-vitales-stream
- Key: pacienteId (para distribución en particiones)
- Serializer: JsonSerializer
- Acks: all
- Retries: 3
```

#### Producer 2: Alert Processor
```yaml
Nombre: producer-alert-processor
Tecnología: Spring Boot 3.2.1 + Spring Kafka
Puerto: 8082
Función: Consumir stream, detectar anomalías, publicar alertas

Características:
- Consumer del tópico signos-vitales-stream
- Producer al tópico alertas-medicas
- Detección de anomalías en 6 parámetros vitales
- Cálculo de severidad (BAJA, MODERADA, ALTA, CRITICA)
- Procesamiento concurrente (2 listeners)
- API REST para estadísticas

Consumer:
- Group ID: alert-processor-group
- Concurrency: 2
- Auto offset reset: latest
- Max poll records: 10

Producer:
- Tópico: alertas-medicas
- Key: pacienteId
- Acks: all

Rangos clínicos:
- FC: Normal 60-100, Crítico <40 o >120
- PA Sistólica: Normal 90-120, Crítico <70 o >160
- Temperatura: Normal 36-37.5, Crítico <35 o >39.5
- SpO2: Normal 95-100, Crítico <90
```

#### Consumer 1: Database Saver
```yaml
Nombre: consumer-database-saver
Tecnología: Spring Boot 3.2.1 + JPA + Oracle JDBC
Puerto: Interno
Función: Persistir signos vitales y alertas en Oracle Cloud

Características:
- 2 consumers independientes (vital signs + alerts)
- Procesamiento concurrente (3 listeners vital signs, 2 alerts)
- Spring Data JPA + Hibernate
- Conexión TLS a Oracle Cloud
- Oracle Wallet para autenticación
- HikariCP connection pool
- Idempotencia (evita duplicados)

Consumers:
1. Vital Signs Consumer
   - Group ID: db-saver-vital-signs-group
   - Concurrency: 3
   - Max poll records: 100
   
2. Alerts Consumer
   - Group ID: db-saver-alerts-group
   - Concurrency: 2
   - Verificación de duplicados por alertId

Base de datos:
- Oracle Autonomous Database
- Connection pool: 10 max, 5 min idle
- Tablas: SIGNOS_VITALES_KAFKA, ALERTAS_KAFKA
```

#### Consumer 2: Summary Generator
```yaml
Nombre: consumer-summary-generator
Tecnología: Spring Boot 3.2.1 + JPA + Spring Scheduler
Puerto: 8083
Función: Generar resúmenes diarios agregados

Características:
- Consultas SQL agregadas sobre Oracle
- Scheduler CRON para resúmenes automáticos
- API REST para generación bajo demanda
- Cálculo de estadísticas completas

Tareas programadas:
1. Resumen diario (medianoche)
   - CRON: 0 0 0 * * ?
   - Genera resumen del día anterior
   
2. Actualización continua (cada 15 min)
   - CRON: 0 */15 * * * ?
   - Actualiza resumen del día actual

Endpoints:
- POST /api/v1/summary/generate
- GET /api/v1/summary/daily/{fecha}
- GET /api/v1/summary/all
- GET /api/v1/summary/health

Estadísticas:
- Total pacientes monitoreados
- Total mediciones y alertas
- Promedios de signos vitales
- Valores máximos y mínimos
- Alertas por severidad
```

### 4. Base de Datos Oracle Cloud

#### SIGNOS_VITALES_KAFKA
```sql
Columnas principales:
- id (PK, auto-increment)
- kafka_topic, kafka_partition, kafka_offset (trazabilidad)
- paciente_id, paciente_nombre, sala, cama
- frecuencia_cardiaca, presion_sistolica, presion_diastolica
- temperatura, saturacion_oxigeno, frecuencia_respiratoria
- device_id, timestamp_medicion
- Constraint: UNIQUE(kafka_topic, kafka_partition, kafka_offset)

Índices:
- idx_sv_kafka_paciente (paciente_id, timestamp DESC)
- idx_sv_kafka_timestamp (timestamp DESC)
- idx_sv_kafka_sala (sala, timestamp DESC)
- idx_sv_kafka_offset (partition, offset)
```

#### ALERTAS_KAFKA
```sql
Columnas principales:
- id (PK)
- alert_id (UNIQUE)
- kafka_topic, kafka_partition, kafka_offset
- paciente_id, paciente_nombre, sala, cama
- tipo_alerta, mensaje, severidad
- signos vitales completos
- anomalias (JSON)
- cantidad_anomalias
- estado (ACTIVA, EN_REVISION, RESUELTA, DESCARTADA)
- detected_at, created_at, updated_at

Índices:
- idx_alertas_kafka_paciente
- idx_alertas_kafka_severidad
- idx_alertas_kafka_estado
- idx_alertas_kafka_alert_id
```

#### RESUMEN_DIARIO_KAFKA
```sql
Columnas principales:
- id (PK)
- fecha (UNIQUE)
- total_pacientes_monitoreados
- total_mediciones, mediciones_por_hora
- total_alertas por severidad
- promedios de todos los signos vitales
- valores máximos y mínimos
- mensajes_procesados por tópico
- timestamps de creación y actualización
```

#### PACIENTES_MONITOREADOS_KAFKA
```sql
Columnas principales:
- id (PK)
- paciente_id (UNIQUE)
- paciente_nombre, sala, cama
- device_id
- estado (ACTIVO, INACTIVO, DADO_DE_ALTA)
- total_mediciones, ultima_medicion
- total_alertas, ultima_alerta
- fecha_ingreso, fecha_alta

Triggers:
- Auto-actualización al recibir mediciones
- Auto-actualización al recibir alertas
```

## 🔄 Flujo de Procesamiento Detallado

### Fase 1: Generación de Stream
```
1. Spring Scheduler (@Scheduled fixedRate=1000)
   ↓
2. VitalSignsGeneratorService.generateRandomVitalSigns()
   - Selecciona paciente aleatorio
   - Genera valores con 85% normales / 15% anormales
   ↓
3. VitalSignsStreamProducer.publishVitalSigns()
   - Key: pacienteId
   - Value: VitalSignsMessage (JSON)
   - Topic: signos-vitales-stream
   ↓
4. Kafka Broker recibe y distribuye por particiones
   - Hash(pacienteId) → Partición
   - Replica en 2 brokers (RF=2)
```

### Fase 2: Detección de Alertas
```
1. VitalSignsStreamConsumer (@KafkaListener)
   - Consume de signos-vitales-stream
   - Group: alert-processor-group
   - Concurrency: 2
   ↓
2. AnomalyDetectionService.detectAnomalies()
   - Verifica 6 parámetros vitales
   - Compara con rangos clínicos
   - Construye lista de anomalías
   ↓
3. Si hay anomalías:
   - Calcula severidad
   - Construye mensaje de alerta
   ↓
4. AlertProducer.publishAlert()
   - Key: pacienteId
   - Value: AlertMessage (JSON)
   - Topic: alertas-medicas
```

### Fase 3: Persistencia en Oracle
```
1. Dos consumidores independientes:

   A. VitalSignsKafkaConsumer
      - Group: db-saver-vital-signs-group
      - Topic: signos-vitales-stream
      - Concurrency: 3
      ↓
      - Deserializa JSON a Map
      - Mapea a entidad SignosVitalesKafka
      - repository.save() → Oracle
      - Trigger actualiza PACIENTES_MONITOREADOS_KAFKA
   
   B. AlertsKafkaConsumer
      - Group: db-saver-alerts-group
      - Topic: alertas-medicas
      - Concurrency: 2
      ↓
      - Deserializa JSON
      - Verifica duplicado por alertId
      - Mapea a entidad AlertaKafka
      - Serializa anomalías a JSON
      - repository.save() → Oracle
```

### Fase 4: Generación de Resúmenes
```
1. DailySummaryScheduler
   
   A. Resumen diario (medianoche)
      - CRON: 0 0 0 * * ?
      - Fecha: día anterior
      ↓
   
   B. Actualización continua (cada 15 min)
      - CRON: 0 */15 * * * ?
      - Fecha: día actual
      ↓
      
2. SummaryService.generateDailySummary()
   - Query SQL agregada sobre SIGNOS_VITALES_KAFKA
   - Query SQL agregada sobre ALERTAS_KAFKA
   - Construye ResumenDiarioKafka
   - Upsert en tabla RESUMEN_DIARIO_KAFKA
```

## 📈 Distribución de Datos

### Particionamiento
```
Key: pacienteId
Hash Function: murmur2

Ejemplo con 5 pacientes y 3 particiones:
- P001 → hash → Partición 0
- P002 → hash → Partición 1
- P003 → hash → Partición 2
- P004 → hash → Partición 0
- P005 → hash → Partición 1

Beneficios:
✅ Mensajes del mismo paciente van a la misma partición
✅ Orden garantizado por paciente
✅ Procesamiento paralelo entre pacientes
```

### Replicación
```
Replication Factor = 2

Topic: signos-vitales-stream
- Partición 0: kafka1 (leader), kafka2 (follower)
- Partición 1: kafka2 (leader), kafka3 (follower)
- Partición 2: kafka3 (leader), kafka1 (follower)

Ventajas:
✅ Alta disponibilidad (1 broker puede fallar)
✅ Sin pérdida de datos
✅ Failover automático
```

### Consumer Groups
```
1. alert-processor-group
   - 1 instancia del Alert Processor
   - Consume de 3 particiones
   - Concurrency: 2 threads

2. db-saver-vital-signs-group
   - 1 instancia del Database Saver
   - Consume de 3 particiones
   - Concurrency: 3 threads

3. db-saver-alerts-group
   - 1 instancia del Database Saver
   - Consume de 3 particiones
   - Concurrency: 2 threads

4. summary-generator-group
   - 1 instancia del Summary Generator
   - No consume mensajes (solo queries SQL)

Beneficios:
✅ Procesamiento independiente por grupo
✅ Cada grupo mantiene su propio offset
✅ Posibilidad de reprocessar datos
```

## 🔐 Seguridad

### Oracle Cloud Connection
```yaml
Protocolo: TLS (tcps://)
Puerto: 1521
Autenticación: Oracle Wallet
  - cwallet.sso
  - ewallet.p12
  - tnsnames.ora
  - sqlnet.ora
Trust Store: /app/wallet/truststore.jks
```

### Kafka Security
```yaml
Protocolo: PLAINTEXT (desarrollo)
Producción recomendada:
  - SSL/TLS encryption
  - SASL authentication
  - ACLs por tópico
  - Zookeeper authentication
```

## 📊 Monitoreo y Observabilidad

### Métricas de Kafka
```
Broker Level:
- Messages in per second
- Bytes in per second
- Under-replicated partitions
- Offline partitions

Topic Level:
- Messages per second
- Bytes per second
- Partition count
- Replication status

Consumer Level:
- Lag (mensajes pendientes)
- Commit rate
- Fetch rate
```

### Métricas de Microservicios
```
Stream Generator:
- totalMessagesSent
- messagesPerMinute
- streamEnabled

Alert Processor:
- messagesProcessed
- alertsGenerated
- alertRate

Database Saver:
- messagesSaved (vital signs)
- alertsSaved
- database connection pool

Acceso via:
- Spring Actuator: /actuator/metrics
- Logs estructurados
- Application-specific endpoints
```

## 🚀 Despliegue

### Local (Docker Compose)
```bash
# 1. Iniciar cluster
./start-kafka-cluster.sh

# 2. Crear tópicos
./create-kafka-topics.sh

# 3. Iniciar microservicios
docker-compose -f docker-compose-kafka.yml up -d

# 4. Verificar
docker-compose -f docker-compose-kafka.yml ps
```

### Azure (Container Apps + Event Hubs)
```bash
# 1. Crear Event Hubs namespace (compatible con Kafka)
az eventhubs namespace create \
  --name vitalwatch-kafka \
  --resource-group rg-vitalwatch-kafka \
  --location southcentralus \
  --sku Standard

# 2. Crear event hubs (equivalente a tópicos)
az eventhubs eventhub create \
  --name signos-vitales-stream \
  --namespace-name vitalwatch-kafka \
  --partition-count 3

# 3. Desplegar microservicios
./deploy-kafka-azure.sh
```

## 🔄 Kafka vs RabbitMQ - Comparación Técnica

| Aspecto | RabbitMQ | Apache Kafka |
|---------|----------|--------------|
| **Arquitectura** | Message Broker (pull/push) | Distributed Log (pull) |
| **Modelo** | Queues + Exchanges | Topics + Partitions |
| **Persistencia** | Opcional, hasta consumo | Persistente, configurable |
| **Orden** | Por cola | Por partición |
| **Reprocessamiento** | No (destrucción mensaje) | Sí (offset management) |
| **Throughput** | 20K-50K msg/s | 100K-1M msg/s |
| **Latencia** | 1-5ms | 10-100ms |
| **Escalabilidad** | Vertical (cluster limitado) | Horizontal (particiones) |
| **Retención** | No (mensaje se elimina) | Sí (días/semanas) |
| **Consumer Groups** | No | Sí (múltiples consumidores) |
| **Replicación** | Mirroring | Nativa (RF) |
| **Casos de uso** | Task queues, RPC | Event sourcing, streaming |

### ¿Cuándo usar Kafka?
✅ Alto volumen de datos (>10K msg/s)  
✅ Necesidad de reprocessar datos  
✅ Event sourcing o audit log  
✅ Streaming analytics  
✅ Múltiples consumidores del mismo stream  
✅ Retención de datos históricos  

### ¿Cuándo usar RabbitMQ?
✅ Baja latencia crítica (<1ms)  
✅ Routing complejo (topic exchanges)  
✅ Task queues tradicionales  
✅ Volumen moderado (<10K msg/s)  
✅ Prioridad de mensajes  
✅ RPC patterns  

## 📚 Conceptos Avanzados

### Offset Management
```
Cada consumidor mantiene su offset (posición de lectura):

Partición 0: [msg0, msg1, msg2, msg3, msg4, msg5, ...]
                                    ↑
                              offset = 3

Ventajas:
- Reprocessamiento desde cualquier offset
- Múltiples consumer groups con offsets independientes
- Replay de eventos históricos
```

### Exactly-Once Semantics
```
Para garantizar procesamiento exactly-once:

Producer:
- enable.idempotence=true
- acks=all
- retries>0

Consumer:
- Procesamiento idempotente
- Transacciones en base de datos
- Verificación de duplicados

En VitalWatch:
✅ Alertas: Verificación por alertId
✅ Signos vitales: Constraint UNIQUE en Kafka metadata
```

### Compresión
```
Tipo: snappy

Ventajas:
✅ Balance entre ratio y velocidad
✅ Reduce bandwidth 2-4x
✅ Reduce storage en disco

Alternativas:
- gzip: Mayor compresión, más CPU
- lz4: Más rápido, menor compresión
- zstd: Mejor balance (Kafka 2.1+)
```

## 🎓 Mejores Prácticas Implementadas

### Producers
✅ Acks=all para durabilidad  
✅ Retries configurados  
✅ Key para distribución  
✅ Compresión habilitada  
✅ Batch y linger para throughput  

### Consumers
✅ Consumer groups independientes  
✅ Concurrencia adecuada  
✅ Error handling robusto  
✅ Offset management automático  
✅ Idempotencia en procesamiento  

### Topics
✅ Particiones múltiples (paralelismo)  
✅ Replication factor ≥ 2  
✅ Retención apropiada al caso de uso  
✅ Compresión configurada  

### Operaciones
✅ Health checks en todos los servicios  
✅ Logging estructurado  
✅ Métricas expuestas  
✅ Scripts de automatización  
✅ Documentación completa  

## 📖 Referencias

- [Apache Kafka Docs](https://kafka.apache.org/documentation/)
- [Spring Kafka](https://spring.io/projects/spring-kafka)
- [Confluent Best Practices](https://docs.confluent.io/platform/current/installation/docker/config-reference.html)
- [Azure Event Hubs (Kafka)](https://docs.microsoft.com/azure/event-hubs/event-hubs-for-kafka-ecosystem-overview)
