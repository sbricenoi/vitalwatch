# 🎬 DIÁLOGO DE PRESENTACIÓN CORTA - RABBITMQ (5-6 MINUTOS)
## VitalWatch: Sistema de Monitoreo Hospitalario

---

**DURACIÓN:** 5-6 minutos exactos
**FORMATO:** Demostración directa y rápida
**ENFOQUE:** Ir directo a las pruebas con alertas médicas

---

## 🎯 ESTRUCTURA RÁPIDA

1. Introducción (30 seg)
2. Mostrar Servicios Docker (30 seg)
3. Postman - 3 Pruebas Clave (2.5 min)
4. Verificar Resultados (1.5 min)
5. Cierre (30 seg)

**TOTAL: 5.5 minutos**

---

## 🎬 INICIO

**[PANTALLA: Postman abierto + Terminal visible]**

### 1️⃣ INTRODUCCIÓN (30 segundos)

**PA:**
"Hola, somos [Nombre A] y [Nombre B]. Vamos a demostrar nuestro sistema de monitoreo de transporte público con RabbitMQ.

Implementamos 4 microservicios con Spring Boot:
- 2 Productores que publican mensajes
- 2 Consumidores: uno guarda en Oracle Cloud, otro genera archivos JSON
- Todo orquestado con Docker Compose

Vamos directo a probarlo."

---

### 2️⃣ SERVICIOS EN AZURE (30 segundos)

**[PANTALLA: Portal de Azure - Resource Group]**

**PB:**
"Primero verificamos que todo está desplegado y corriendo en Azure."

**[Mostrar Azure Portal con Resource Group]**

**PB:**
"Tenemos 5 Container Apps activos en Azure:
- RabbitMQ (message broker) con Management UI
- Productor 1 - Detector de Anomalías Médicas
- Productor 2 - Generador de Resúmenes Hospitalarios  
- Consumidor 1 - DB Saver: guarda alertas en Oracle Cloud
- Consumidor 2 - JSON Generator: crea archivos de auditoría

Todos 'Running' con URLs públicas de Azure. Ahora las pruebas con Postman contra estos servicios en la nube."

---

### 3️⃣ PRUEBAS CON POSTMAN (2.5 minutos)

**[Abrir Postman]**

#### **TEST 1: Health Check (15 seg)**

**PA:**
"Verificamos que los productores en Azure están activos."

**[GET https://vitalwatch-producer-anomaly.{azure-url}/api/v1/vital-signs/health → Send]**

**PA:**
"Código 200. Productor 1 en Azure operativo. URL pública funcionando."

**[GET https://vitalwatch-producer-summary.{azure-url}/api/v1/summary/health → Send]**

**PA:**
"Código 200. Productor 2 también. Ambos respondiendo desde Azure Container Apps. Vamos a generar alertas médicas."

---

#### **TEST 2: Signos Vitales Críticos (45 seg)**

**[POST https://vitalwatch-producer-anomaly.{azure-url}/api/v1/vital-signs/check]**

**PB:**
"Enviamos signos vitales de un paciente con valores críticos al servicio en Azure:
- Frecuencia cardíaca: 150 lpm (taquicardia severa)
- Presión: 180/110 mmHg (crisis hipertensiva)
- Saturación de oxígeno: 85% (hipoxemia - peligro)
- Temperatura: 39.5°C (fiebre alta)"

**[Mostrar body rápidamente, luego Send]**

**PB:**
"Respuesta código 201:
```json
{
  "code": "201",
  "message": "Anomalías detectadas. Alerta publicada a RabbitMQ",
  "data": {
    "severity": "CRITICA",
    "anomaliesCount": 6,
    "alertPublished": true
  }
}
```

Seis anomalías médicas detectadas en los signos vitales. El sistema generó una alerta crítica que se publicó a RabbitMQ. Los consumidores van a procesar esta alerta médica inmediatamente."

---

#### **TEST 3: Generar Resumen (30 seg)**

**[POST https://vitalwatch-producer-summary.{azure-url}/api/v1/summary/generate]**

**PA:**
"Ahora generamos un resumen médico del sistema hospitalario desde Azure."

**[Send]**

**PA:**
"Código 200. El resumen médico incluye:
- Total de pacientes monitoreados
- Pacientes en estado crítico
- Cantidad de alertas médicas generadas
- Promedios de signos vitales (frecuencia cardíaca, temperatura, saturación)
- Estado de cada paciente

Este resumen se publicó a RabbitMQ para que el equipo médico tenga estadísticas actualizadas. Ahora verificamos que los consumidores procesaron las alertas."

---

### 4️⃣ VERIFICAR RESULTADOS (1.5 minutos)

#### **A. Logs del Consumidor DB en Azure (30 seg)**

**[PANTALLA: Azure Portal > Container Apps > vitalwatch-consumer-db > Logs]**

**PB:**
"Logs en tiempo real del consumidor que guarda alertas médicas en Oracle, corriendo en Azure:

```
[INFO] AlertConsumerService - 📥 Alerta recibida desde RabbitMQ: Paciente X - Severidad: CRITICA - 6 anomalías
[INFO] AlertConsumerService - ✅ Alerta guardada en Oracle con ID: 5 - Total procesadas: 5
```

El servicio en Azure recibió la alerta médica desde RabbitMQ (también en Azure) y la guardó en Oracle Cloud. Integración cloud-to-cloud funcionando perfectamente."

---

#### **B. Logs del Consumidor JSON en Azure (30 seg)**

**[PANTALLA: Azure Portal > Container Apps > vitalwatch-consumer-json > Logs]**

**PA:**
"Logs del consumidor JSON Generator en Azure:

```
[INFO] JsonGeneratorService - 📥 Alerta recibida desde RabbitMQ
[INFO] JsonGeneratorService - ✅ Archivo JSON generado: alert_20260213_P2_critica.json
```

El servicio generó archivos JSON con:
- Datos del paciente (nombre, sala, cama)
- Severidad de la alerta
- Detalles de cada anomalía en signos vitales
- Timestamp exacto de detección

Perfecto para auditorías médicas y análisis retrospectivo."

---

#### **C. Oracle Cloud (30 seg)**

**[PANTALLA: Oracle Cloud Console - SQL Developer Web]**

**PB:**
"Finalmente, verificamos en Oracle Cloud que las alertas médicas están guardadas."

**[Ejecutar query]**
```sql
SELECT ID, PACIENTE_NOMBRE, SEVERITY, ANOMALIES_COUNT, SALA, CAMA
FROM ALERTAS_MQ 
ORDER BY DETECTED_AT DESC 
FETCH FIRST 5 ROWS ONLY;
```

**PB:**
"Aquí están todas las alertas médicas guardadas, incluyendo la que acabamos de generar con 6 anomalías críticas. Cada registro incluye:
- Datos del paciente
- Ubicación hospitalaria (sala/cama)
- Severidad de la alerta
- Cantidad de anomalías detectadas

Datos perfectamente sincronizados entre dispositivos médicos → RabbitMQ → Consumidores → Oracle Cloud."

---

### 5️⃣ CIERRE (30 segundos)

**[PANTALLA: Volver a mostrar docker ps o RabbitMQ Management]**

**PA:**
"Resumen rápido de VitalWatch:

✅ Sistema de Alertas Médicas en Tiempo Real funcionando
✅ 4 Microservicios con Spring Boot corriendo en Docker
✅ RabbitMQ como message broker médico con 2 colas
✅ 2 Productores: Detector de Anomalías + Generador de Resúmenes Hospitalarios
✅ 2 Consumidores: Persistencia en Oracle + Archivos de Auditoría Médica
✅ Pruebas completas con Postman (alertas críticas generadas)
✅ Persistencia verificada en Oracle Cloud (tabla ALERTAS_MQ)
✅ Archivos JSON médicos generados correctamente"

**PB:**
"Cumple todos los requisitos académicos y tiene aplicación real en hospitales:
- Desarrollo con Spring Boot ✓
- Git/GitHub ✓
- Oracle Cloud Database ✓
- RESTful APIs con validación médica ✓
- Pruebas con Postman ✓
- RabbitMQ con productores y consumidores ✓
- Detección de emergencias médicas en tiempo real ✓

VitalWatch está listo para salvar vidas. Gracias."

---

---

## 📋 PREPARACIÓN RÁPIDA

### ANTES DE GRABAR:

**Tener abierto:**
- [ ] Portal de Azure (portal.azure.com) con Resource Group abierto
- [ ] Postman con ambiente "Azure Production" seleccionado
- [ ] Oracle Cloud SQL Developer (con query lista)

**En Postman, preparar estos 3 requests con URLs de Azure:**
1. GET https://vitalwatch-producer-anomaly.{azure-url}/api/v1/vital-signs/health
2. POST https://vitalwatch-producer-anomaly.{azure-url}/api/v1/vital-signs/check (con body crítico)
3. POST https://vitalwatch-producer-summary.{azure-url}/api/v1/summary/generate

**Navegación en Azure preparada:**
- Resource Group: rg-vitalwatch-rabbitmq-prod
- Container Apps: todos los servicios visibles
- Logs: vitalwatch-consumer-db y vitalwatch-consumer-json

**Query Oracle preparada:**
```sql
SELECT ID, PACIENTE_NOMBRE, SEVERITY, ANOMALIES_COUNT 
FROM ALERTAS_MQ 
ORDER BY DETECTED_AT DESC 
FETCH FIRST 5 ROWS ONLY;
```

---

## ⏱️ TIMING EXACTO

| Acción | Tiempo | Acum |
|--------|--------|------|
| Intro | 0:30 | 0:30 |
| Docker ps | 0:30 | 1:00 |
| Health check P1 | 0:15 | 1:15 |
| Health check P2 | 0:15 | 1:30 |
| POST signos críticos | 0:45 | 2:15 |
| POST generar resumen | 0:30 | 2:45 |
| Ver logs consumidor | 0:30 | 3:15 |
| Ver archivos JSON | 0:30 | 3:45 |
| Query Oracle | 0:30 | 4:15 |
| Cierre | 0:30 | 4:45 |

**Margen:** +0:45 segundos para transiciones = **5:30 minutos total**

---

## 🎯 PUNTOS CLAVE A MENCIONAR

✅ **"4 microservicios con Spring Boot"**
✅ **"RabbitMQ como message broker"**
✅ **"2 productores, 2 consumidores"**
✅ **"Persistencia en Oracle Cloud"**
✅ **"Archivos JSON para auditorías"**
✅ **"Todo containerizado con Docker"**
✅ **"Pruebas con Postman exitosas"**

---

## 💡 TIPS PARA GRABACIÓN RÁPIDA

**HABLAR:**
- ✅ Más rápido que en conversación normal (pero claro)
- ✅ Ir directo al punto, sin rodeos
- ✅ No explicar detalles técnicos profundos
- ✅ Solo mostrar códigos de respuesta importantes

**MOSTRAR:**
- ✅ Solo lo esencial en cada pantalla
- ✅ No leer todo el JSON completo
- ✅ Resaltar solo los campos importantes
- ✅ Transiciones rápidas entre herramientas

**EVITAR:**
- ❌ Pausas largas mientras carga algo
- ❌ Explicaciones técnicas detalladas
- ❌ Mostrar documentación extensa
- ❌ Repetir información

---

## ✨ MENSAJE MOTIVACIONAL

Este guion es para una demo rápida y al grano. Perfecto si:
- Tienen poco tiempo
- Prefieren enfoque práctico
- Quieren ir directo a las pruebas

**Recuerden:**
- Practiquen al menos 2 veces
- Cronométrense para ajustar timing
- Tengan todo preparado antes de grabar
- Respiren y vayan con confianza

**¡Éxito! 🚀**

---

**Versión:** 1.0 - Corta
**Fecha:** Febrero 2026
