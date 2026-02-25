# 🎯 VitalWatch - Integración Apache Kafka

Sistema de streaming en tiempo real para monitoreo de signos vitales usando Apache Kafka.

## 🏗️ Arquitectura General

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      VITALWATCH KAFKA ARCHITECTURE                       │
└─────────────────────────────────────────────────────────────────────────┘

                         ┌──────────────────────┐
                         │   KAFKA CLUSTER      │
                         │  ┌────────────────┐  │
                         │  │ Zookeeper (3x) │  │
                         │  │  Coordinación  │  │
                         │  └────────────────┘  │
                         │  ┌────────────────┐  │
                         │  │ Kafka (3x)     │  │
                         │  │  Brokers       │  │
                         │  └────────────────┘  │
                         │  ┌────────────────┐  │
                         │  │ Kafka UI       │  │
                         │  │  :8080         │  │
                         │  └────────────────┘  │
                         └──────────────────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │              │              │
            ┌───────▼──────┐       │       ┌─────▼────────┐
            │   TOPIC 1    │       │       │   TOPIC 2    │
            │ signos-      │       │       │ alertas-     │
            │ vitales-     │       │       │ medicas      │
            │ stream       │       │       │              │
            │ (3 part, 2RF)│       │       │ (3 part, 2RF)│
            └──────┬───────┘       │       └───────┬──────┘
                   │               │               │
         ┌─────────┼───────────────┼───────────────┼──────────┐
         │         │               │               │          │
    ┌────▼─────────▼────┐   ┌─────▼──────┐   ┌───▼──────────▼───┐
    │   PRODUCER 1      │   │ PRODUCER 2 │   │   CONSUMER 1     │
    │ Stream Generator  │   │Alert Proc. │   │ Database Saver   │
    │                   │   │            │   │                  │
    │ - Genera signos   │   │- Consume   │   │- Guarda signos   │
    │   cada 1 segundo  │   │  stream    │   │  vitales en BD   │
    │ - CRON scheduler  │   │- Detecta   │   │- Guarda alertas  │
    │ - 5 pacientes     │   │  anomalías │   │  en BD           │
    │ - Produce a       │   │- Produce   │   │- Oracle Cloud    │
    │   signos-vitales- │   │  alertas   │   │- JPA/Hibernate   │
    │   stream          │   │            │   │                  │
    │                   │   │            │   │                  │
    │ Port: 8081        │   │Port: 8082  │   │Port: interno     │
    └───────────────────┘   └────────────┘   └──────────────────┘
                                                      │
                                              ┌───────▼──────────┐
                                              │   CONSUMER 2     │
                                              │ Summary Generator│
                                              │                  │
                                              │- Genera resúmenes│
                                              │  diarios         │
                                              │- CRON medianoche │
                                              │- Estadísticas    │
                                              │- Oracle Cloud    │
                                              │                  │
                                              │ Port: 8083       │
                                              └──────────────────┘
                                                      │
                                              ┌───────▼──────────┐
                                              │  ORACLE CLOUD    │
                                              │  Autonomous DB   │
                                              │                  │
                                              │- SIGNOS_VITALES_ │
                                              │  KAFKA           │
                                              │- ALERTAS_KAFKA   │
                                              │- RESUMEN_DIARIO_ │
                                              │  KAFKA           │
                                              │- PACIENTES_      │
                                              │  MONITOREADOS_   │
                                              │  KAFKA           │
                                              └──────────────────┘
```

## 📦 Componentes del Sistema

### Infraestructura Kafka

| Componente | Cantidad | Puerto | Función |
|------------|----------|--------|---------|
| Zookeeper | 3 | 2181-2183 | Coordinación del cluster |
| Kafka Broker | 3 | 19092-19094 | Mensajería distribuida |
| Kafka UI | 1 | 8080 | Interfaz web de monitoreo |

### Microservicios

| Servicio | Tipo | Puerto | Función |
|----------|------|--------|---------|
| Stream Generator | Producer | 8081 | Genera signos vitales cada 1s |
| Alert Processor | Producer/Consumer | 8082 | Detecta anomalías y genera alertas |
| Database Saver | Consumer | - | Guarda en Oracle Cloud |
| Summary Generator | Consumer | 8083 | Genera resúmenes diarios |

### Tópicos Kafka

| Tópico | Particiones | Replicación | Retención | Uso |
|--------|-------------|-------------|-----------|-----|
| signos-vitales-stream | 3 | 2 | 7 días | Stream continuo de mediciones |
| alertas-medicas | 3 | 2 | 30 días | Alertas detectadas |

## 🚀 Guía de Inicio Rápido

### 1. Crear tablas en Oracle Cloud

```bash
# Conectarse a Oracle SQL Developer o SQLcl
sqlplus ADMIN@s58onuxcx4c1qxe9_high

# Ejecutar el script
@database/create_tables_kafka.sql
```

### 2. Iniciar cluster Kafka

```bash
chmod +x start-kafka-cluster.sh
./start-kafka-cluster.sh
```

Esto levantará:
- 3 Zookeepers
- 3 Kafka Brokers
- Kafka UI en http://localhost:8080

### 3. Crear tópicos

```bash
chmod +x create-kafka-topics.sh
./create-kafka-topics.sh
```

Esto creará:
- `signos-vitales-stream` (3 particiones, RF=2)
- `alertas-medicas` (3 particiones, RF=2)

### 4. Iniciar microservicios

```bash
docker-compose -f docker-compose-kafka.yml up -d
```

### 5. Verificar funcionamiento

```bash
# Ver logs del stream generator
docker logs -f vitalwatch-producer-stream

# Ver logs del alert processor
docker logs -f vitalwatch-producer-alert

# Ver todos los servicios
docker-compose -f docker-compose-kafka.yml ps
```

### 6. Acceder a Kafka UI

Abre tu navegador en: http://localhost:8080

Podrás ver:
- Estado del cluster
- Mensajes en los tópicos
- Consumer groups
- Brokers y particiones

## 🔄 Flujo de Datos

1. **Stream Generator** genera signos vitales cada 1 segundo
2. Los mensajes se publican al tópico `signos-vitales-stream`
3. **Alert Processor** consume el stream y detecta anomalías
4. Las alertas se publican al tópico `alertas-medicas`
5. **Database Saver** consume ambos tópicos y guarda en Oracle
6. **Summary Generator** genera resúmenes diarios a medianoche

## 📊 Rendimiento Esperado

| Métrica | Valor |
|---------|-------|
| Mensajes por segundo | 1 |
| Mensajes por minuto | 60 |
| Mensajes por hora | 3,600 |
| Mensajes por día | 86,400 |
| Alertas por día (15%) | ~13,000 |
| Throughput total | ~99,400 mensajes/día |

## 🧪 Pruebas con POSTMAN

### 1. Iniciar el stream
```
POST http://localhost:8081/api/v1/stream/start
```

### 2. Ver estadísticas del stream
```
GET http://localhost:8081/api/v1/stream/stats
```

### 3. Ver estadísticas del procesador de alertas
```
GET http://localhost:8082/api/v1/processor/stats
```

### 4. Generar resumen diario
```
POST http://localhost:8083/api/v1/summary/generate
```

### 5. Ver resumen del día actual
```
GET http://localhost:8083/api/v1/summary/daily/2026-02-13
```

## 🔍 Monitoreo

### Ver mensajes en Kafka UI
1. Abrir http://localhost:8080
2. Navegar a "Topics"
3. Seleccionar tópico
4. Ver mensajes en tiempo real

### Consultar datos en Oracle

```sql
-- Últimas 10 mediciones
SELECT * FROM SIGNOS_VITALES_KAFKA 
ORDER BY timestamp_medicion DESC 
FETCH FIRST 10 ROWS ONLY;

-- Alertas críticas del día
SELECT * FROM ALERTAS_KAFKA 
WHERE TRUNC(detected_at) = TRUNC(SYSDATE) 
AND severidad = 'CRITICA'
ORDER BY detected_at DESC;

-- Resumen del día actual
SELECT * FROM RESUMEN_DIARIO_KAFKA 
WHERE fecha = TRUNC(SYSDATE);
```

### Ver logs de microservicios

```bash
# Stream Generator
docker logs -f vitalwatch-producer-stream

# Alert Processor
docker logs -f vitalwatch-producer-alert

# Database Saver
docker logs -f vitalwatch-consumer-db-kafka

# Summary Generator
docker logs -f vitalwatch-consumer-summary-kafka
```

## 🛑 Detener el Sistema

```bash
# Detener todos los servicios
docker-compose -f docker-compose-kafka.yml down

# Detener y eliminar volúmenes
docker-compose -f docker-compose-kafka.yml down -v
```

## 📁 Estructura del Proyecto

```
├── docker-compose-kafka.yml          # Configuración del cluster
├── start-kafka-cluster.sh            # Script de inicio
├── create-kafka-topics.sh            # Script para crear tópicos
├── database/
│   └── create_tables_kafka.sql       # Tablas de Oracle
├── producer-stream-generator/        # Productor 1
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
├── producer-alert-processor/         # Productor 2
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
├── consumer-database-saver/          # Consumidor 1
│   ├── src/
│   ├── wallet/                       # Oracle Wallet
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
└── consumer-summary-generator/       # Consumidor 2
    ├── src/
    ├── wallet/                       # Oracle Wallet
    ├── pom.xml
    ├── Dockerfile
    └── README.md
```

## 🔐 Seguridad

- Conexión TLS a Oracle Autonomous Database
- Oracle Wallet montado como volumen read-only
- Consumer groups independientes
- Idempotencia en procesamiento de mensajes

## 📈 Escalabilidad

- Cluster Kafka con 3 brokers (alta disponibilidad)
- Tópicos con múltiples particiones (paralelismo)
- Consumer groups con múltiples instancias
- Replicación de datos (RF=2)

## 🆚 Kafka vs RabbitMQ

| Característica | RabbitMQ | Kafka |
|----------------|----------|-------|
| Patrón | Message Queue | Event Stream |
| Throughput | 20K msg/s | 1M msg/s |
| Retención | Hasta consumo | Configurable (días) |
| Orden garantizado | Por cola | Por partición |
| Reprocessamiento | No | Sí (offset) |
| Escalabilidad | Vertical | Horizontal |
| Latencia | Baja (~ms) | Media (~10ms) |

## 🎓 Conceptos Clave

### Broker
Servidor Kafka que almacena y distribuye mensajes.

### Tópico
Canal lógico donde se publican mensajes.

### Partición
División de un tópico para paralelismo.

### Replication Factor (RF)
Número de copias de cada partición.

### Consumer Group
Grupo de consumidores que procesan mensajes en paralelo.

### Offset
Posición de un mensaje en una partición.

## 📚 Referencias

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring for Apache Kafka](https://spring.io/projects/spring-kafka)
- [Confluent Platform](https://docs.confluent.io/)
