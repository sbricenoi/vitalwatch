# 🏗️ Arquitectura VitalWatch

## 📊 Visión General

Sistema de monitoreo hospitalario en tiempo real con dos implementaciones de mensajería:
1. **RabbitMQ** - Sistema principal (Semanas 1-7)
2. **Apache Kafka** - Sistema de streaming (Semana 8)

---

## 🔄 Arquitectura RabbitMQ

### Componentes

```
┌─────────────┐     ┌─────────────┐     ┌──────────────┐
│  Frontend   │────▶│ API Gateway │────▶│   Backend    │
│  Angular 17 │     │   Kong      │     │ Spring Boot  │
└─────────────┘     └─────────────┘     └──────┬───────┘
                                               │
                                               ▼
                            ┌──────────────────────────┐
                            │       RabbitMQ           │
                            │  ┌────────┐ ┌─────────┐ │
                            │  │ Queue  │ │ Queue   │ │
                            │  │ Signos │ │ Alertas │ │
                            │  └────────┘ └─────────┘ │
                            └──────────────────────────┘
                                   │           │
                         ┌─────────┴───────┬───┴─────────┐
                         ▼                 ▼             ▼
                  ┌──────────┐      ┌──────────┐ ┌──────────┐
                  │ Anomaly  │      │ Summary  │ │    DB    │
                  │ Detector │      │ Generator│ │  Saver   │
                  └──────────┘      └──────────┘ └─────┬────┘
                                                        │
                                                        ▼
                                            ┌────────────────────┐
                                            │ Oracle Cloud DB    │
                                            └────────────────────┘
```

### Microservicios

| Servicio | Puerto | Función |
|----------|--------|---------|
| Frontend | 80/443 | Interfaz web Angular |
| API Gateway | 8000 | Kong - Routing y auth |
| Backend | 8080 | API REST principal |
| Producer Anomaly | 8081 | Detecta anomalías |
| Producer Summary | 8082 | Genera resúmenes |
| Consumer DB | - | Persiste en Oracle |
| Consumer JSON | - | Genera archivos |
| RabbitMQ | 5672/15672 | Message broker |

### Queues

- **signos_vitales** - Signos vitales capturados
- **anomalias** - Anomalías detectadas
- **resumen_diario** - Resúmenes generados

---

## 📨 Arquitectura Kafka

### Componentes

```
┌─────────────────┐
│Stream Generator │ Produce 1 msg/s
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│          KAFKA CLUSTER                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐│
│  │ Broker 1 │ │ Broker 2 │ │ Broker 3 ││
│  └──────────┘ └──────────┘ └──────────┘│
│  ┌─────────────────────────────────────┐│
│  │ Topics:                             ││
│  │ - signos-vitales-stream (3 parts)  ││
│  │ - alertas-medicas (3 parts)        ││
│  └─────────────────────────────────────┘│
└─────────────────────────────────────────┘
         │ Consume
         ▼
┌─────────────────┐
│Alert Processor  │ Detecta y publica alertas
└────────┬────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌─────────┐ ┌──────────────┐
│DB Saver │ │Summary Gen   │
└────┬────┘ └──────┬───────┘
     │             │
     └──────┬──────┘
            ▼
    ┌───────────────┐
    │ Oracle Cloud  │
    └───────────────┘
```

### Kafka Cluster

- **3 Zookeepers** (puertos 2181-2183) - Coordinación
- **3 Kafka Brokers** (puertos 19092-19094) - Storage
- **Kafka UI** (puerto 9000) - Monitoreo

### Topics

#### signos-vitales-stream
- Particiones: 3
- Replicación: 2
- Retención: 7 días
- Throughput: 1 msg/s

#### alertas-medicas
- Particiones: 3
- Replicación: 2
- Retención: 30 días
- Throughput: 0.15 msg/s

### Microservicios Kafka

| Servicio | Puerto | Función |
|----------|--------|---------|
| Stream Generator | 8091 | Genera signos vitales |
| Alert Processor | 8092 | Detecta anomalías |
| Database Saver | 8093 | Persiste en Oracle (2 consumers) |
| Summary Generator | 8094 | Resúmenes con scheduler |

---

## 🗄️ Base de Datos Oracle

### Esquema RabbitMQ

```sql
SIGNOS_VITALES (
  id, paciente_id, frecuencia_cardiaca, temperatura,
  saturacion_oxigeno, timestamp
)

ANOMALIAS (
  id, paciente_id, tipo_anomalia, severidad,
  valor_detectado, timestamp
)

PACIENTES (
  id, nombre, edad, sala, cama
)

RESUMEN_DIARIO (
  id, fecha, total_mediciones, total_anomalias
)
```

### Esquema Kafka

```sql
SIGNOS_VITALES_KAFKA (
  id, kafka_topic, kafka_partition, kafka_offset,
  kafka_timestamp, paciente_id, ...signos vitales
)

ALERTAS_KAFKA (
  id, alert_id, kafka_topic, kafka_partition,
  severidad, anomalias_detectadas, timestamp
)

RESUMEN_DIARIO_KAFKA (
  id, fecha, total_mensajes, total_alertas,
  promedio_fc, promedio_temp, ...
)

PACIENTES_MONITOREADOS_KAFKA (
  id, paciente_id, total_mediciones,
  ultima_medicion, estado
)
```

---

## 🔐 Seguridad

### Oracle Cloud
- **Conexión:** TCPS (SSL/TLS)
- **Wallet:** Certificados en carpeta `wallet/`
- **Credenciales:** Variables de entorno

### API Gateway (Kong)
- **JWT:** Tokens para autenticación
- **Rate Limiting:** Límite de requests
- **CORS:** Configurado para frontend

### Docker
- **Networks:** Aislamiento por red
- **Secrets:** No en código
- **Volumes:** Persistencia segura

---

## 📊 Escalabilidad

### RabbitMQ
- **Vertical:** Más recursos al broker
- **Queues:** Múltiples consumers por queue
- **Clustering:** Posible con múltiples nodos

### Kafka
- **Horizontal:** Agregar más brokers
- **Particiones:** Paralelismo automático
- **Consumer Groups:** Múltiples instancias

---

## 🔄 Flujo de Datos

### RabbitMQ Flow

```
1. Frontend captura signos vitales
2. Backend valida y publica a RabbitMQ
3. Producers consumen y procesan
   - Anomaly: Detecta anomalías → Nueva queue
   - Summary: Genera resúmenes → Nueva queue
4. Consumers persisten en Oracle
5. Frontend consulta via Backend/API
```

### Kafka Flow

```
1. Stream Generator produce cada 1s
   → Tópico: signos-vitales-stream
   
2. Alert Processor consume
   → Detecta anomalías
   → Produce: alertas-medicas
   
3. Database Saver consume ambos tópicos
   → Persiste en Oracle con metadata Kafka
   
4. Summary Generator
   → Scheduler cada 15 min
   → Agrega datos de Oracle
   → Persiste resúmenes
```

---

## 💾 Persistencia

### Datos Volátiles (Kafka)
- Mensajes en tópicos: 7-30 días
- Offsets de consumers: 7 días
- Logs de brokers: 7 días

### Datos Permanentes (Oracle)
- Signos vitales: Indefinido
- Alertas: Indefinido  
- Resúmenes: Indefinido
- Auditoría: Indefinido

---

## 🔍 Monitoreo

### RabbitMQ
- **Management UI:** http://localhost:15672
- **Métricas:** Queues, consumers, messages
- **Alertas:** Configurables via plugins

### Kafka
- **Kafka UI:** http://localhost:9000
- **Métricas:** Topics, partitions, lag, throughput
- **JMX:** Expuesto para Prometheus/Grafana

### Aplicaciones
- **Spring Actuator:** `/actuator/health`, `/actuator/metrics`
- **Logs:** Agregados por Docker

---

## 📈 Performance

### RabbitMQ
- **Throughput:** ~50K msg/s
- **Latencia:** <10ms
- **Conexiones:** Hasta 10K simultáneas

### Kafka
- **Throughput:** ~1M msg/s (cluster)
- **Latencia:** <100ms (99th percentile)
- **Storage:** Ilimitado (disco)
- **Retención:** Configurable

---

## 🎯 Comparación Tecnologías

| Aspecto | RabbitMQ | Kafka |
|---------|----------|-------|
| **Modelo** | Message Queue | Event Stream |
| **Persistencia** | Temporal | Duradera (días) |
| **Throughput** | ~50K msg/s | ~1M msg/s |
| **Latencia** | <10ms | <100ms |
| **Orden** | Por queue | Por partición |
| **Replay** | No | Sí (seek offset) |
| **Uso** | Tareas, RPC | Logs, Eventos, Stream |

---

## 🔗 Referencias

- **Spring Boot Docs:** https://spring.io/projects/spring-boot
- **RabbitMQ Docs:** https://www.rabbitmq.com/documentation.html
- **Apache Kafka Docs:** https://kafka.apache.org/documentation/
- **Oracle Cloud:** https://docs.oracle.com/en/cloud/
