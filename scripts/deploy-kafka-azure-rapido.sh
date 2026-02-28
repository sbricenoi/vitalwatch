#!/bin/bash

# =============================================================================
# VitalWatch Kafka - Deploy Rápido a Azure (Reutilizando infraestructura)
# =============================================================================

set -e

echo "🚀 VitalWatch Kafka - Deploy Rápido a Azure"
echo "============================================"
echo ""

# Variables (usando recursos existentes)
RESOURCE_GROUP="rg-vitalwatch-prod"
LOCATION="southcentralus"
ENVIRONMENT_NAME="env-vitalwatch-prod"
ACR_NAME="acrvitalwatch"
EVENT_HUB_NAMESPACE="vitalwatch-kafka-hub"

# Variables de Oracle
ORACLE_DB_URL="jdbc:oracle:thin:@(description=(retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.sa-santiago-1.oraclecloud.com))(connect_data=(service_name=s58onuxcx4c1qxe9_high.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))"
ORACLE_USERNAME="ADMIN"
ORACLE_PASSWORD="\$-123.Sb-123"

echo "📋 Configuración:"
echo "   Resource Group: $RESOURCE_GROUP (existente)"
echo "   Location: $LOCATION"
echo "   Environment: $ENVIRONMENT_NAME (existente)"
echo "   ACR: $ACR_NAME (existente)"
echo "   Event Hub: $EVENT_HUB_NAMESPACE (nuevo)"
echo ""

# =============================================================================
# 1. OBTENER CREDENCIALES DEL ACR EXISTENTE
# =============================================================================

echo "🔑 Obteniendo credenciales del ACR existente..."
ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username -o tsv)
ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query passwords[0].value -o tsv)
ACR_LOGIN_SERVER="$ACR_NAME.azurecr.io"

echo "✅ ACR: $ACR_LOGIN_SERVER"

# =============================================================================
# 2. LOGIN A ACR Y BUILD DE IMÁGENES
# =============================================================================

echo ""
echo "🐳 Building y pushing imágenes Kafka a ACR..."
echo ""

az acr login --name $ACR_NAME

# Producer 1: Stream Generator
echo "📦 1/4 - Stream Generator..."
docker build -t $ACR_LOGIN_SERVER/kafka-stream-generator:latest ./producer-stream-generator
docker push $ACR_LOGIN_SERVER/kafka-stream-generator:latest

# Producer 2: Alert Processor
echo "📦 2/4 - Alert Processor..."
docker build -t $ACR_LOGIN_SERVER/kafka-alert-processor:latest ./producer-alert-processor
docker push $ACR_LOGIN_SERVER/kafka-alert-processor:latest

# Consumer 1: Database Saver
echo "📦 3/4 - Database Saver..."
docker build -t $ACR_LOGIN_SERVER/kafka-database-saver:latest ./consumer-database-saver
docker push $ACR_LOGIN_SERVER/kafka-database-saver:latest

# Consumer 2: Summary Generator
echo "📦 4/4 - Summary Generator..."
docker build -t $ACR_LOGIN_SERVER/kafka-summary-generator:latest ./consumer-summary-generator
docker push $ACR_LOGIN_SERVER/kafka-summary-generator:latest

echo "✅ Todas las imágenes Kafka pushed a ACR"

# =============================================================================
# 3. CREAR AZURE EVENT HUBS (Kafka Compatible)
# =============================================================================

echo ""
echo "📨 Creando Azure Event Hubs (Kafka API)..."

az eventhubs namespace create \
  --name $EVENT_HUB_NAMESPACE \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION \
  --sku Standard \
  --enable-kafka true

# Crear Event Hub para signos vitales
echo "   Creando Event Hub: signos-vitales-stream..."
az eventhubs eventhub create \
  --name signos-vitales-stream \
  --resource-group $RESOURCE_GROUP \
  --namespace-name $EVENT_HUB_NAMESPACE \
  --partition-count 3 \
  --message-retention 7

# Crear Event Hub para alertas
echo "   Creando Event Hub: alertas-medicas..."
az eventhubs eventhub create \
  --name alertas-medicas \
  --resource-group $RESOURCE_GROUP \
  --namespace-name $EVENT_HUB_NAMESPACE \
  --partition-count 3 \
  --message-retention 30

# Obtener connection string
echo "   Obteniendo connection string..."
EVENT_HUB_CONNECTION=$(az eventhubs namespace authorization-rule keys list \
  --resource-group $RESOURCE_GROUP \
  --namespace-name $EVENT_HUB_NAMESPACE \
  --name RootManageSharedAccessKey \
  --query primaryConnectionString \
  --output tsv)

# Construir Kafka bootstrap server
KAFKA_BOOTSTRAP="$EVENT_HUB_NAMESPACE.servicebus.windows.net:9093"

echo "✅ Event Hubs creados"
echo "   Bootstrap Server: $KAFKA_BOOTSTRAP"

# =============================================================================
# 4. DEPLOY MICROSERVICIOS KAFKA
# =============================================================================

echo ""
echo "🚀 Deploying Microservicios Kafka..."
echo ""

# Producer 1: Stream Generator
echo "📦 1/4 - Deploying Stream Generator..."
az containerapp create \
  --name vitalwatch-kafka-stream \
  --resource-group $RESOURCE_GROUP \
  --environment $ENVIRONMENT_NAME \
  --image $ACR_LOGIN_SERVER/kafka-stream-generator:latest \
  --target-port 8080 \
  --ingress external \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --cpu 0.5 \
  --memory 1.0Gi \
  --min-replicas 1 \
  --max-replicas 2 \
  --env-vars \
    SPRING_PROFILES_ACTIVE=docker \
    KAFKA_BOOTSTRAP_SERVERS=$KAFKA_BOOTSTRAP \
    KAFKA_TOPIC_VITAL_SIGNS=signos-vitales-stream \
    STREAM_GENERATION_ENABLED=true \
    STREAM_INTERVAL_MS=1000

# Producer 2: Alert Processor
echo "📦 2/4 - Deploying Alert Processor..."
az containerapp create \
  --name vitalwatch-kafka-alert \
  --resource-group $RESOURCE_GROUP \
  --environment $ENVIRONMENT_NAME \
  --image $ACR_LOGIN_SERVER/kafka-alert-processor:latest \
  --target-port 8080 \
  --ingress external \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --cpu 0.5 \
  --memory 1.0Gi \
  --min-replicas 1 \
  --max-replicas 2 \
  --env-vars \
    SPRING_PROFILES_ACTIVE=docker \
    KAFKA_BOOTSTRAP_SERVERS=$KAFKA_BOOTSTRAP \
    KAFKA_TOPIC_VITAL_SIGNS=signos-vitales-stream \
    KAFKA_TOPIC_ALERTS=alertas-medicas

# Consumer 1: Database Saver
echo "📦 3/4 - Deploying Database Saver..."
az containerapp create \
  --name vitalwatch-kafka-db \
  --resource-group $RESOURCE_GROUP \
  --environment $ENVIRONMENT_NAME \
  --image $ACR_LOGIN_SERVER/kafka-database-saver:latest \
  --target-port 8080 \
  --ingress internal \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --cpu 0.5 \
  --memory 1.0Gi \
  --min-replicas 1 \
  --max-replicas 2 \
  --env-vars \
    SPRING_PROFILES_ACTIVE=docker \
    KAFKA_BOOTSTRAP_SERVERS=$KAFKA_BOOTSTRAP \
    KAFKA_TOPIC_VITAL_SIGNS=signos-vitales-stream \
    KAFKA_TOPIC_ALERTS=alertas-medicas \
    ORACLE_DB_URL="$ORACLE_DB_URL" \
    ORACLE_DB_USERNAME=$ORACLE_USERNAME \
    ORACLE_DB_PASSWORD="$ORACLE_PASSWORD"

# Consumer 2: Summary Generator
echo "📦 4/4 - Deploying Summary Generator..."
az containerapp create \
  --name vitalwatch-kafka-summary \
  --resource-group $RESOURCE_GROUP \
  --environment $ENVIRONMENT_NAME \
  --image $ACR_LOGIN_SERVER/kafka-summary-generator:latest \
  --target-port 8080 \
  --ingress external \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --cpu 0.5 \
  --memory 1.0Gi \
  --min-replicas 1 \
  --max-replicas 2 \
  --env-vars \
    SPRING_PROFILES_ACTIVE=docker \
    ORACLE_DB_URL="$ORACLE_DB_URL" \
    ORACLE_DB_USERNAME=$ORACLE_USERNAME \
    ORACLE_DB_PASSWORD="$ORACLE_PASSWORD"

# =============================================================================
# 5. OBTENER URLS DE ACCESO
# =============================================================================

echo ""
echo "✅ Deploy Completado!"
echo ""
echo "📋 URLs de Acceso:"
echo ""

STREAM_URL=$(az containerapp show --name vitalwatch-kafka-stream --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv)
ALERT_URL=$(az containerapp show --name vitalwatch-kafka-alert --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv)
SUMMARY_URL=$(az containerapp show --name vitalwatch-kafka-summary --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv)

echo "🔄 Stream Generator:    https://$STREAM_URL"
echo "⚠️  Alert Processor:     https://$ALERT_URL"
echo "📊 Summary Generator:   https://$SUMMARY_URL"
echo "💾 Database Saver:      (internal only)"
echo ""
echo "📨 Event Hubs:"
echo "   Namespace: $EVENT_HUB_NAMESPACE"
echo "   Bootstrap: $KAFKA_BOOTSTRAP"
echo ""

# =============================================================================
# 6. INICIAR EL STREAM
# =============================================================================

echo "🚀 Iniciando el stream de datos..."
curl -X POST "https://$STREAM_URL/api/v1/stream/start" || echo "   (se iniciará automáticamente)"

echo ""
echo "✅ Sistema Kafka desplegado y funcionando en Azure!"
echo ""
echo "📊 Verificar estado:"
echo "   curl https://$STREAM_URL/api/v1/stream/stats"
echo "   curl https://$ALERT_URL/api/v1/processor/stats"
echo "   curl https://$SUMMARY_URL/api/v1/summary/today"
echo ""
