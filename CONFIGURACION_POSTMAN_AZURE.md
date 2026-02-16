# 🔧 CONFIGURACIÓN DE POSTMAN PARA AZURE

## 📌 Guía Rápida para Configurar Ambiente Azure en Postman

---

## 1️⃣ Crear Ambiente "Azure Production"

### Pasos:

1. Abrir Postman
2. Click en "Environments" (esquina superior derecha)
3. Click en "+" para crear nuevo ambiente
4. Nombrar: **"Azure Production"**

---

## 2️⃣ Configurar Variables del Ambiente

### Variables a Crear:

| Variable | Valor Inicial | Descripción |
|----------|---------------|-------------|
| `base_url_anomaly` | `https://vitalwatch-producer-anomaly.graycoast-xxxxx.southcentralus.azurecontainerapps.io` | URL del Productor 1 en Azure |
| `base_url_summary` | `https://vitalwatch-producer-summary.graycoast-xxxxx.southcentralus.azurecontainerapps.io` | URL del Productor 2 en Azure |
| `api_version` | `v1` | Versión de la API |

**NOTA:** Reemplaza `xxxxx` con el ID real de tu Azure Container Apps Environment.

---

## 3️⃣ Actualizar Requests en la Colección

### Request 1: Health Check Productor 1
```
GET {{base_url_anomaly}}/api/{{api_version}}/vital-signs/health
```

### Request 2: Health Check Productor 2
```
GET {{base_url_summary}}/api/{{api_version}}/summary/health
```

### Request 3: Enviar Signos Vitales Normales
```
POST {{base_url_anomaly}}/api/{{api_version}}/vital-signs/check
```

**Body (JSON):**
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

### Request 4: Enviar Signos Vitales Críticos
```
POST {{base_url_anomaly}}/api/{{api_version}}/vital-signs/check
```

**Body (JSON):**
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

### Request 5: Generar Resumen Manual
```
POST {{base_url_summary}}/api/{{api_version}}/summary/generate
```

### Request 6: Obtener Estadísticas
```
GET {{base_url_summary}}/api/{{api_version}}/summary/stats
```

---

## 4️⃣ Obtener las URLs Reales de Azure

### Opción A: Desde Azure Portal

1. Ir a https://portal.azure.com
2. Navegar a **Resource Groups** > `rg-vitalwatch-rabbitmq-prod`
3. Click en **vitalwatch-producer-anomaly**
4. Copiar la **Application Url** (ejemplo: `https://vitalwatch-producer-anomaly.graycoast-fc35a2d0.southcentralus.azurecontainerapps.io`)
5. Repetir para **vitalwatch-producer-summary**

### Opción B: Desde Azure CLI

```bash
# Obtener URL del Productor 1
az containerapp show \
  --name vitalwatch-producer-anomaly \
  --resource-group rg-vitalwatch-rabbitmq-prod \
  --query properties.configuration.ingress.fqdn \
  --output tsv

# Obtener URL del Productor 2
az containerapp show \
  --name vitalwatch-producer-summary \
  --resource-group rg-vitalwatch-rabbitmq-prod \
  --query properties.configuration.ingress.fqdn \
  --output tsv
```

---

## 5️⃣ Verificar Configuración

### Test Rápido:

1. Seleccionar ambiente **"Azure Production"** en Postman
2. Ejecutar: `GET {{base_url_anomaly}}/api/v1/vital-signs/health`
3. **Resultado esperado:** 
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

## 6️⃣ Carpetas de la Colección

### Organización Recomendada:

```
📁 VitalWatch RabbitMQ - Azure
├── 📁 1. Health Checks
│   ├── ✅ Health Check - Productor Anomaly
│   └── ✅ Health Check - Productor Summary
├── 📁 2. Productor Anomaly Detector
│   ├── ✉️ POST - Signos Vitales Normales
│   └── ✉️ POST - Signos Vitales Críticos (Generar Alerta)
└── 📁 3. Productor Summary Generator
    ├── ✉️ POST - Generar Resumen Manual
    └── ✅ GET - Obtener Estadísticas
```

---

## 7️⃣ Tips para la Presentación

### ✅ Antes de grabar:

- [ ] Verificar que el ambiente "Azure Production" está seleccionado
- [ ] Probar cada request al menos una vez
- [ ] Verificar que las URLs de Azure están correctas
- [ ] Guardar los cambios en la colección

### ✅ Durante la presentación:

- Mostrar claramente que las URLs son de Azure (`.azurecontainerapps.io`)
- Mencionar la región: `southcentralus`
- Destacar que los servicios están en la nube, no en localhost
- Resaltar los códigos de respuesta HTTP exitosos (200, 201)

---

## 8️⃣ Solución de Problemas

### Error: "Could not get response"

**Posibles causas:**
- URLs incorrectas (verificar en Azure Portal)
- Servicios detenidos en Azure (verificar estado en Portal)
- Problemas de red/firewall

**Solución:**
```bash
# Verificar que los servicios están corriendo
az containerapp list \
  --resource-group rg-vitalwatch-rabbitmq-prod \
  --query "[].{Name:name, Status:properties.runningStatus}" \
  --output table
```

### Error: "Timeout"

**Solución:**
- Aumentar el timeout en Postman: Settings > General > Request timeout: 30000 ms
- Verificar que el servicio no está en "cold start" (primera request puede tardar más)

---

## 📊 Respuestas Esperadas

### Health Check Exitoso:
```json
{
  "code": "200",
  "message": "Productor operativo",
  "data": {
    "service": "Anomaly Detector Producer",
    "status": "UP",
    "timestamp": "2026-02-13T10:30:00Z"
  }
}
```

### Signos Vitales Normales:
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

### Signos Vitales Críticos (Alerta Generada):
```json
{
  "code": "201",
  "message": "Anomalías detectadas. Alerta publicada a RabbitMQ",
  "data": {
    "severity": "CRITICA",
    "hasAnomalies": true,
    "anomaliesCount": 6,
    "alertPublished": true,
    "anomalies": [...]
  }
}
```

---

## ✨ Checklist Final

- [ ] Ambiente "Azure Production" creado
- [ ] Variables configuradas con URLs reales de Azure
- [ ] Todos los requests actualizados con variables
- [ ] Requests probados y funcionando
- [ ] Colección guardada
- [ ] Ambiente seleccionado en Postman
- [ ] Listo para la presentación

---

**Fecha de creación:** Febrero 2026
**Versión:** 1.0
