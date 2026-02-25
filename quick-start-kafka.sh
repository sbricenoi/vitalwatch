#!/bin/bash

# =============================================================================
# VitalWatch Kafka - Script de Inicio Rápido
# =============================================================================

set -e

clear

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║         VitalWatch - Kafka Streaming Platform                 ║"
echo "║              Quick Start Script                                ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# =============================================================================
# VERIFICACIONES PREVIAS
# =============================================================================

echo "🔍 Verificando requisitos previos..."
echo ""

# Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker no está instalado${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker instalado${NC}"

# Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose no está instalado${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker Compose instalado${NC}"

# Oracle Wallet
if [ ! -d "Wallet_S58ONUXCX4C1QXE9" ]; then
    echo -e "${YELLOW}⚠️  Oracle Wallet no encontrado${NC}"
    echo "   El Database Saver y Summary Generator no podrán conectarse a Oracle"
    read -p "   ¿Continuar de todas formas? (y/n): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo ""

# =============================================================================
# PASO 1: INICIAR CLUSTER KAFKA
# =============================================================================

echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}PASO 1/5: Iniciar Cluster Kafka${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

if docker ps | grep -q "vitalwatch-kafka1"; then
    echo -e "${YELLOW}⚠️  Kafka ya está corriendo${NC}"
    read -p "¿Reiniciar el cluster? (y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🛑 Deteniendo cluster existente..."
        docker-compose -f docker-compose-kafka.yml down
        echo ""
    fi
fi

if ! docker ps | grep -q "vitalwatch-kafka1"; then
    echo "🚀 Iniciando Zookeeper ensemble..."
    docker-compose -f docker-compose-kafka.yml up -d zookeeper1 zookeeper2 zookeeper3
    
    echo "⏳ Esperando Zookeeper (30s)..."
    sleep 30
    
    echo "🚀 Iniciando Kafka brokers..."
    docker-compose -f docker-compose-kafka.yml up -d kafka1 kafka2 kafka3
    
    echo "⏳ Esperando Kafka (45s)..."
    sleep 45
    
    echo "🚀 Iniciando Kafka UI..."
    docker-compose -f docker-compose-kafka.yml up -d kafka-ui
    
    echo "⏳ Esperando Kafka UI (10s)..."
    sleep 10
fi

echo -e "${GREEN}✅ Cluster Kafka iniciado${NC}"
echo ""

# =============================================================================
# PASO 2: CREAR TÓPICOS
# =============================================================================

echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}PASO 2/5: Crear Tópicos Kafka${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

echo "📝 Verificando tópicos existentes..."
TOPICS=$(docker exec vitalwatch-kafka1 kafka-topics --bootstrap-server kafka1:9092 --list 2>/dev/null || echo "")

if echo "$TOPICS" | grep -q "signos-vitales-stream"; then
    echo -e "${YELLOW}⚠️  Tópico signos-vitales-stream ya existe${NC}"
else
    echo "📝 Creando tópico: signos-vitales-stream..."
    docker exec vitalwatch-kafka1 kafka-topics \
        --bootstrap-server kafka1:9092 \
        --create \
        --topic signos-vitales-stream \
        --partitions 3 \
        --replication-factor 2 \
        --config retention.ms=604800000 \
        --if-not-exists
fi

if echo "$TOPICS" | grep -q "alertas-medicas"; then
    echo -e "${YELLOW}⚠️  Tópico alertas-medicas ya existe${NC}"
else
    echo "📝 Creando tópico: alertas-medicas..."
    docker exec vitalwatch-kafka1 kafka-topics \
        --bootstrap-server kafka1:9092 \
        --create \
        --topic alertas-medicas \
        --partitions 3 \
        --replication-factor 2 \
        --config retention.ms=2592000000 \
        --if-not-exists
fi

echo -e "${GREEN}✅ Tópicos configurados${NC}"
echo ""

# =============================================================================
# PASO 3: INICIAR MICROSERVICIOS
# =============================================================================

echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}PASO 3/5: Iniciar Microservicios${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

echo "🚀 Iniciando microservicios..."
docker-compose -f docker-compose-kafka.yml up -d producer-stream-generator producer-alert-processor consumer-database-saver consumer-summary-generator

echo "⏳ Esperando que los servicios estén listos (60s)..."
sleep 60

echo -e "${GREEN}✅ Microservicios iniciados${NC}"
echo ""

# =============================================================================
# PASO 4: VERIFICAR HEALTH
# =============================================================================

echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}PASO 4/5: Verificar Health de Servicios${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

check_health() {
    local service=$1
    local port=$2
    local url="http://localhost:$port/actuator/health"
    
    if curl -s -f "$url" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ $service: UP${NC}"
        return 0
    else
        echo -e "${RED}❌ $service: DOWN${NC}"
        return 1
    fi
}

check_health "Stream Generator" "8081"
check_health "Alert Processor" "8082"
check_health "Summary Generator" "8083"

echo ""

# =============================================================================
# PASO 5: INICIAR STREAM
# =============================================================================

echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}PASO 5/5: Iniciar Stream de Signos Vitales${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

echo "▶️  Iniciando stream..."
RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/stream/start)

if echo "$RESPONSE" | grep -q '"code":"200"'; then
    echo -e "${GREEN}✅ Stream iniciado exitosamente${NC}"
else
    echo -e "${RED}❌ Error iniciando stream${NC}"
    echo "$RESPONSE"
fi

echo ""

# =============================================================================
# RESUMEN FINAL
# =============================================================================

echo -e "${GREEN}╔═══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║           ✅ SISTEMA KAFKA INICIADO EXITOSAMENTE               ║${NC}"
echo -e "${GREEN}╚═══════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo "📊 ESTADO DEL SISTEMA:"
docker-compose -f docker-compose-kafka.yml ps

echo ""
echo "🌐 ACCESOS:"
echo -e "   ${BLUE}• Kafka UI:${NC}          http://localhost:8080"
echo -e "   ${BLUE}• Stream Generator:${NC}  http://localhost:8081/api/v1/stream/health"
echo -e "   ${BLUE}• Alert Processor:${NC}   http://localhost:8082/api/v1/processor/health"
echo -e "   ${BLUE}• Summary Generator:${NC} http://localhost:8083/api/v1/summary/health"
echo ""

echo "📈 COMANDOS ÚTILES:"
echo "   # Ver estadísticas del stream"
echo "   curl http://localhost:8081/api/v1/stream/stats"
echo ""
echo "   # Ver estadísticas del processor"
echo "   curl http://localhost:8082/api/v1/processor/stats"
echo ""
echo "   # Generar resumen diario"
echo "   curl -X POST http://localhost:8083/api/v1/summary/generate"
echo ""
echo "   # Ver logs del stream generator"
echo "   docker logs -f vitalwatch-producer-stream"
echo ""
echo "   # Ver logs del alert processor"
echo "   docker logs -f vitalwatch-producer-alert"
echo ""
echo "   # Detener todo"
echo "   docker-compose -f docker-compose-kafka.yml down"
echo ""

echo -e "${GREEN}🎉 ¡Listo para usar!${NC}"
echo ""
echo "📚 Documentación completa en:"
echo "   • README_KAFKA.md"
echo "   • docs/ARQUITECTURA_KAFKA.md"
echo "   • GUIA_PRUEBAS_KAFKA.md"
echo ""
