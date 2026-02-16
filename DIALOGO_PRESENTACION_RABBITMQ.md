# 🎬 DIÁLOGO DE PRESENTACIÓN - INTEGRACIÓN RABBITMQ
## VitalWatch: Sistema de Monitoreo Hospitalario en Tiempo Real

---

**INFORMACIÓN DE LA PRESENTACIÓN**
- **Duración:** 8-10 minutos
- **Formato:** Diálogo entre 2 participantes con demostración en vivo
- **Plataforma:** Microsoft Teams (grabación)
- **Asignatura:** Desarrollo Cloud Native I (DSY2206) - Semana 6
- **Tema:** Integración RabbitMQ con Microservicios para Alertas Médicas

---

## 📋 ESTRUCTURA DE LA PRESENTACIÓN

1. **Introducción y Contexto del Proyecto** (1 min)
2. **Explicación de la Arquitectura RabbitMQ** (1.5 min)
3. **Demostración del Despliegue en Azure** (1 min)
4. **Pruebas con Postman - Productores en Azure** (2.5 min)
5. **Verificación de Consumidores en Azure** (2 min)
6. **Verificación en Oracle Cloud** (1 min)
7. **Cierre y Conclusiones** (1 min)

---

## 👥 DISTRIBUCIÓN DE ROLES

**PARTICIPANTE A (PA):** Introducción, Arquitectura, Demostración Azure, Verificación Consumidores
**PARTICIPANTE B (PB):** Pruebas Postman, Oracle Cloud, Cierre

---

---

## 🎬 INICIO DE LA PRESENTACIÓN

---

### 1️⃣ INTRODUCCIÓN Y CONTEXTO DEL PROYECTO (1 minuto)

**[PANTALLA: Mostrar título del proyecto o README_RABBITMQ.md]**

**PA:**
"Buenos días/tardes. Somos [Nombre A] y [Nombre B], y en esta presentación vamos a demostrar nuestro proyecto de la Semana 6: VitalWatch - Sistema de Monitoreo Hospitalario en Tiempo Real con RabbitMQ.

VitalWatch es un sistema de alertas médicas que implementa una arquitectura de mensajería asíncrona para gestionar signos vitales de pacientes hospitalizados. Específicamente, nuestro sistema permite:

- Monitorear signos vitales de pacientes en tiempo real
- Detectar automáticamente valores anormales (frecuencia cardíaca, presión arterial, temperatura, etc.)
- Generar alertas médicas críticas de forma inmediata
- Guardar alertas en Oracle Cloud Database
- Generar archivos JSON para auditorías médicas

Todo esto utilizando RabbitMQ como message broker y una arquitectura de microservicios con Spring Boot."

**PB:**
"Exacto. El caso de uso es muy práctico: imaginen un hospital que necesita monitorear continuamente los signos vitales de pacientes críticos en UCI. En lugar de que el personal médico revise manualmente cada medición, nuestro sistema procesa automáticamente los datos y genera alertas cuando detecta valores peligrosos. Usamos colas de mensajes para desacoplar los componentes del sistema.

Nuestro sistema cuenta con:
- **2 Productores** que publican mensajes a RabbitMQ
- **2 Consumidores** que procesan esos mensajes
- **2 Colas** diferentes para distintos tipos de eventos
- **Oracle Cloud Database** para persistencia
- **Archivos JSON** para auditorías

Todo containerizado con Docker y completamente funcional. Vamos a ver cómo funciona."

---

### 2️⃣ EXPLICACIÓN DE LA ARQUITECTURA RABBITMQ (1.5 minutos)

**[PANTALLA: Abrir archivo README_RABBITMQ.md y mostrar el diagrama de flujo]**

**PA:**
"La arquitectura que implementamos es la siguiente:

En el centro tenemos **RabbitMQ**, que actúa como intermediario de mensajes. Este es el corazón de nuestra arquitectura event-driven.

Del lado de los **productores**, tenemos dos microservicios:

**Productor 1: Detector de Anomalías** - Puerto 8081
- Recibe datos de signos vitales de pacientes hospitalizados
- Ejemplo: frecuencia cardíaca, presión arterial, temperatura, saturación de oxígeno
- Compara cada valor con rangos médicos normales
- Si detecta valores anormales, publica una alerta médica a RabbitMQ
- Cola destino: `vital-signs-alerts`

**Productor 2: Generador de Resúmenes** - Puerto 8082
- Se ejecuta automáticamente cada 5 minutos
- También puede ejecutarse manualmente mediante API
- Genera estadísticas del sistema hospitalario: total de pacientes monitoreados, alertas generadas, promedios de signos vitales
- Cola destino: `vital-signs-summary`"

**PB:**
"Y del lado de los **consumidores**, tenemos:

**Consumidor 1: Guardador en Base de Datos**
- Escucha la cola `vital-signs-alerts`
- Cuando recibe un mensaje, lo guarda en Oracle Cloud
- Usa Spring Data JPA con Hibernate
- Guarda en la tabla `ALERTAS_MQ`
- Permite trazabilidad completa de todas las alertas

**Consumidor 2: Generador de Archivos JSON**
- También escucha la cola `vital-signs-alerts`
- Cuando recibe un mensaje, genera un archivo .json con los detalles de la alerta médica
- Los archivos se guardan en el directorio `./alerts-json/`
- Nomenclatura única: `alert_YYYYMMDD_HHMMSS_P{ID}_{severity}.json`
- Útil para auditorías médicas, revisión de casos y análisis posteriores

Importante: RabbitMQ distribuye los mensajes entre ambos consumidores usando round-robin. Si se publican 10 alertas médicas, aproximadamente 5 las procesará cada consumidor. Esto permite balanceo de carga automático."

---

### 3️⃣ DEMOSTRACIÓN DEL DESPLIEGUE EN AZURE (1 minuto)

**[PANTALLA: Portal de Azure en navegador - portal.azure.com]**

**PA:**
"Ahora vamos a mostrar que todos nuestros microservicios están desplegados y corriendo en Microsoft Azure."

**[Navegar a portal.azure.com > Resource Groups > rg-vitalwatch-rabbitmq-prod]**

**PA:**
"Como pueden ver, tenemos nuestro Resource Group 'rg-vitalwatch-rabbitmq-prod' en Azure con todos los servicios desplegados:

1. **Azure Container Apps Environment**: Donde corren todos nuestros contenedores
2. **vitalwatch-producer-anomaly**: Productor 1 - Detector de Anomalías
3. **vitalwatch-producer-summary**: Productor 2 - Generador de Resúmenes
4. **vitalwatch-consumer-db**: Consumidor que guarda en Oracle Cloud
5. **vitalwatch-consumer-json**: Consumidor que genera archivos JSON
6. **vitalwatch-rabbitmq**: El message broker con Management UI

Todos los servicios tienen URLs públicas asignadas por Azure."

**[Click en vitalwatch-producer-anomaly para mostrar detalles]**

**PA:**
"Aquí podemos ver:
- Estado: Running (activo)
- URL pública para acceder al servicio
- Réplicas activas: 1-3 (auto-scaling)
- Logs en tiempo real disponibles

Los otros microservicios tienen configuraciones similares. Ahora mi compañero va a demostrar las pruebas con Postman contra estos servicios en Azure."

---

### 4️⃣ PRUEBAS CON POSTMAN - PRODUCTORES (2.5 minutos)

**[PANTALLA: Abrir Postman con la colección importada]**

**PB:**
"Ahora vamos a probar los productores usando Postman. Tengo aquí la colección de endpoints organizados por servicio."

**[Mostrar la estructura de carpetas en Postman]**

**PB:**
"Primero vamos a verificar que los productores están activos con los health checks."

---

#### **TEST 1: Health Check Productor 1**

**[Seleccionar: GET https://vitalwatch-producer-anomaly.{azure-url}/api/v1/vital-signs/health]**

**PB:**
"Este es el health check del Productor 1 - Detector de Anomalías, desplegado en Azure Container Apps."

**[Mostrar la URL de Azure en Postman]**

**PB:**
"La URL es de Azure: `vitalwatch-producer-anomaly.graycoast-xxxxx.southcentralus.azurecontainerapps.io`"

**[Click en Send]**

**PB:**
"Perfecto, recibimos código 200 con el mensaje 'Productor operativo'. El servicio en Azure está funcionando correctamente."

---

#### **TEST 2: Health Check Productor 2**

**[Seleccionar: GET https://vitalwatch-producer-summary.{azure-url}/api/v1/summary/health]**

**[Click en Send]**

**PB:**
"Igual el Productor 2 - Generador de Resúmenes en Azure. Ambos productores están UP y respondiendo desde la nube."

---

#### **TEST 3: Enviar Signos Vitales Normales**

**[Seleccionar: POST https://vitalwatch-producer-anomaly.{azure-url}/api/v1/vital-signs/check]**

**PB:**
"Ahora voy a enviar signos vitales dentro de rangos normales al servicio en Azure. Este sería el caso de un paciente estable, sin ninguna complicación."

**[Mostrar el body JSON]**

**PB:**
"Los valores son:
- Frecuencia cardíaca: 75 lpm (normal: 60-100)
- Presión: 120/80 mmHg (normal)
- Temperatura: 36.5°C (normal)
- Saturación de oxígeno: 98% (normal: >95%)

Todos están dentro de rangos normales."

**[Click en Send]**

**PB:**
"Como esperábamos, el sistema responde con código 200 y nos dice:
```json
{
  "code": "200",
  "message": "Signos vitales verificados correctamente",
  "data": {
    "hasAnomalies": false,
    "anomaliesCount": 0,
    "alertPublished": false
  }
}
```

No se detectaron anomalías, por lo tanto NO se publicó ningún mensaje a RabbitMQ. El sistema es inteligente y solo genera alertas cuando es necesario."

---

#### **TEST 4: Enviar Signos Vitales Críticos (Generar Alerta)**

**[Seleccionar: POST https://vitalwatch-producer-anomaly.{azure-url}/api/v1/vital-signs/check]**

**PB:**
"Ahora vamos a enviar valores anormales al servicio en Azure para que el sistema genere una alerta médica."

**[Mostrar el body JSON con valores críticos]**

**PB:**
"Valores críticos:
- Frecuencia cardíaca: 150 lpm (muy alta, >120) - Taquicardia severa
- Presión: 180/110 mmHg (hipertensión crítica)
- Temperatura: 39.5°C (fiebre alta)
- Saturación de oxígeno: 85% (hipoxemia, <90%) - Peligro
- Frecuencia respiratoria: 30 rpm (muy alta, >25)

Este paciente claramente necesita atención médica inmediata."

**[Click en Send]**

**PB:**
"¡Excelente! El sistema detectó las anomalías:

```json
{
  "code": "201",
  "message": "Anomalías detectadas. Alerta publicada a RabbitMQ",
  "data": {
    "severity": "CRITICA",
    "hasAnomalies": true,
    "anomaliesCount": 6,
    "alertPublished": true
  }
}
```

Se detectaron 6 anomalías y se publicó la alerta a RabbitMQ. Ahora los consumidores van a procesar este mensaje."

**PA:**
"Perfecto. Esto demuestra que nuestro Productor 1 está funcionando correctamente: recibe signos vitales de dispositivos médicos, los analiza comparándolos con rangos clínicos normales, y publica alertas solo cuando detecta valores peligrosos para el paciente."

---

#### **TEST 5: Generar Resumen Manual**

**[Seleccionar: POST https://vitalwatch-producer-summary.{azure-url}/api/v1/summary/generate]**

**PB:**
"Ahora vamos a probar el Productor 2 en Azure. Este productor se ejecuta automáticamente cada 5 minutos para generar reportes periódicos, pero también podemos activarlo manualmente mediante esta API."

**[Click en Send]**

**PB:**
"Código 200. El resumen médico fue generado y publicado a RabbitMQ:

```json
{
  "code": "200",
  "message": "Resumen generado y publicado a RabbitMQ",
  "data": {
    "summaryType": "PERIODIC_SUMMARY",
    "totalPacientes": 9,
    "pacientesCriticos": 3,
    "alertasGeneradas": 13,
    "alertasCriticas": 5,
    "promedioFrecuenciaCardiaca": 76.29,
    "promedioTemperatura": 37.00,
    "promedioSaturacionOxigeno": 95.67
  }
}
```

Este resumen contiene estadísticas médicas agregadas que son útiles para el equipo médico y administrativo del hospital."

---

### 5️⃣ VERIFICACIÓN DE CONSUMIDORES (2 minutos)

**[PANTALLA: Terminal con logs de Docker]**

**PA:**
"Ahora vamos a verificar que los consumidores recibieron y procesaron los mensajes que acabamos de publicar."

---

#### **Verificar Logs del Consumidor DB Saver**

**[Ejecutar: `docker logs vitalwatch-consumer-db --tail 30`]**

**PA:**
"Estos son los logs del Consumidor 1 - DB Saver. Pueden ver líneas como:

```
[INFO] AlertConsumerService - 📥 Alerta recibida desde RabbitMQ: Paciente 2 - Severidad: CRITICA - 6 anomalías
[INFO] AlertConsumerService - ✅ Alerta guardada en Oracle con ID: 3 - Total procesadas: 3
```

El consumidor recibió el mensaje de RabbitMQ y lo guardó exitosamente en Oracle Cloud con el ID 3. La conexión con la base de datos está funcionando perfectamente."

---

#### **Verificar Logs del Consumidor JSON Generator**

**[Ejecutar: `docker logs vitalwatch-consumer-json --tail 30`]**

**PA:**
"Ahora veamos el Consumidor 2 - JSON Generator:

```
[INFO] JsonGeneratorService - 📥 Alerta recibida desde RabbitMQ: Paciente 2 - Severidad: CRITICA - 6 anomalías
[INFO] JsonGeneratorService - ✅ Archivo JSON generado: alert_20260213_020139_P2_critica.json - Total generados: 2
```

También recibió el mensaje y generó el archivo JSON correspondiente."

---

#### **Mostrar Archivos JSON Generados**

**[Abrir explorador de archivos o ejecutar: `ls -lh alerts-json/`]**

**PA:**
"Y aquí están los archivos JSON generados en el directorio `alerts-json/`."

**[Ejecutar: `cat alerts-json/alert_20260213_020139_P2_critica.json | jq`]**

**PA:**
"El contenido del archivo es:

```json
{
  "alertId": "ALERT-1707782499097",
  "timestamp": "2026-02-13T02:01:39.097Z",
  "pacienteId": 2,
  "pacienteNombre": "María García",
  "severity": "CRITICA",
  "anomaliesCount": 6,
  "anomalies": [
    {
      "parametro": "Frecuencia Cardíaca",
      "valorActual": "150 lpm",
      "rangoNormal": "60-100 lpm"
    },
    ...
  ]
}
```

Un archivo JSON completo con toda la información de la alerta. Perfecto para auditorías."

**PB:**
"Esto demuestra que ambos consumidores están procesando los mensajes correctamente: uno guarda en base de datos y el otro genera archivos. La arquitectura está funcionando como esperábamos."

---

### 6️⃣ VERIFICACIÓN EN ORACLE CLOUD (1 minuto)

**[PANTALLA: Abrir Oracle Cloud Console o SQL Developer Web]**

**PB:**
"Finalmente, vamos a verificar que los datos están efectivamente en Oracle Cloud."

**[Navegar a: Autonomous Database > Service Console > Database Actions > SQL]**

**PB:**
"Voy a ejecutar una consulta para ver las alertas guardadas:"

**[Ejecutar query]**
```sql
SELECT 
    ID,
    ALERT_ID,
    PACIENTE_NOMBRE,
    SEVERITY,
    ANOMALIES_COUNT,
    TO_CHAR(DETECTED_AT, 'DD/MM/YYYY HH24:MI:SS') as FECHA
FROM ALERTAS_MQ
ORDER BY DETECTED_AT DESC
FETCH FIRST 10 ROWS ONLY;
```

**PB:**
"Como pueden ver, aquí están todas las alertas médicas que procesamos, incluyendo la que acabamos de generar hace un momento con 6 anomalías detectadas.

Los datos incluyen:
- ID único de la alerta médica
- Nombre del paciente
- Sala y cama hospitalaria
- Severidad (CRITICA, MODERADA, BAJA)
- Cantidad de anomalías en signos vitales
- Fecha y hora de detección
- Detalles JSON de cada anomalía

Todo perfectamente sincronizado entre dispositivos médicos → RabbitMQ → Consumidores → Oracle Cloud. El personal médico puede consultar estas alertas en tiempo real."

---

### 7️⃣ CIERRE Y CONCLUSIONES (1 minuto)

**[PANTALLA: Volver a RabbitMQ Management Console o mostrar arquitectura]**

**PA:**
"Para resumir lo que hemos demostrado:

✅ **Sistema de Alertas Médicas en Tiempo Real:**
- Monitoreo continuo de signos vitales de pacientes
- Detección automática de valores anormales
- Generación de alertas críticas inmediatas

✅ **Arquitectura Completa con RabbitMQ:**
- RabbitMQ funcionando como message broker médico
- 2 colas diferentes para alertas y resúmenes
- Comunicación asíncrona y desacoplada

✅ **2 Productores Operativos:**
- Detector de Anomalías: analiza signos vitales y genera alertas médicas
- Generador de Resúmenes: crea reportes estadísticos del estado hospitalario

✅ **2 Consumidores Procesando Mensajes:**
- Guardador en BD: persiste alertas médicas en Oracle Cloud para consulta del personal médico
- Generador JSON: crea archivos para auditorías médicas y análisis de casos

✅ **Integración con Oracle Cloud:**
- Conexión segura mediante Oracle Wallet
- Tabla ALERTAS_MQ con todas las alertas procesadas
- Queries médicas funcionando correctamente"

**PB:**
"El sistema cumple con todos los requisitos de la evaluación:

✅ **Requisitos Técnicos:**
- Desarrollado con Spring Boot (arquitectura de microservicios médicos)
- Gestión de repositorio con Git/GitHub
- Conexión a Base de Datos Oracle Cloud
- Microservicios RESTful (GET, POST con validación médica)
- Validación completa con Postman

✅ **Requisitos de RabbitMQ:**
- 1 Docker Compose con todos los servicios
- 2 Productores: Detector de Anomalías Médicas + Generador de Reportes Hospitalarios
- 2 Consumidores: Persistencia en Oracle + Generación de Archivos de Auditoría
- Persistencia en Oracle Cloud (tabla ALERTAS_MQ) ✓
- Generación de archivos JSON médicos ✓

✅ **Valor Médico del Sistema:**
- Prevención de emergencias médicas mediante detección temprana
- Alertas inmediatas al personal médico
- Trazabilidad completa de eventos críticos
- Estadísticas para mejora de atención

✅ **Extras Implementados:**
- Health checks en todos los servicios
- Rangos clínicos configurables
- Logs médicos detallados
- Validación estricta de datos vitales
- Documentación técnica completa

El proyecto está completamente funcional y aplicable en un entorno hospitalario real."

**PA:**
"Todo el código fuente está disponible en nuestro repositorio GitHub con:
- Documentación completa en README_RABBITMQ.md
- Scripts SQL para crear las tablas
- Colección de Postman para pruebas
- Dockerfiles para cada microservicio
- Docker Compose para orquestación completa

Muchas gracias por su atención. Quedamos disponibles para responder cualquier pregunta."

---

---

## 📌 NOTAS IMPORTANTES PARA LA GRABACIÓN

### ✅ ANTES DE GRABAR:

**1. PREPARAR AMBIENTE:**
- [ ] Cerrar pestañas innecesarias del navegador
- [ ] Limpiar el escritorio
- [ ] Configurar resolución de pantalla (1920x1080 recomendado)
- [ ] Desactivar notificaciones (Slack, email, etc.)
- [ ] Tener todos los servicios corriendo con `docker-compose up`

**2. HERRAMIENTAS A TENER ABIERTAS:**
- [ ] Portal de Azure (portal.azure.com) - Para mostrar Container Apps
- [ ] Postman con colección importada y ambiente "Azure Production"
- [ ] Oracle Cloud Console (cloud.oracle.com)
- [ ] Editor de código con README_RABBITMQ.md (opcional)
- [ ] Azure Container Apps Logs (para ver logs en tiempo real)

**3. CREDENCIALES Y URLs LISTAS:**
```
Azure Portal:
- URL: https://portal.azure.com
- Resource Group: rg-vitalwatch-rabbitmq-prod

Productores en Azure:
- Productor 1 (Anomaly): https://vitalwatch-producer-anomaly.graycoast-xxxxx.southcentralus.azurecontainerapps.io
- Productor 2 (Summary): https://vitalwatch-producer-summary.graycoast-xxxxx.southcentralus.azurecontainerapps.io

Oracle Cloud:
- URL: https://cloud.oracle.com
- Usuario: ADMIN
- Password: $-123.Sb-123
- Service: s58onuxcx4c1qxe9_high
```

**4. DATOS DE PRUEBA EN POSTMAN:**
- Request con signos vitales normales (listo para enviar)
- Request con signos vitales críticos (listo para enviar)
- Request para generar resumen manual

**5. NAVEGACIÓN EN AZURE PREPARADA:**
```
Portal Azure:
1. Resource Groups > rg-vitalwatch-rabbitmq-prod
2. Container Apps > vitalwatch-producer-anomaly (mostrar estado)
3. Container Apps > vitalwatch-consumer-db > Logs (ver logs en tiempo real)
4. Container Apps > vitalwatch-consumer-json > Logs

Postman:
- Ambiente: Azure Production (con URLs de Azure)
- Variables configuradas con las URLs reales de Azure Container Apps
```

### 🎥 DURANTE LA GRABACIÓN:

**TIPS DE PRESENTACIÓN:**
- ✅ Hablar claro y a ritmo moderado (no muy rápido)
- ✅ Esperar a que las pantallas carguen completamente antes de continuar
- ✅ Señalar con el cursor lo que están explicando
- ✅ Leer en voz alta los datos importantes (códigos HTTP, mensajes)
- ✅ Si algo falla, mantener la calma y explicar cómo lo solucionarían
- ✅ Coordinarse bien entre ambos participantes (evitar interrupciones)

**MOMENTOS CLAVE A CAPTURAR:**
- ✅ Container Apps en Azure mostrando todos los servicios activos
- ✅ URLs públicas de Azure asignadas a cada microservicio
- ✅ Respuestas exitosas en Postman desde Azure (códigos 200/201)
- ✅ Logs en tiempo real desde Azure Portal
- ✅ Integración cloud-to-cloud (Azure ↔ Oracle Cloud)
- ✅ Datos persistidos en Oracle Cloud Database

### ⏱️ TIMING RECOMENDADO:

| Sección | Tiempo | Acumulado |
|---------|--------|-----------|
| 1. Introducción | 1:00 | 1:00 |
| 2. Arquitectura | 1:30 | 2:30 |
| 3. Docker/RabbitMQ | 1:00 | 3:30 |
| 4. Postman (5 tests) | 2:30 | 6:00 |
| 5. Consumidores | 2:00 | 8:00 |
| 6. Oracle Cloud | 1:00 | 9:00 |
| 7. Cierre | 1:00 | 10:00 |

**Tiempo objetivo:** 9-10 minutos
**Tiempo mínimo aceptable:** 8 minutos
**Tiempo máximo recomendado:** 12 minutos

---

## 🎯 CHECKLIST DE CUMPLIMIENTO DE LA PAUTA

**Según DSY2206_Exp2_S6_pauta_de_evaluacion:**

### ✅ Elementos Obligatorios a Mostrar:

- [x] **Microservicios con Spring Boot** → 4 microservicios (2 prod, 2 cons)
- [x] **Gestión con Git/GitHub** → Repositorio completo
- [x] **Base de Datos Oracle Cloud** → Tabla ALERTAS_MQ
- [x] **Controladores RESTful** → GET, POST en productores
- [x] **Pruebas con Postman** → 5+ requests demostrados
- [x] **RabbitMQ con 2 Productores** → Anomaly Detector + Summary Generator
- [x] **RabbitMQ con 2 Consumidores** → DB Saver + JSON Generator
- [x] **Persistencia en Oracle** → Tabla con alertas guardadas
- [x] **Generación de archivos JSON** → Directorio alerts-json/
- [x] **Docker Compose** → docker-compose-rabbitmq.yml con 5 servicios

### ✅ Criterios de Evaluación:

**1. Funcionamiento del Sistema (30%):**
- ✅ Servicios corriendo sin errores
- ✅ Comunicación entre microservicios funcional
- ✅ RabbitMQ procesando mensajes correctamente

**2. Pruebas con Postman (25%):**
- ✅ Health checks exitosos
- ✅ POST con signos vitales normales
- ✅ POST con signos vitales críticos (genera alerta)
- ✅ POST para generar resumen manual
- ✅ Respuestas HTTP correctas (200, 201)

**3. Persistencia de Datos (20%):**
- ✅ Datos guardados en Oracle Cloud (tabla ALERTAS_MQ)
- ✅ Archivos JSON generados en filesystem
- ✅ Verificación mediante queries SQL

**4. Dominio del Tema (15%):**
- ✅ Explicación clara de la arquitectura
- ✅ Entendimiento de cómo funciona RabbitMQ
- ✅ Explicación de productores y consumidores

**5. Calidad de Presentación (10%):**
- ✅ Video claro y bien organizado
- ✅ Duración entre 5-10 minutos
- ✅ Demostración en tiempo real

---

## 📚 ENLACES Y RECURSOS DE REFERENCIA

**Documentación del Proyecto:**
- README_RABBITMQ.md
- TESTING_RABBITMQ.md
- docs/RESULTADOS_PRUEBAS_RABBITMQ.md
- database/create_alertas_mq_table.sql

**Endpoints para Pruebas:**
```
Productor 1 (Anomaly Detector):
http://localhost:8081/api/v1/vital-signs/health
http://localhost:8081/api/v1/vital-signs/check

Productor 2 (Summary Generator):
http://localhost:8082/api/v1/summary/health
http://localhost:8082/api/v1/summary/generate
http://localhost:8082/api/v1/summary/stats

RabbitMQ Management:
http://localhost:15672
```

**Comandos Docker Útiles:**
```bash
# Iniciar todos los servicios
docker-compose -f docker-compose-rabbitmq.yml up -d

# Ver logs en tiempo real
docker-compose -f docker-compose-rabbitmq.yml logs -f

# Reiniciar un servicio específico
docker-compose -f docker-compose-rabbitmq.yml restart vitalwatch-producer-anomaly

# Detener todos los servicios
docker-compose -f docker-compose-rabbitmq.yml down
```

---

## 🚀 PREPARACIÓN FINAL

### DÍA ANTES DE GRABAR:
1. Ejecutar todas las pruebas para asegurarse que funciona
2. Revisar y practicar el guion al menos 2 veces
3. Verificar que la conexión a Oracle Cloud está activa
4. Limpiar el directorio alerts-json/ para empezar limpio
5. Actualizar la colección de Postman si es necesario

### 30 MINUTOS ANTES:
1. Reiniciar todos los servicios Docker
2. Verificar health checks de todos los microservicios
3. Hacer una prueba rápida end-to-end
4. Cerrar todas las aplicaciones innecesarias
5. Preparar agua para tomar durante la grabación 😊

### JUSTO ANTES DE GRABAR:
1. Respirar profundo y relajarse
2. Iniciar grabación en Teams
3. Sonreír y empezar con confianza
4. ¡Recuerden que lo tienen dominado! 💪

---

## ✨ MENSAJE FINAL

Este diálogo está diseñado para mostrar TODO lo que han implementado de forma clara y profesional. La clave es:

1. **Ser claros y concisos** en las explicaciones
2. **Mostrar, no solo contar** (demostración en vivo)
3. **Destacar el cumplimiento de requisitos**
4. **Demostrar dominio técnico**

¡Mucha suerte en su presentación! 🎉

Si tienen alguna duda o necesitan ajustar algo del guion, háganlo con tiempo. Este es su momento para brillar y mostrar todo el trabajo que han realizado.

**¡A romperla! 🚀**

---

**Documento creado por:** Sistema de Documentación VitalWatch
**Fecha:** Febrero 2026
**Versión:** 1.0
