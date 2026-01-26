#!/bin/bash

################################################################################
# Script de Despliegue Automatizado en Azure - VitalWatch
# Este script automatiza todo el proceso de despliegue en Azure
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

# Verificar prerequisitos
check_prerequisites() {
    print_header "Verificando Prerequisitos"
    
    # Verificar Azure CLI
    if ! command -v az &> /dev/null; then
        print_error "Azure CLI no está instalado"
        echo "Instalar desde: https://docs.microsoft.com/cli/azure/install-azure-cli"
        exit 1
    fi
    print_success "Azure CLI instalado"
    
    # Verificar Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker no está instalado"
        exit 1
    fi
    print_success "Docker instalado"
    
    # Verificar que Docker está corriendo
    if ! docker ps &> /dev/null; then
        print_error "Docker no está corriendo"
        exit 1
    fi
    print_success "Docker está corriendo"
    
    # Verificar estructura del proyecto
    if [[ ! -d "backend" ]] || [[ ! -d "frontend" ]] || [[ ! -d "api-manager" ]]; then
        print_error "Estructura del proyecto incorrecta"
        print_info "Este script debe ejecutarse desde la raíz del proyecto"
        exit 1
    fi
    print_success "Estructura del proyecto correcta"
    
    # Verificar Oracle Wallet
    if [[ ! -d "Wallet_S58ONUXCX4C1QXE9" ]]; then
        print_error "Oracle Wallet no encontrado"
        exit 1
    fi
    print_success "Oracle Wallet encontrado"
}

# Cargar configuración
load_config() {
    print_header "Cargando Configuración"
    
    if [[ -f "azure-config.env" ]]; then
        source azure-config.env
        print_success "Configuración cargada desde azure-config.env"
    else
        print_warning "Archivo azure-config.env no encontrado"
        print_info "Creando configuración por defecto..."
        
        # Solicitar configuración básica
        read -p "Nombre del Resource Group [rg-vitalwatch-prod]: " RESOURCE_GROUP
        RESOURCE_GROUP=${RESOURCE_GROUP:-rg-vitalwatch-prod}
        
        read -p "Location [eastus]: " LOCATION
        LOCATION=${LOCATION:-eastus}
        
        read -p "Nombre del Container Registry (solo letras y números) [acrvitalwatch]: " ACR_NAME
        ACR_NAME=${ACR_NAME:-acrvitalwatch}
        
        # Crear archivo de configuración
        cat > azure-config.env << EOF
# Configuración Azure - VitalWatch
export RESOURCE_GROUP="$RESOURCE_GROUP"
export LOCATION="$LOCATION"
export PROJECT_NAME="vitalwatch"

# Container Registry
export ACR_NAME="$ACR_NAME"
export ACR_SKU="Basic"

# Container Apps
export CONTAINERAPPS_ENVIRONMENT="env-vitalwatch-prod"
export BACKEND_APP_NAME="vitalwatch-backend"
export FRONTEND_APP_NAME="vitalwatch-frontend"
export GATEWAY_APP_NAME="vitalwatch-api-gateway"

# Key Vault
export KEYVAULT_NAME="kv-vitalwatch-\${RANDOM}"

# Oracle Database
export ORACLE_USERNAME="ADMIN"
export ORACLE_PASSWORD="\\\$-123.Sb-123"
export ORACLE_SERVICE="s58onuxcx4c1qxe9_high"

# Tags
export TAGS="Environment=Production Project=VitalWatch Owner=DUOC"
EOF
        source azure-config.env
        print_success "Configuración creada en azure-config.env"
    fi
    
    # Mostrar configuración
    print_info "Resource Group: $RESOURCE_GROUP"
    print_info "Location: $LOCATION"
    print_info "ACR Name: $ACR_NAME"
}

# Login en Azure
azure_login() {
    print_header "Autenticación en Azure"
    
    # Verificar si ya está logueado
    if az account show &> /dev/null; then
        print_success "Ya estás autenticado en Azure"
        az account show --output table
    else
        print_info "Iniciando login en Azure..."
        az login
    fi
    
    # Mostrar subscripciones disponibles
    print_info "Subscripciones disponibles:"
    az account list --output table
    
    # Confirmar subscripción
    CURRENT_SUB=$(az account show --query name --output tsv)
    read -p "¿Usar subscripción '$CURRENT_SUB'? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        read -p "Ingresa el nombre o ID de la subscripción a usar: " SUB_ID
        az account set --subscription "$SUB_ID"
        print_success "Subscripción cambiada"
    fi
}

# Crear Resource Group
create_resource_group() {
    print_header "Creando Resource Group"
    
    if az group exists --name $RESOURCE_GROUP &> /dev/null && [[ $(az group exists --name $RESOURCE_GROUP) == "true" ]]; then
        print_warning "Resource Group '$RESOURCE_GROUP' ya existe"
        read -p "¿Deseas continuar y usar el existente? (y/n): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    else
        print_info "Creando Resource Group..."
        az group create \
            --name $RESOURCE_GROUP \
            --location $LOCATION \
            --tags $TAGS
        print_success "Resource Group creado"
    fi
}

# Crear Container Registry
create_container_registry() {
    print_header "Configurando Azure Container Registry"
    
    # Verificar si existe
    if az acr show --name $ACR_NAME --resource-group $RESOURCE_GROUP &> /dev/null; then
        print_warning "ACR '$ACR_NAME' ya existe"
    else
        print_info "Creando Azure Container Registry..."
        az acr create \
            --resource-group $RESOURCE_GROUP \
            --name $ACR_NAME \
            --sku $ACR_SKU \
            --admin-enabled true \
            --tags $TAGS
        print_success "ACR creado"
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

# Construir y publicar imágenes
build_and_push_images() {
    print_header "Construyendo y Publicando Imágenes Docker"
    
    VERSION="v1.0.0"
    
    # Backend
    print_info "Construyendo Backend para linux/amd64..."
    cd backend
    docker build --platform linux/amd64 -t vitalwatch-backend:$VERSION .
    docker tag vitalwatch-backend:$VERSION $ACR_LOGIN_SERVER/vitalwatch-backend:$VERSION
    docker tag vitalwatch-backend:$VERSION $ACR_LOGIN_SERVER/vitalwatch-backend:latest
    
    print_info "Publicando Backend en ACR..."
    docker push $ACR_LOGIN_SERVER/vitalwatch-backend:$VERSION
    docker push $ACR_LOGIN_SERVER/vitalwatch-backend:latest
    print_success "Backend publicado"
    cd ..
    
    # API Gateway
    print_info "Construyendo API Gateway..."
    cd api-manager
    
    # Crear Dockerfile si no existe
    if [[ ! -f "Dockerfile" ]]; then
        cat > Dockerfile << 'EOF'
FROM kong:3.4

COPY kong.yml /kong/declarative/kong.yml

EXPOSE 8000 8001 8443 8444

ENV KONG_DATABASE=off
ENV KONG_DECLARATIVE_CONFIG=/kong/declarative/kong.yml
ENV KONG_PROXY_ACCESS_LOG=/dev/stdout
ENV KONG_ADMIN_ACCESS_LOG=/dev/stdout
ENV KONG_PROXY_ERROR_LOG=/dev/stderr
ENV KONG_ADMIN_ERROR_LOG=/dev/stderr

CMD ["kong", "docker-start"]
EOF
    fi
    
    docker build --platform linux/amd64 -t vitalwatch-api-gateway:$VERSION .
    docker tag vitalwatch-api-gateway:$VERSION $ACR_LOGIN_SERVER/vitalwatch-api-gateway:$VERSION
    docker tag vitalwatch-api-gateway:$VERSION $ACR_LOGIN_SERVER/vitalwatch-api-gateway:latest
    
    print_info "Publicando API Gateway en ACR..."
    docker push $ACR_LOGIN_SERVER/vitalwatch-api-gateway:$VERSION
    docker push $ACR_LOGIN_SERVER/vitalwatch-api-gateway:latest
    print_success "API Gateway publicado"
    cd ..
    
    # Frontend
    print_info "Construyendo Frontend para linux/amd64..."
    cd frontend
    docker build --platform linux/amd64 -t vitalwatch-frontend:$VERSION .
    docker tag vitalwatch-frontend:$VERSION $ACR_LOGIN_SERVER/vitalwatch-frontend:$VERSION
    docker tag vitalwatch-frontend:$VERSION $ACR_LOGIN_SERVER/vitalwatch-frontend:latest
    
    print_info "Publicando Frontend en ACR..."
    docker push $ACR_LOGIN_SERVER/vitalwatch-frontend:$VERSION
    docker push $ACR_LOGIN_SERVER/vitalwatch-frontend:latest
    print_success "Frontend publicado"
    cd ..
    
    # Verificar imágenes
    print_info "Imágenes en ACR:"
    az acr repository list --name $ACR_NAME --output table
}

# Crear Key Vault y secrets
create_key_vault() {
    print_header "Configurando Azure Key Vault"
    
    # Verificar si ya existe un Key Vault configurado
    if [[ -z "$KEYVAULT_NAME" ]]; then
        # Nombre único para Key Vault
        KEYVAULT_NAME="kv-vitalwatch-${RANDOM}"
    fi
    
    # Verificar si el Key Vault ya existe
    EXISTING_KV=$(az keyvault list --resource-group $RESOURCE_GROUP --query "[?name=='$KEYVAULT_NAME'].name" -o tsv 2>/dev/null)
    
    if [[ -n "$EXISTING_KV" ]]; then
        print_info "Key Vault ya existe: $KEYVAULT_NAME"
    else
        print_info "Creando Key Vault..."
        az keyvault create \
            --name $KEYVAULT_NAME \
            --resource-group $RESOURCE_GROUP \
            --location $LOCATION \
            --enable-rbac-authorization true \
            --tags $TAGS
        
        # Obtener el ID del usuario actual
        USER_ID=$(az ad signed-in-user show --query id -o tsv)
        
        # Asignar permisos al usuario
        print_info "Asignando permisos al usuario..."
        az role assignment create \
            --role "Key Vault Secrets Officer" \
            --assignee $USER_ID \
            --scope "/subscriptions/$(az account show --query id -o tsv)/resourceGroups/$RESOURCE_GROUP/providers/Microsoft.KeyVault/vaults/$KEYVAULT_NAME"
        
        # Esperar a que los permisos se propaguen
        print_info "Esperando propagación de permisos (30 segundos)..."
        sleep 30
    fi
    
    # Agregar o actualizar secrets
    print_info "Configurando secrets..."
    az keyvault secret set --vault-name $KEYVAULT_NAME --name "oracle-username" --value "$ORACLE_USERNAME" 2>/dev/null || print_error "Error agregando oracle-username"
    az keyvault secret set --vault-name $KEYVAULT_NAME --name "oracle-password" --value "$ORACLE_PASSWORD" 2>/dev/null || print_error "Error agregando oracle-password"
    az keyvault secret set --vault-name $KEYVAULT_NAME --name "oracle-service" --value "$ORACLE_SERVICE" 2>/dev/null || print_error "Error agregando oracle-service"
    
    # Actualizar archivo de configuración con el nombre del Key Vault
    if [[ -f "azure-config.env" ]]; then
        if grep -q "KEYVAULT_NAME" azure-config.env; then
            sed -i.bak "s/export KEYVAULT_NAME=.*/export KEYVAULT_NAME=\"$KEYVAULT_NAME\"/" azure-config.env
        else
            echo "export KEYVAULT_NAME=\"$KEYVAULT_NAME\"" >> azure-config.env
        fi
    fi
    
    print_success "Key Vault configurado: $KEYVAULT_NAME"
}

# Crear Container Apps Environment
create_container_apps_environment() {
    print_header "Creando Container Apps Environment"
    
    # Instalar extensión
    print_info "Instalando extensión de Container Apps..."
    az extension add --name containerapp --upgrade --yes
    
    # Registrar providers
    print_info "Registrando providers..."
    az provider register --namespace Microsoft.App
    az provider register --namespace Microsoft.OperationalInsights
    
    # Crear environment
    print_info "Creando environment..."
    az containerapp env create \
        --name $CONTAINERAPPS_ENVIRONMENT \
        --resource-group $RESOURCE_GROUP \
        --location $LOCATION \
        --tags $TAGS
    
    print_success "Environment creado: $CONTAINERAPPS_ENVIRONMENT"
}

# Desplegar Backend
deploy_backend() {
    print_header "Desplegando Backend"
    
    print_info "Creando Container App para Backend..."
    az containerapp create \
        --name $BACKEND_APP_NAME \
        --resource-group $RESOURCE_GROUP \
        --environment $CONTAINERAPPS_ENVIRONMENT \
        --image $ACR_LOGIN_SERVER/vitalwatch-backend:latest \
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
            "ORACLE_USERNAME=$ORACLE_USERNAME" \
            "ORACLE_PASSWORD=$ORACLE_PASSWORD" \
            "TNS_ADMIN=/app/wallet" \
            "SERVER_PORT=8080" \
            "SPRING_APPLICATION_NAME=vitalwatch-backend" \
        --tags $TAGS
    
    # Obtener URL
    export BACKEND_URL=$(az containerapp show \
        --name $BACKEND_APP_NAME \
        --resource-group $RESOURCE_GROUP \
        --query properties.configuration.ingress.fqdn \
        --output tsv)
    
    print_success "Backend desplegado: https://$BACKEND_URL"
}

# Desplegar API Gateway
deploy_api_gateway() {
    print_header "Desplegando API Gateway"
    
    print_info "Creando Container App para API Gateway..."
    az containerapp create \
        --name $GATEWAY_APP_NAME \
        --resource-group $RESOURCE_GROUP \
        --environment $CONTAINERAPPS_ENVIRONMENT \
        --image $ACR_LOGIN_SERVER/vitalwatch-api-gateway:latest \
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
        --tags $TAGS
    
    # Obtener URL
    export GATEWAY_URL=$(az containerapp show \
        --name $GATEWAY_APP_NAME \
        --resource-group $RESOURCE_GROUP \
        --query properties.configuration.ingress.fqdn \
        --output tsv)
    
    print_success "API Gateway desplegado: https://$GATEWAY_URL"
}

# Desplegar Frontend
deploy_frontend() {
    print_header "Desplegando Frontend"
    
    print_info "Creando Container App para Frontend..."
    az containerapp create \
        --name $FRONTEND_APP_NAME \
        --resource-group $RESOURCE_GROUP \
        --environment $CONTAINERAPPS_ENVIRONMENT \
        --image $ACR_LOGIN_SERVER/vitalwatch-frontend:latest \
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
    
    # Obtener URL
    export FRONTEND_URL=$(az containerapp show \
        --name $FRONTEND_APP_NAME \
        --resource-group $RESOURCE_GROUP \
        --query properties.configuration.ingress.fqdn \
        --output tsv)
    
    print_success "Frontend desplegado: https://$FRONTEND_URL"
}

# Configurar CORS
configure_cors() {
    print_header "Configurando CORS"
    
    print_info "Actualizando configuración de CORS en Backend..."
    az containerapp update \
        --name $BACKEND_APP_NAME \
        --resource-group $RESOURCE_GROUP \
        --set-env-vars "ALLOWED_ORIGINS=https://$FRONTEND_URL,https://$GATEWAY_URL"
    
    print_success "CORS configurado"
}

# Validar despliegue
validate_deployment() {
    print_header "Validando Despliegue"
    
    print_info "Esperando a que los servicios estén listos (30 segundos)..."
    sleep 30
    
    # Health check Backend
    print_info "Probando Backend health check..."
    if curl -f -s "https://$BACKEND_URL/api/v1/health" > /dev/null; then
        print_success "Backend health check: OK"
    else
        print_warning "Backend health check: FAILED (puede tardar unos minutos en estar listo)"
    fi
    
    # Health check API Gateway
    print_info "Probando API Gateway..."
    if curl -f -s "https://$GATEWAY_URL/api/v1/health" > /dev/null; then
        print_success "API Gateway health check: OK"
    else
        print_warning "API Gateway health check: FAILED (puede tardar unos minutos en estar listo)"
    fi
    
    # Frontend
    print_info "Probando Frontend..."
    if curl -f -s "https://$FRONTEND_URL" > /dev/null; then
        print_success "Frontend: OK"
    else
        print_warning "Frontend: FAILED (puede tardar unos minutos en estar listo)"
    fi
}

# Mostrar resumen
show_summary() {
    print_header "✅ DESPLIEGUE COMPLETADO"
    
    echo -e "${GREEN}URLs de la Aplicación:${NC}"
    echo -e "  📱 Frontend:        https://$FRONTEND_URL"
    echo -e "  🔌 API Gateway:     https://$GATEWAY_URL"
    echo -e "  ⚙️  Backend:         https://$BACKEND_URL"
    echo ""
    echo -e "${GREEN}Recursos en Azure:${NC}"
    echo -e "  📦 Resource Group:  $RESOURCE_GROUP"
    echo -e "  🐳 ACR:             $ACR_NAME"
    echo -e "  🗝️  Key Vault:       $KEYVAULT_NAME"
    echo ""
    echo -e "${GREEN}Credenciales de Prueba:${NC}"
    echo -e "  Admin:      admin@vitalwatch.com / Admin123!"
    echo -e "  Médico:     medico@vitalwatch.com / Medico123!"
    echo -e "  Enfermera:  enfermera@vitalwatch.com / Enfermera123!"
    echo ""
    echo -e "${YELLOW}Próximos Pasos:${NC}"
    echo -e "  1. Abre https://$FRONTEND_URL en tu navegador"
    echo -e "  2. Inicia sesión con las credenciales de prueba"
    echo -e "  3. Revisa los logs: az containerapp logs show --name $BACKEND_APP_NAME --resource-group $RESOURCE_GROUP --follow"
    echo -e "  4. Consulta la guía completa en: docs/GUIA_DESPLIEGUE_AZURE.md"
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
║     VitalWatch - Despliegue Automatizado en Azure            ║
║     Versión 1.0.0                                            ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
EOF
    echo -e "${NC}\n"
    
    print_warning "Este script desplegará VitalWatch en Azure"
    print_warning "Se crearán recursos que pueden generar costos"
    read -p "¿Deseas continuar? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Despliegue cancelado"
        exit 0
    fi
    
    # Ejecutar pasos
    check_prerequisites
    load_config
    azure_login
    create_resource_group
    create_container_registry
    build_and_push_images
    create_key_vault
    create_container_apps_environment
    deploy_backend
    deploy_api_gateway
    deploy_frontend
    configure_cors
    validate_deployment
    show_summary
    
    # Guardar URLs en archivo
    cat > azure-deployment-urls.txt << EOF
VitalWatch - URLs de Despliegue en Azure
=========================================
Fecha: $(date)

Frontend:        https://$FRONTEND_URL
API Gateway:     https://$GATEWAY_URL
Backend:         https://$BACKEND_URL

Resource Group:  $RESOURCE_GROUP
ACR:             $ACR_NAME
Key Vault:       $KEYVAULT_NAME
Location:        $LOCATION
EOF
    
    print_success "URLs guardadas en: azure-deployment-urls.txt"
}

# Manejar Ctrl+C
trap 'echo -e "\n${RED}Despliegue interrumpido${NC}"; exit 1' INT

# Ejecutar script
main "$@"
