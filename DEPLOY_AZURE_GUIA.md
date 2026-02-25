# 🚀 Guía Completa: Deploy VitalWatch Kafka a Azure

## 📋 Información de la Rama

- **Rama GitHub:** `feature/kafka-implementation`
- **Repositorio:** https://github.com/sbricenoi/vitalwatch
- **Pull Request:** https://github.com/sbricenoi/vitalwatch/pull/new/feature/kafka-implementation
- **Commit:** 8818bad - "feat: Implementación completa de Apache Kafka para VitalWatch"
- **Archivos:** 74 archivos nuevos/modificados
- **Líneas de código:** +9,913

---

## ✅ ESTADO ACTUAL

### Local (FUNCIONANDO)
- ✅ Sistema Kafka completamente operativo
- ✅ 3 Zookeepers + 3 Kafka Brokers
- ✅ 4 Microservicios Spring Boot
- ✅ Generando datos en tiempo real (1 msg/s)
- ✅ Kafka UI en http://localhost:9000
- ✅ APIs en puertos 8091-8094

### GitHub (SUBIDO)
- ✅ Rama `feature/kafka-implementation` creada
- ✅ Todo el código subido
- ✅ Listo para deploy

---

## 🎯 OPCIONES DE DEPLOY A AZURE

### Opción 1: Azure Container Apps (Recomendado ⭐)

**Ventajas:**
- ✅ Serverless, escalado automático
- ✅ Pago por uso
- ✅ Integración nativa con Azure Event Hubs (Kafka compatible)
- ✅ Fácil de configurar
- ✅ Script automatizado disponible

**Arquitectura:**
```
Azure Container Registry (ACR)
  ↓
Azure Container Apps
  ├── Stream Generator
  ├── Alert Processor
  ├── Database Saver
  └── Summary Generator
  ↓
Azure Event Hubs (Kafka API)
  ↓
Oracle Cloud Database
```

**Script disponible:** `deploy-kafka-azure.sh`

### Opción 2: Azure Kubernetes Service (AKS)

**Ventajas:**
- ✅ Control total del cluster Kafka
- ✅ 3 Zookeepers + 3 Kafka Brokers
- ✅ Kafka UI incluido
- ✅ Mayor flexibilidad

**Desventajas:**
- ⚠️ Más complejo de configurar
- ⚠️ Más costoso (siempre activo)
- ⚠️ Requiere gestión de infraestructura

---

## 🚀 DEPLOY CON AZURE CONTAINER APPS (Opción 1)

### Prerrequisitos

```bash
# 1. Azure CLI instalado
az --version

# 2. Login a Azure
az login

# 3. Verificar suscripción
az account show

# 4. (Opcional) Cambiar suscripción
az account set --subscription "TU_SUBSCRIPTION_ID"
```

### Variables a Configurar

Antes de ejecutar el script, revisa y ajusta estas variables en `deploy-kafka-azure.sh`:

```bash
# Configuración general
RESOURCE_GROUP="vitalwatch-kafka-rg"
LOCATION="eastus"
ACR_NAME="vitalwatchkafkaacr"
ENVIRONMENT_NAME="vitalwatch-kafka-env"

# Azure Event Hubs (Kafka)
EVENT_HUB_NAMESPACE="vitalwatch-kafka-ns"
EVENT_HUB_SKU="Standard"

# Oracle Database (tus credenciales)
ORACLE_DB_URL="jdbc:oracle:thin:@(description=(retry_count=20)...)"
ORACLE_DB_USERNAME="ADMIN"
ORACLE_DB_PASSWORD="tu_password_aqui"
```

### Paso 1: Preparar el Script

```bash
# 1. Abrir el script
nano deploy-kafka-azure.sh

# 2. Actualizar las variables (especialmente Oracle password)

# 3. Guardar y salir (Ctrl+X, Y, Enter)
```

### Paso 2: Ejecutar Deploy

```bash
# Dar permisos de ejecución (si no lo tiene)
chmod +x deploy-kafka-azure.sh

# Ejecutar el script
./deploy-kafka-azure.sh
```

### Paso 3: Monitorear el Deploy

El script hará automáticamente:

1. ✅ Crear Resource Group
2. ✅ Crear Azure Container Registry (ACR)
3. ✅ Crear Azure Event Hubs Namespace
4. ✅ Crear 2 Event Hubs (tópicos Kafka)
5. ✅ Crear Azure Container Apps Environment
6. ✅ Build y push de las 4 imágenes Docker a ACR
7. ✅ Deploy de los 4 microservicios
8. ✅ Configurar variables de entorno
9. ✅ Configurar ingress (URLs públicas)

**Tiempo estimado:** 15-20 minutos

### Paso 4: Verificar el Deploy

Después de que el script termine:

```bash
# Ver recursos creados
az group show --name vitalwatch-kafka-rg

# Ver Container Apps
az containerapp list --resource-group vitalwatch-kafka-rg --output table

# Ver URLs de acceso
az containerapp show \
  --name stream-generator \
  --resource-group vitalwatch-kafka-rg \
  --query properties.configuration.ingress.fqdn \
  --output tsv

az containerapp show \
  --name alert-processor \
  --resource-group vitalwatch-kafka-rg \
  --query properties.configuration.ingress.fqdn \
  --output tsv

az containerapp show \
  --name summary-generator \
  --resource-group vitalwatch-kafka-rg \
  --query properties.configuration.ingress.fqdn \
  --output tsv
```

### Paso 5: Probar el Sistema en Azure

```bash
# Variables (reemplazar con tus URLs)
STREAM_URL="https://stream-generator.xxx.azurecontainerapps.io"
ALERT_URL="https://alert-processor.xxx.azurecontainerapps.io"
SUMMARY_URL="https://summary-generator.xxx.azurecontainerapps.io"

# Iniciar stream
curl -X POST $STREAM_URL/api/v1/stream/start

# Ver stats del stream
curl $STREAM_URL/api/v1/stream/stats

# Ver stats de alertas
curl $ALERT_URL/api/v1/processor/stats

# Ver resumen diario
curl $SUMMARY_URL/api/v1/summary/today
```

---

## 🛠️ CONFIGURACIÓN MANUAL (Si prefieres hacerlo paso a paso)

### 1. Crear Resource Group

```bash
az group create \
  --name vitalwatch-kafka-rg \
  --location eastus
```

### 2. Crear Azure Container Registry

```bash
az acr create \
  --resource-group vitalwatch-kafka-rg \
  --name vitalwatchkafkaacr \
  --sku Basic \
  --admin-enabled true
```

### 3. Login a ACR

```bash
az acr login --name vitalwatchkafkaacr
```

### 4. Build y Push de Imágenes

```bash
# Stream Generator
docker build -t vitalwatchkafkaacr.azurecr.io/stream-generator:latest ./producer-stream-generator
docker push vitalwatchkafkaacr.azurecr.io/stream-generator:latest

# Alert Processor
docker build -t vitalwatchkafkaacr.azurecr.io/alert-processor:latest ./producer-alert-processor
docker push vitalwatchkafkaacr.azurecr.io/alert-processor:latest

# Database Saver
docker build -t vitalwatchkafkaacr.azurecr.io/database-saver:latest ./consumer-database-saver
docker push vitalwatchkafkaacr.azurecr.io/database-saver:latest

# Summary Generator
docker build -t vitalwatchkafkaacr.azurecr.io/summary-generator:latest ./consumer-summary-generator
docker push vitalwatchkafkaacr.azurecr.io/summary-generator:latest
```

### 5. Crear Azure Event Hubs

```bash
# Crear namespace
az eventhubs namespace create \
  --name vitalwatch-kafka-ns \
  --resource-group vitalwatch-kafka-rg \
  --location eastus \
  --sku Standard

# Crear Event Hub para signos vitales
az eventhubs eventhub create \
  --name signos-vitales-stream \
  --resource-group vitalwatch-kafka-rg \
  --namespace-name vitalwatch-kafka-ns \
  --partition-count 3 \
  --message-retention 7

# Crear Event Hub para alertas
az eventhubs eventhub create \
  --name alertas-medicas \
  --resource-group vitalwatch-kafka-rg \
  --namespace-name vitalwatch-kafka-ns \
  --partition-count 3 \
  --message-retention 30

# Obtener connection string
az eventhubs namespace authorization-rule keys list \
  --resource-group vitalwatch-kafka-rg \
  --namespace-name vitalwatch-kafka-ns \
  --name RootManageSharedAccessKey \
  --query primaryConnectionString \
  --output tsv
```

### 6. Crear Container Apps Environment

```bash
az containerapp env create \
  --name vitalwatch-kafka-env \
  --resource-group vitalwatch-kafka-rg \
  --location eastus
```

### 7. Deploy de Microservicios

#### Stream Generator

```bash
az containerapp create \
  --name stream-generator \
  --resource-group vitalwatch-kafka-rg \
  --environment vitalwatch-kafka-env \
  --image vitalwatchkafkaacr.azurecr.io/stream-generator:latest \
  --registry-server vitalwatchkafkaacr.azurecr.io \
  --registry-username $(az acr credential show -n vitalwatchkafkaacr --query username -o tsv) \
  --registry-password $(az acr credential show -n vitalwatchkafkaacr --query passwords[0].value -o tsv) \
  --target-port 8080 \
  --ingress external \
  --env-vars \
    SPRING_PROFILES_ACTIVE=docker \
    KAFKA_BOOTSTRAP_SERVERS="vitalwatch-kafka-ns.servicebus.windows.net:9093" \
    KAFKA_TOPIC_VITAL_SIGNS=signos-vitales-stream \
    STREAM_GENERATION_ENABLED=true \
    STREAM_INTERVAL_MS=1000
```

#### Alert Processor

```bash
az containerapp create \
  --name alert-processor \
  --resource-group vitalwatch-kafka-rg \
  --environment vitalwatch-kafka-env \
  --image vitalwatchkafkaacr.azurecr.io/alert-processor:latest \
  --registry-server vitalwatchkafkaacr.azurecr.io \
  --registry-username $(az acr credential show -n vitalwatchkafkaacr --query username -o tsv) \
  --registry-password $(az acr credential show -n vitalwatchkafkaacr --query passwords[0].value -o tsv) \
  --target-port 8080 \
  --ingress external \
  --env-vars \
    SPRING_PROFILES_ACTIVE=docker \
    KAFKA_BOOTSTRAP_SERVERS="vitalwatch-kafka-ns.servicebus.windows.net:9093" \
    KAFKA_TOPIC_VITAL_SIGNS=signos-vitales-stream \
    KAFKA_TOPIC_ALERTS=alertas-medicas
```

#### Database Saver

```bash
az containerapp create \
  --name database-saver \
  --resource-group vitalwatch-kafka-rg \
  --environment vitalwatch-kafka-env \
  --image vitalwatchkafkaacr.azurecr.io/database-saver:latest \
  --registry-server vitalwatchkafkaacr.azurecr.io \
  --registry-username $(az acr credential show -n vitalwatchkafkaacr --query username -o tsv) \
  --registry-password $(az acr credential show -n vitalwatchkafkaacr --query passwords[0].value -o tsv) \
  --target-port 8080 \
  --ingress internal \
  --env-vars \
    SPRING_PROFILES_ACTIVE=docker \
    KAFKA_BOOTSTRAP_SERVERS="vitalwatch-kafka-ns.servicebus.windows.net:9093" \
    KAFKA_TOPIC_VITAL_SIGNS=signos-vitales-stream \
    KAFKA_TOPIC_ALERTS=alertas-medicas \
    ORACLE_DB_URL="tu_jdbc_url" \
    ORACLE_DB_USERNAME=ADMIN \
    ORACLE_DB_PASSWORD="tu_password"
```

#### Summary Generator

```bash
az containerapp create \
  --name summary-generator \
  --resource-group vitalwatch-kafka-rg \
  --environment vitalwatch-kafka-env \
  --image vitalwatchkafkaacr.azurecr.io/summary-generator:latest \
  --registry-server vitalwatchkafkaacr.azurecr.io \
  --registry-username $(az acr credential show -n vitalwatchkafkaacr --query username -o tsv) \
  --registry-password $(az acr credential show -n vitalwatchkafkaacr --query passwords[0].value -o tsv) \
  --target-port 8080 \
  --ingress external \
  --env-vars \
    SPRING_PROFILES_ACTIVE=docker \
    ORACLE_DB_URL="tu_jdbc_url" \
    ORACLE_DB_USERNAME=ADMIN \
    ORACLE_DB_PASSWORD="tu_password"
```

---

## 📊 MONITOREO EN AZURE

### Ver Logs

```bash
# Logs del Stream Generator
az containerapp logs show \
  --name stream-generator \
  --resource-group vitalwatch-kafka-rg \
  --follow

# Logs del Alert Processor
az containerapp logs show \
  --name alert-processor \
  --resource-group vitalwatch-kafka-rg \
  --follow
```

### Ver Métricas en Portal Azure

1. Ir a https://portal.azure.com
2. Buscar "vitalwatch-kafka-rg"
3. Seleccionar cada Container App
4. Ver:
   - Logs (bajo "Monitoring")
   - Métricas (CPU, Memoria, Requests)
   - Application Insights (si está configurado)

### Ver Event Hubs

```bash
# Ver mensajes en Event Hub
az eventhubs eventhub show \
  --resource-group vitalwatch-kafka-rg \
  --namespace-name vitalwatch-kafka-ns \
  --name signos-vitales-stream
```

---

## 💰 COSTOS ESTIMADOS (Azure)

### Opción 1: Container Apps + Event Hubs

| Servicio | Costo Mensual Estimado |
|----------|------------------------|
| Azure Container Registry (Basic) | $5 |
| Azure Container Apps (4 apps, consumo mínimo) | $20-40 |
| Azure Event Hubs (Standard, 2 hubs) | $20 |
| Oracle Cloud Database | (Ya tienes) |
| **TOTAL** | **$45-65/mes** |

### Opción 2: AKS + Kafka

| Servicio | Costo Mensual Estimado |
|----------|------------------------|
| AKS (3 nodos Standard_B2s) | $60 |
| Azure Load Balancer | $20 |
| Storage (discos persistentes) | $30 |
| **TOTAL** | **$110/mes** |

**Recomendación:** Usar Container Apps + Event Hubs (más económico)

---

## 🔐 SEGURIDAD

### Variables de Entorno Sensibles

```bash
# Usar Azure Key Vault para secretos
az keyvault create \
  --name vitalwatch-kv \
  --resource-group vitalwatch-kafka-rg \
  --location eastus

# Guardar password de Oracle
az keyvault secret set \
  --vault-name vitalwatch-kv \
  --name oracle-password \
  --value "tu_password"

# Referenciar en Container App
az containerapp update \
  --name database-saver \
  --resource-group vitalwatch-kafka-rg \
  --set-env-vars \
    ORACLE_DB_PASSWORD=secretref:oracle-password
```

### Connection String de Event Hubs

```bash
# Guardar en Key Vault
az keyvault secret set \
  --vault-name vitalwatch-kv \
  --name eventhub-connection-string \
  --value "$(az eventhubs namespace authorization-rule keys list ...)"
```

---

## 🧹 LIMPIEZA (Eliminar recursos)

### Eliminar todo el Resource Group

```bash
az group delete \
  --name vitalwatch-kafka-rg \
  --yes \
  --no-wait
```

### Eliminar recursos individuales

```bash
# Eliminar Container Apps
az containerapp delete --name stream-generator --resource-group vitalwatch-kafka-rg --yes
az containerapp delete --name alert-processor --resource-group vitalwatch-kafka-rg --yes
az containerapp delete --name database-saver --resource-group vitalwatch-kafka-rg --yes
az containerapp delete --name summary-generator --resource-group vitalwatch-kafka-rg --yes

# Eliminar Event Hubs
az eventhubs namespace delete --name vitalwatch-kafka-ns --resource-group vitalwatch-kafka-rg

# Eliminar ACR
az acr delete --name vitalwatchkafkaacr --resource-group vitalwatch-kafka-rg --yes
```

---

## 📝 CHECKLIST DE DEPLOY

### Antes del Deploy
- [ ] Sistema funcionando localmente
- [ ] Rama subida a GitHub (`feature/kafka-implementation`)
- [ ] Azure CLI instalado y configurado
- [ ] Login a Azure (`az login`)
- [ ] Suscripción verificada
- [ ] Variables configuradas en `deploy-kafka-azure.sh`
- [ ] Oracle Cloud Database accesible desde Azure

### Durante el Deploy
- [ ] Script ejecutado sin errores
- [ ] 4 imágenes Docker creadas y pusheadas a ACR
- [ ] Azure Event Hubs creado
- [ ] 4 Container Apps desplegadas
- [ ] Variables de entorno configuradas

### Después del Deploy
- [ ] URLs de acceso obtenidas
- [ ] Stream Generator iniciado
- [ ] Datos llegando a Event Hubs
- [ ] Alert Processor generando alertas
- [ ] Datos persistiéndose en Oracle
- [ ] Logs verificados (sin errores)
- [ ] Métricas en Portal Azure
- [ ] Pruebas con Postman

---

## 🆘 TROUBLESHOOTING

### Error: "Failed to push image to ACR"

```bash
# Re-login a ACR
az acr login --name vitalwatchkafkaacr

# Verificar credenciales
az acr credential show --name vitalwatchkafkaacr
```

### Error: "Container App not starting"

```bash
# Ver logs detallados
az containerapp logs show \
  --name stream-generator \
  --resource-group vitalwatch-kafka-rg \
  --tail 100

# Verificar variables de entorno
az containerapp show \
  --name stream-generator \
  --resource-group vitalwatch-kafka-rg \
  --query properties.template.containers[0].env
```

### Error: "Cannot connect to Event Hubs"

```bash
# Verificar connection string
az eventhubs namespace authorization-rule keys list \
  --resource-group vitalwatch-kafka-rg \
  --namespace-name vitalwatch-kafka-ns \
  --name RootManageSharedAccessKey

# Verificar firewall rules
az eventhubs namespace network-rule-set show \
  --resource-group vitalwatch-kafka-rg \
  --namespace-name vitalwatch-kafka-ns
```

### Error: "Cannot connect to Oracle"

```bash
# Verificar que Oracle permita conexiones desde Azure
# En Oracle Cloud Console:
# 1. Ir a Autonomous Database
# 2. Network → Access Control List
# 3. Agregar rangos IP de Azure

# Verificar connection string en variables de entorno
az containerapp show \
  --name database-saver \
  --resource-group vitalwatch-kafka-rg \
  --query properties.template.containers[0].env
```

---

## 🎯 PRÓXIMOS PASOS

### Después del Deploy Exitoso

1. **Monitorear sistema** (primeros 30 minutos)
2. **Verificar datos en Oracle**
3. **Probar todas las APIs**
4. **Configurar alertas en Azure Monitor**
5. **Actualizar documentación con URLs de Azure**
6. **Grabar video de presentación**

### Mejoras Futuras

- [ ] Configurar Application Insights
- [ ] Implementar Azure Monitor dashboards
- [ ] Configurar auto-scaling avanzado
- [ ] Agregar Azure API Management
- [ ] Implementar CI/CD con GitHub Actions
- [ ] Agregar tests automatizados
- [ ] Configurar backup de Event Hubs

---

## 📞 SOPORTE

### Comandos Útiles de Azure

```bash
# Ver todos los recursos en el grupo
az resource list --resource-group vitalwatch-kafka-rg --output table

# Ver costos actuales
az consumption usage list --output table

# Ver límites de suscripción
az vm list-usage --location eastus --output table

# Ver actividad reciente
az monitor activity-log list --resource-group vitalwatch-kafka-rg --max-events 20
```

### Documentación Oficial

- [Azure Container Apps](https://learn.microsoft.com/azure/container-apps/)
- [Azure Event Hubs](https://learn.microsoft.com/azure/event-hubs/)
- [Azure Container Registry](https://learn.microsoft.com/azure/container-registry/)
- [Event Hubs for Kafka](https://learn.microsoft.com/azure/event-hubs/event-hubs-for-kafka-ecosystem-overview)

---

**Última actualización:** 2026-02-25  
**Versión:** 1.0  
**Rama:** feature/kafka-implementation  
**Commit:** 8818bad
