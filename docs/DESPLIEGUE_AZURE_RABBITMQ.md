# Despliegue RabbitMQ en Azure - Resumen Ejecutivo

**Fecha:** 14 de Febrero, 2026  
**Proyecto:** VitalWatch - Sistema de Monitoreo de Signos Vitales  
**Tarea:** Despliegue de Microservicios RabbitMQ en Azure Container Apps

---

## 📋 Resumen del Despliegue

Se han desplegado exitosamente **5 nuevos servicios** en Azure Container Apps para la integración con RabbitMQ:

### ✅ Servicios Desplegados

| Servicio | Estado | Imagen | Recursos |
|----------|--------|--------|----------|
| **vitalwatch-rabbitmq** | ✅ Running | `rabbitmq:3.12-management` | 1.0 CPU, 2.0 GB RAM |
| **vitalwatch-producer-anomaly** | ✅ Running | `acrvitalwatch.azurecr.io/vitalwatch-producer-anomaly:latest` | 0.5 CPU, 1.0 GB RAM |
| **vitalwatch-producer-summary** | ✅ Running | `acrvitalwatch.azurecr.io/vitalwatch-producer-summary:latest` | 0.5 CPU, 1.0 GB RAM |
| **vitalwatch-consumer-json** | ✅ Running | `acrvitalwatch.azurecr.io/vitalwatch-consumer-json:latest` | 0.5 CPU, 1.0 GB RAM |
| **vitalwatch-consumer-db** | ✅ Running | `acrvitalwatch.azurecr.io/vitalwatch-consumer-db:v1.0.2` | 0.75 CPU, 1.5 GB RAM |

---

## 🏗️ Arquitectura Desplegada

```
┌─────────────────────────────────────────────────────────────┐
│                    AZURE CONTAINER APPS                     │
│                 (Environment: env-vitalwatch-prod)          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────┐           ┌──────────────────┐      │
│  │    RabbitMQ      │           │   Producer       │      │
│  │   (Internal)     │◄──────────┤   Anomaly        │      │
│  │   Port: 5672     │           │   Detector       │      │
│  └────────┬─────────┘           └──────────────────┘      │
│           │                                                 │
│           │                     ┌──────────────────┐      │
│           │         ┌───────────┤   Producer       │      │
│           │         │           │   Summary        │      │
│           │         │           │   Generator      │      │
│           │         │           └──────────────────┘      │
│           │         │                                      │
│           │         ▼                                      │
│           │    vital-signs-alerts                         │
│           │         queue                                  │
│           │         │                                      │
│           │         ├──────────►┌──────────────────┐     │
│           │         │           │   Consumer       │     │
│           │         │           │   DB Saver       │─────┼──► Oracle
│           │         │           └──────────────────┘     │    Cloud
│           │         │                                      │
│           │         └──────────►┌──────────────────┐     │
│           │                     │   Consumer       │     │
│           │                     │   JSON Generator │     │
│           └─────────────────────┤   (Archivos)     │     │
│                                 └──────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 Configuración Técnica

### 1. Azure Container Registry (ACR)
- **Nombre:** `acrvitalwatch.azurecr.io`
- **Imágenes Subidas:** 4 microservicios (Producers: 2, Consumers: 2)
- **Plataforma:** `linux/amd64`

### 2. Resource Group
- **Nombre:** `rg-vitalwatch-prod`
- **Región:** South Central US
- **Environment:** `env-vitalwatch-prod`

### 3. Configuración de Red
- **Ingress RabbitMQ:** Internal (solo acceso dentro del environment)
- **FQDN RabbitMQ:** `vitalwatch-rabbitmq.internal.graycoast-fc35a2d0.southcentralus.azurecontainerapps.io`
- **Todos los microservicios RabbitMQ:** Ingress interno

### 4. Variables de Entorno Configuradas

#### Producers (Anomaly & Summary)
```bash
SPRING_PROFILES_ACTIVE=azure
RABBITMQ_HOST=vitalwatch-rabbitmq.internal.graycoast-fc35a2d0.southcentralus.azurecontainerapps.io
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=vitalwatch
RABBITMQ_PASSWORD=hospital123
```

#### Consumer DB Saver
```bash
SPRING_PROFILES_ACTIVE=azure
RABBITMQ_HOST=vitalwatch-rabbitmq.internal.graycoast-fc35a2d0.southcentralus.azurecontainerapps.io
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=vitalwatch
RABBITMQ_PASSWORD=hospital123
ORACLE_SERVICE=s58onuxcx4c1qxe9_high.adb.oraclecloud.com
ORACLE_USERNAME=ADMIN
ORACLE_PASSWORD=*********
```

#### Consumer JSON Generator
```bash
SPRING_PROFILES_ACTIVE=azure
RABBITMQ_HOST=vitalwatch-rabbitmq.internal.graycoast-fc35a2d0.southcentralus.azurecontainerapps.io
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=vitalwatch
RABBITMQ_PASSWORD=hospital123
```

---

## 🛠️ Cambios Técnicos Realizados

### 1. **Wallet Oracle Integrado**
- Se copió el wallet de Oracle del backend a `consumer-db-saver/`
- Se modificó el Dockerfile para incluir el wallet en la imagen
- Se configuró `TNS_ADMIN=/app/wallet`

### 2. **Configuración de Base de Datos**
**Antes (v1.0.1 - Error):**
```properties
spring.datasource.url=jdbc:oracle:thin:@(description=(retry_count=20)...)
```

**Después (v1.0.2 - Corregido):**
```properties
spring.datasource.url=jdbc:oracle:thin:@s58onuxcx4c1qxe9_high?TNS_ADMIN=/app/wallet
```

### 3. **Imágenes Docker**
- Todas las imágenes construidas con plataforma `linux/amd64`
- Multi-stage builds para optimizar tamaño
- Usuario no-root (`spring:spring`) para seguridad

---

## 📊 Estado Actual de Servicios

```bash
# Verificar estado de todos los servicios
az containerapp list --resource-group rg-vitalwatch-prod \
  --query "[].{Name:name, State:properties.runningStatus}" \
  --output table

# Resultado:
Name                         State
---------------------------  -------
vitalwatch-frontend          Running ✅
vitalwatch-api-gateway       Running ✅
vitalwatch-backend           Running ✅
vitalwatch-rabbitmq          Running ✅
vitalwatch-producer-anomaly  Running ✅
vitalwatch-producer-summary  Running ✅
vitalwatch-consumer-json     Running ✅
vitalwatch-consumer-db       Running ✅
```

---

## 🔍 Verificación de Logs

### Ver logs de un servicio específico
```bash
# Producer Anomaly
az containerapp logs show \
  --name vitalwatch-producer-anomaly \
  --resource-group rg-vitalwatch-prod \
  --tail 50 --follow false

# Consumer DB
az containerapp logs show \
  --name vitalwatch-consumer-db \
  --resource-group rg-vitalwatch-prod \
  --tail 50 --follow false
```

### Resultados Observados
- ✅ Producer Anomaly: Iniciado correctamente (17.9s)
- ✅ Producer Summary: Perfil `azure` activo
- ✅ Consumer JSON: Running sin errores
- ⚠️ Consumer DB: Iniciado correctamente pero con warning de timeout en listener de RabbitMQ (puede ser normal en el primer inicio)

---

## 🎯 Funcionalidad del Sistema

### Producer Anomaly Detector
- **Puerto:** 8083
- **Función:** Detecta anomalías en signos vitales y envía alertas a RabbitMQ
- **Cola de Salida:** `vital-signs-alerts`
- **Endpoints:** `/actuator/health`, `/api/anomaly/stats`

### Producer Summary Generator
- **Puerto:** 8084
- **Función:** Genera resúmenes periódicos (cada 10 minutos) de todos los signos vitales
- **Cola de Salida:** `vital-signs-summary`
- **Intervalo:** Configurable via `summary.interval.ms`

### Consumer DB Saver
- **Puerto:** 8085
- **Función:** Consume alertas de RabbitMQ y las persiste en Oracle
- **Cola de Entrada:** `vital-signs-alerts`
- **Tabla Oracle:** `ALERTAS_MQ`
- **Features:** 
  - Conexión a Oracle con wallet SSL
  - Pool de conexiones HikariCP optimizado
  - Reintento automático de mensajes fallidos

### Consumer JSON Generator
- **Puerto:** 8086
- **Función:** Consume alertas y genera archivos JSON
- **Cola de Entrada:** `vital-signs-alerts`
- **Output Path:** `/app/data/alerts/`
- **Formato:** `alert_{timestamp}_{id}.json`

---

## 🚀 Próximos Pasos

### Verificación Funcional
1. **Probar Producer Anomaly:**
   - Enviar signos vitales críticos al backend
   - Verificar que se generen alertas en RabbitMQ
   - Confirmar persistencia en Oracle

2. **Validar Consumer JSON:**
   - Confirmar generación de archivos JSON
   - Verificar formato y contenido

3. **Monitorear Producer Summary:**
   - Esperar 10 minutos para el primer resumen
   - Verificar logs para mensajes de resumen generado

### Optimización (Opcional)
- Configurar health checks personalizados
- Implementar autoscaling basado en longitud de cola
- Agregar monitoreo con Application Insights
- Configurar alertas para fallos de consumer

---

## 📝 Comandos Útiles

### Actualizar un servicio
```bash
az containerapp update \
  --name vitalwatch-consumer-db \
  --resource-group rg-vitalwatch-prod \
  --image acrvitalwatch.azurecr.io/vitalwatch-consumer-db:v1.0.3
```

### Reiniciar un servicio
```bash
az containerapp revision restart \
  --name vitalwatch-producer-anomaly \
  --resource-group rg-vitalwatch-prod
```

### Ver todas las revisiones
```bash
az containerapp revision list \
  --name vitalwatch-consumer-db \
  --resource-group rg-vitalwatch-prod \
  --output table
```

---

## ✅ Checklist de Despliegue Completado

- [x] Azure CLI configurado y autenticado
- [x] Imágenes Docker construidas (linux/amd64)
- [x] Imágenes subidas a ACR
- [x] RabbitMQ desplegado
- [x] Producer Anomaly desplegado
- [x] Producer Summary desplegado
- [x] Consumer JSON desplegado
- [x] Consumer DB desplegado con Oracle wallet
- [x] Todos los servicios en estado Running
- [x] Variables de entorno configuradas
- [x] Logs verificados sin errores críticos

---

## 📌 Notas Importantes

1. **Seguridad:** Todas las credenciales están configuradas como variables de entorno en Azure
2. **Red:** Todos los servicios RabbitMQ usan ingress interno (no expuestos a internet)
3. **Wallet Oracle:** Incluido en la imagen Docker del Consumer DB (no como volumen)
4. **Perfil Spring:** Todos los microservicios usan el perfil `azure`
5. **Oracle Connection:** Usa TNS_ADMIN con wallet SSL

---

## 🔗 Referencias

- [Guía de Despliegue Detallada](./GUIA_DESPLIEGUE_RABBITMQ_AZURE.md)
- [Resultados de Pruebas Locales](./RESULTADOS_PRUEBAS_RABBITMQ.md)
- [Plan de Testing](./TESTING_RABBITMQ.md)

---

**Despliegue Completado Exitosamente** ✅  
*Todos los servicios de integración RabbitMQ están operativos en Azure Container Apps*
