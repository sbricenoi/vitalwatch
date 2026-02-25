# 🎥 VitalWatch - Guión de Presentación Kafka

**Duración:** 5-10 minutos  
**Participantes:** 2 personas  
**Tema:** Sistema de Streaming en Tiempo Real con Apache Kafka

---

## 👥 Roles

- **Presentador 1 (P1):** Líder técnico - Explicaciones arquitectura y flujo
- **Presentador 2 (P2):** DevOps - Demostraciones prácticas y monitoreo

---

## 🎬 INTRODUCCIÓN (30 segundos)

**P1:** Hola a todos. Hoy presentaremos la evolución de VitalWatch hacia un sistema de streaming en tiempo real usando Apache Kafka. Este sistema permite monitorear signos vitales de pacientes hospitalarios con procesamiento continuo y alta disponibilidad.

**P2:** Correcto. Pasamos de un modelo de mensajería tradicional con RabbitMQ a una arquitectura de event streaming que nos permite procesar hasta 86,400 mediciones diarias con garantías de orden y la capacidad de reprocessar datos históricos.

---

## 📊 ARQUITECTURA DEL SISTEMA (1 minuto)

**P1:** Nuestra arquitectura Kafka consta de tres capas principales. En la base tenemos un cluster de 3 brokers Kafka con 3 nodos Zookeeper para alta disponibilidad. Esto nos garantiza que el sistema siga operando incluso si un broker falla.

**P2:** Sobre esta infraestructura, tenemos 2 tópicos principales: "signos-vitales-stream" que recibe las mediciones continuas, y "alertas-medicas" que contiene las alertas detectadas. Ambos tópicos tienen 3 particiones y un factor de replicación de 2, lo que significa que cada mensaje se guarda en 2 brokers diferentes.

**P1:** Y en la capa de aplicación tenemos 4 microservicios: dos productores y dos consumidores. El primer productor genera signos vitales cada 1 segundo simulando 5 pacientes en UCI. El segundo productor consume este stream, detecta anomalías médicas y publica alertas. Los consumidores se encargan de persistir todo en Oracle Cloud y generar resúmenes diarios.

---

## 🚀 DEMOSTRACIÓN 1: INICIAR CLUSTER KAFKA (1.5 minutos)

**P2:** Vamos a iniciar el cluster. *[Compartir pantalla - Terminal]*

```bash
./start-kafka-cluster.sh
```

**P2:** Como pueden ver, el script está levantando los 3 Zookeepers primero, luego los 3 brokers Kafka, y finalmente Kafka UI. Este proceso toma aproximadamente 1 minuto.

**P1:** Mientras esperamos, es importante destacar que Kafka es un sistema distribuido diseñado para manejar millones de mensajes por segundo. A diferencia de RabbitMQ donde los mensajes se eliminan después de consumirse, en Kafka los mensajes se persisten por 7 días en nuestro caso, permitiendo que múltiples consumidores lean el mismo stream.

**P2:** *[Cluster iniciado]* Perfecto. Ahora vamos a crear los tópicos.

```bash
./create-kafka-topics.sh
```

**P2:** Este script crea los dos tópicos: "signos-vitales-stream" con retención de 7 días para el stream continuo, y "alertas-medicas" con retención de 30 días porque las alertas son críticas y pueden necesitarse para auditoría.

---

## 🌐 DEMOSTRACIÓN 2: KAFKA UI (1 minuto)

**P2:** Ahora abramos Kafka UI en el navegador. *[Abrir http://localhost:8080]*

**P2:** Aquí podemos ver el estado completo del cluster. *[Navegar a Brokers]* Tenemos 3 brokers activos. *[Navegar a Topics]* Y aquí están nuestros dos tópicos con sus 3 particiones cada uno.

**P1:** Kafka UI es una herramienta esencial para monitoreo en producción. Nos permite ver en tiempo real el throughput, los consumer groups activos, el lag de procesamiento, y hasta inspeccionar mensajes individuales para debugging.

---

## 🔄 DEMOSTRACIÓN 3: INICIAR MICROSERVICIOS (2 minutos)

**P2:** Ahora iniciemos los microservicios. *[Terminal]*

```bash
docker-compose -f docker-compose-kafka.yml up -d
```

**P2:** Esto levanta los 4 microservicios conectados al cluster Kafka. Esperemos unos segundos mientras arrancan...

```bash
docker-compose -f docker-compose-kafka.yml ps
```

**P2:** Perfecto, todos los servicios están "running". Ahora vamos a iniciar el stream de signos vitales. *[Abrir Postman]*

**P2:** Aquí tengo la colección de Postman con todos los endpoints. Primero verificamos el health del Stream Generator.

```
GET http://localhost:8081/api/v1/stream/health
```

**P1:** *[Mostrar respuesta JSON]* Como ven, el servicio está "UP" y listo. Este microservicio tiene un scheduler interno que genera signos vitales cada 1 segundo, pero empieza pausado para que tengamos control total.

**P2:** Ahora lo iniciamos.

```
POST http://localhost:8081/api/v1/stream/start
```

**P2:** *[Mostrar respuesta]* Excelente, el stream está "RUNNING". Volvamos a Kafka UI para ver los mensajes llegando.

---

## 📈 DEMOSTRACIÓN 4: MENSAJES EN TIEMPO REAL (1.5 minutos)

**P2:** *[Volver a Kafka UI - Topics - signos-vitales-stream - Messages]*

**P2:** ¡Ahí están! Vean cómo los mensajes están llegando continuamente. Cada mensaje contiene los signos vitales completos de un paciente: frecuencia cardíaca, presión arterial, temperatura, saturación de oxígeno y frecuencia respiratoria.

**P1:** Noten que los mensajes se distribuyen automáticamente entre las 3 particiones. Kafka usa el "pacienteId" como key, lo que garantiza que todos los mensajes de un mismo paciente vayan siempre a la misma partición. Esto es crucial para mantener el orden de los eventos por paciente.

**P2:** Ahora miremos el segundo tópico de alertas. *[Navegar a alertas-medicas]*

**P2:** Aquí vemos las alertas que el Alert Processor está generando automáticamente. Aproximadamente el 15% de las mediciones tienen alguna anomalía, así que deberíamos ver alertas cada 6-7 segundos.

**P1:** *[Expandir un mensaje de alerta]* Cada alerta incluye el detalle completo de las anomalías detectadas, la severidad calculada, y los valores exactos que causaron la alerta. Por ejemplo, aquí vemos una alerta "CRITICA" porque la saturación de oxígeno está en 88%, por debajo del rango normal de 95-100%.

---

## 📊 DEMOSTRACIÓN 5: ESTADÍSTICAS Y MONITOREO (1.5 minutos)

**P2:** Vamos a esperar 1 minuto para acumular datos y luego ver las estadísticas. *[Esperar 1 minuto o usar time-lapse]*

**P2:** *[En Postman]* Consultamos las estadísticas del Stream Generator:

```
GET http://localhost:8081/api/v1/stream/stats
```

**P1:** *[Mostrar respuesta]* Perfecto. En 1 minuto generamos 60 mensajes, que es exactamente la tasa esperada de 1 mensaje por segundo. El sistema está configurado para generar 3,600 mensajes por hora, lo que equivale a 86,400 mensajes por día.

**P2:** Ahora veamos las estadísticas del Alert Processor:

```
GET http://localhost:8082/api/v1/processor/stats
```

**P1:** *[Mostrar respuesta]* Interesante. De 60 mensajes procesados, se generaron aproximadamente 9 alertas, lo que da una tasa del 15%. Esta tasa es configurable y refleja la probabilidad que programamos para generar valores anormales en el simulador.

---

## 💾 DEMOSTRACIÓN 6: PERSISTENCIA EN ORACLE (1 minuto)

**P1:** Ahora verifiquemos que los datos se están guardando correctamente en Oracle Cloud. *[Abrir Oracle SQL Developer o mostrar captura]*

```sql
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
```

**P1:** *[Mostrar resultados]* Perfecto. Aquí vemos las últimas 20 mediciones guardadas. Noten las columnas "kafka_partition" y "kafka_offset" - estas nos dan trazabilidad completa de dónde vino cada mensaje en Kafka.

**P2:** Y ahora las alertas:

```sql
SELECT 
    alert_id,
    paciente_nombre,
    severidad,
    cantidad_anomalias,
    detected_at
FROM ALERTAS_KAFKA
ORDER BY detected_at DESC
FETCH FIRST 10 ROWS ONLY;
```

**P2:** *[Mostrar resultados]* Aquí están las alertas más recientes, incluyendo alertas CRITICAS que requieren atención inmediata del personal médico.

---

## 📈 DEMOSTRACIÓN 7: RESUMEN DIARIO (1 minuto)

**P1:** Uno de los beneficios de Kafka es que podemos tener múltiples consumidores procesando el mismo stream de datos de diferentes formas. Nuestro Summary Generator consume los mismos datos y genera resúmenes agregados.

**P2:** *[En Postman]* Generemos un resumen del día actual:

```
POST http://localhost:8083/api/v1/summary/generate
```

**P1:** *[Mostrar respuesta]* Excelente. El resumen nos muestra estadísticas agregadas: 5 pacientes monitoreados, las mediciones totales del día, las alertas generadas por severidad, y los promedios de todos los signos vitales.

**P2:** Este resumen se genera automáticamente a medianoche usando un CRON scheduler, pero también podemos generarlo bajo demanda para cualquier fecha específica. Esto es perfecto para reportes gerenciales o análisis retrospectivos.

---

## 🔍 DEMOSTRACIÓN 8: CONSUMER GROUPS (1 minuto)

**P1:** Una característica clave de Kafka son los Consumer Groups. Vamos a verificar que no tengamos lag en el procesamiento.

**P2:** *[En terminal]*

```bash
docker exec -it vitalwatch-kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:9092 \
  --group alert-processor-group \
  --describe
```

**P1:** *[Mostrar output]* Aquí vemos el estado del consumer group "alert-processor-group". Las columnas más importantes son "CURRENT-OFFSET" que indica hasta dónde ha leído el consumidor, y "LAG" que muestra cuántos mensajes están pendientes de procesar.

**P2:** En nuestro caso, el LAG es 0 en todas las particiones, lo que significa que estamos procesando los mensajes en tiempo real sin retraso. Si el LAG fuera alto, indicaría que necesitamos escalar los consumidores.

---

## 📊 ARQUITECTURA TÉCNICA: KAFKA VS RABBITMQ (1 minuto)

**P1:** Antes de concluir, quiero destacar por qué migramos de RabbitMQ a Kafka para este sistema.

**P1:** RabbitMQ es excelente para colas de tareas tradicionales con baja latencia, pero tiene limitaciones para streaming:
- Los mensajes se eliminan después de consumirse
- Throughput limitado a 20-50 mil mensajes por segundo
- No permite reprocessar eventos pasados

**P2:** Kafka, en cambio, está diseñado específicamente para event streaming:
- Los mensajes se persisten por 7-30 días
- Throughput de hasta 1 millón de mensajes por segundo
- Múltiples consumidores pueden leer el mismo stream
- Podemos reprocessar datos históricos moviendo el offset
- Garantiza orden por partición

**P1:** Para nuestro caso de uso de monitoreo hospitalario, esto es crítico. Si necesitamos analizar el historial de signos vitales de un paciente, o si un nuevo servicio de machine learning necesita entrenar con datos pasados, simplemente creamos un nuevo consumer group y procesamos el stream desde el principio.

---

## 🎯 PUNTOS CLAVE DEMOSTRADOS (30 segundos)

**P2:** Resumiendo lo que vimos:

**P2:**
1. ✅ Cluster Kafka con 3 brokers para alta disponibilidad
2. ✅ Stream Generator produciendo 1 mensaje por segundo de forma continua
3. ✅ Alert Processor detectando anomalías en tiempo real con tasa del 15%
4. ✅ Database Saver persistiendo todo en Oracle Cloud sin lag
5. ✅ Summary Generator generando resúmenes automáticos
6. ✅ Kafka UI para monitoreo visual del sistema
7. ✅ Consumer groups sin lag, procesando en tiempo real

**P1:**
8. ✅ Trazabilidad completa con offset y partición
9. ✅ Replicación de datos (RF=2) para tolerancia a fallos
10. ✅ API REST en todos los microservicios para integración

---

## 🚀 DEPLOYMENT EN AZURE (30 segundos)

**P1:** Para el deployment en Azure, usamos Azure Container Apps para los microservicios, combinado con Azure Event Hubs que ofrece una API compatible con Kafka.

**P2:** Event Hubs nos da todas las ventajas de Kafka pero completamente gestionado por Azure: sin necesidad de administrar brokers, auto-scaling, backups automáticos, y monitoreo integrado con Azure Monitor.

**P1:** El script de deployment `deploy-kafka-azure.sh` automatiza todo el proceso: crea el resource group, el Azure Container Registry, hace build y push de las 4 imágenes Docker, y despliega cada microservicio con sus variables de entorno correspondientes.

---

## 📈 RENDIMIENTO Y ESCALABILIDAD (30 segundos)

**P2:** En términos de rendimiento, nuestro sistema actual procesa:
- 1 mensaje por segundo por diseño (configurable)
- 60 mensajes por minuto
- 3,600 mensajes por hora
- 86,400 mediciones diarias más 13,000 alertas

**P1:** Pero lo importante es que Kafka nos permite escalar esto fácilmente. Si necesitamos monitorear 100 pacientes en lugar de 5, simplemente:
1. Aumentamos la frecuencia del scheduler
2. Agregamos más instancias del Alert Processor (Kafka distribuye automáticamente)
3. Aumentamos la concurrencia de los consumidores

**P2:** Y gracias al particionamiento, el procesamiento es paralelo. Cada partición se puede consumir independientemente, lo que nos permite escalar horizontalmente agregando más consumidores al grupo.

---

## 💡 CASOS DE USO REALES (30 segundos)

**P1:** Más allá de esta demo, este sistema tiene aplicaciones reales muy potentes:

**P1:**
1. **Alertas en tiempo real:** Un dashboard de enfermería puede consumir el tópico de alertas y mostrar notificaciones instantáneas cuando un paciente presenta signos críticos.

2. **Machine Learning:** Un modelo de IA puede consumir el stream histórico para predecir deterioro del paciente horas antes de que ocurra.

3. **Análisis retrospectivo:** Si un paciente tuvo un evento médico, podemos reprocessar su stream completo para análisis post-mortem.

**P2:**
4. **Integración con otros sistemas:** Cualquier sistema externo (registro médico electrónico, sistema de farmacia, etc.) puede consumir nuestro stream sin afectar el procesamiento existente.

---

## 🎬 CIERRE: RESULTADOS Y CONCLUSIONES (1 minuto)

**P1:** En conclusión, hemos implementado exitosamente un sistema de streaming en tiempo real con Apache Kafka que ofrece:

**P1:**
- ✅ **Alta disponibilidad:** Cluster de 3 brokers con replicación
- ✅ **Procesamiento en tiempo real:** Latencia menor a 100ms
- ✅ **Escalabilidad:** Arquitectura lista para crecer de 5 a 500 pacientes
- ✅ **Trazabilidad:** Cada mensaje con su offset y partición
- ✅ **Persistencia:** Retención de 7-30 días para análisis

**P2:**
- ✅ **Múltiples consumidores:** Base de datos, resúmenes, y futuros servicios
- ✅ **Reprocessamiento:** Capacidad de replay de eventos históricos
- ✅ **Monitoreo:** Kafka UI y métricas en cada microservicio
- ✅ **Cloud Native:** Despliegue en Azure con auto-scaling
- ✅ **Producción ready:** Health checks, logging, error handling

**P1:** Lo más importante es que este sistema no solo cumple con los requisitos actuales, sino que está diseñado para evolucionar. Podemos agregar nuevos consumidores sin tocar los existentes, escalar componentes independientemente, y mantener el historial completo de eventos para análisis futuros.

**P2:** El código está completamente documentado, con READMEs en cada microservicio, scripts de automatización, y una guía completa de pruebas. Todo está listo para producción.

**P1 & P2:** ¡Gracias por su atención! ¿Alguna pregunta?

---

## 📸 CHECKLIST PARA EL VIDEO

Antes de grabar, asegúrate de tener:

- [ ] Cluster Kafka levantado y funcionando
- [ ] Tópicos creados con 3 particiones cada uno
- [ ] Los 4 microservicios corriendo en Docker
- [ ] Stream iniciado y generando mensajes
- [ ] Kafka UI abierto en el navegador
- [ ] Postman con la colección cargada
- [ ] Oracle SQL Developer con queries preparadas
- [ ] Terminal preparada con comandos
- [ ] Pantalla limpia, cerrar notificaciones
- [ ] Slides de arquitectura (opcional)

## ⏱️ TIMING SUGERIDO

| Sección | Tiempo | Acumulado |
|---------|--------|-----------|
| Introducción | 0:30 | 0:30 |
| Arquitectura | 1:00 | 1:30 |
| Demo 1: Cluster | 1:30 | 3:00 |
| Demo 2: Kafka UI | 1:00 | 4:00 |
| Demo 3: Microservicios | 2:00 | 6:00 |
| Demo 4: Mensajes en tiempo real | 1:30 | 7:30 |
| Kafka vs RabbitMQ | 1:00 | 8:30 |
| Casos de uso | 0:30 | 9:00 |
| Cierre | 1:00 | 10:00 |

**Total:** 10 minutos

## 🎤 CONSEJOS PARA LA GRABACIÓN

1. **Preparación:**
   - Ejecuta una prueba completa antes de grabar
   - Ten el sistema ya corriendo si es posible
   - Usa time-lapse para esperas largas

2. **Durante la grabación:**
   - Habla con claridad y ritmo pausado
   - Muestra el mouse para que se vea qué estás haciendo
   - Pausa entre secciones para edición posterior

3. **Post-producción:**
   - Agrega zoom a áreas importantes
   - Acelera las esperas (compilación, docker pull)
   - Agrega música de fondo suave
   - Subtítulos opcionales para términos técnicos

4. **Backup plan:**
   - Si algo falla en vivo, ten capturas de pantalla listas
   - Practica el guión al menos 2 veces
   - Ten un segundo terminal abierto por si acaso

¡Éxito con tu presentación! 🚀
