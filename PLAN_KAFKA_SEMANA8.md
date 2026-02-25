# 📋 PLAN DE IMPLEMENTACIÓN - SEMANA 8
## VitalWatch con Apache Kafka - "Consumiendo Stream de datos"

**Fecha inicio:** Semana 8
**Experiencia:** 3 - Streaming con Kafka
**Puntos totales:** 100 puntos

---

## 🎯 OBJETIVOS DE LA SEMANA 8

1. ✅ Migrar de RabbitMQ a Apache Kafka
2. ✅ Configurar cluster Kafka (3 nodos + 3 Zookeeper)
3. ✅ Crear 2 tópicos de Kafka
4. ✅ Desarrollar 2 productores + 2 consumidores
5. ✅ Integrar con Oracle Cloud
6. ✅ Video de presentación (5-10 min)

---

## 🏗️ ARQUITECTURA KAFKA - VITALWATCH

```
┌─────────────────────────────────────────────────────────────────┐
│                    CLUSTER KAFKA (Docker)                        │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Kafka 1     │  │  Kafka 2     │  │  Kafka 3     │          │
│  │  Port: 9092  │  │  Port: 9093  │  │  Port: 9094  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         ▲                 ▲                 ▲                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Zookeeper 1  │  │ Zookeeper 2  │  │ Zookeeper 3  │          │
│  │  Port: 2181  │  │  Port: 2182  │  │  Port: 2183  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                   │
│  ┌──────────────────────────────────────────────────┐           │
│  │           Kafka UI (Port: 8080)                   │           │
│  │  - Ver tópicos                                    │           │
│  │  - Monitorear mensajes                            │           │
│  │  - Ver consumidores activos                       │           │
│  └──────────────────────────────────────────────────┘           │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         TÓPICOS KAFKA                            │
│                                                                   │
│  📊 signos-vitales-stream                                        │
│     ├─ Particiones: 3                                            │
│     ├─ Replication Factor: 2                                     │
│     └─ Mensajes: Stream continuo de signos vitales              │
│                                                                   │
│  🚨 alertas-medicas                                              │
│     ├─ Particiones: 3                                            │
│     ├─ Replication Factor: 2                                     │
│     └─ Mensajes: Alertas médicas críticas                       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    MICROSERVICIOS PRODUCTORES                    │
│                                                                   │
│  1️⃣ PRODUCER: Vital Signs Stream Generator (Port: 8081)        │
│     ├─ Función: Generar signos vitales cada 1 segundo (CRON)   │
│     ├─ Publica a: signos-vitales-stream                         │
│     ├─ Tecnología: Spring Boot + Spring Kafka                   │
│     └─ Endpoints: /api/v1/stream/start, /api/v1/stream/stop    │
│                                                                   │
│  2️⃣ PRODUCER: Alert Processor (Port: 8082)                     │
│     ├─ Función: Procesar stream y detectar anomalías            │
│     ├─ Consume de: signos-vitales-stream                        │
│     ├─ Publica a: alertas-medicas                               │
│     └─ Lógica: Detecta valores críticos en tiempo real          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    MICROSERVICIOS CONSUMIDORES                   │
│                                                                   │
│  3️⃣ CONSUMER: Database Saver                                   │
│     ├─ Función: Guardar datos en Oracle Cloud                   │
│     ├─ Consume de: signos-vitales-stream + alertas-medicas     │
│     ├─ Base de datos: Oracle Autonomous Database                │
│     └─ Tablas: SIGNOS_VITALES_KAFKA, ALERTAS_KAFKA             │
│                                                                   │
│  4️⃣ CONSUMER: Summary Generator                                │
│     ├─ Función: Generar resúmenes diarios                       │
│     ├─ Consume de: ambos tópicos                                │
│     ├─ Almacena: Estadísticas agregadas                         │
│     └─ Tabla: RESUMEN_DIARIO_KAFKA                              │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📦 ESTRUCTURA DEL PROYECTO

```
vitalwatch-kafka/
│
├── docker-compose-kafka.yml              # ⭐ NUEVO: Cluster Kafka completo
│
├── kafka-config/                         # ⭐ NUEVO: Configuraciones Kafka
│   ├── server-1.properties
│   ├── server-2.properties
│   ├── server-3.properties
│   └── zookeeper.properties
│
├── producer-vital-signs-stream/          # ⭐ NUEVO: Productor 1
│   ├── src/main/java/.../
│   │   ├── producer/StreamProducer.java
│   │   ├── scheduler/VitalSignsScheduler.java  (CRON cada 1 seg)
│   │   ├── model/VitalSignsMessage.java
│   │   └── config/KafkaProducerConfig.java
│   ├── pom.xml
│   └── Dockerfile
│
├── producer-alert-processor/             # ⭐ NUEVO: Productor 2 (también consume)
│   ├── src/main/java/.../
│   │   ├── consumer/VitalSignsConsumer.java
│   │   ├── processor/AlertProcessor.java
│   │   ├── producer/AlertProducer.java
│   │   └── config/KafkaConfig.java
│   ├── pom.xml
│   └── Dockerfile
│
├── consumer-database-saver/              # ⭐ NUEVO: Consumidor 1
│   ├── src/main/java/.../
│   │   ├── consumer/MultiTopicConsumer.java
│   │   ├── service/DatabaseService.java
│   │   ├── repository/SignosVitalesKafkaRepo.java
│   │   └── model/SignosVitalesKafka.java
│   ├── wallet/                           # Oracle Wallet
│   ├── pom.xml
│   └── Dockerfile
│
├── consumer-summary-generator/           # ⭐ NUEVO: Consumidor 2
│   ├── src/main/java/.../
│   │   ├── consumer/SummaryConsumer.java
│   │   ├── service/SummaryService.java
│   │   └── model/ResumenDiario.java
│   ├── pom.xml
│   └── Dockerfile
│
├── database/
│   ├── create_tables_kafka.sql           # ⭐ NUEVO: Tablas para Kafka
│   └── README.md
│
├── docs/
│   ├── ARQUITECTURA_KAFKA.md             # ⭐ NUEVO
│   ├── GUIA_KAFKA.md                     # ⭐ NUEVO
│   └── postman-collection-kafka.json     # ⭐ NUEVO
│
└── PLAN_KAFKA_SEMANA8.md                 # Este archivo
```

---

## 📝 TAREAS DETALLADAS

### ✅ TAREA 1: Configurar Cluster Kafka (20 puntos)

**Subtareas:**

- [ ] **1.1** Crear `docker-compose-kafka.yml` con:
  - 3 contenedores Kafka (brokers)
  - 3 contenedores Zookeeper
  - 1 contenedor Kafka UI
  - Networking entre contenedores

- [ ] **1.2** Configurar variables de entorno:
  ```yaml
  KAFKA_BROKER_ID: 1, 2, 3
  KAFKA_ZOOKEEPER_CONNECT: zookeeper1:2181,zookeeper2:2182,zookeeper3:2183
  KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
  KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka1:9092
  ```

- [ ] **1.3** Configurar Kafka UI:
  ```yaml
  KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka1:9092,kafka2:9093,kafka3:9094
  ```

- [ ] **1.4** Probar el cluster:
  ```bash
  docker-compose -f docker-compose-kafka.yml up -d
  docker ps  # Verificar 7 contenedores corriendo
  ```

**Entregable:** Cluster Kafka funcionando con 7 contenedores activos

---

### ✅ TAREA 2: Crear Tópicos en Kafka (20 puntos)

- [ ] **2.1** Crear tópico `signos-vitales-stream`:
  ```bash
  docker exec -it kafka1 kafka-topics.sh --create \
    --topic signos-vitales-stream \
    --bootstrap-server localhost:9092 \
    --partitions 3 \
    --replication-factor 2
  ```

- [ ] **2.2** Crear tópico `alertas-medicas`:
  ```bash
  docker exec -it kafka1 kafka-topics.sh --create \
    --topic alertas-medicas \
    --bootstrap-server localhost:9092 \
    --partitions 3 \
    --replication-factor 2
  ```

- [ ] **2.3** Verificar tópicos creados:
  ```bash
  docker exec -it kafka1 kafka-topics.sh --list \
    --bootstrap-server localhost:9092
  ```

- [ ] **2.4** Ver en Kafka UI: http://localhost:8080

**Entregable:** 2 tópicos creados y visibles en Kafka UI

---

### ✅ TAREA 3: Desarrollar Productor 1 - Stream Generator (20 puntos)

**Descripción:** Microservicio que genera signos vitales simulados cada 1 segundo

**Archivos a crear:**

- [ ] **3.1** `VitalSignsScheduler.java`:
  ```java
  @Scheduled(fixedRate = 1000) // Cada 1 segundo
  public void generateVitalSigns() {
      VitalSignsMessage message = generateRandomVitalSigns();
      kafkaTemplate.send("signos-vitales-stream", message);
  }
  ```

- [ ] **3.2** `VitalSignsMessage.java`:
  ```java
  public class VitalSignsMessage {
      private String pacienteId;
      private String pacienteNombre;
      private int frecuenciaCardiaca;
      private int presionSistolica;
      private int presionDiastolica;
      private double temperatura;
      private int saturacionOxigeno;
      private LocalDateTime timestamp;
  }
  ```

- [ ] **3.3** `KafkaProducerConfig.java`:
  - Bootstrap servers: kafka1:9092,kafka2:9093,kafka3:9094
  - Key serializer: StringSerializer
  - Value serializer: JsonSerializer

- [ ] **3.4** Endpoints REST:
  - `POST /api/v1/stream/start` → Iniciar generación
  - `POST /api/v1/stream/stop` → Detener generación
  - `GET /api/v1/stream/stats` → Estadísticas

**Entregable:** Microservicio que publica mensajes cada segundo

---

### ✅ TAREA 4: Desarrollar Productor 2 - Alert Processor (20 puntos)

**Descripción:** Consume signos vitales, detecta anomalías, publica alertas

- [ ] **4.1** `VitalSignsConsumer.java`:
  ```java
  @KafkaListener(topics = "signos-vitales-stream", groupId = "alert-processor-group")
  public void consume(VitalSignsMessage message) {
      if (hasAnomalies(message)) {
          AlertMessage alert = createAlert(message);
          alertProducer.send("alertas-medicas", alert);
      }
  }
  ```

- [ ] **4.2** `AlertProcessor.java`:
  - Lógica de detección de anomalías
  - Rangos médicos normales
  - Cálculo de severidad

- [ ] **4.3** `AlertProducer.java`:
  - Publicar alertas a tópico `alertas-medicas`

**Entregable:** Microservicio que procesa stream y genera alertas

---

### ✅ TAREA 5: Desarrollar Consumidor 1 - Database Saver (20 puntos)

**Descripción:** Consume ambos tópicos y guarda en Oracle Cloud

- [ ] **5.1** `MultiTopicConsumer.java`:
  ```java
  @KafkaListener(topics = {"signos-vitales-stream", "alertas-medicas"})
  public void consumeMultiple(ConsumerRecord<String, String> record) {
      if (record.topic().equals("signos-vitales-stream")) {
          saveVitalSigns(record.value());
      } else {
          saveAlert(record.value());
      }
  }
  ```

- [ ] **5.2** Crear tablas en Oracle:
  ```sql
  CREATE TABLE SIGNOS_VITALES_KAFKA (
      id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
      paciente_id VARCHAR2(50),
      frecuencia_cardiaca NUMBER,
      temperatura NUMBER(4,2),
      timestamp TIMESTAMP,
      kafka_partition NUMBER,
      kafka_offset NUMBER
  );

  CREATE TABLE ALERTAS_KAFKA (
      id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
      paciente_id VARCHAR2(50),
      tipo_alerta VARCHAR2(100),
      severidad VARCHAR2(20),
      timestamp TIMESTAMP
  );
  ```

- [ ] **5.3** Configurar Oracle Wallet
- [ ] **5.4** Spring Data JPA repositories

**Entregable:** Microservicio que persiste datos en Oracle

---

### ✅ TAREA 6: Desarrollar Consumidor 2 - Summary Generator (20 puntos)

**Descripción:** Genera resúmenes diarios del sistema

- [ ] **6.1** `SummaryConsumer.java`:
  - Consume ambos tópicos
  - Agrega datos por día
  - Calcula estadísticas

- [ ] **6.2** Tabla Oracle:
  ```sql
  CREATE TABLE RESUMEN_DIARIO_KAFKA (
      id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
      fecha DATE,
      total_pacientes NUMBER,
      total_mediciones NUMBER,
      total_alertas NUMBER,
      promedio_frecuencia_cardiaca NUMBER(5,2),
      promedio_temperatura NUMBER(4,2),
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  );
  ```

- [ ] **6.3** Endpoint para ver resumen:
  - `GET /api/v1/summary/daily/{fecha}`

**Entregable:** Microservicio que genera resúmenes diarios

---

### ✅ TAREA 7: Video de Presentación (10 puntos)

**Requisitos:**
- ✅ Duración: 5-10 minutos
- ✅ Mostrar despliegue en cloud (opcional: Azure)
- ✅ Mostrar Kafka UI en funcionamiento
- ✅ Demostrar publicación y consumo de mensajes
- ✅ Mostrar datos en Oracle Cloud
- ✅ Explicar arquitectura y flujo de datos

**Puntos a cubrir:**
1. Introducción al sistema VitalWatch con Kafka
2. Mostrar cluster Kafka (7 contenedores)
3. Kafka UI: Tópicos, particiones, mensajes
4. Productor 1: Stream generando datos cada segundo
5. Productor 2: Procesando y generando alertas
6. Consumidores: Guardando en Oracle
7. Query en Oracle mostrando datos
8. Conclusiones

---

## 🔧 DEPENDENCIAS MAVEN

### Para todos los microservicios:

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <!-- Spring Data JPA (solo consumidores que guardan en BD) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Oracle JDBC -->
    <dependency>
        <groupId>com.oracle.database.jdbc</groupId>
        <artifactId>ojdbc8</artifactId>
        <version>23.3.0.23.09</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Jackson para JSON -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
</dependencies>
```

---

## 📊 DIFERENCIAS: RABBITMQ vs KAFKA

| Característica | RabbitMQ (Semana 6) | Kafka (Semana 8) |
|----------------|---------------------|------------------|
| **Tipo** | Message Queue | Streaming Platform |
| **Modelo** | Push (broker envía) | Pull (consumer solicita) |
| **Persistencia** | Temporal | Permanente (log) |
| **Ordenamiento** | Por cola | Por partición |
| **Escalabilidad** | Vertical | Horizontal |
| **Caso de uso** | Tasks, jobs | Streaming, eventos |
| **Configuración** | 1 broker simple | Cluster (3 brokers + ZK) |
| **UI** | Management Plugin | Kafka UI |
| **Puerto** | 5672, 15672 | 9092, 8080 (UI) |

---

## ⏱️ CRONOGRAMA RECOMENDADO

### **Día 1: Cluster Kafka**
- [ ] Configurar docker-compose-kafka.yml
- [ ] Levantar 7 contenedores
- [ ] Crear 2 tópicos
- [ ] Verificar en Kafka UI

### **Día 2: Productores**
- [ ] Desarrollar Producer 1 (Stream Generator)
- [ ] Desarrollar Producer 2 (Alert Processor)
- [ ] Probar publicación de mensajes

### **Día 3: Consumidores**
- [ ] Desarrollar Consumer 1 (Database Saver)
- [ ] Desarrollar Consumer 2 (Summary Generator)
- [ ] Crear tablas en Oracle
- [ ] Probar persistencia

### **Día 4: Integración y Pruebas**
- [ ] Integración completa
- [ ] Pruebas end-to-end
- [ ] Documentación

### **Día 5: Video y Entrega**
- [ ] Grabar video de presentación
- [ ] Comprimir proyecto
- [ ] Subir a GitHub
- [ ] Entregar

---

## 🎯 CRITERIOS DE EVALUACIÓN (100 puntos)

| # | Criterio | Puntos | Estado |
|---|----------|--------|--------|
| 1 | Uso de Git/GitHub | 10 | ⬜ |
| 2 | Configurar cluster Kafka | 20 | ⬜ |
| 3 | Configurar 2 tópicos y publicar | 20 | ⬜ |
| 4 | 2 microservicios consumidores | 20 | ⬜ |
| 5 | 2 microservicios productores | 20 | ⬜ |
| 6 | Video presentación | 10 | ⬜ |
| **TOTAL** | | **100** | |

---

## 🚀 COMANDOS RÁPIDOS

### **Levantar Cluster Kafka:**
```bash
docker-compose -f docker-compose-kafka.yml up -d
```

### **Ver logs:**
```bash
docker-compose -f docker-compose-kafka.yml logs -f kafka1
```

### **Listar tópicos:**
```bash
docker exec -it kafka1 kafka-topics.sh --list --bootstrap-server localhost:9092
```

### **Consumir mensajes (debug):**
```bash
docker exec -it kafka1 kafka-console-consumer.sh \
  --topic signos-vitales-stream \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

### **Ver grupos de consumidores:**
```bash
docker exec -it kafka1 kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --list
```

---

## 📚 RECURSOS ÚTILES

- **Apache Kafka Docs:** https://kafka.apache.org/documentation/
- **Spring Kafka:** https://spring.io/projects/spring-kafka
- **Kafka UI:** https://github.com/provectus/kafka-ui
- **Docker Compose Kafka:** https://github.com/wurstmeister/kafka-docker

---

## ✅ CHECKLIST FINAL

Antes de entregar, verificar:

- [ ] Cluster Kafka con 7 contenedores funcionando
- [ ] 2 tópicos creados y visibles en Kafka UI
- [ ] Productor 1 generando mensajes cada 1 segundo
- [ ] Productor 2 procesando y generando alertas
- [ ] Consumidor 1 guardando en Oracle
- [ ] Consumidor 2 generando resúmenes
- [ ] Datos visibles en Oracle Cloud
- [ ] Colección Postman con pruebas
- [ ] Video de presentación (5-10 min)
- [ ] Código subido a GitHub
- [ ] Proyecto comprimido (.zip)
- [ ] Documentación completa

---

**Creado:** Febrero 2026
**Versión:** 1.0
**Autor:** Sistema de Planificación VitalWatch
