#!/bin/bash

# =============================================================================
# VitalWatch - Deployment Script para Azure Container Apps (Kafka)
# =============================================================================

set -e

echo "🚀 VitalWatch Kafka - Deployment a Azure"
echo "========================================="
echo ""

# Variables de configuración
RESOURCE_GROUP="rg-vitalwatch-kafka-prod"
LOCATION="southcentralus"
ENVIRONMENT_NAME="vitalwatch-kafka-env"
ACR_NAME="vitalwatchkafka"

# Variables de Oracle
ORACLE_DB_URL="jdbc:oracle:thin:@(description=(retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.sa-santiago-1.oraclecloud.com))(connect_data=(service_name=s58onuxcx4c1qxe9_high.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))"
ORACLE_USERNAME="ADMIN"
ORACLE_PASSWORD="\$-123.Sb-123"

echo "📋 Configuración:"
echo "   Resource Group: $RESOURCE_GROUP"
echo "   Location: $LOCATION"
echo "   Environment: $ENVIRONMENT_NAME"
echo "   ACR: $ACR_NAME"
echo ""

read -p "¿Continuar con el deployment? (y/n): " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    exit 1
fi

# =============================================================================
# 1. CREAR RESOURCE GROUP
# =============================================================================

echo "📦 Creando Resource Group..."
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION \
  --tags proyecto=VitalWatch tipo=Kafka ambiente=produccion

# =============================================================================
# 2. CREAR AZURE CONTAINER REGISTRY
# =============================================================================

echo "🏗️ Creando Azure Container Registry..."
az acr create \
  --resource-group $RESOURCE_GROUP \
  --name $ACR_NAME \
  --sku Basic \
  --admin-enabled true

echo "🔑 Obteniendo credenciales del ACR..."
ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username -o tsv)
ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query passwords[0].value -o tsv)
ACR_LOGIN_SERVER="$ACR_NAME.azurecr.io"

echo "✅ ACR creado: $ACR_LOGIN_SERVER"

# =============================================================================
# 3. BUILD Y PUSH DE IMÁGENES
# =============================================================================

echo ""
echo "🐳 Building y pushing imágenes a ACR..."
echo ""

# Login a ACR
az acr login --name $ACR_NAME

# Producer 1: Stream Generator
echo "📦 1/4 - Stream Generator..."
docker build -t $ACR_LOGIN_SERVER/producer-stream-generator:latest ./producer-stream-generator
docker push $ACR_LOGIN_SERVER/producer-stream-generator:latest

# Producer 2: Alert Processor
echo "📦 2/4 - Alert Processor..."
docker build -t $ACR_LOGIN_SERVER/producer-alert-processor:latest ./producer-alert-processor
docker push $ACR_LOGIN_SERVER/producer-alert-processor:latest

# Consumer 1: Database Saver
echo "📦 3/4 - Database Saver..."
docker build -t $ACR_LOGIN_SERVER/consumer-database-saver:latest ./consumer-database-saver
docker push $ACR_LOGIN_SERVER/consumer-database-saver:latest

# Consumer 2: Summary Generator
echo "📦 4/4 - Summary Generator..."
docker build -t $ACR_LOGIN_SERVER/consumer-summary-generator:latest ./consumer-summary-generator
docker push $ACR_LOGIN_SERVER/consumer-summary-generator:latest

echo "✅ Todas las imágenes pushed a ACR"

# =============================================================================
# 4. CREAR CONTAINER APPS ENVIRONMENT
# =============================================================================

echo ""
echo "🌐 Creando Container Apps Environment..."
az containerapp env create \
  --name $ENVIRONMENT_NAME \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION

# =============================================================================
# NOTA IMPORTANTE: KAFKA EN AZURE
# =============================================================================

echo ""
echo "⚠️  NOTA IMPORTANTE SOBRE KAFKA EN AZURE"
echo "=========================================="
echo ""
echo "Azure Container Apps no soporta nativamente clusters de Kafka."
echo "Para producción, se recomienda usar uno de estos servicios:"
echo ""
echo "1. Azure Event Hubs (API compatible con Kafka)"
echo "2. Confluent Cloud (Kafka gestionado)"
echo "3. Azure HDInsight con Kafka"
echo "4. Azure Kubernetes Service (AKS) con Kafka"
echo ""
echo "Este script desplegará los microservicios apuntando a un cluster"
echo "de Kafka externo. Debes configurar las variables de entorno con"
echo "el bootstrap server de tu cluster."
echo ""

read -p "Ingresa el Kafka Bootstrap Server (ej: my-kafka.eastus.cloudapp.azure.com:9092): " KAFKA_BOOTSTRAP

if [ -z "$KAFKA_BOOTSTRAP" ]; then
    echo "❌ Bootstrap server requerido"
    exit 1
fi

# =============================================================================
# 5. DEPLOY PRODUCER 1: STREAM GENERATOR
# =============================================================================

echo ""
echo "🚀 Deploying Producer 1: Stream Generator..."
az containerapp create \
  --name vitalwatch-stream-generator \
  --resource-group $RESOURCE_GROUP \
  --environment $ENVIRONMENT_NAME \
  --image $ACR_LOGIN_SERVER/producer-stream-generator:latest \
  --target-port 8080 \
  --ingress external \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --cpu 0.5 \
  --memory 1.0Gi \
  --min-replicas 1 \
  --max-replicas 3 \
  --env-vars \
    SPRING_PROFILES_ACTIVE=docker \
    KAFKA_BOOTSTRAP_SERVERS=$KAFKA_BOOTSTRAP \
    KAFKA_TOPIC_VITAL_SIGNS=signos-vitales-stream \
    STREAM_GENERATION_ENABLED=true \
    STREAM_INTERVAL_MS=1000

STREAM_URL=$(az containerapp show \
  --name vitalwatch-stream-generator \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn -o tsv)

echo "✅ Stream Generator deployed: https://$STREAM_URL"

# =============================================================================
# 6. DEPLOY PRODUCER 2: ALERT PROCESSOR
# =============================================================================

echo ""
echo "🚀 Deploying Producer 2: Alert Processor..."
az containerapp create \
  --name vitalwatch-alert-processor \
  --resource-group $RESOURCE_GROUP \
  --environment $ENVIRONMENT_NAME \
  --image $ACR_LOGIN_SERVER/producer-alert-processor:latest \
  --target-port 8080 \
  --ingress external \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --cpu 0.5 \
  --memory 1.0Gi \
  --min-replicas 1 \
  --max-replicas 3 \
  --env-vars \
    SPRING_PROFILES_ACTIVE=docker \
    KAFKA_BOOTSTRAP_SERVERS=$KAFKA_BOOTSTRAP \
    KAFKA_TOPIC_VITAL_SIGNS=signos-vitales-stream \
    KAFKA_TOPIC_ALERTS=alertas-medicas \
    KAFKA_GROUP_ID=alert-processor-group

ALERT_URL=$(az containerapp show \
  --name vitalwatch-alert-processor \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn -o tsv)

echo "✅ Alert Processor deployed: https://$ALERT_URL"

# =============================================================================
# 7. DEPLOY CONSUMER 1: DATABASE SAVER
# =============================================================================

echo ""
echo "🚀 Deploying Consumer 1: Database Saver..."

echo "⚠️  Database Saver requiere Oracle Wallet."
echo "   El wallet debe ser subido como Azure Files o montado como volumen."
echo "   Por ahora se desplegará sin ingress (solo interno)."

az containerapp create \
  --name vitalwatch-database-saver \
  --resource-group $RESOURCE_GROUP \
  --environment $ENVIRONMENT_NAME \
  --image $ACR_LOGIN_SERVER/consumer-database-saver:latest \
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
    KAFKA_GROUP_ID_VITAL_SIGNS=db-saver-vital-signs-group \
    KAFKA_GROUP_ID_ALERTS=db-saver-alerts-group \
    ORACLE_DB_URL="$ORACLE_DB_URL" \
    ORACLE_DB_USERNAME=$ORACLE_USERNAME \
    ORACLE_DB_PASSWORD=$ORACLE_PASSWORD

echo "✅ Database Saver deployed (internal)"

# =============================================================================
# 8. DEPLOY CONSUMER 2: SUMMARY GENERATOR
# =============================================================================

echo ""
echo "🚀 Deploying Consumer 2: Summary Generator..."
az containerapp create \
  --name vitalwatch-summary-generator \
  --resource-group $RESOURCE_GROUP \
  --environment $ENVIRONMENT_NAME \
  --image $ACR_LOGIN_SERVER/consumer-summary-generator:latest \
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
    KAFKA_TOPIC_ALERTS=alertas-medicas \
    KAFKA_GROUP_ID=summary-generator-group \
    ORACLE_DB_URL="$ORACLE_DB_URL" \
    ORACLE_DB_USERNAME=$ORACLE_USERNAME \
    ORACLE_DB_PASSWORD=$ORACLE_PASSWORD

SUMMARY_URL=$(az containerapp show \
  --name vitalwatch-summary-generator \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn -o tsv)

echo "✅ Summary Generator deployed: https://$SUMMARY_URL"

# =============================================================================
# 9. RESUMEN FINAL
# =============================================================================

echo ""
echo "✅ DEPLOYMENT COMPLETADO"
echo "========================"
echo ""
echo "📊 Resource Group: $RESOURCE_GROUP"
echo "📦 ACR: $ACR_LOGIN_SERVER"
echo ""
echo "🌐 URLs de Microservicios:"
echo "   - Stream Generator: https://$STREAM_URL"
echo "   - Alert Processor: https://$ALERT_URL"
echo "   - Summary Generator: https://$SUMMARY_URL"
echo "   - Database Saver: (internal only)"
echo ""
echo "📝 Siguientes pasos:"
echo "   1. Configurar Oracle Wallet en Azure Files"
echo "   2. Actualizar Database Saver con montaje de wallet"
echo "   3. Probar endpoints con Postman"
echo "   4. Verificar datos en Oracle Cloud"
echo ""
echo "🔍 Ver logs:"
echo "   az containerapp logs show -n vitalwatch-stream-generator -g $RESOURCE_GROUP --follow"
echo ""
