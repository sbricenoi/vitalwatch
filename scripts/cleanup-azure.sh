#!/bin/bash

################################################################################
# Script de Limpieza de Recursos Azure - VitalWatch
# Este script elimina todos los recursos creados en Azure
################################################################################

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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
if [[ -f "azure-config.env" ]]; then
    source azure-config.env
    print_success "Configuración cargada"
else
    print_error "Archivo azure-config.env no encontrado"
    print_info "Por favor, proporciona el nombre del Resource Group manualmente"
    read -p "Resource Group: " RESOURCE_GROUP
fi

clear
echo -e "${RED}"
cat << "EOF"
╔═══════════════════════════════════════════════════════════════╗
║                                                               ║
║     ⚠️  ADVERTENCIA: LIMPIEZA DE RECURSOS AZURE  ⚠️           ║
║                                                               ║
║     Este script eliminará TODOS los recursos de Azure        ║
║     asociados al proyecto VitalWatch.                        ║
║                                                               ║
║     Esta acción es IRREVERSIBLE.                             ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}\n"

# Verificar login
if ! az account show &> /dev/null; then
    print_error "No estás autenticado en Azure"
    print_info "Ejecuta: az login"
    exit 1
fi

# Mostrar recursos a eliminar
print_header "Recursos a Eliminar"

if az group exists --name $RESOURCE_GROUP &> /dev/null && [[ $(az group exists --name $RESOURCE_GROUP) == "true" ]]; then
    print_info "Resource Group: $RESOURCE_GROUP"
    
    echo -e "\n${YELLOW}Recursos encontrados:${NC}"
    az resource list --resource-group $RESOURCE_GROUP --output table
    
    # Contar recursos
    RESOURCE_COUNT=$(az resource list --resource-group $RESOURCE_GROUP --query "length(@)" --output tsv)
    echo -e "\n${YELLOW}Total de recursos: $RESOURCE_COUNT${NC}\n"
else
    print_error "Resource Group '$RESOURCE_GROUP' no existe"
    exit 1
fi

# Confirmación múltiple para seguridad
print_warning "¿Estás ABSOLUTAMENTE seguro de que deseas eliminar todos estos recursos?"
read -p "Escribe 'ELIMINAR' en mayúsculas para confirmar: " CONFIRMATION

if [[ "$CONFIRMATION" != "ELIMINAR" ]]; then
    print_info "Operación cancelada"
    exit 0
fi

print_warning "Segunda confirmación: Esto eliminará permanentemente todos los datos"
read -p "¿Continuar? (yes/no): " CONFIRMATION2

if [[ "$CONFIRMATION2" != "yes" ]]; then
    print_info "Operación cancelada"
    exit 0
fi

# Eliminar recursos paso a paso (opcional: más control)
print_header "Iniciando Limpieza"

# Opción 1: Eliminación rápida del Resource Group completo
print_info "Eliminando Resource Group completo..."
print_warning "Esto puede tardar varios minutos..."

az group delete \
    --name $RESOURCE_GROUP \
    --yes \
    --no-wait

print_success "Comando de eliminación enviado"
print_info "La eliminación se está procesando en segundo plano"

# Esperar un momento para verificar
sleep 5

print_info "Verificando estado de eliminación..."
while az group exists --name $RESOURCE_GROUP &> /dev/null && [[ $(az group exists --name $RESOURCE_GROUP) == "true" ]]; do
    echo -n "."
    sleep 5
done

echo ""
print_success "Resource Group eliminado completamente"

# Limpiar archivos locales de configuración (opcional)
print_header "Limpieza de Archivos Locales"

print_warning "¿Deseas eliminar también los archivos de configuración local? (azure-config.env, azure-deployment-urls.txt)"
read -p "(y/n): " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]; then
    if [[ -f "azure-config.env" ]]; then
        rm azure-config.env
        print_success "azure-config.env eliminado"
    fi
    
    if [[ -f "azure-deployment-urls.txt" ]]; then
        rm azure-deployment-urls.txt
        print_success "azure-deployment-urls.txt eliminado"
    fi
fi

# Resumen final
print_header "✅ LIMPIEZA COMPLETADA"

echo -e "${GREEN}Todos los recursos han sido eliminados:${NC}"
echo -e "  ✓ Container Apps"
echo -e "  ✓ Container Apps Environment"
echo -e "  ✓ Azure Container Registry"
echo -e "  ✓ Key Vault"
echo -e "  ✓ Log Analytics Workspace"
echo -e "  ✓ Application Insights"
echo -e "  ✓ Virtual Network (si existía)"
echo -e "  ✓ Resource Group"
echo ""
echo -e "${YELLOW}Nota: Las imágenes Docker locales no fueron eliminadas${NC}"
echo -e "${YELLOW}Para eliminarlas, ejecuta:${NC}"
echo -e "  docker rmi \$(docker images 'vitalwatch-*' -q)"
echo ""
echo -e "${BLUE}========================================${NC}\n"

# Registro de limpieza
cat > cleanup-log.txt << EOF
VitalWatch - Registro de Limpieza Azure
=========================================
Fecha: $(date)
Resource Group Eliminado: $RESOURCE_GROUP
Usuario: $(az account show --query user.name --output tsv)
Subscripción: $(az account show --query name --output tsv)

Estado: COMPLETADO
EOF

print_success "Registro guardado en: cleanup-log.txt"
