# 🔷 Guía de Despliegue en Azure - VitalWatch

## 📋 Índice

1. [Arquitectura de Despliegue](#-arquitectura-de-despliegue)
2. [Prerequisitos](#-prerequisitos)
3. [Fase 1: Preparación del Entorno](#fase-1-preparación-del-entorno)
4. [Fase 2: Configuración de Azure Container Registry](#fase-2-configuración-de-azure-container-registry)
5. [Fase 3: Construcción y Publicación de Imágenes](#fase-3-construcción-y-publicación-de-imágenes)
6. [Fase 4: Despliegue del Backend](#fase-4-despliegue-del-backend)
7. [Fase 5: Despliegue del API Gateway](#fase-5-despliegue-del-api-gateway)
8. [Fase 6: Despliegue del Frontend](#fase-6-despliegue-del-frontend)
9. [Fase 7: Configuración de Networking](#fase-7-configuración-de-networking)
10. [Fase 8: Configuración de Dominios y SSL](#fase-8-configuración-de-dominios-y-ssl)
11. [Fase 9: Monitoreo y Logging](#fase-9-monitoreo-y-logging)
12. [Fase 10: Pruebas y Validación](#fase-10-pruebas-y-validación)
13. [Gestión y Mantenimiento](#gestión-y-mantenimiento)
14. [Troubleshooting](#troubleshooting)

---

## 🏗️ Arquitectura de Despliegue

### Diagrama de Arquitectura Azure

```
                                    INTERNET
                                        │
                                        ▼
┌──────────────────────────────────────────────────────────────────┐
│                        AZURE CLOUD                                │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              Azure Front Door (CDN + WAF)                 │   │
│  │                  - SSL/TLS Termination                    │   │
│  │                  - DDoS Protection                        │   │
│  │                  - Global Load Balancing                  │   │
│  └────────────────────┬─────────────────────────────────────┘   │
│                       │                                           │
│                       ▼                                           │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │        Azure Container Apps Environment                   │   │
│  │                  (vitalwatch-env)                         │   │
│  │                                                            │   │
│  │   ┌─────────────────────────────────────────────────┐   │   │
│  │   │  Container App: vitalwatch-frontend             │   │   │
│  │   │  - Angular 17 + Nginx                           │   │   │
│  │   │  - Port: 80/443                                 │   │   │
│  │   │  - Auto-scaling: 1-10 replicas                  │   │   │
│  │   │  - URL: vitalwatch-frontend.azurecontainer.io   │   │   │
│  │   └─────────────────────────────────────────────────┘   │   │
│  │                          │                               │   │
│  │                          ▼                               │   │
│  │   ┌─────────────────────────────────────────────────┐   │   │
│  │   │  Container App: vitalwatch-api-gateway          │   │   │
│  │   │  - Kong 3.4                                     │   │   │
│  │   │  - Port: 8000                                   │   │   │
│  │   │  - Auto-scaling: 2-5 replicas                   │   │   │
│  │   │  - URL: vitalwatch-api.azurecontainer.io        │   │   │
│  │   └─────────────────────────────────────────────────┘   │   │
│  │                          │                               │   │
│  │                          ▼                               │   │
│  │   ┌─────────────────────────────────────────────────┐   │   │
│  │   │  Container App: vitalwatch-backend              │   │   │
│  │   │  - Spring Boot 3.2 + Java 17                    │   │   │
│  │   │  - Port: 8080                                   │   │   │
│  │   │  - Auto-scaling: 2-10 replicas                  │   │   │
│  │   │  - URL: vitalwatch-backend.azurecontainer.io    │   │   │
│  │   └─────────────────────────────────────────────────┘   │   │
│  │                          │                               │   │
│  └──────────────────────────┼───────────────────────────────┘   │
│                             │                                    │
│  ┌──────────────────────────┼───────────────────────────────┐   │
│  │     Azure Key Vault      │                               │   │
│  │  - Oracle DB Password    │                               │   │
│  │  - JWT Secret            │                               │   │
│  │  - Oracle Wallet Files   │                               │   │
│  └──────────────────────────┼───────────────────────────────┘   │
│                             │                                    │
│  ┌──────────────────────────┼───────────────────────────────┐   │
│  │  Azure Container Registry│                               │   │
│  │  - vitalwatch-backend    │                               │   │
│  │  - vitalwatch-frontend   │                               │   │
│  │  - vitalwatch-api-gateway│                               │   │
│  └──────────────────────────┼───────────────────────────────┘   │
│                             │                                    │
│  ┌──────────────────────────┼───────────────────────────────┐   │
│  │  Azure Monitor + Log Analytics                          │   │
│  │  - Application Insights                                  │   │
│  │  - Container Logs                                        │   │
│  │  - Metrics & Alerts                                      │   │
│  └──────────────────────────────────────────────────────────┘   │
│                             │                                    │
└─────────────────────────────┼────────────────────────────────────┘
                              │ JDBC over TLS
                              ▼
                   ┌────────────────────────┐
                   │   ORACLE CLOUD         │
                   │ Autonomous Database    │
                   │ (s58onuxcx4c1qxe9)    │
                   │ Santiago, Chile        │
                   └────────────────────────┘
```

### Servicios de Azure a Utilizar

| Servicio | Propósito | Costo Estimado |
|----------|-----------|----------------|
| **Azure Container Registry** | Almacenar imágenes Docker | ~$5/mes (Basic) |
| **Azure Container Apps** | Ejecutar contenedores (3 apps) | ~$30-50/mes |
| **Azure Key Vault** | Secrets management | ~$0.03/10k operations |
| **Azure Monitor** | Logging y métricas | ~$2-5/mes |
| **Azure Front Door** (Opcional) | CDN + WAF | ~$35/mes |
| **Azure Virtual Network** | Networking privado | Gratis |
| **TOTAL ESTIMADO** | | **~$37-95/mes** |

---

## ✅ Prerequisitos

### 1. Cuenta de Azure
- [ ] Cuenta de Azure activa (Free tier o Pay-as-you-go)
- [ ] Suscripción con permisos de Contributor
- [ ] Créditos disponibles (puede usar crédito estudiantil)

### 2. Herramientas Locales
- [ ] **Azure CLI** instalado (versión 2.50+)
- [ ] **Docker Desktop** instalado y funcionando
- [ ] **Git** instalado
- [ ] **Terminal** (Bash, Zsh, PowerShell)

### 3. Archivos del Proyecto
- [ ] Código fuente completo de VitalWatch
- [ ] Wallet de Oracle Cloud (`Wallet_S58ONUXCX4C1QXE9/`)
- [ ] Dockerfiles validados localmente

### 4. Verificación de Prerequisitos

```bash
# Verificar Azure CLI
az version

# Verificar Docker
docker --version
docker ps

# Verificar estructura del proyecto
ls -la
# Debe mostrar: backend/, frontend/, api-manager/, Wallet_S58ONUXCX4C1QXE9/
```

---

## FASE 1: Preparación del Entorno

### 1.1 Instalar Azure CLI (si no lo tienes)

**macOS:**
```bash
brew update && brew install azure-cli
```

**Linux:**
```bash
curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash
```

**Windows (PowerShell):**
```powershell
Invoke-WebRequest -Uri https://aka.ms/installazurecliwindows -OutFile .\AzureCLI.msi
Start-Process msiexec.exe -Wait -ArgumentList '/I AzureCLI.msi /quiet'
```

### 1.2 Login a Azure

```bash
# Login interactivo
az login

# Verificar subscripción activa
az account show

# Si tienes múltiples subscripciones, seleccionar una
az account list --output table
az account set --subscription "NOMBRE_O_ID_DE_SUBSCRIPCION"
```

### 1.3 Definir Variables de Entorno

```bash
# Crear archivo de configuración
cat > azure-config.env << 'EOF'
# Configuración General
export RESOURCE_GROUP="rg-vitalwatch-prod"
export LOCATION="eastus"  # O la más cercana a tu región
export PROJECT_NAME="vitalwatch"

# Container Registry
export ACR_NAME="acrvitalwatch"  # Debe ser único globalmente, solo letras y números
export ACR_SKU="Basic"

# Container Apps
export CONTAINERAPPS_ENVIRONMENT="env-vitalwatch-prod"
export BACKEND_APP_NAME="vitalwatch-backend"
export FRONTEND_APP_NAME="vitalwatch-frontend"
export GATEWAY_APP_NAME="vitalwatch-api-gateway"

# Key Vault
export KEYVAULT_NAME="kv-vitalwatch"  # Debe ser único globalmente

# Oracle Database (mantener desde config actual)
export ORACLE_USERNAME="ADMIN"
export ORACLE_PASSWORD="\$-123.Sb-123"
export ORACLE_SERVICE="s58onuxcx4c1qxe9_high"

# Tags para recursos
export TAGS="Environment=Production Project=VitalWatch Owner=DUOC"
EOF

# Cargar variables
source azure-config.env

# Verificar variables
echo "Resource Group: $RESOURCE_GROUP"
echo "Location: $LOCATION"
echo "ACR Name: $ACR_NAME"
```

### 1.4 Crear Resource Group

```bash
# Crear grupo de recursos principal
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION \
  --tags $TAGS

# Verificar creación
az group show --name $RESOURCE_GROUP
```

---

## FASE 2: Configuración de Azure Container Registry

### 2.1 Crear Azure Container Registry

```bash
# Crear ACR
az acr create \
  --resource-group $RESOURCE_GROUP \
  --name $ACR_NAME \
  --sku $ACR_SKU \
  --admin-enabled true \
  --tags $TAGS

# Verificar creación
az acr show --name $ACR_NAME --resource-group $RESOURCE_GROUP
```

### 2.2 Obtener Credenciales del Registry

```bash
# Login en ACR
az acr login --name $ACR_NAME

# Obtener servidor de login
export ACR_LOGIN_SERVER=$(az acr show --name $ACR_NAME --query loginServer --output tsv)
echo "ACR Login Server: $ACR_LOGIN_SERVER"

# Obtener credenciales de admin
az acr credential show --name $ACR_NAME

# Guardar credenciales (para uso posterior)
export ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username --output tsv)
export ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query passwords[0].value --output tsv)

echo "ACR Username: $ACR_USERNAME"
echo "ACR Password: [GUARDADO EN VARIABLE]"
```

---

## FASE 3: Construcción y Publicación de Imágenes

### 3.1 Preparar el Proyecto

```bash
# Navegar al directorio del proyecto
cd /Users/sbriceno/Documents/DUOC/CLOUDNATIVE/Semana\ 3\ Sumativa\ 2\ v2/

# Verificar estructura
ls -la backend/Dockerfile
ls -la frontend/Dockerfile
ls -la api-manager/
```

### 3.2 Construir y Publicar Backend

```bash
# Navegar a backend
cd backend

# Construir imagen localmente (para testing)
docker build -t vitalwatch-backend:local .

# Tag para ACR
docker tag vitalwatch-backend:local $ACR_LOGIN_SERVER/vitalwatch-backend:latest
docker tag vitalwatch-backend:local $ACR_LOGIN_SERVER/vitalwatch-backend:v1.0.0

# Push a ACR
docker push $ACR_LOGIN_SERVER/vitalwatch-backend:latest
docker push $ACR_LOGIN_SERVER/vitalwatch-backend:v1.0.0

# Verificar en ACR
az acr repository show --name $ACR_NAME --repository vitalwatch-backend
az acr repository show-tags --name $ACR_NAME --repository vitalwatch-backend --output table

cd ..
```

### 3.3 Construir y Publicar Frontend

```bash
# Navegar a frontend
cd frontend

# Construir imagen
docker build -t vitalwatch-frontend:local .

# Tag para ACR
docker tag vitalwatch-frontend:local $ACR_LOGIN_SERVER/vitalwatch-frontend:latest
docker tag vitalwatch-frontend:local $ACR_LOGIN_SERVER/vitalwatch-frontend:v1.0.0

# Push a ACR
docker push $ACR_LOGIN_SERVER/vitalwatch-frontend:latest
docker push $ACR_LOGIN_SERVER/vitalwatch-frontend:v1.0.0

# Verificar
az acr repository show-tags --name $ACR_NAME --repository vitalwatch-frontend --output table

cd ..
```

### 3.4 Construir y Publicar API Gateway (Kong)

**OPCIÓN A: Usar Kong base con configuración custom**

```bash
# Crear Dockerfile para Kong con tu configuración
cat > api-manager/Dockerfile << 'EOF'
FROM kong:3.4

# Copiar configuración
COPY kong.yml /kong/declarative/kong.yml

# Exponer puertos
EXPOSE 8000 8001 8443 8444

# Variables de entorno por defecto
ENV KONG_DATABASE=off
ENV KONG_DECLARATIVE_CONFIG=/kong/declarative/kong.yml
ENV KONG_PROXY_ACCESS_LOG=/dev/stdout
ENV KONG_ADMIN_ACCESS_LOG=/dev/stdout
ENV KONG_PROXY_ERROR_LOG=/dev/stderr
ENV KONG_ADMIN_ERROR_LOG=/dev/stderr

CMD ["kong", "docker-start"]
EOF

# Construir
cd api-manager
docker build -t vitalwatch-api-gateway:local .

# Tag y Push
docker tag vitalwatch-api-gateway:local $ACR_LOGIN_SERVER/vitalwatch-api-gateway:latest
docker tag vitalwatch-api-gateway:local $ACR_LOGIN_SERVER/vitalwatch-api-gateway:v1.0.0
docker push $ACR_LOGIN_SERVER/vitalwatch-api-gateway:latest
docker push $ACR_LOGIN_SERVER/vitalwatch-api-gateway:v1.0.0

# Verificar
az acr repository show-tags --name $ACR_NAME --repository vitalwatch-api-gateway --output table

cd ..
```

**OPCIÓN B: Usar Azure API Management (nativo)**

Si prefieres usar el servicio nativo de Azure en lugar de Kong, lo configuraremos en una fase posterior.

### 3.5 Verificar Todas las Imágenes

```bash
# Listar todos los repositorios
az acr repository list --name $ACR_NAME --output table

# Ver tamaños de imágenes
az acr repository show --name $ACR_NAME --repository vitalwatch-backend --query "imageSize"
az acr repository show --name $ACR_NAME --repository vitalwatch-frontend --query "imageSize"
az acr repository show --name $ACR_NAME --repository vitalwatch-api-gateway --query "imageSize"
```

---

## FASE 4: Despliegue del Backend

### 4.1 Crear Azure Key Vault para Secrets

```bash
# Crear Key Vault
az keyvault create \
  --name $KEYVAULT_NAME \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION \
  --tags $TAGS

# Agregar secrets
az keyvault secret set --vault-name $KEYVAULT_NAME --name "oracle-username" --value "$ORACLE_USERNAME"
az keyvault secret set --vault-name $KEYVAULT_NAME --name "oracle-password" --value "$ORACLE_PASSWORD"
az keyvault secret set --vault-name $KEYVAULT_NAME --name "oracle-service" --value "$ORACLE_SERVICE"

# Verificar secrets
az keyvault secret list --vault-name $KEYVAULT_NAME --output table
```

### 4.2 Subir Oracle Wallet a Azure

**OPCIÓN A: Usar Azure File Share**

```bash
# Crear Storage Account
STORAGE_ACCOUNT_NAME="stavitalwatch${RANDOM}"
az storage account create \
  --name $STORAGE_ACCOUNT_NAME \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION \
  --sku Standard_LRS

# Crear File Share
az storage share create \
  --name oracle-wallet \
  --account-name $STORAGE_ACCOUNT_NAME

# Obtener connection string
STORAGE_CONNECTION_STRING=$(az storage account show-connection-string \
  --name $STORAGE_ACCOUNT_NAME \
  --resource-group $RESOURCE_GROUP \
  --output tsv)

# Subir archivos del Wallet
az storage file upload-batch \
  --destination oracle-wallet \
  --source ./Wallet_S58ONUXCX4C1QXE9 \
  --account-name $STORAGE_ACCOUNT_NAME

# Verificar archivos
az storage file list \
  --share-name oracle-wallet \
  --account-name $STORAGE_ACCOUNT_NAME \
  --output table
```

**OPCIÓN B: Empaquetar Wallet en la Imagen Docker (más simple)**

Modificar `backend/Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jre-alpine

# Crear directorio de trabajo
WORKDIR /app

# Copiar JAR
COPY target/vitalwatch-*.jar app.jar

# Copiar Oracle Wallet
COPY ../Wallet_S58ONUXCX4C1QXE9 /app/wallet

# Exponer puerto
EXPOSE 8080

# Variables de entorno
ENV TNS_ADMIN=/app/wallet
ENV JAVA_TOOL_OPTIONS="-Doracle.net.tns_admin=/app/wallet -Djavax.net.ssl.trustStore=/app/wallet/truststore.jks -Djavax.net.ssl.trustStoreType=JKS -Djavax.net.ssl.keyStore=/app/wallet/keystore.jks -Djavax.net.ssl.keyStoreType=JKS -Doracle.net.ssl_server_dn_match=true"

# Ejecutar aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Reconstruir y subir imagen:

```bash
cd backend
docker build -t vitalwatch-backend:wallet .
docker tag vitalwatch-backend:wallet $ACR_LOGIN_SERVER/vitalwatch-backend:v1.0.1
docker push $ACR_LOGIN_SERVER/vitalwatch-backend:v1.0.1
cd ..
```

### 4.3 Crear Container Apps Environment

```bash
# Instalar extensión de Container Apps
az extension add --name containerapp --upgrade

# Registrar provider
az provider register --namespace Microsoft.App
az provider register --namespace Microsoft.OperationalInsights

# Crear environment
az containerapp env create \
  --name $CONTAINERAPPS_ENVIRONMENT \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION \
  --tags $TAGS

# Verificar creación
az containerapp env show \
  --name $CONTAINERAPPS_ENVIRONMENT \
  --resource-group $RESOURCE_GROUP
```

### 4.4 Crear Container App para Backend

```bash
# Crear app del backend
az containerapp create \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINERAPPS_ENVIRONMENT \
  --image $ACR_LOGIN_SERVER/vitalwatch-backend:v1.0.1 \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --target-port 8080 \
  --ingress external \
  --min-replicas 2 \
  --max-replicas 10 \
  --cpu 1.0 \
  --memory 2.0Gi \
  --env-vars \
    "ORACLE_USERNAME=secretref:oracle-username" \
    "ORACLE_PASSWORD=secretref:oracle-password" \
    "TNS_ADMIN=/app/wallet" \
    "SERVER_PORT=8080" \
    "SPRING_APPLICATION_NAME=vitalwatch-backend" \
    "ALLOWED_ORIGINS=https://vitalwatch-frontend.azurecontainerapps.io" \
  --secrets \
    "oracle-username=$ORACLE_USERNAME" \
    "oracle-password=$ORACLE_PASSWORD" \
  --tags $TAGS

# Obtener URL del backend
BACKEND_URL=$(az containerapp show \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn \
  --output tsv)

echo "Backend URL: https://$BACKEND_URL"

# Probar endpoint de health
curl https://$BACKEND_URL/api/v1/health
```

### 4.5 Configurar Auto-scaling del Backend

```bash
# Configurar reglas de scaling basadas en CPU y memoria
az containerapp update \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --scale-rule-name cpu-scaling \
  --scale-rule-type cpu \
  --scale-rule-metadata concurrentRequests=50

# Configurar reglas basadas en HTTP requests
az containerapp update \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --scale-rule-name http-scaling \
  --scale-rule-type http \
  --scale-rule-metadata concurrentRequests=100
```

---

## FASE 5: Despliegue del API Gateway

### 5.1 Actualizar Configuración de Kong

Primero, actualizar `api-manager/kong.yml` con las URLs de Azure:

```yaml
_format_version: "3.0"

services:
  - name: vitalwatch-backend-service
    url: https://[BACKEND_URL_DE_AZURE]
    routes:
      - name: api-route
        paths:
          - /api
        strip_path: false
        methods:
          - GET
          - POST
          - PUT
          - DELETE
          - OPTIONS
    plugins:
      - name: cors
        config:
          origins:
            - "https://vitalwatch-frontend.azurecontainerapps.io"
            - "https://*.azurecontainerapps.io"
          methods:
            - GET
            - POST
            - PUT
            - DELETE
            - OPTIONS
          headers:
            - Authorization
            - Content-Type
          exposed_headers:
            - X-Auth-Token
          credentials: true
          max_age: 3600
      
      - name: rate-limiting
        config:
          minute: 100
          hour: 10000
          policy: local
      
      - name: request-size-limiting
        config:
          allowed_payload_size: 10
      
      - name: response-transformer
        config:
          add:
            headers:
              - "X-Frame-Options: DENY"
              - "X-Content-Type-Options: nosniff"
              - "X-XSS-Protection: 1; mode=block"
```

### 5.2 Reconstruir y Subir Imagen de Kong

```bash
cd api-manager

# Reconstruir con configuración actualizada
docker build -t vitalwatch-api-gateway:azure .
docker tag vitalwatch-api-gateway:azure $ACR_LOGIN_SERVER/vitalwatch-api-gateway:v1.0.1
docker push $ACR_LOGIN_SERVER/vitalwatch-api-gateway:v1.0.1

cd ..
```

### 5.3 Desplegar Kong en Container App

```bash
# Crear container app para API Gateway
az containerapp create \
  --name $GATEWAY_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINERAPPS_ENVIRONMENT \
  --image $ACR_LOGIN_SERVER/vitalwatch-api-gateway:v1.0.1 \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --target-port 8000 \
  --ingress external \
  --min-replicas 2 \
  --max-replicas 5 \
  --cpu 0.5 \
  --memory 1.0Gi \
  --env-vars \
    "KONG_DATABASE=off" \
    "KONG_DECLARATIVE_CONFIG=/kong/declarative/kong.yml" \
    "KONG_PROXY_ACCESS_LOG=/dev/stdout" \
    "KONG_ADMIN_ACCESS_LOG=/dev/stdout" \
    "KONG_PROXY_ERROR_LOG=/dev/stderr" \
    "KONG_ADMIN_ERROR_LOG=/dev/stderr" \
  --tags $TAGS

# Obtener URL del API Gateway
GATEWAY_URL=$(az containerapp show \
  --name $GATEWAY_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn \
  --output tsv)

echo "API Gateway URL: https://$GATEWAY_URL"

# Probar API Gateway
curl https://$GATEWAY_URL/api/v1/health
```

---

## FASE 6: Despliegue del Frontend

### 6.1 Actualizar Configuración del Frontend

Modificar `frontend/src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://[GATEWAY_URL_DE_AZURE]/api/v1',  // Reemplazar con URL real
  appName: 'VitalWatch',
  version: '1.0.0'
};
```

### 6.2 Reconstruir Frontend con Configuración de Azure

```bash
cd frontend

# Actualizar environment.prod.ts con la URL real del Gateway
sed -i '' "s|apiUrl: .*|apiUrl: 'https://$GATEWAY_URL/api/v1',|" src/environments/environment.prod.ts

# Reconstruir imagen
docker build -t vitalwatch-frontend:azure .
docker tag vitalwatch-frontend:azure $ACR_LOGIN_SERVER/vitalwatch-frontend:v1.0.1
docker push $ACR_LOGIN_SERVER/vitalwatch-frontend:v1.0.1

cd ..
```

### 6.3 Desplegar Frontend en Container App

```bash
# Crear container app para frontend
az containerapp create \
  --name $FRONTEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINERAPPS_ENVIRONMENT \
  --image $ACR_LOGIN_SERVER/vitalwatch-frontend:v1.0.1 \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --target-port 80 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 10 \
  --cpu 0.5 \
  --memory 1.0Gi \
  --env-vars \
    "API_URL=https://$GATEWAY_URL/api/v1" \
  --tags $TAGS

# Obtener URL del frontend
FRONTEND_URL=$(az containerapp show \
  --name $FRONTEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn \
  --output tsv)

echo "=========================================="
echo "✅ DESPLIEGUE COMPLETADO"
echo "=========================================="
echo "Frontend: https://$FRONTEND_URL"
echo "API Gateway: https://$GATEWAY_URL"
echo "Backend: https://$BACKEND_URL"
echo "=========================================="
```

---

## FASE 7: Configuración de Networking

### 7.1 Configurar CORS en Backend

Actualizar el backend para permitir el nuevo origen de Azure:

```bash
# Actualizar variable de entorno del backend
az containerapp update \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --set-env-vars "ALLOWED_ORIGINS=https://$FRONTEND_URL,https://$GATEWAY_URL"
```

### 7.2 Configurar Comunicación entre Container Apps

```bash
# Las Container Apps en el mismo environment pueden comunicarse internamente
# Verificar networking
az containerapp env show \
  --name $CONTAINERAPPS_ENVIRONMENT \
  --resource-group $RESOURCE_GROUP \
  --query properties.vnetConfiguration
```

### 7.3 (Opcional) Crear Virtual Network Privado

Si quieres una red privada más segura:

```bash
# Crear VNet
VNET_NAME="vnet-vitalwatch"
az network vnet create \
  --name $VNET_NAME \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION \
  --address-prefix 10.0.0.0/16

# Crear subnet para Container Apps
az network vnet subnet create \
  --name subnet-containerapps \
  --vnet-name $VNET_NAME \
  --resource-group $RESOURCE_GROUP \
  --address-prefixes 10.0.1.0/24

# Obtener subnet ID
SUBNET_ID=$(az network vnet subnet show \
  --name subnet-containerapps \
  --vnet-name $VNET_NAME \
  --resource-group $RESOURCE_GROUP \
  --query id \
  --output tsv)

# Recrear environment con VNet (requiere eliminar el anterior)
# NOTA: Hacer esto solo si necesitas mayor seguridad
```

---

## FASE 8: Configuración de Dominios y SSL

### 8.1 Configurar Custom Domain (Opcional)

Si tienes un dominio propio:

```bash
# Agregar custom domain al frontend
az containerapp hostname add \
  --hostname app.tudominio.com \
  --name $FRONTEND_APP_NAME \
  --resource-group $RESOURCE_GROUP

# Obtener el CNAME que debes configurar en tu DNS
az containerapp hostname list \
  --name $FRONTEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --output table
```

Configurar en tu proveedor de DNS:
```
Tipo: CNAME
Nombre: app
Valor: vitalwatch-frontend.azurecontainerapps.io
TTL: 3600
```

### 8.2 Certificado SSL Automático

Azure Container Apps proporciona SSL automático con Let's Encrypt:

```bash
# Bind certificado SSL (automático después de configurar DNS)
az containerapp hostname bind \
  --hostname app.tudominio.com \
  --name $FRONTEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --validation-method CNAME
```

### 8.3 (Opcional) Configurar Azure Front Door

Para mejor rendimiento global y CDN:

```bash
# Crear Azure Front Door
FRONTDOOR_NAME="fd-vitalwatch"
az afd profile create \
  --profile-name $FRONTDOOR_NAME \
  --resource-group $RESOURCE_GROUP \
  --sku Standard_AzureFrontDoor

# Agregar endpoint
az afd endpoint create \
  --profile-name $FRONTDOOR_NAME \
  --endpoint-name vitalwatch \
  --resource-group $RESOURCE_GROUP

# Agregar origin group
az afd origin-group create \
  --origin-group-name frontend-origins \
  --profile-name $FRONTDOOR_NAME \
  --resource-group $RESOURCE_GROUP \
  --probe-path /health \
  --probe-protocol Https \
  --probe-method GET \
  --probe-interval-in-seconds 30

# Agregar origin (tu Container App)
az afd origin create \
  --origin-group-name frontend-origins \
  --origin-name frontend-origin \
  --profile-name $FRONTDOOR_NAME \
  --resource-group $RESOURCE_GROUP \
  --host-name $FRONTEND_URL \
  --origin-host-header $FRONTEND_URL \
  --priority 1 \
  --weight 1000 \
  --http-port 80 \
  --https-port 443

# Agregar route
az afd route create \
  --route-name default-route \
  --endpoint-name vitalwatch \
  --profile-name $FRONTDOOR_NAME \
  --resource-group $RESOURCE_GROUP \
  --origin-group frontend-origins \
  --supported-protocols Http Https \
  --https-redirect Enabled \
  --forwarding-protocol HttpsOnly
```

---

## FASE 9: Monitoreo y Logging

### 9.1 Configurar Application Insights

```bash
# Crear workspace de Log Analytics
LOG_ANALYTICS_WORKSPACE="log-vitalwatch"
az monitor log-analytics workspace create \
  --workspace-name $LOG_ANALYTICS_WORKSPACE \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION

# Obtener workspace ID
WORKSPACE_ID=$(az monitor log-analytics workspace show \
  --workspace-name $LOG_ANALYTICS_WORKSPACE \
  --resource-group $RESOURCE_GROUP \
  --query customerId \
  --output tsv)

# Crear Application Insights
APPINSIGHTS_NAME="appi-vitalwatch"
az monitor app-insights component create \
  --app $APPINSIGHTS_NAME \
  --location $LOCATION \
  --resource-group $RESOURCE_GROUP \
  --workspace $WORKSPACE_ID

# Obtener instrumentation key
INSTRUMENTATION_KEY=$(az monitor app-insights component show \
  --app $APPINSIGHTS_NAME \
  --resource-group $RESOURCE_GROUP \
  --query instrumentationKey \
  --output tsv)

echo "Instrumentation Key: $INSTRUMENTATION_KEY"
```

### 9.2 Conectar Container Apps con Application Insights

```bash
# Actualizar backend con App Insights
az containerapp update \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --set-env-vars "APPLICATIONINSIGHTS_CONNECTION_STRING=InstrumentationKey=$INSTRUMENTATION_KEY"

# Actualizar frontend
az containerapp update \
  --name $FRONTEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --set-env-vars "APPLICATIONINSIGHTS_CONNECTION_STRING=InstrumentationKey=$INSTRUMENTATION_KEY"
```

### 9.3 Ver Logs en Tiempo Real

```bash
# Ver logs del backend
az containerapp logs show \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --tail 50 \
  --follow

# Ver logs del frontend
az containerapp logs show \
  --name $FRONTEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --tail 50 \
  --follow

# Ver logs del API Gateway
az containerapp logs show \
  --name $GATEWAY_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --tail 50 \
  --follow
```

### 9.4 Configurar Alertas

```bash
# Alerta para CPU alta
az monitor metrics alert create \
  --name "High CPU - Backend" \
  --resource-group $RESOURCE_GROUP \
  --scopes $(az containerapp show --name $BACKEND_APP_NAME --resource-group $RESOURCE_GROUP --query id --output tsv) \
  --condition "avg Percentage CPU > 80" \
  --window-size 5m \
  --evaluation-frequency 1m \
  --description "Alert when backend CPU exceeds 80%"

# Alerta para errores HTTP
az monitor metrics alert create \
  --name "High Error Rate - Backend" \
  --resource-group $RESOURCE_GROUP \
  --scopes $(az containerapp show --name $BACKEND_APP_NAME --resource-group $RESOURCE_GROUP --query id --output tsv) \
  --condition "total Http Server Errors > 100" \
  --window-size 5m \
  --evaluation-frequency 1m \
  --description "Alert when backend has more than 100 errors in 5 minutes"
```

---

## FASE 10: Pruebas y Validación

### 10.1 Checklist de Validación

```bash
# ✅ 1. Health Check del Backend
curl https://$BACKEND_URL/api/v1/health
# Esperado: {"status":"UP"}

# ✅ 2. Health Check del Database
curl https://$BACKEND_URL/api/v1/health/database
# Esperado: {"status":"UP","database":"Oracle"}

# ✅ 3. Verificar API Gateway
curl https://$GATEWAY_URL/api/v1/health
# Esperado: Misma respuesta que backend

# ✅ 4. Test de Login
curl -X POST https://$GATEWAY_URL/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@vitalwatch.com",
    "password": "Admin123!"
  }'
# Esperado: {"token":"...", "user":{...}}

# ✅ 5. Test CORS
curl -H "Origin: https://$FRONTEND_URL" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -X OPTIONS \
  https://$GATEWAY_URL/api/v1/health \
  -v
# Esperado: Headers Access-Control-Allow-Origin

# ✅ 6. Verificar Frontend
curl https://$FRONTEND_URL
# Esperado: HTML de la aplicación Angular
```

### 10.2 Pruebas de Carga (Opcional)

```bash
# Instalar Apache Bench
# macOS: brew install apache2
# Linux: apt-get install apache2-utils

# Test de carga básico (100 requests, 10 concurrent)
ab -n 100 -c 10 https://$BACKEND_URL/api/v1/health

# Test más intensivo (1000 requests, 50 concurrent)
ab -n 1000 -c 50 https://$GATEWAY_URL/api/v1/health
```

### 10.3 Validación Funcional Manual

1. **Abrir Frontend**: `https://$FRONTEND_URL`
2. **Login**:
   - Email: `admin@vitalwatch.com`
   - Password: `Admin123!`
3. **Verificar Dashboard**: Debe mostrar estadísticas
4. **Crear Paciente**: Llenar formulario y guardar
5. **Registrar Signos Vitales**: Con valores anormales para generar alerta
6. **Verificar Alerta**: Debe aparecer en el módulo de alertas

---

## Gestión y Mantenimiento

### Actualizar una Aplicación

```bash
# 1. Construir nueva versión
cd backend
docker build -t vitalwatch-backend:v1.0.2 .
docker tag vitalwatch-backend:v1.0.2 $ACR_LOGIN_SERVER/vitalwatch-backend:v1.0.2
docker push $ACR_LOGIN_SERVER/vitalwatch-backend:v1.0.2
cd ..

# 2. Actualizar Container App (Zero-downtime deployment)
az containerapp update \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --image $ACR_LOGIN_SERVER/vitalwatch-backend:v1.0.2

# Verificar revisión
az containerapp revision list \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --output table
```

### Rollback a Versión Anterior

```bash
# Listar revisiones
az containerapp revision list \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --output table

# Activar revisión anterior
az containerapp revision activate \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --revision [NOMBRE_REVISION_ANTERIOR]
```

### Escalar Manualmente

```bash
# Escalar backend a 5 réplicas
az containerapp update \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --min-replicas 5 \
  --max-replicas 15
```

### Ver Métricas

```bash
# CPU y Memoria
az monitor metrics list \
  --resource $(az containerapp show --name $BACKEND_APP_NAME --resource-group $RESOURCE_GROUP --query id --output tsv) \
  --metric "UsageNanoCores" "WorkingSetBytes" \
  --output table

# Requests
az monitor metrics list \
  --resource $(az containerapp show --name $BACKEND_APP_NAME --resource-group $RESOURCE_GROUP --query id --output tsv) \
  --metric "Requests" \
  --output table
```

---

## Troubleshooting

### Problema: Container no arranca

```bash
# Ver logs detallados
az containerapp logs show \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --tail 100

# Ver eventos del container
az containerapp show \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query properties.latestRevisionName

az containerapp revision show \
  --name [REVISION_NAME] \
  --app $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP
```

### Problema: No se puede conectar a Oracle DB

```bash
# Verificar variables de entorno
az containerapp show \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query properties.template.containers[0].env

# Verificar que el Wallet está en la imagen
# Ejecutar shell en el container
az containerapp exec \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --command "/bin/sh"

# Dentro del container:
ls -la /app/wallet
cat /app/wallet/tnsnames.ora
```

### Problema: CORS errors

```bash
# Verificar configuración de CORS en Kong
az containerapp show \
  --name $GATEWAY_APP_NAME \
  --resource-group $RESOURCE_GROUP

# Verificar logs de Kong
az containerapp logs show \
  --name $GATEWAY_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --tail 50
```

### Problema: SSL/TLS errors

```bash
# Verificar certificado
curl -v https://$FRONTEND_URL 2>&1 | grep -i ssl

# Ver configuración de ingress
az containerapp ingress show \
  --name $FRONTEND_APP_NAME \
  --resource-group $RESOURCE_GROUP
```

### Problema: Alto consumo de recursos

```bash
# Ver uso actual
az containerapp show \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query "properties.template.containers[0].resources"

# Aumentar recursos
az containerapp update \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --cpu 2.0 \
  --memory 4.0Gi
```

---

## Comandos Útiles de Referencia Rápida

```bash
# Ver estado de todos los recursos
az resource list --resource-group $RESOURCE_GROUP --output table

# Ver costos estimados
az consumption usage list --output table

# Restart de una app
az containerapp revision restart \
  --name $BACKEND_APP_NAME \
  --resource-group $RESOURCE_GROUP

# Ver todas las apps del environment
az containerapp list \
  --environment $CONTAINERAPPS_ENVIRONMENT \
  --resource-group $RESOURCE_GROUP \
  --output table

# Eliminar todo (CUIDADO!)
az group delete --name $RESOURCE_GROUP --yes --no-wait
```

---

## Resumen de URLs Finales

Al completar todas las fases, tendrás:

```
📱 Frontend:        https://vitalwatch-frontend.azurecontainerapps.io
🔌 API Gateway:     https://vitalwatch-api-gateway.azurecontainerapps.io
⚙️  Backend:         https://vitalwatch-backend.azurecontainerapps.io
📊 App Insights:    Portal Azure > Application Insights
🗝️  Key Vault:       Portal Azure > Key Vault
📦 Container Registry: Portal Azure > Container Registry
```

---

## Próximos Pasos Recomendados

1. ✅ **Configurar CI/CD** con GitHub Actions o Azure DevOps
2. ✅ **Implementar Backup Strategy** para configuraciones
3. ✅ **Configurar Azure Front Door** para CDN global
4. ✅ **Implementar Rate Limiting** más granular
5. ✅ **Configurar Azure Monitor Dashboards** personalizados
6. ✅ **Documentar Runbooks** para operaciones comunes
7. ✅ **Implementar Disaster Recovery Plan**

---

**Última actualización**: 2026-01-26  
**Versión**: 1.0.0  
**Autor**: Equipo VitalWatch
