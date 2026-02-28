#!/bin/bash

# ============================================================================
# VitalWatch - Script de Despliegue Automático
# ============================================================================
# Descripción: Despliega toda la aplicación con verificaciones automáticas
# Uso: ./deploy.sh
# ============================================================================

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Variables
PROJECT_NAME="VitalWatch"
WALLET_DIR="Wallet_S58ONUXCX4C1QXE9"

# ============================================================================
# Funciones de Utilidad
# ============================================================================

print_header() {
    echo ""
    echo "============================================================================"
    echo -e "${BLUE}  $1${NC}"
    echo "============================================================================"
    echo ""
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# ============================================================================
# Verificaciones de Prerrequisitos
# ============================================================================

check_prerequisites() {
    print_header "Verificando Prerrequisitos"
    
    local all_ok=true
    
    # Verificar Docker
    if command -v docker &> /dev/null; then
        DOCKER_VERSION=$(docker --version | awk '{print $3}' | sed 's/,//')
        print_success "Docker instalado: $DOCKER_VERSION"
    else
        print_error "Docker no está instalado"
        all_ok=false
    fi
    
    # Verificar Docker Compose
    if command -v docker-compose &> /dev/null; then
        COMPOSE_VERSION=$(docker-compose --version | awk '{print $4}' | sed 's/,//')
        print_success "Docker Compose instalado: $COMPOSE_VERSION"
    else
        print_error "Docker Compose no está instalado"
        all_ok=false
    fi
    
    # Verificar que Docker esté corriendo
    if docker info &> /dev/null; then
        print_success "Docker daemon está corriendo"
    else
        print_error "Docker daemon no está corriendo. Inicia Docker Desktop."
        all_ok=false
    fi
    
    # Verificar Wallet de Oracle
    if [ -d "$WALLET_DIR" ]; then
        print_success "Wallet de Oracle encontrado: $WALLET_DIR"
        
        # Verificar archivos críticos del Wallet
        local wallet_files=("cwallet.sso" "keystore.jks" "truststore.jks" "tnsnames.ora" "sqlnet.ora")
        for file in "${wallet_files[@]}"; do
            if [ -f "$WALLET_DIR/$file" ]; then
                print_info "  ✓ $file"
            else
                print_warning "  ✗ $file no encontrado"
            fi
        done
    else
        print_error "Wallet de Oracle no encontrado: $WALLET_DIR"
        print_info "Asegúrate de que la carpeta $WALLET_DIR esté en el directorio raíz"
        all_ok=false
    fi
    
    if [ "$all_ok" = false ]; then
        print_error "Algunos prerrequisitos no se cumplen. Por favor, corrígelos antes de continuar."
        exit 1
    fi
    
    print_success "Todos los prerrequisitos se cumplen"
}

# ============================================================================
# Detener Servicios Existentes
# ============================================================================

stop_existing_services() {
    print_header "Deteniendo Servicios Existentes"
    
    if docker-compose ps 2>/dev/null | grep -q "Up"; then
        print_info "Deteniendo contenedores existentes..."
        docker-compose down
        print_success "Servicios detenidos"
    else
        print_info "No hay servicios corriendo"
    fi
}

# ============================================================================
# Construir Imágenes
# ============================================================================

build_images() {
    print_header "Construyendo Imágenes Docker"
    
    print_info "Esto puede tomar varios minutos..."
    
    # Construir backend
    print_info "Construyendo Backend (Spring Boot)..."
    if docker-compose build backend; then
        print_success "Backend construido"
    else
        print_error "Error al construir el backend"
        exit 1
    fi
    
    # Construir frontend
    print_info "Construyendo Frontend (Angular)..."
    if docker-compose build frontend; then
        print_success "Frontend construido"
    else
        print_error "Error al construir el frontend"
        exit 1
    fi
    
    # Pull API Gateway
    print_info "Descargando API Gateway (Kong)..."
    if docker-compose pull api-gateway; then
        print_success "API Gateway listo"
    else
        print_error "Error al descargar API Gateway"
        exit 1
    fi
    
    print_success "Todas las imágenes construidas exitosamente"
}

# ============================================================================
# Iniciar Servicios
# ============================================================================

start_services() {
    print_header "Iniciando Servicios"
    
    print_info "Levantando contenedores..."
    if docker-compose up -d; then
        print_success "Servicios iniciados"
    else
        print_error "Error al iniciar los servicios"
        exit 1
    fi
    
    # Mostrar estado
    echo ""
    docker-compose ps
}

# ============================================================================
# Health Checks
# ============================================================================

wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    print_info "Esperando a que $service_name esté listo..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "$url" > /dev/null 2>&1; then
            print_success "$service_name está listo"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_warning "$service_name no respondió en el tiempo esperado"
    return 1
}

health_checks() {
    print_header "Verificando Salud de los Servicios"
    
    # Esperar un poco para que los servicios inicien
    print_info "Esperando 15 segundos para que los servicios inicien..."
    sleep 15
    
    # Backend
    wait_for_service "http://localhost:8080/api/v1/health" "Backend"
    
    # Frontend (puerto 80, no 4200)
    wait_for_service "http://localhost:80" "Frontend"
    
    # API Gateway
    wait_for_service "http://localhost:8000/api/v1/health" "API Gateway" || print_info "API Gateway puede estar configurándose..."
    
    # Verificar conexión a base de datos
    print_info "Verificando conexión a base de datos..."
    if curl -f -s "http://localhost:8080/api/v1/health/database" > /dev/null 2>&1; then
        print_success "Conexión a Oracle Cloud establecida"
    else
        print_warning "No se pudo verificar la conexión a la base de datos"
        print_info "Revisa los logs: docker-compose logs backend"
    fi
}

# ============================================================================
# Mostrar Información de Acceso
# ============================================================================

show_access_info() {
    print_header "Despliegue Completado"
    
    echo ""
    echo -e "${GREEN}🎉 ¡$PROJECT_NAME está corriendo!${NC}"
    echo ""
    echo "📍 URLs de Acceso:"
    echo ""
    echo -e "  ${BLUE}Frontend:${NC}     http://localhost"
    echo -e "  ${BLUE}Backend API:${NC}  http://localhost:8080"
    echo -e "  ${BLUE}Swagger UI:${NC}   http://localhost:8080/swagger-ui.html"
    echo -e "  ${BLUE}API Gateway:${NC}  http://localhost:8000"
    echo ""
    echo "🔐 Credenciales de Prueba:"
    echo ""
    echo "  Admin:"
    echo "    Email:    admin@vitalwatch.com"
    echo "    Password: Admin123!"
    echo ""
    echo "  Médico:"
    echo "    Email:    medico@vitalwatch.com"
    echo "    Password: Medico123!"
    echo ""
    echo "  Enfermera:"
    echo "    Email:    enfermera@vitalwatch.com"
    echo "    Password: Enfermera123!"
    echo ""
    echo "📚 Comandos Útiles:"
    echo ""
    echo "  Ver logs:           docker-compose logs -f"
    echo "  Ver logs backend:   docker-compose logs -f backend"
    echo "  Ver logs frontend:  docker-compose logs -f frontend"
    echo "  Detener servicios:  docker-compose down"
    echo "  Reiniciar:          docker-compose restart"
    echo ""
    echo "📖 Documentación:"
    echo ""
    echo "  README:             README.md"
    echo "  Arquitectura:       docs/ARQUITECTURA.md"
    echo "  Guía Integración:   docs/GUIA_INTEGRACION.md"
    echo "  Postman Collection: docs/postman-collection.json"
    echo ""
    echo "============================================================================"
    echo ""
}

# ============================================================================
# Función Principal
# ============================================================================

main() {
    clear
    
    print_header "$PROJECT_NAME - Despliegue Automático"
    
    # Ejecutar pasos
    check_prerequisites
    stop_existing_services
    build_images
    start_services
    health_checks
    show_access_info
    
    # Preguntar si quiere ver logs
    echo ""
    read -p "¿Deseas ver los logs en tiempo real? (s/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        docker-compose logs -f
    fi
}

# ============================================================================
# Manejo de Errores
# ============================================================================

trap 'echo ""; print_error "Script interrumpido"; exit 1' INT TERM

# ============================================================================
# Ejecutar
# ============================================================================

main "$@"
