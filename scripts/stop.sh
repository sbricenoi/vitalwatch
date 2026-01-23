#!/bin/bash

# ============================================================================
# VitalWatch - Script de Detención
# ============================================================================

echo "============================================================================"
echo "  Deteniendo VitalWatch"
echo "============================================================================"
echo ""

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "¿Qué deseas hacer?"
echo "1) Detener servicios (mantener datos)"
echo "2) Detener y eliminar todo (incluye volúmenes)"
read -p "Selecciona una opción (1-2): " option

case $option in
    1)
        echo ""
        echo -e "${YELLOW}🛑 Deteniendo servicios...${NC}"
        docker-compose down
        echo -e "${GREEN}✅ Servicios detenidos${NC}"
        ;;
    2)
        echo ""
        echo -e "${RED}⚠️  ADVERTENCIA: Esto eliminará todos los volúmenes y datos${NC}"
        read -p "¿Estás seguro? (s/n): " confirm
        if [ "$confirm" = "s" ] || [ "$confirm" = "S" ]; then
            echo -e "${YELLOW}🛑 Deteniendo servicios y eliminando volúmenes...${NC}"
            docker-compose down -v
            echo -e "${GREEN}✅ Servicios detenidos y volúmenes eliminados${NC}"
        else
            echo "Operación cancelada"
        fi
        ;;
    *)
        echo -e "${RED}❌ Opción inválida${NC}"
        exit 1
        ;;
esac

echo ""
echo "============================================================================"
