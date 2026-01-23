#!/bin/bash

# ============================================================================
# VitalWatch - Script de Inicio Rápido
# ============================================================================

set -e  # Salir si hay algún error

echo "============================================================================"
echo "  VitalWatch - Sistema de Alertas Médicas en Tiempo Real"
echo "============================================================================"
echo ""

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verificar configuración de Oracle (ya está en application.properties)
echo -e "${GREEN}✅ Configuración de Oracle verificada${NC}"
echo "   Contraseña configurada en: backend/src/main/resources/application.properties"
echo ""

# Verificar si existe el Wallet de Oracle
if [ ! -d "Wallet_S58ONUXCX4C1QXE9" ]; then
    echo -e "${RED}❌ ERROR: No se encontró el Wallet de Oracle Cloud${NC}"
    echo "Asegúrate de que la carpeta Wallet_S58ONUXCX4C1QXE9 esté en el directorio raíz"
    exit 1
fi

echo -e "${GREEN}✅ Wallet de Oracle encontrado${NC}"
echo ""

# Verificar Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ ERROR: Docker no está instalado${NC}"
    echo "Por favor instala Docker desde: https://docs.docker.com/get-docker/"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ ERROR: Docker Compose no está instalado${NC}"
    echo "Por favor instala Docker Compose desde: https://docs.docker.com/compose/install/"
    exit 1
fi

echo -e "${GREEN}✅ Docker y Docker Compose encontrados${NC}"
echo ""

# Preguntar qué servicios iniciar
echo "¿Qué deseas hacer?"
echo "1) Iniciar solo el backend"
echo "2) Iniciar backend + frontend"
echo "3) Iniciar todo (backend + frontend + API Gateway)"
echo "4) Detener todos los servicios"
echo "5) Ver logs"
read -p "Selecciona una opción (1-5): " option

case $option in
    1)
        echo ""
        echo -e "${GREEN}🚀 Iniciando backend...${NC}"
        docker-compose up -d backend
        ;;
    2)
        echo ""
        echo -e "${GREEN}🚀 Iniciando backend y frontend...${NC}"
        docker-compose up -d backend frontend
        ;;
    3)
        echo ""
        echo -e "${GREEN}🚀 Iniciando todos los servicios...${NC}"
        docker-compose up -d
        ;;
    4)
        echo ""
        echo -e "${YELLOW}🛑 Deteniendo todos los servicios...${NC}"
        docker-compose down
        echo -e "${GREEN}✅ Servicios detenidos${NC}"
        exit 0
        ;;
    5)
        echo ""
        echo "Selecciona el servicio:"
        echo "1) Backend"
        echo "2) Frontend"
        echo "3) API Gateway"
        echo "4) Todos"
        read -p "Opción: " log_option
        
        case $log_option in
            1) docker-compose logs -f backend ;;
            2) docker-compose logs -f frontend ;;
            3) docker-compose logs -f api-gateway ;;
            4) docker-compose logs -f ;;
            *) echo "Opción inválida" ;;
        esac
        exit 0
        ;;
    *)
        echo -e "${RED}❌ Opción inválida${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}⏳ Esperando que los servicios estén listos...${NC}"
sleep 10

# Verificar estado de servicios
echo ""
echo "Estado de los servicios:"
docker-compose ps

echo ""
echo "============================================================================"
echo -e "${GREEN}✅ ¡Servicios iniciados exitosamente!${NC}"
echo "============================================================================"
echo ""
echo "📍 URLs de acceso:"
echo "   - Backend API:     http://localhost:8080"
echo "   - Swagger UI:      http://localhost:8080/swagger-ui.html"
echo "   - Health Check:    http://localhost:8080/api/v1/health"
echo "   - Frontend:        http://localhost:80"
echo "   - API Gateway:     http://localhost:8000"
echo ""
echo "📝 Comandos útiles:"
echo "   - Ver logs:        docker-compose logs -f [servicio]"
echo "   - Detener:         docker-compose down"
echo "   - Reiniciar:       docker-compose restart [servicio]"
echo "   - Estado:          docker-compose ps"
echo ""
echo "============================================================================"
