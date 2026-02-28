#!/bin/bash

# =============================================================================
# VitalWatch - Script de Inicio del Cluster Kafka
# =============================================================================

set -e

echo "🚀 VitalWatch - Iniciando Cluster Kafka"
echo "========================================"
echo ""

echo "📋 Verificando docker-compose..."
if [ ! -f "docker-compose-kafka.yml" ]; then
    echo "❌ Error: docker-compose-kafka.yml no encontrado"
    exit 1
fi

echo "✅ Archivo docker-compose encontrado"
echo ""

echo "🔧 Levantando infraestructura Kafka..."
echo "   - 3 Brokers Kafka"
echo "   - 3 Nodos Zookeeper"
echo "   - Kafka UI"
echo ""

docker-compose -f docker-compose-kafka.yml up -d zookeeper1 zookeeper2 zookeeper3

echo "⏳ Esperando Zookeeper (30 segundos)..."
sleep 30

docker-compose -f docker-compose-kafka.yml up -d kafka1 kafka2 kafka3

echo "⏳ Esperando Kafka Brokers (45 segundos)..."
sleep 45

docker-compose -f docker-compose-kafka.yml up -d kafka-ui

echo "⏳ Esperando Kafka UI (10 segundos)..."
sleep 10

echo ""
echo "✅ Infraestructura Kafka iniciada exitosamente"
echo ""
echo "📊 Estado de servicios:"
docker-compose -f docker-compose-kafka.yml ps

echo ""
echo "🌐 Accesos:"
echo "   - Kafka UI: http://localhost:8080"
echo "   - Kafka Broker 1: localhost:19092"
echo "   - Kafka Broker 2: localhost:19093"
echo "   - Kafka Broker 3: localhost:19094"
echo ""

echo "📝 Siguiente paso: Ejecutar ./create-kafka-topics.sh para crear los tópicos"
echo ""
