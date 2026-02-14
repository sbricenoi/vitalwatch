# 🎯 ENDPOINTS PARA LA PRESENTACIÓN
## VitalWatch RabbitMQ - Lista Completa de APIs

---

## 🌐 URLs BASE EN AZURE

Reemplaza `{tu-environment-id}` con el ID real de tu Azure Container Apps Environment.

```
Productor 1 (Anomaly Detector):
https://vitalwatch-producer-anomaly.graycoast-{tu-environment-id}.southcentralus.azurecontainerapps.io

Productor 2 (Summary Generator):
https://vitalwatch-producer-summary.graycoast-{tu-environment-id}.southcentralus.azurecontainerapps.io
```

**Para obtener tus URLs reales:**
```bash
az containerapp list \
  --resource-group rg-vitalwatch-rabbitmq-prod \
  --query "[].{Name:name, URL:properties.configuration.ingress.fqdn}" \
  --output table
```

---

## 📌 ENDPOINTS PARA LA PRESENTACIÓN (Orden Recomendado)

### 1️⃣ HEALTH CHECKS (Para verificar que todo está UP)

#### ✅ Health Check - Productor 1 (Anomaly Detector)
```http
GET https://vitalwatch-producer-anomaly.graycoast-{id}.southcentralus.azurecontainerapps.io/api/v1/vital-signs/health
```

**Respuesta esperada:**
```json
{
  "code": "200",
  "message": "Productor operativo",
  "data": {
    "service": "Anomaly Detector Producer",
    "status": "UP"
  }
}
```

---

#### ✅ Health Check - Productor 2 (Summary Generator)
```http
GET https://vitalwatch-producer-summary.graycoast-{id}.southcentralus.azurecontainerapps.io/api/v1/summary/health
```

**Respuesta esperada:**
```json
{
  "code": "200",
  "message": "Productor operativo",
  "data": {
    "service": "Summary Generator Producer",
    "status": "UP"
  }
}
```

---

### 2️⃣ PRODUCTOR 1 - DETECTOR DE ANOMALÍAS

#### 📊 Verificar Signos Vitales NORMALES (NO genera alerta)
```http
POST https://vitalwatch-producer-anomaly.graycoast-{id}.southcentralus.azurecontainerapps.io/api/v1/vital-signs/check
Content-Type: application/json
```

**Body:**
```json
{
  "pacienteId": 1,
  "pacienteNombre": "Juan Pérez",
  "sala": "UCI-A",
  "cama": "101",
  "frecuenciaCardiaca": 75,
  "presionSistolica": 120,
  "presionDiastolica": 80,
  "temperatura": 36.5,
  "saturacionOxigeno": 98,
  "frecuenciaRespiratoria": 16,
  "deviceId": "DEVICE-001"
}
```

**Respuesta esperada:**
```json
{
  "code": "200",
  "message": "Signos vitales verificados correctamente",
  "data": {
    "hasAnomalies": false,
    "anomaliesCount": 0,
    "message": "Signos vitales dentro de rangos normales",
    "alertPublished": false
  }
}
```

---

#### 🚨 Verificar Signos Vitales CRÍTICOS (SÍ genera alerta)
```http
POST https://vitalwatch-producer-anomaly.graycoast-{id}.southcentralus.azurecontainerapps.io/api/v1/vital-signs/check
Content-Type: application/json
```

**Body:**
```json
{
  "pacienteId": 2,
  "pacienteNombre": "María García",
  "sala": "UCI-A",
  "cama": "102",
  "frecuenciaCardiaca": 150,
  "presionSistolica": 180,
  "presionDiastolica": 110,
  "temperatura": 39.5,
  "saturacionOxigeno": 85,
  "frecuenciaRespiratoria": 30,
  "deviceId": "DEVICE-002"
}
```

**Respuesta esperada:**
```json
{
  "code": "201",
  "message": "Anomalías detectadas. Alerta publicada a RabbitMQ",
  "data": {
    "severity": "CRITICA",
    "hasAnomalies": true,
    "anomaliesCount": 6,
    "alertPublished": true,
    "anomalies": [
      {
        "tipo": "CRITICA",
        "parametro": "Frecuencia Cardíaca",
        "valorActual": "150 lpm",
        "rangoNormal": "60-100 lpm"
      },
      {
        "tipo": "CRITICA",
        "parametro": "Presión Sistólica",
        "valorActual": "180 mmHg",
        "rangoNormal": "90-120 mmHg"
      },
      {
        "tipo": "CRITICA",
        "parametro": "Presión Diastólica",
        "valorActual": "110 mmHg",
        "rangoNormal": "60-80 mmHg"
      },
      {
        "tipo": "CRITICA",
        "parametro": "Temperatura",
        "valorActual": "39.5 °C",
        "rangoNormal": "36.0-37.5 °C"
      },
      {
        "tipo": "CRITICA",
        "parametro": "Saturación O2",
        "valorActual": "85 %",
        "rangoNormal": "95-100 %"
      },
      {
        "tipo": "CRITICA",
        "parametro": "Frecuencia Respiratoria",
        "valorActual": "30 rpm",
        "rangoNormal": "12-20 rpm"
      }
    ]
  }
}
```

---

### 3️⃣ PRODUCTOR 2 - GENERADOR DE RESÚMENES

#### 📈 Generar Resumen Manual
```http
POST https://vitalwatch-producer-summary.graycoast-{id}.southcentralus.azurecontainerapps.io/api/v1/summary/generate
```

**Respuesta esperada:**
```json
{
  "code": "200",
  "message": "Resumen generado y publicado a RabbitMQ",
  "data": {
    "summaryType": "PERIODIC_SUMMARY",
    "totalPacientes": 9,
    "pacientesCriticos": 3,
    "pacientesMonitoreados": 9,
    "alertasGeneradas": 13,
    "alertasCriticas": 5,
    "promedioFrecuenciaCardiaca": 76.29,
    "promedioTemperatura": 37.00,
    "promedioSaturacionOxigeno": 95.67,
    "pacientesStatus": [
      {
        "pacienteId": 1,
        "pacienteNombre": "Juan Pérez",
        "estado": "CRÍTICO",
        "alertasActivas": 2
      },
      {
        "pacienteId": 2,
        "pacienteNombre": "María García",
        "estado": "ESTABLE",
        "alertasActivas": 0
      }
    ]
  }
}
```

---

#### 📊 Obtener Estadísticas del Generador
```http
GET https://vitalwatch-producer-summary.graycoast-{id}.southcentralus.azurecontainerapps.io/api/v1/summary/stats
```

**Respuesta esperada:**
```json
{
  "totalSummariesGenerated": 15,
  "lastGeneratedAt": "2026-02-13T10:30:00Z",
  "schedulerActive": true,
  "intervalMinutes": 5
}
```

---

## 🎬 ORDEN RECOMENDADO PARA LA PRESENTACIÓN

### **Presentación Larga (8-10 minutos):**

1. ✅ **Health Check Productor 1** → Verificar que está UP
2. ✅ **Health Check Productor 2** → Verificar que está UP
3. 📊 **POST Signos Vitales Normales** → Demostrar que NO genera alerta
4. 🚨 **POST Signos Vitales Críticos** → Demostrar generación de alerta
5. 📈 **POST Generar Resumen** → Demostrar resumen del sistema
6. 📊 **GET Stats** (opcional) → Mostrar estadísticas del generador

### **Presentación Corta (5-6 minutos):**

1. ✅ **Health Check Productor 1**
2. 🚨 **POST Signos Vitales Críticos** (ir directo a la demo importante)
3. 📈 **POST Generar Resumen**

---

## 📋 COLECCIÓN DE POSTMAN - ESTRUCTURA RECOMENDADA

```
📁 VitalWatch RabbitMQ - Azure Production
│
├── 📁 1. Health Checks
│   ├── GET Health Check - Productor 1 (Anomaly)
│   └── GET Health Check - Productor 2 (Summary)
│
├── 📁 2. Productor 1 - Anomaly Detector
│   ├── POST Verificar Signos Vitales - NORMALES
│   └── POST Verificar Signos Vitales - CRÍTICOS ⭐ (IMPORTANTE)
│
└── 📁 3. Productor 2 - Summary Generator
    ├── POST Generar Resumen Manual ⭐ (IMPORTANTE)
    └── GET Obtener Estadísticas
```

---

## 🔧 VARIABLES DE POSTMAN

### Ambiente: "Azure Production"

| Variable | Valor |
|----------|-------|
| `base_url_anomaly` | `https://vitalwatch-producer-anomaly.graycoast-{id}.southcentralus.azurecontainerapps.io` |
| `base_url_summary` | `https://vitalwatch-producer-summary.graycoast-{id}.southcentralus.azurecontainerapps.io` |
| `api_version` | `v1` |

### Uso en Requests:
```
GET {{base_url_anomaly}}/api/{{api_version}}/vital-signs/health
POST {{base_url_anomaly}}/api/{{api_version}}/vital-signs/check
POST {{base_url_summary}}/api/{{api_version}}/summary/generate
```

---

## 🎯 RANGOS CLÍNICOS (Para referencia)

Úsalos para explicar qué es "normal" vs "crítico":

| Parámetro | Normal | Crítico |
|-----------|--------|---------|
| **Frecuencia Cardíaca** | 60-100 lpm | <40 o >120 lpm |
| **Presión Sistólica** | 90-120 mmHg | <70 o >160 mmHg |
| **Presión Diastólica** | 60-80 mmHg | <40 o >100 mmHg |
| **Temperatura** | 36.0-37.5 °C | <35.0 o >39.5 °C |
| **Saturación O2** | 95-100% | <90% |
| **Frecuencia Respiratoria** | 12-20 rpm | <8 o >25 rpm |

---

## 🧪 EJEMPLOS DE CASOS PARA PROBAR

### Caso 1: Paciente Estable ✅
```json
{
  "pacienteId": 1,
  "pacienteNombre": "Juan Pérez",
  "sala": "UCI-A",
  "cama": "101",
  "frecuenciaCardiaca": 75,
  "presionSistolica": 120,
  "presionDiastolica": 80,
  "temperatura": 36.5,
  "saturacionOxigeno": 98,
  "frecuenciaRespiratoria": 16,
  "deviceId": "DEVICE-001"
}
```
**Resultado:** No genera alerta

---

### Caso 2: Taquicardia Severa 🚨
```json
{
  "pacienteId": 2,
  "pacienteNombre": "María García",
  "sala": "UCI-A",
  "cama": "102",
  "frecuenciaCardiaca": 150,
  "presionSistolica": 120,
  "presionDiastolica": 80,
  "temperatura": 36.5,
  "saturacionOxigeno": 98,
  "frecuenciaRespiratoria": 16,
  "deviceId": "DEVICE-002"
}
```
**Resultado:** 1 alerta (frecuencia cardíaca)

---

### Caso 3: Múltiples Anomalías (Para la Demo) ⭐
```json
{
  "pacienteId": 3,
  "pacienteNombre": "Carlos López",
  "sala": "UCI-B",
  "cama": "203",
  "frecuenciaCardiaca": 150,
  "presionSistolica": 180,
  "presionDiastolica": 110,
  "temperatura": 39.5,
  "saturacionOxigeno": 85,
  "frecuenciaRespiratoria": 30,
  "deviceId": "DEVICE-003"
}
```
**Resultado:** 6 alertas (¡CASO CRÍTICO!)

---

### Caso 4: Hipoxemia Crítica 🚨
```json
{
  "pacienteId": 4,
  "pacienteNombre": "Ana Martínez",
  "sala": "UCI-C",
  "cama": "305",
  "frecuenciaCardiaca": 75,
  "presionSistolica": 120,
  "presionDiastolica": 80,
  "temperatura": 36.5,
  "saturacionOxigeno": 85,
  "frecuenciaRespiratoria": 16,
  "deviceId": "DEVICE-004"
}
```
**Resultado:** 1 alerta crítica (saturación de oxígeno peligrosa)

---

## ✅ CHECKLIST PRE-PRESENTACIÓN

### Verificar en Postman:
- [ ] Ambiente "Azure Production" creado y seleccionado
- [ ] Variables configuradas con URLs reales de Azure
- [ ] Request 1: Health Check Productor 1 → probado y funciona
- [ ] Request 2: Health Check Productor 2 → probado y funciona
- [ ] Request 3: POST Signos Normales → probado y funciona
- [ ] Request 4: POST Signos Críticos → probado y funciona (⭐ IMPORTANTE)
- [ ] Request 5: POST Generar Resumen → probado y funciona
- [ ] Request 6: GET Stats → probado y funciona

### Verificar en Azure:
- [ ] Container Apps están "Running"
- [ ] URLs públicas son accesibles
- [ ] Logs están disponibles en Azure Portal

### Verificar en Oracle Cloud:
- [ ] Tabla ALERTAS_MQ existe
- [ ] Query de verificación preparada

---

## 🎤 PUNTOS CLAVE PARA MENCIONAR

Durante la presentación, destaca:

1. **"Estamos probando contra servicios desplegados en Microsoft Azure"** → Mostrar URL con `.azurecontainerapps.io`

2. **"Los productores están en Azure, RabbitMQ en Azure, consumidores en Azure, pero la base de datos en Oracle Cloud"** → Integración multi-cloud

3. **"Código 200 significa que el servicio está operativo"** → Al hacer health checks

4. **"Código 201 significa que la alerta fue creada y publicada a RabbitMQ"** → Al detectar anomalías

5. **"El sistema detectó 6 anomalías críticas que requieren atención médica inmediata"** → Al mostrar el resultado

6. **"Todo esto sucede en tiempo real sin intervención manual"** → Enfatizar la automatización

---

## 🆘 PLAN B - Si algo falla

### Si un endpoint no responde:
1. Verificar que el servicio está UP en Azure Portal
2. Usar otro request que sí funcione
3. Explicar: "En un ambiente real de producción, tendríamos reintentos automáticos"

### Si Postman tiene problemas:
1. Tener la colección exportada como backup
2. Usar curl desde terminal como alternativa
3. Tener screenshots de resultados exitosos previos

### Comandos curl de backup:

```bash
# Health Check Productor 1
curl https://vitalwatch-producer-anomaly.graycoast-{id}.southcentralus.azurecontainerapps.io/api/v1/vital-signs/health

# POST Signos Críticos
curl -X POST \
  https://vitalwatch-producer-anomaly.graycoast-{id}.southcentralus.azurecontainerapps.io/api/v1/vital-signs/check \
  -H "Content-Type: application/json" \
  -d '{
    "pacienteId": 2,
    "pacienteNombre": "María García",
    "sala": "UCI-A",
    "cama": "102",
    "frecuenciaCardiaca": 150,
    "presionSistolica": 180,
    "presionDiastolica": 110,
    "temperatura": 39.5,
    "saturacionOxigeno": 85,
    "frecuenciaRespiratoria": 30,
    "deviceId": "DEVICE-002"
  }'
```

---

## 📊 MÉTRICAS ESPERADAS

| Métrica | Valor Esperado |
|---------|----------------|
| Tiempo de respuesta health checks | 50-200 ms |
| Tiempo de respuesta POST signos vitales | 100-500 ms |
| Tiempo de respuesta POST resumen | 200-800 ms |
| Código HTTP health checks | 200 |
| Código HTTP signos normales | 200 |
| Código HTTP signos críticos | 201 |
| Código HTTP generar resumen | 200 |

---

## 🎓 TIPS FINALES

1. **Practica el orden** de los requests al menos 2 veces antes de grabar
2. **Lee en voz alta** los códigos de respuesta y mensajes importantes
3. **Menciona las URLs de Azure** para demostrar que está en la nube
4. **Explica qué significa cada anomalía** (ej: "150 lpm es taquicardia severa")
5. **Destaca la integración multi-cloud** (Azure + Oracle Cloud)

---

**¡Buena suerte con tu presentación! 🚀**

---

**Documento creado:** Febrero 2026  
**Versión:** 1.0  
**Autor:** Sistema de Documentación VitalWatch
