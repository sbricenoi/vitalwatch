# Guía de Despliegue - Microservicios RabbitMQ en Azure

## 📋 Requisitos Previos

Antes de ejecutar el despliegue, asegúrate de tener:

✅ **Azure CLI** instalado y configurado  
✅ **Docker Desktop** corriendo  
✅ **Despliegue base** completado (`deploy-azure.sh`)  
✅ **Git** con cambios committed  
✅ **Oracle Wallet** configurado

## 🚀 Despliegue Rápido

### Opción 1: Despliegue Completo Automatizado

```bash
# 1. Asegúrate de estar en la raíz del proyecto
cd "/Users/sbriceno/Documents/DUOC/CLOUDNATIVE/Semana 3 Sumativa 2 v2"

# 2. Verifica que Docker esté corriendo
docker ps

# 3. Ejecuta el script de despliegue
./deploy-rabbitmq-azure.sh
```

El script ejecutará automáticamente:
1. ✅ Construcción de 4 imágenes Docker (linux/amd64)
2. ✅ Push a Azure Container Registry
3. ✅ Despliegue de RabbitMQ Container App
4. ✅ Despliegue de 2 Productores (Anomaly, Summary)
5. ✅ Despliegue de 2 Consumidores (DB, JSON)
6. ✅ Validación de health checks

**Tiempo estimado:** 15-20 minutos

---

### Opción 2: Despliegue Paso a Paso

Si prefieres más control, ejecuta cada paso manualmente:

#### 1. Construir Imágenes Localmente

```bash
# Producer Anomaly Detector
cd producer-anomaly-detector
docker build --platform linux/amd64 -t vitalwatch-producer-anomaly:v1.0.0 .
cd ..

# Producer Summary Generator
cd producer-summary
docker build --platform linux/amd64 -t vitalwatch-producer-summary:v1.0.0 .
cd ..

# Consumer DB Saver
cd consumer-db-saver
docker build --platform linux/amd64 -t vitalwatch-consumer-db:v1.0.0 .
cd ..

# Consumer JSON Generator
cd consumer-json-generator
docker build --platform linux/amd64 -t vitalwatch-consumer-json:v1.0.0 .
cd ..
```

#### 2. Hacer Push a Azure Container Registry

```bash
# Cargar configuración
source azure-config.env

# Login en ACR
az acr login --name $ACR_NAME

# Obtener login server
ACR_LOGIN_SERVER=$(az acr show --name $ACR_NAME --query loginServer --output tsv)

# Tag y Push de cada imagen
docker tag vitalwatch-producer-anomaly:v1.0.0 $ACR_LOGIN_SERVER/vitalwatch-producer-anomaly:latest
docker push $ACR_LOGIN_SERVER/vitalwatch-producer-anomaly:latest

docker tag vitalwatch-producer-summary:v1.0.0 $ACR_LOGIN_SERVER/vitalwatch-producer-summary:latest
docker push $ACR_LOGIN_SERVER/vitalwatch-producer-summary:latest

docker tag vitalwatch-consumer-db:v1.0.0 $ACR_LOGIN_SERVER/vitalwatch-consumer-db:latest
docker push $ACR_LOGIN_SERVER/vitalwatch-consumer-db:latest

docker tag vitalwatch-consumer-json:v1.0.0 $ACR_LOGIN_SERVER/vitalwatch-consumer-json:latest
docker push $ACR_LOGIN_SERVER/vitalwatch-consumer-json:latest
```

#### 3. Desplegar en Azure Container Apps

```bash
# Obtener credenciales de ACR
ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username --output tsv)
ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query passwords[0].value --output tsv)

# Desplegar RabbitMQ
az containerapp create \
    --name vitalwatch-rabbitmq \
    --resource-group $RESOURCE_GROUP \
    --environment $CONTAINERAPPS_ENVIRONMENT \
    --image rabbitmq:3.12-management \
    --target-port 5672 \
    --ingress internal \
    --min-replicas 1 \
    --max-replicas 1 \
    --cpu 1.0 \
    --memory 2.0Gi \
    --env-vars \
        "RABBITMQ_DEFAULT_USER=vitalwatch" \
        "RABBITMQ_DEFAULT_PASS=hospital123"

# Obtener host de RabbitMQ
RABBITMQ_HOST=$(az containerapp show \
    --name vitalwatch-rabbitmq \
    --resource-group $RESOURCE_GROUP \
    --query properties.configuration.ingress.fqdn \
    --output tsv)

# Desplegar Producer Anomaly
az containerapp create \
    --name vitalwatch-producer-anomaly \
    --resource-group $RESOURCE_GROUP \
    --environment $CONTAINERAPPS_ENVIRONMENT \
    --image $ACR_LOGIN_SERVER/vitalwatch-producer-anomaly:latest \
    --registry-server $ACR_LOGIN_SERVER \
    --registry-username $ACR_USERNAME \
    --registry-password $ACR_PASSWORD \
    --target-port 8080 \
    --ingress external \
    --min-replicas 1 \
    --max-replicas 5 \
    --cpu 0.5 \
    --memory 1.0Gi \
    --env-vars \
        "RABBITMQ_HOST=$RABBITMQ_HOST" \
        "RABBITMQ_PORT=5672" \
        "RABBITMQ_USERNAME=vitalwatch" \
        "RABBITMQ_PASSWORD=hospital123" \
        "SPRING_PROFILES_ACTIVE=azure"

# Desplegar Producer Summary
az containerapp create \
    --name vitalwatch-producer-summary \
    --resource-group $RESOURCE_GROUP \
    --environment $CONTAINERAPPS_ENVIRONMENT \
    --image $ACR_LOGIN_SERVER/vitalwatch-producer-summary:latest \
    --registry-server $ACR_LOGIN_SERVER \
    --registry-username $ACR_USERNAME \
    --registry-password $ACR_PASSWORD \
    --target-port 8080 \
    --ingress external \
    --min-replicas 1 \
    --max-replicas 3 \
    --cpu 0.5 \
    --memory 1.0Gi \
    --env-vars \
        "RABBITMQ_HOST=$RABBITMQ_HOST" \
        "RABBITMQ_PORT=5672" \
        "RABBITMQ_USERNAME=vitalwatch" \
        "RABBITMQ_PASSWORD=hospital123" \
        "SPRING_PROFILES_ACTIVE=azure"

# Desplegar Consumer DB Saver
az containerapp create \
    --name vitalwatch-consumer-db \
    --resource-group $RESOURCE_GROUP \
    --environment $CONTAINERAPPS_ENVIRONMENT \
    --image $ACR_LOGIN_SERVER/vitalwatch-consumer-db:latest \
    --registry-server $ACR_LOGIN_SERVER \
    --registry-username $ACR_USERNAME \
    --registry-password $ACR_PASSWORD \
    --ingress internal \
    --min-replicas 1 \
    --max-replicas 5 \
    --cpu 0.5 \
    --memory 1.0Gi \
    --env-vars \
        "RABBITMQ_HOST=$RABBITMQ_HOST" \
        "RABBITMQ_PORT=5672" \
        "RABBITMQ_USERNAME=vitalwatch" \
        "RABBITMQ_PASSWORD=hospital123" \
        "ORACLE_USERNAME=$ORACLE_USERNAME" \
        "ORACLE_PASSWORD=$ORACLE_PASSWORD" \
        "TNS_ADMIN=/app/wallet" \
        "SPRING_PROFILES_ACTIVE=azure"

# Desplegar Consumer JSON Generator
az containerapp create \
    --name vitalwatch-consumer-json \
    --resource-group $RESOURCE_GROUP \
    --environment $CONTAINERAPPS_ENVIRONMENT \
    --image $ACR_LOGIN_SERVER/vitalwatch-consumer-json:latest \
    --registry-server $ACR_LOGIN_SERVER \
    --registry-username $ACR_USERNAME \
    --registry-password $ACR_PASSWORD \
    --ingress internal \
    --min-replicas 1 \
    --max-replicas 3 \
    --cpu 0.25 \
    --memory 0.5Gi \
    --env-vars \
        "RABBITMQ_HOST=$RABBITMQ_HOST" \
        "RABBITMQ_PORT=5672" \
        "RABBITMQ_USERNAME=vitalwatch" \
        "RABBITMQ_PASSWORD=hospital123" \
        "SPRING_PROFILES_ACTIVE=azure"
```

---

## ✅ Verificación del Despliegue

### 1. Verificar Estado de Container Apps

```bash
# Ver todos los Container Apps de RabbitMQ
az containerapp list \
    --resource-group $RESOURCE_GROUP \
    --query "[?contains(name, 'rabbitmq') || contains(name, 'producer') || contains(name, 'consumer')].{Name:name, Status:properties.runningStatus}" \
    --output table
```

### 2. Health Checks de Productores

```bash
# Obtener URLs
PRODUCER_ANOMALY_URL=$(az containerapp show \
    --name vitalwatch-producer-anomaly \
    --resource-group $RESOURCE_GROUP \
    --query properties.configuration.ingress.fqdn \
    --output tsv)

PRODUCER_SUMMARY_URL=$(az containerapp show \
    --name vitalwatch-producer-summary \
    --resource-group $RESOURCE_GROUP \
    --query properties.configuration.ingress.fqdn \
    --output tsv)

# Probar health checks
curl https://$PRODUCER_ANOMALY_URL/api/v1/vital-signs/health | jq
curl https://$PRODUCER_SUMMARY_URL/api/v1/summary/health | jq
```

### 3. Verificar Logs

```bash
# Logs de Producer Anomaly
az containerapp logs show \
    --name vitalwatch-producer-anomaly \
    --resource-group $RESOURCE_GROUP \
    --follow

# Logs de Consumer DB
az containerapp logs show \
    --name vitalwatch-consumer-db \
    --resource-group $RESOURCE_GROUP \
    --follow

# Logs de RabbitMQ
az containerapp logs show \
    --name vitalwatch-rabbitmq \
    --resource-group $RESOURCE_GROUP \
    --tail 100
```

---

## 🧪 Pruebas de Funcionalidad

### 1. Enviar Signos Vitales Normales

```bash
curl -X POST https://$PRODUCER_ANOMALY_URL/api/v1/vital-signs/check \
  -H "Content-Type: application/json" \
  -d '{
    "pacienteId": 1,
    "pacienteNombre": "Juan Pérez Azure",
    "sala": "UCI-Azure",
    "cama": "A-01",
    "frecuenciaCardiaca": 75,
    "presionSistolica": 120,
    "presionDiastolica": 80,
    "temperatura": 36.5,
    "saturacionOxigeno": 98,
    "frecuenciaRespiratoria": 16,
    "deviceId": "AZURE-DEVICE-001"
  }' | jq
```

**Respuesta Esperada:** Sin anomalías detectadas

### 2. Enviar Signos Vitales Críticos

```bash
curl -X POST https://$PRODUCER_ANOMALY_URL/api/v1/vital-signs/check \
  -H "Content-Type: application/json" \
  -d '{
    "pacienteId": 2,
    "pacienteNombre": "María García Azure",
    "sala": "UCI-Azure",
    "cama": "A-02",
    "frecuenciaCardiaca": 160,
    "presionSistolica": 190,
    "presionDiastolica": 120,
    "temperatura": 40.0,
    "saturacionOxigeno": 80,
    "frecuenciaRespiratoria": 35,
    "deviceId": "AZURE-DEVICE-002"
  }' | jq
```

**Respuesta Esperada:** 6 anomalías críticas detectadas + alerta publicada a RabbitMQ

### 3. Generar Resumen Manual

```bash
curl -X POST https://$PRODUCER_SUMMARY_URL/api/v1/summary/generate | jq
```

### 4. Verificar Persistencia en Oracle

Verifica que las alertas se guardaron en la tabla `ALERTAS_MQ`:

```sql
-- En Oracle SQL Developer o SQL*Plus
SELECT 
    ID,
    PACIENTE_NOMBRE,
    SEVERITY,
    ANOMALIAS_COUNT,
    DETECTED_AT,
    SAVED_AT
FROM ALERTAS_MQ
ORDER BY SAVED_AT DESC
FETCH FIRST 10 ROWS ONLY;
```

---

## 🔧 Comandos Útiles de Administración

### Escalar Servicios

```bash
# Escalar Producer Anomaly
az containerapp update \
    --name vitalwatch-producer-anomaly \
    --resource-group $RESOURCE_GROUP \
    --min-replicas 2 \
    --max-replicas 10

# Escalar Consumer DB
az containerapp update \
    --name vitalwatch-consumer-db \
    --resource-group $RESOURCE_GROUP \
    --min-replicas 2 \
    --max-replicas 8
```

### Actualizar Imagen

```bash
# Rebuild local
cd producer-anomaly-detector
docker build --platform linux/amd64 -t vitalwatch-producer-anomaly:v1.0.1 .

# Push a ACR
docker tag vitalwatch-producer-anomaly:v1.0.1 $ACR_LOGIN_SERVER/vitalwatch-producer-anomaly:latest
docker push $ACR_LOGIN_SERVER/vitalwatch-producer-anomaly:latest

# Actualizar Container App
az containerapp update \
    --name vitalwatch-producer-anomaly \
    --resource-group $RESOURCE_GROUP \
    --image $ACR_LOGIN_SERVER/vitalwatch-producer-anomaly:latest
```

### Reiniciar Servicio

```bash
# Reiniciar Producer Anomaly
az containerapp revision restart \
    --name vitalwatch-producer-anomaly \
    --resource-group $RESOURCE_GROUP \
    --revision $(az containerapp revision list \
        --name vitalwatch-producer-anomaly \
        --resource-group $RESOURCE_GROUP \
        --query "[0].name" -o tsv)
```

### Ver Métricas

```bash
# CPU y Memoria
az containerapp show \
    --name vitalwatch-producer-anomaly \
    --resource-group $RESOURCE_GROUP \
    --query "properties.template.containers[0].resources"
```

---

## 🐛 Troubleshooting

### Error: "Image pull failed"

```bash
# Verificar que la imagen existe en ACR
az acr repository show \
    --name $ACR_NAME \
    --repository vitalwatch-producer-anomaly

# Verificar credenciales de ACR
az acr credential show --name $ACR_NAME
```

### Error: "Health check failed"

```bash
# Ver logs detallados
az containerapp logs show \
    --name vitalwatch-producer-anomaly \
    --resource-group $RESOURCE_GROUP \
    --tail 200

# Verificar variables de entorno
az containerapp show \
    --name vitalwatch-producer-anomaly \
    --resource-group $RESOURCE_GROUP \
    --query "properties.template.containers[0].env"
```

### Error: "Cannot connect to RabbitMQ"

```bash
# Verificar que RabbitMQ está corriendo
az containerapp show \
    --name vitalwatch-rabbitmq \
    --resource-group $RESOURCE_GROUP \
    --query "properties.runningStatus"

# Ver logs de RabbitMQ
az containerapp logs show \
    --name vitalwatch-rabbitmq \
    --resource-group $RESOURCE_GROUP \
    --tail 100
```

### Consumer no procesa mensajes

```bash
# Verificar logs del consumer
az containerapp logs show \
    --name vitalwatch-consumer-db \
    --resource-group $RESOURCE_GROUP \
    --follow

# Verificar conexión a Oracle
# Buscar en logs: "HikariPool-1 - Starting..."
```

---

## 💰 Costos Estimados

**Configuración Base (por mes):**
- Container Apps Environment: ~$0
- RabbitMQ (1 replica, 2GB RAM): ~$60
- Producer Anomaly (1-5 replicas): ~$40-200
- Producer Summary (1-3 replicas): ~$30-90
- Consumer DB (1-5 replicas): ~$40-200
- Consumer JSON (1-3 replicas): ~$20-60
- **Total estimado:** ~$190-610/mes

**Nota:** Los costos son aproximados y dependen del uso real, región de Azure y scaling configurado.

---

## 📚 Referencias

- [README_RABBITMQ.md](./README_RABBITMQ.md) - Documentación completa de RabbitMQ
- [TESTING_RABBITMQ.md](./TESTING_RABBITMQ.md) - Plan de pruebas
- [RESULTADOS_PRUEBAS_RABBITMQ.md](./docs/RESULTADOS_PRUEBAS_RABBITMQ.md) - Resultados de pruebas locales
- [Azure Container Apps Documentation](https://learn.microsoft.com/en-us/azure/container-apps/)

---

**Última actualización:** Febrero 2026  
**Versión:** 1.0.0
