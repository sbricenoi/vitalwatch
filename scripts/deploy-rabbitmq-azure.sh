#!/bin/bash

################################################################################
# Script de Despliegue de Microservicios RabbitMQ en Azure - VitalWatch
# Este script despliega los 5 nuevos microservicios de la integración RabbitMQ
################################################################################

set -e  # Salir si hay algún error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funciones de utilidad
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Cargar configuración
load_config() {
    print_header "Cargando Configuración"
    
    if [[ -f "azure-config.env" ]]; then
        source azure-config.env
        print_success "Configuración cargada desde azure-config.env"
    else
        print_error "Archivo azure-config.env no encontrado"
        print_info "Ejecuta primero: ./deploy-azure.sh"
        exit 1
    fi
    
    # Verificar variables requeridas
    if [[ -z "$RESOURCE_GROUP" ]] || [[ -z "$ACR_NAME" ]]; then
        print_error "Variables de configuración faltantes"
        exit 1
    fi
    
    print_info "Resource Group: $RESOURCE_GROUP"
    print_info "ACR Name: $ACR_NAME"
}

# Login en Azure y ACR
azure_login() {
    print_header "Autenticación en Azure"
    
    if ! az account show &> /dev/null; then
        print_info "Iniciando login en Azure..."
        az login
    else
        print_success "Ya estás autenticado en Azure"
    fi
    
    # Login en ACR
    print_info "Autenticando en ACR..."
    az acr login --name $ACR_NAME
    
    # Obtener credenciales
    export ACR_LOGIN_SERVER=$(az acr show --name $ACR_NAME --query loginServer --output tsv)
    export ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username --output tsv)
    export ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query passwords[0].value --output tsv)
    
    print_success "ACR configurado: $ACR_LOGIN_SERVER"
}

# Construir y publicar imágenes RabbitMQ
build_and_push_rabbitmq_images() {
    print_header "Construyendo Imágenes de Microservicios RabbitMQ"
    
    VERSION="v1.0.0"
    
    # Producer 1: Anomaly Detector
    print_info "Construyendo Producer Anomaly Detector..."
    cd producer-anomaly-detector
    docker build --platform linux/amd64 -t vitalwatch-producer-anomaly:$VERSION .
    docker tag vitalwatch-producer-anomaly:$VERSION $ACR_LOGIN_SERVER/vitalwatch-producer-anomaly:$VERSION
    docker tag vitalwatch-producer-anomaly:$VERSION $ACR_LOGIN_SERVER/vitalwatch-producer-anomaly:latest
    
    print_info "Publicando Producer Anomaly Detector en ACR..."
    docker push $ACR_LOGIN_SERVER/vitalwatch-producer-anomaly:$VERSION
    docker push $ACR_LOGIN_SERVER/vitalwatch-producer-anomaly:latest
    print_success "Producer Anomaly Detector publicado"
    cd ..
    
    # Producer 2: Summary Generator
    print_info "Construyendo Producer Summary Generator..."
    cd producer-summary
    docker build --platform linux/amd64 -t vitalwatch-producer-summary:$VERSION .
    docker tag vitalwatch-producer-summary:$VERSION $ACR_LOGIN_SERVER/vitalwatch-producer-summary:$VERSION
    docker tag vitalwatch-producer-summary:$VERSION $ACR_LOGIN_SERVER/vitalwatch-producer-summary:latest
    
    print_info "Publicando Producer Summary Generator en ACR..."
    docker push $ACR_LOGIN_SERVER/vitalwatch-producer-summary:$VERSION
    docker push $ACR_LOGIN_SERVER/vitalwatch-producer-summary:latest
    print_success "Producer Summary Generator publicado"
    cd ..
    
    # Consumer 1: DB Saver
    print_info "Construyendo Consumer DB Saver..."
    cd consumer-db-saver
    docker build --platform linux/amd64 -t vitalwatch-consumer-db:$VERSION .
    docker tag vitalwatch-consumer-db:$VERSION $ACR_LOGIN_SERVER/vitalwatch-consumer-db:$VERSION
    docker tag vitalwatch-consumer-db:$VERSION $ACR_LOGIN_SERVER/vitalwatch-consumer-db:latest
    
    print_info "Publicando Consumer DB Saver en ACR..."
    docker push $ACR_LOGIN_SERVER/vitalwatch-consumer-db:$VERSION
    docker push $ACR_LOGIN_SERVER/vitalwatch-consumer-db:latest
    print_success "Consumer DB Saver publicado"
    cd ..
    
    # Consumer 2: JSON Generator
    print_info "Construyendo Consumer JSON Generator..."
    cd consumer-json-generator
    docker build --platform linux/amd64 -t vitalwatch-consumer-json:$VERSION .
    docker tag vitalwatch-consumer-json:$VERSION $ACR_LOGIN_SERVER/vitalwatch-consumer-json:$VERSION
    docker tag vitalwatch-consumer-json:$VERSION $ACR_LOGIN_SERVER/vitalwatch-consumer-json:latest
    
    print_info "Publicando Consumer JSON Generator en ACR..."
    docker push $ACR_LOGIN_SERVER/vitalwatch-consumer-json:$VERSION
    docker push $ACR_LOGIN_SERVER/vitalwatch-consumer-json:latest
    print_success "Consumer JSON Generator publicado"
    cd ..
    
    # RabbitMQ Management (imagen oficial)
    print_info "RabbitMQ usará la imagen oficial de Docker Hub"
    print_success "rabbitmq:3.12-management"
    
    # Verificar imágenes
    print_info "Imágenes RabbitMQ en ACR:"
    az acr repository list --name $ACR_NAME --output table | grep -E "producer|consumer"
}

# Crear Azure Service Bus como alternativa a RabbitMQ
create_service_bus() {
    print_header "Configurando Azure Service Bus"
    
    SERVICE_BUS_NAMESPACE="sb-vitalwatch-${RANDOM}"
    
    print_info "Creando Service Bus Namespace..."
    az servicebus namespace create \
        --resource-group $RESOURCE_GROUP \
        --name $SERVICE_BUS_NAMESPACE \
        --location $LOCATION \
        --sku Standard \
        --tags $TAGS
    
    print_info "Creando colas..."
    az servicebus queue create \
        --resource-group $RESOURCE_GROUP \
        --namespace-name $SERVICE_BUS_NAMESPACE \
        --name vital-signs-alerts \
        --max-size 1024
    
    az servicebus queue create \
        --resource-group $RESOURCE_GROUP \
        --namespace-name $SERVICE_BUS_NAMESPACE \
        --name vital-signs-summary \
        --max-size 1024
    
    # Obtener connection string
    export SERVICE_BUS_CONNECTION=$(az servicebus namespace authorization-rule keys list \
        --resource-group $RESOURCE_GROUP \
        --namespace-name $SERVICE_BUS_NAMESPACE \
        --name RootManageSharedAccessKey \
        --query primaryConnectionString \
        --output tsv)
    
    print_success "Service Bus configurado: $SERVICE_BUS_NAMESPACE"
    
    # Guardar en Key Vault
    if [[ -n "$KEYVAULT_NAME" ]]; then
        print_info "Guardando connection string en Key Vault..."
        az keyvault secret set \
            --vault-name $KEYVAULT_NAME \
            --name "service-bus-connection" \
            --value "$SERVICE_BUS_CONNECTION"
        print_success "Connection string guardado en Key Vault"
    fi
}

# Desplegar RabbitMQ en Container App
deploy_rabbitmq() {
    print_header "Desplegando RabbitMQ"
    
    print_info "Creando Container App para RabbitMQ..."
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
            "RABBITMQ_DEFAULT_PASS=hospital123" \
        --tags $TAGS
    
    # Obtener URL interna
    export RABBITMQ_HOST=$(az containerapp show \
        --name vitalwatch-rabbitmq \
        --resource-group $RESOURCE_GROUP \
        --query properties.configuration.ingress.fqdn \
        --output tsv)
    
    print_success "RabbitMQ desplegado: $RABBITMQ_HOST"
}

# Desplegar Producer 1: Anomaly Detector
deploy_producer_anomaly() {
    print_header "Desplegando Producer: Anomaly Detector"
    
    print_info "Creando Container App..."
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
            "SPRING_PROFILES_ACTIVE=azure" \
        --tags $TAGS
    
    export PRODUCER_ANOMALY_URL=$(az containerapp show \
        --name vitalwatch-producer-anomaly \
        --resource-group $RESOURCE_GROUP \
        --query properties.configuration.ingress.fqdn \
        --output tsv)
    
    print_success "Producer Anomaly desplegado: https://$PRODUCER_ANOMALY_URL"
}

# Desplegar Producer 2: Summary Generator
deploy_producer_summary() {
    print_header "Desplegando Producer: Summary Generator"
    
    print_info "Creando Container App..."
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
            "SPRING_PROFILES_ACTIVE=azure" \
        --tags $TAGS
    
    export PRODUCER_SUMMARY_URL=$(az containerapp show \
        --name vitalwatch-producer-summary \
        --resource-group $RESOURCE_GROUP \
        --query properties.configuration.ingress.fqdn \
        --output tsv)
    
    print_success "Producer Summary desplegado: https://$PRODUCER_SUMMARY_URL"
}

# Desplegar Consumer 1: DB Saver
deploy_consumer_db() {
    print_header "Desplegando Consumer: DB Saver"
    
    print_info "Creando Container App..."
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
            "SPRING_PROFILES_ACTIVE=azure" \
        --tags $TAGS
    
    print_success "Consumer DB Saver desplegado"
}

# Desplegar Consumer 2: JSON Generator
deploy_consumer_json() {
    print_header "Desplegando Consumer: JSON Generator"
    
    print_info "Creando Container App..."
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
            "SPRING_PROFILES_ACTIVE=azure" \
        --tags $TAGS
    
    print_success "Consumer JSON Generator desplegado"
}

# Validar despliegue
validate_deployment() {
    print_header "Validando Despliegue RabbitMQ"
    
    print_info "Esperando a que los servicios estén listos (60 segundos)..."
    sleep 60
    
    # Health check Producer Anomaly
    print_info "Probando Producer Anomaly health check..."
    if curl -f -s "https://$PRODUCER_ANOMALY_URL/api/v1/vital-signs/health" > /dev/null; then
        print_success "Producer Anomaly health check: OK"
    else
        print_warning "Producer Anomaly health check: FAILED (puede tardar en estar listo)"
    fi
    
    # Health check Producer Summary
    print_info "Probando Producer Summary health check..."
    if curl -f -s "https://$PRODUCER_SUMMARY_URL/api/v1/summary/health" > /dev/null; then
        print_success "Producer Summary health check: OK"
    else
        print_warning "Producer Summary health check: FAILED (puede tardar en estar listo)"
    fi
    
    # Verificar que los consumidores estén corriendo
    print_info "Verificando estado de consumidores..."
    CONSUMER_DB_STATUS=$(az containerapp show \
        --name vitalwatch-consumer-db \
        --resource-group $RESOURCE_GROUP \
        --query properties.runningStatus \
        --output tsv)
    
    CONSUMER_JSON_STATUS=$(az containerapp show \
        --name vitalwatch-consumer-json \
        --resource-group $RESOURCE_GROUP \
        --query properties.runningStatus \
        --output tsv)
    
    if [[ "$CONSUMER_DB_STATUS" == "Running" ]]; then
        print_success "Consumer DB Saver: Running"
    else
        print_warning "Consumer DB Saver: $CONSUMER_DB_STATUS"
    fi
    
    if [[ "$CONSUMER_JSON_STATUS" == "Running" ]]; then
        print_success "Consumer JSON Generator: Running"
    else
        print_warning "Consumer JSON Generator: $CONSUMER_JSON_STATUS"
    fi
}

# Mostrar resumen
show_summary() {
    print_header "✅ DESPLIEGUE RABBITMQ COMPLETADO"
    
    echo -e "${GREEN}URLs de Microservicios:${NC}"
    echo -e "  📡 Producer Anomaly:   https://$PRODUCER_ANOMALY_URL"
    echo -e "  📊 Producer Summary:   https://$PRODUCER_SUMMARY_URL"
    echo -e "  🔌 RabbitMQ (interno): $RABBITMQ_HOST"
    echo ""
    echo -e "${GREEN}Consumidores (sin URL pública):${NC}"
    echo -e "  💾 Consumer DB Saver:  vitalwatch-consumer-db"
    echo -e "  📄 Consumer JSON Gen:  vitalwatch-consumer-json"
    echo ""
    echo -e "${GREEN}Health Checks:${NC}"
    echo -e "  curl https://$PRODUCER_ANOMALY_URL/api/v1/vital-signs/health"
    echo -e "  curl https://$PRODUCER_SUMMARY_URL/api/v1/summary/health"
    echo ""
    echo -e "${YELLOW}Próximos Pasos:${NC}"
    echo -e "  1. Probar envío de signos vitales al Producer Anomaly"
    echo -e "  2. Verificar que las alertas se guardan en Oracle"
    echo -e "  3. Revisar logs: az containerapp logs show --name vitalwatch-producer-anomaly --resource-group $RESOURCE_GROUP --follow"
    echo -e "  4. Consulta la documentación en: README_RABBITMQ.md"
    echo ""
    echo -e "${BLUE}========================================${NC}\n"
}

# Función principal
main() {
    clear
    echo -e "${BLUE}"
    cat << "EOF"
╔═══════════════════════════════════════════════════════════════╗
║                                                               ║
║     VitalWatch - Despliegue RabbitMQ en Azure                ║
║     Versión 1.0.0                                            ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
EOF
    echo -e "${NC}\n"
    
    print_warning "Este script desplegará los microservicios RabbitMQ en Azure"
    print_warning "Se crearán recursos adicionales que pueden generar costos"
    read -p "¿Deseas continuar? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Despliegue cancelado"
        exit 0
    fi
    
    # Ejecutar pasos
    load_config
    azure_login
    build_and_push_rabbitmq_images
    deploy_rabbitmq
    deploy_producer_anomaly
    deploy_producer_summary
    deploy_consumer_db
    deploy_consumer_json
    validate_deployment
    show_summary
    
    # Guardar URLs en archivo
    cat >> azure-deployment-urls.txt << EOF

RabbitMQ Microservicios - Agregados $(date)
==========================================
Producer Anomaly:   https://$PRODUCER_ANOMALY_URL
Producer Summary:   https://$PRODUCER_SUMMARY_URL
RabbitMQ (interno): $RABBITMQ_HOST
Consumer DB Saver:  vitalwatch-consumer-db (sin URL pública)
Consumer JSON Gen:  vitalwatch-consumer-json (sin URL pública)
EOF
    
    print_success "URLs guardadas en: azure-deployment-urls.txt"
}

# Manejar Ctrl+C
trap 'echo -e "\n${RED}Despliegue interrumpido${NC}"; exit 1' INT

# Ejecutar script
main "$@"
